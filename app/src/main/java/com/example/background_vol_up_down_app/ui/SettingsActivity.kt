package com.example.background_vol_up_down_app.ui

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.background_vol_up_down_app.R
import com.example.background_vol_up_down_app.utils.PreferencesHelper
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        preferencesHelper = PreferencesHelper(this)

        // Setup toolbar
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Initialize views
        val switchAutoRecord = findViewById<SwitchMaterial>(R.id.switchAutoRecord)
        val switchAutoVideo = findViewById<SwitchMaterial>(R.id.switchAutoVideo)
        val seekBarTrackingInterval = findViewById<SeekBar>(R.id.seekBarTrackingInterval)
        val tvTrackingInterval = findViewById<TextView>(R.id.tvTrackingInterval)
        val seekBarStoppedAlert = findViewById<SeekBar>(R.id.seekBarStoppedAlert)
        val tvStoppedAlert = findViewById<TextView>(R.id.tvStoppedAlert)
        val switchAutoSync = findViewById<SwitchMaterial>(R.id.switchAutoSync)
        val switchWifiOnly = findViewById<SwitchMaterial>(R.id.switchWifiOnly)

        // Load current settings
        switchAutoRecord.isChecked = preferencesHelper.autoRecordAudio
        switchAutoVideo.isChecked = preferencesHelper.autoRecordVideo
        seekBarTrackingInterval.progress = preferencesHelper.journeyTrackingIntervalSeconds
        tvTrackingInterval.text = "${preferencesHelper.journeyTrackingIntervalSeconds} ثانية"
        seekBarStoppedAlert.progress = preferencesHelper.stoppedAlertMinutes
        tvStoppedAlert.text = "${preferencesHelper.stoppedAlertMinutes} دقائق"
        switchAutoSync.isChecked = preferencesHelper.autoSyncEnabled
        switchWifiOnly.isChecked = preferencesHelper.syncWifiOnly

        // Setup listeners
        switchAutoRecord.setOnCheckedChangeListener { _, isChecked ->
            preferencesHelper.autoRecordAudio = isChecked
        }

        switchAutoVideo.setOnCheckedChangeListener { _, isChecked ->
            preferencesHelper.autoRecordVideo = isChecked
        }

        seekBarTrackingInterval.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvTrackingInterval.text = "$progress ثانية"
                preferencesHelper.journeyTrackingIntervalSeconds = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarStoppedAlert.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvStoppedAlert.text = "$progress دقائق"
                preferencesHelper.stoppedAlertMinutes = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        switchAutoSync.setOnCheckedChangeListener { _, isChecked ->
            preferencesHelper.autoSyncEnabled = isChecked
        }

        switchWifiOnly.setOnCheckedChangeListener { _, isChecked ->
            preferencesHelper.syncWifiOnly = isChecked
        }
    }
}
