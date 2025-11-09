package com.example.background_vol_up_down_app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.background_vol_up_down_app.data.local.database.HerSafeDatabase
import com.example.background_vol_up_down_app.data.repository.*
import com.example.background_vol_up_down_app.services.MonitoringService
import com.example.background_vol_up_down_app.utils.*
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var emergencyManager: EmergencyManager
    private lateinit var preferencesHelper: PreferencesHelper
    private val scope = CoroutineScope(Dispatchers.Main)

    private lateinit var btnEmergency: Button
    private lateinit var switchMonitoring: SwitchMaterial
    private lateinit var btnSafeJourney: Button
    private lateinit var btnContacts: Button
    private lateinit var btnSafeZones: Button
    private lateinit var btnHistory: Button
    private lateinit var btnSettings: Button
    private lateinit var btnRequestPermissions: Button
    private lateinit var tvPermissionsStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize
        initializeComponents()
        initializeViews()
        setupClickListeners()
        checkPermissions()

        // Show first launch dialog if needed
        if (preferencesHelper.isFirstLaunch) {
            showWelcomeDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initializeComponents() {
        preferencesHelper = PreferencesHelper(this)

        // Initialize database and repositories
        val database = HerSafeDatabase.getDatabase(this)
        val emergencyRepository = EmergencyRepository(database.emergencyEventDao())
        val trustedContactRepository = TrustedContactRepository(database.trustedContactDao())
        val locationRepository = LocationRepository(this)
        val safeZoneRepository = SafeZoneRepository(database.safeZoneDao())

        emergencyManager = EmergencyManager(
            this,
            emergencyRepository,
            trustedContactRepository,
            locationRepository,
            safeZoneRepository
        )

        // Create notification channels
        NotificationHelper.createNotificationChannels(this)
    }

    private fun initializeViews() {
        btnEmergency = findViewById(R.id.btnEmergency)
        switchMonitoring = findViewById(R.id.switchMonitoring)
        btnSafeJourney = findViewById(R.id.btnSafeJourney)
        btnContacts = findViewById(R.id.btnContacts)
        btnSafeZones = findViewById(R.id.btnSafeZones)
        btnHistory = findViewById(R.id.btnHistory)
        btnSettings = findViewById(R.id.btnSettings)
        btnRequestPermissions = findViewById(R.id.btnRequestPermissions)
        tvPermissionsStatus = findViewById(R.id.tvPermissionsStatus)
    }

    private fun setupClickListeners() {
        // Emergency button
        btnEmergency.setOnClickListener {
            showEmergencyConfirmDialog()
        }

        // Monitoring switch
        switchMonitoring.setOnCheckedChangeListener { _, isChecked ->
            toggleMonitoring(isChecked)
        }

        // Safe Journey
        btnSafeJourney.setOnClickListener {
            Toast.makeText(this, "ميزة الرحلة الآمنة - قريباً", Toast.LENGTH_SHORT).show()
            // TODO: Start Safe Journey Activity
        }

        // Contacts
        btnContacts.setOnClickListener {
            Toast.makeText(this, "إدارة جهات الاتصال - قريباً", Toast.LENGTH_SHORT).show()
            // TODO: Start Contacts Activity
        }

        // Safe Zones
        btnSafeZones.setOnClickListener {
            Toast.makeText(this, "المناطق الآمنة - قريباً", Toast.LENGTH_SHORT).show()
            // TODO: Start Safe Zones Activity
        }

        // History
        btnHistory.setOnClickListener {
            Toast.makeText(this, "السجل - قريباً", Toast.LENGTH_SHORT).show()
            // TODO: Start History Activity
        }

        // Settings
        btnSettings.setOnClickListener {
            Toast.makeText(this, "الإعدادات - قريباً", Toast.LENGTH_SHORT).show()
            // TODO: Start Settings Activity
        }

        // Request Permissions
        btnRequestPermissions.setOnClickListener {
            PermissionHelper.requestAllEssentialPermissions(this)
        }
    }

    private fun showEmergencyConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ تأكيد الطوارئ")
            .setMessage("هل تريد حقاً إرسال تنبيه الطوارئ؟\n\nسيتم:\n• إرسال رسالة SMS لجهات الاتصال الموثوقة\n• إرسال موقعك الحالي\n• تسجيل الحدث")
            .setPositiveButton("نعم، أرسل التنبيه") { _, _ ->
                triggerEmergency()
            }
            .setNegativeButton("إلغاء", null)
            .setCancelable(true)
            .show()
    }

    private fun triggerEmergency() {
        if (!PermissionHelper.hasAllCriticalPermissions(this)) {
            Toast.makeText(
                this,
                "الأذونات الأساسية مفقودة. يرجى منح الأذونات أولاً.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        Toast.makeText(this, "جاري إرسال تنبيه الطوارئ...", Toast.LENGTH_SHORT).show()

        emergencyManager.manualEmergencyTrigger(
            onComplete = { eventId ->
                runOnUiThread {
                    AlertDialog.Builder(this)
                        .setTitle("✅ تم إرسال التنبيه")
                        .setMessage("تم إرسال تنبيه الطوارئ بنجاح إلى جهات الاتصال الموثوقة.")
                        .setPositiveButton("موافق", null)
                        .show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "خطأ: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun toggleMonitoring(enabled: Boolean) {
        val intent = Intent(this, MonitoringService::class.java)
        intent.action = if (enabled) {
            MonitoringService.ACTION_START_MONITORING
        } else {
            MonitoringService.ACTION_STOP_MONITORING
        }

        if (enabled) {
            startForegroundService(intent)
            Toast.makeText(this, "تم تفعيل المراقبة", Toast.LENGTH_SHORT).show()
        } else {
            stopService(intent)
            Toast.makeText(this, "تم إيقاف المراقبة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        val status = PermissionHelper.getPermissionStatusMessage(this)
        tvPermissionsStatus.text = status

        if (PermissionHelper.hasAllEssentialPermissions(this)) {
            btnRequestPermissions.isEnabled = false
            btnRequestPermissions.text = "تم منح جميع الأذونات ✓"
        } else {
            btnRequestPermissions.isEnabled = true
        }
    }

    private fun updateUI() {
        // Update monitoring switch
        switchMonitoring.isChecked = preferencesHelper.isMonitoringEnabled

        // Update permissions status
        checkPermissions()
    }

    private fun showWelcomeDialog() {
        AlertDialog.Builder(this)
            .setTitle("مرحباً بك في HerSafe")
            .setMessage("تطبيق HerSafe مصمم لحمايتك وضمان أمانك.\n\n" +
                    "الميزات الرئيسية:\n" +
                    "• زر الطوارئ السريع\n" +
                    "• إرسال موقعك لجهات الاتصال الموثوقة\n" +
                    "• تتبع الرحلة الآمنة\n" +
                    "• تحليل المناطق الآمنة\n\n" +
                    "للبدء، يرجى منح الأذونات المطلوبة.")
            .setPositiveButton("ابدأ") { _, _ ->
                preferencesHelper.isFirstLaunch = false
                PermissionHelper.requestAllEssentialPermissions(this)
            }
            .setCancelable(false)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        checkPermissions()
    }
}
