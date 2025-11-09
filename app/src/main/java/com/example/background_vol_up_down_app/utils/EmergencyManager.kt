package com.example.background_vol_up_down_app.utils

import android.content.Context
import android.util.Log
import com.example.background_vol_up_down_app.data.local.entities.EmergencyType
import com.example.background_vol_up_down_app.data.repository.EmergencyRepository
import com.example.background_vol_up_down_app.data.repository.LocationRepository
import com.example.background_vol_up_down_app.data.repository.SafeZoneRepository
import com.example.background_vol_up_down_app.data.repository.TrustedContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmergencyManager(
    private val context: Context,
    private val emergencyRepository: EmergencyRepository,
    private val trustedContactRepository: TrustedContactRepository,
    private val locationRepository: LocationRepository,
    private val safeZoneRepository: SafeZoneRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val preferencesHelper = PreferencesHelper(context)

    companion object {
        private const val TAG = "EmergencyManager"
    }

    // Trigger emergency
    fun triggerEmergency(
        eventType: EmergencyType = EmergencyType.VOLUME_BUTTON_TRIGGER,
        onComplete: ((Long) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        scope.launch {
            try {
                Log.d(TAG, "Triggering emergency: $eventType")

                // 1. Get current location
                val location = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (location == null) {
                    Log.e(TAG, "Could not get location")
                    onError?.invoke(Exception("Could not get location"))
                    return@launch
                }

                val latitude = location.latitude
                val longitude = location.longitude
                val accuracy = location.accuracy

                // 2. Get address
                val address = locationRepository.getAddressFromLocation(latitude, longitude)

                Log.d(TAG, "Location: $latitude, $longitude, Address: $address")

                // 3. Create emergency event in database
                val eventId = emergencyRepository.createEmergencyEvent(
                    latitude = latitude,
                    longitude = longitude,
                    eventType = eventType,
                    address = address,
                    locationAccuracy = accuracy
                )

                Log.d(TAG, "Emergency event created with ID: $eventId")

                // 4. Get trusted contacts
                val contacts = trustedContactRepository.getSmsEnabledContacts()

                if (contacts.isEmpty()) {
                    Log.w(TAG, "No trusted contacts configured")
                }

                // 5. Send SMS to trusted contacts
                if (contacts.isNotEmpty()) {
                    val smsResults = SmsHelper.sendEmergencySms(
                        contacts = contacts,
                        latitude = latitude,
                        longitude = longitude,
                        address = address
                    )

                    // Log SMS results
                    smsResults.forEach { (phone, success) ->
                        Log.d(TAG, "SMS to $phone: ${if (success) "Sent" else "Failed"}")
                    }

                    // Update contacts last notified timestamp
                    contacts.forEach { contact ->
                        trustedContactRepository.updateLastNotified(contact.id)
                    }

                    // Mark SMS as sent in event
                    val recipients = contacts.joinToString(",") { it.phoneNumber }
                    emergencyRepository.markSmsSent(eventId, recipients)
                }

                // 6. Show notification
                NotificationHelper.showEmergencyNotification(
                    context,
                    "تم إرسال تنبيه الطوارئ لجهات الاتصال الموثوقة"
                )

                // 7. Record incident in safe zone database
                safeZoneRepository.recordIncidentAtLocation(latitude, longitude)

                Log.d(TAG, "Emergency triggered successfully")
                onComplete?.invoke(eventId)

            } catch (e: Exception) {
                Log.e(TAG, "Error triggering emergency", e)
                onError?.invoke(e)
            }
        }
    }

    // Cancel/resolve emergency
    fun resolveEmergency(eventId: Long, onComplete: (() -> Unit)? = null) {
        scope.launch {
            try {
                emergencyRepository.resolveEmergency(eventId)
                NotificationHelper.cancelEmergencyNotification(context)
                Log.d(TAG, "Emergency $eventId resolved")
                onComplete?.invoke()
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving emergency", e)
            }
        }
    }

    // Quick emergency trigger (for button press)
    fun quickEmergencyTrigger() {
        Log.d(TAG, "Quick emergency trigger activated")

        // Show immediate notification
        NotificationHelper.showEmergencyNotification(
            context,
            "جاري إرسال تنبيه الطوارئ..."
        )

        // Trigger emergency
        triggerEmergency(
            eventType = EmergencyType.VOLUME_BUTTON_TRIGGER,
            onComplete = { eventId ->
                Log.d(TAG, "Quick emergency completed: Event ID $eventId")
            },
            onError = { error ->
                Log.e(TAG, "Quick emergency failed", error)
                NotificationHelper.showEmergencyNotification(
                    context,
                    "فشل إرسال التنبيه: ${error.message}"
                )
            }
        )
    }

    // Manual emergency trigger (from UI button)
    fun manualEmergencyTrigger(onComplete: ((Long) -> Unit)? = null, onError: ((Exception) -> Unit)? = null) {
        triggerEmergency(
            eventType = EmergencyType.MANUAL_TRIGGER,
            onComplete = onComplete,
            onError = onError
        )
    }

    // Test emergency system (without sending SMS)
    fun testEmergencySystem(onComplete: ((String) -> Unit)? = null) {
        scope.launch {
            try {
                val location = locationRepository.getCurrentLocation()
                    ?: locationRepository.getLastKnownLocation()

                if (location == null) {
                    onComplete?.invoke("❌ فشل الحصول على الموقع")
                    return@launch
                }

                val address = locationRepository.getAddressFromLocation(
                    location.latitude,
                    location.longitude
                )

                val contacts = trustedContactRepository.getSmsEnabledContacts()

                val result = buildString {
                    appendLine("✅ اختبار نظام الطوارئ")
                    appendLine()
                    appendLine("📍 الموقع: ${location.latitude}, ${location.longitude}")
                    appendLine("📮 العنوان: ${address ?: "غير متاح"}")
                    appendLine("📱 جهات الاتصال: ${contacts.size}")
                    appendLine()
                    appendLine("النظام جاهز للعمل!")
                }

                onComplete?.invoke(result)

            } catch (e: Exception) {
                onComplete?.invoke("❌ خطأ: ${e.message}")
            }
        }
    }
}
