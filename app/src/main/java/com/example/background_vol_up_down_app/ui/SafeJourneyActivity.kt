package com.example.background_vol_up_down_app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.background_vol_up_down_app.R
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.repository.LocationRepository
import com.example.background_vol_up_down_app.data.repository.TrustedContactRepository
import com.example.background_vol_up_down_app.services.LocationTrackingService
import com.example.background_vol_up_down_app.utils.PermissionHelper
import com.example.background_vol_up_down_app.utils.SmsHelper
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SafeJourneyActivity : AppCompatActivity() {

    private lateinit var etDestination: TextInputEditText
    private lateinit var seekBarDuration: SeekBar
    private lateinit var tvDuration: TextView
    private lateinit var tvContactsInfo: TextView
    private lateinit var btnStartJourney: Button
    private lateinit var btnShareWhatsApp: Button
    private lateinit var btnOpenMaps: Button
    private lateinit var locationRepository: LocationRepository
    private lateinit var contactRepository: TrustedContactRepository

    private var selectedDestLat: Double? = null
    private var selectedDestLng: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_journey)

        // Initialize repositories
        val database = HerSafeDatabase.getDatabase(this)
        locationRepository = LocationRepository(this)
        contactRepository = TrustedContactRepository(database.trustedContactDao())

        // Initialize views
        etDestination = findViewById(R.id.etDestination)
        seekBarDuration = findViewById(R.id.seekBarDuration)
        tvDuration = findViewById(R.id.tvDuration)
        tvContactsInfo = findViewById(R.id.tvContactsInfo)
        btnStartJourney = findViewById(R.id.btnStartJourney)
        btnShareWhatsApp = findViewById(R.id.btnShareWhatsApp)
        btnOpenMaps = findViewById(R.id.btnOpenMaps)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Setup seekbar
        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvDuration.text = "$progress دقيقة"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Load contacts info
        loadContactsInfo()

        // Start journey button
        btnStartJourney.setOnClickListener {
            startSafeJourney()
        }

        // WhatsApp sharing button
        btnShareWhatsApp.setOnClickListener {
            shareLocationViaWhatsApp()
        }

        // Google Maps picker button
        btnOpenMaps.setOnClickListener {
            openMapsPicker()
        }
    }

    private fun loadContactsInfo() {
        lifecycleScope.launch {
            val contacts = contactRepository.getSmsEnabledContacts()
            runOnUiThread {
                if (contacts.isEmpty()) {
                    tvContactsInfo.text = "⚠️ لا توجد جهات اتصال موثوقة. أضف جهات اتصال أولاً."
                } else {
                    tvContactsInfo.text = "سيتم إخطار ${contacts.size} جهة اتصال عند بدء الرحلة"
                }
            }
        }
    }

    private fun startSafeJourney() {
        val destination = etDestination.text.toString().trim()

        if (destination.isEmpty()) {
            Toast.makeText(this, "يرجى إدخال الوجهة", Toast.LENGTH_SHORT).show()
            return
        }

        if (!PermissionHelper.hasLocationPermission(this)) {
            Toast.makeText(this, "يرجى منح إذن الموقع", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestLocationPermissions(this)
            return
        }

        if (!PermissionHelper.hasBackgroundLocationPermission(this)) {
            Toast.makeText(this, "يرجى منح إذن الموقع في الخلفية", Toast.LENGTH_LONG).show()
            PermissionHelper.requestBackgroundLocationPermission(this)
            return
        }

        lifecycleScope.launch {
            try {
                // Get current location
                val currentLocation = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (currentLocation == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SafeJourneyActivity,
                            "لا يمكن الحصول على الموقع الحالي",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val currentAddress = locationRepository.getAddressFromLocation(
                    currentLocation.latitude,
                    currentLocation.longitude
                )

                // For now, use current location as destination (will be improved with map picker)
                val destLat = currentLocation.latitude + 0.01 // Mock destination
                val destLng = currentLocation.longitude + 0.01

                val contacts = contactRepository.getSmsEnabledContacts()
                val contactIds = contacts.joinToString(",") { it.id.toString() }

                // Start LocationTrackingService
                val intent = Intent(this@SafeJourneyActivity, LocationTrackingService::class.java)
                intent.action = LocationTrackingService.ACTION_START_JOURNEY
                intent.putExtra(LocationTrackingService.EXTRA_START_LAT, currentLocation.latitude)
                intent.putExtra(LocationTrackingService.EXTRA_START_LNG, currentLocation.longitude)
                intent.putExtra(LocationTrackingService.EXTRA_START_ADDRESS, currentAddress)
                intent.putExtra(LocationTrackingService.EXTRA_DEST_LAT, destLat)
                intent.putExtra(LocationTrackingService.EXTRA_DEST_LNG, destLng)
                intent.putExtra(LocationTrackingService.EXTRA_DEST_ADDRESS, destination)
                intent.putExtra(LocationTrackingService.EXTRA_CONTACTS, contactIds)

                startForegroundService(intent)

                runOnUiThread {
                    Toast.makeText(
                        this@SafeJourneyActivity,
                        "تم بدء الرحلة الآمنة ✓",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SafeJourneyActivity,
                        "خطأ: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun shareLocationViaWhatsApp() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            Toast.makeText(this, "يرجى منح إذن الموقع", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestLocationPermissions(this)
            return
        }

        lifecycleScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (location == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SafeJourneyActivity,
                            "لا يمكن الحصول على الموقع الحالي",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val contacts = contactRepository.getSmsEnabledContacts()
                if (contacts.isEmpty()) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SafeJourneyActivity,
                            "لا توجد جهات اتصال موثوقة",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                runOnUiThread {
                    SmsHelper.shareLiveLocationWhatsApp(
                        this@SafeJourneyActivity,
                        contacts,
                        location.latitude,
                        location.longitude
                    )
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SafeJourneyActivity,
                        "خطأ: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun openMapsPicker() {
        if (!PermissionHelper.hasLocationPermission(this)) {
            Toast.makeText(this, "يرجى منح إذن الموقع", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestLocationPermissions(this)
            return
        }

        lifecycleScope.launch {
            try {
                // Get current location for map center
                val location = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (location != null) {
                    // Open Google Maps with current location to pick destination
                    val uri = Uri.parse("geo:${location.latitude},${location.longitude}?q=")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setPackage("com.google.android.apps.maps")

                    runOnUiThread {
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                            Toast.makeText(
                                this@SafeJourneyActivity,
                                "اختر الوجهة من الخريطة وانسخ العنوان",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this@SafeJourneyActivity,
                                "يرجى تثبيت خرائط Google",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@SafeJourneyActivity,
                            "لا يمكن الحصول على الموقع الحالي",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SafeJourneyActivity,
                        "خطأ: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
