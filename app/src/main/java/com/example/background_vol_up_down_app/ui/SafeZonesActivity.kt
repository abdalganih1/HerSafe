package com.example.background_vol_up_down_app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.background_vol_up_down_app.R
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.local.entities.SafeZone
import com.example.background_vol_up_down_app.data.repository.LocationRepository
import com.example.background_vol_up_down_app.data.repository.SafeZoneRepository
import com.example.background_vol_up_down_app.utils.PermissionHelper
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SafeZonesActivity : AppCompatActivity() {

    private lateinit var btnAddSafeZone: Button
    private lateinit var btnAddDangerZone: Button
    private lateinit var tvSafeZonesCount: TextView
    private lateinit var tvDangerZonesCount: TextView
    private lateinit var safeZoneRepository: SafeZoneRepository
    private lateinit var locationRepository: LocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_zones)

        // Initialize repositories
        val database = HerSafeDatabase.getDatabase(this)
        safeZoneRepository = SafeZoneRepository(database.safeZoneDao())
        locationRepository = LocationRepository(this)

        // Initialize views
        btnAddSafeZone = findViewById(R.id.btnAddSafeZone)
        btnAddDangerZone = findViewById(R.id.btnAddDangerZone)
        tvSafeZonesCount = findViewById(R.id.tvSafeZonesCount)
        tvDangerZonesCount = findViewById(R.id.tvDangerZonesCount)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Load statistics
        loadStatistics()

        // Add safe zone button
        btnAddSafeZone.setOnClickListener {
            showAddZoneDialog(isSafe = true)
        }

        // Add danger zone button
        btnAddDangerZone.setOnClickListener {
            showAddZoneDialog(isSafe = false)
        }
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                safeZoneRepository.allZones.collect { allZones ->
                    val safeCount = allZones.count { it.safetyScore >= 50 }
                    val dangerCount = allZones.count { it.safetyScore < 50 }

                    runOnUiThread {
                        tvSafeZonesCount.text = "✅ المناطق الآمنة: $safeCount"
                        tvDangerZonesCount.text = "⚠️ المناطق الخطرة: $dangerCount"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SafeZonesActivity,
                        "خطأ في تحميل الإحصائيات",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun showAddZoneDialog(isSafe: Boolean) {
        if (!PermissionHelper.hasLocationPermission(this)) {
            Toast.makeText(this, "يرجى منح إذن الموقع", Toast.LENGTH_SHORT).show()
            PermissionHelper.requestLocationPermissions(this)
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_add_zone, null)
        val etZoneName = dialogView.findViewById<TextInputEditText>(R.id.etZoneName)
        val etRadius = dialogView.findViewById<TextInputEditText>(R.id.etRadius)

        val title = if (isSafe) "إضافة منطقة آمنة" else "إضافة منطقة خطرة"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("إضافة") { _, _ ->
                val name = etZoneName.text.toString().trim()
                val radiusStr = etRadius.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(this, "يرجى إدخال اسم المنطقة", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val radius = if (radiusStr.isEmpty()) 500f else radiusStr.toFloatOrNull() ?: 500f

                addZoneAtCurrentLocation(name, radius, isSafe)
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun addZoneAtCurrentLocation(name: String, radius: Float, isSafe: Boolean) {
        lifecycleScope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (location == null) {
                    runOnUiThread {
                        Toast.makeText(
                            this@SafeZonesActivity,
                            "لا يمكن الحصول على الموقع الحالي",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }

                val address = locationRepository.getAddressFromLocation(
                    location.latitude,
                    location.longitude
                )

                val safetyScore = if (isSafe) 80 else 20

                val zone = SafeZone(
                    name = name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radiusMeters = radius,
                    safetyScore = safetyScore,
                    address = address
                )

                safeZoneRepository.insertZone(zone)

                runOnUiThread {
                    Toast.makeText(
                        this@SafeZonesActivity,
                        "تم إضافة المنطقة بنجاح",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadStatistics()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        this@SafeZonesActivity,
                        "خطأ: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
