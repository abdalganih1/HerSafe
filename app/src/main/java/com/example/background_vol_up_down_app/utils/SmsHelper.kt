package com.example.background_vol_up_down_app.utils

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.background_vol_up_down_app.data.local.entities.TrustedContact

object SmsHelper {

    private const val TAG = "SmsHelper"
    private const val MAX_SMS_LENGTH = 160

    // Send SMS to a single recipient
    fun sendSms(phoneNumber: String, message: String): Boolean {
        return try {
            val smsManager = SmsManager.getDefault()

            // If message is longer than 160 characters, split it
            if (message.length > MAX_SMS_LENGTH) {
                val parts = smsManager.divideMessage(message)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    message,
                    null,
                    null
                )
            }
            Log.d(TAG, "SMS sent successfully to $phoneNumber")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send SMS to $phoneNumber", e)
            false
        }
    }

    // Send SMS to multiple recipients
    fun sendSmsToMultiple(phoneNumbers: List<String>, message: String): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()
        phoneNumbers.forEach { phoneNumber ->
            results[phoneNumber] = sendSms(phoneNumber, message)
        }
        return results
    }

    // Send emergency SMS with location
    fun sendEmergencySms(
        contacts: List<TrustedContact>,
        latitude: Double,
        longitude: Double,
        address: String? = null
    ): Map<String, Boolean> {
        val locationUrl = "https://www.google.com/maps?q=$latitude,$longitude"

        val message = buildString {
            append("⚠️ تنبيه طوارئ من HerSafe!\n\n")
            append("أحتاج المساعدة فوراً!\n\n")
            if (address != null) {
                append("الموقع: $address\n\n")
            }
            append("الموقع على الخريطة:\n$locationUrl\n\n")
            append("الرجاء الاتصال بي فوراً!")
        }

        val phoneNumbers = contacts
            .filter { it.receiveSms && it.isActive }
            .map { it.phoneNumber }

        return sendSmsToMultiple(phoneNumbers, message)
    }

    // Send safe journey start notification
    fun sendSafeJourneyStartSms(
        contacts: List<TrustedContact>,
        startAddress: String?,
        destinationAddress: String?,
        estimatedDuration: Int?
    ): Map<String, Boolean> {
        val message = buildString {
            append("📍 رحلة آمنة - HerSafe\n\n")
            append("بدأت رحلة آمنة الآن\n\n")
            if (startAddress != null) {
                append("من: $startAddress\n")
            }
            if (destinationAddress != null) {
                append("إلى: $destinationAddress\n")
            }
            if (estimatedDuration != null) {
                append("\nالوقت المتوقع: $estimatedDuration دقيقة\n")
            }
            append("\nسأخبرك عند الوصول بأمان.")
        }

        val phoneNumbers = contacts
            .filter { it.receiveSms && it.isActive }
            .map { it.phoneNumber }

        return sendSmsToMultiple(phoneNumbers, message)
    }

    // Send safe journey completion notification
    fun sendSafeJourneyCompleteSms(
        contacts: List<TrustedContact>,
        destinationAddress: String?
    ): Map<String, Boolean> {
        val message = buildString {
            append("✅ وصلت بأمان - HerSafe\n\n")
            if (destinationAddress != null) {
                append("الموقع: $destinationAddress\n\n")
            }
            append("شكراً على متابعتك!")
        }

        val phoneNumbers = contacts
            .filter { it.receiveSms && it.isActive }
            .map { it.phoneNumber }

        return sendSmsToMultiple(phoneNumbers, message)
    }

    // Send deviation alert
    fun sendDeviationAlertSms(
        contacts: List<TrustedContact>,
        currentLocation: String?,
        latitude: Double,
        longitude: Double
    ): Map<String, Boolean> {
        val locationUrl = "https://www.google.com/maps?q=$latitude,$longitude"

        val message = buildString {
            append("⚠️ تنبيه انحراف عن المسار - HerSafe\n\n")
            append("انحرفت عن المسار المخطط\n\n")
            if (currentLocation != null) {
                append("الموقع الحالي: $currentLocation\n\n")
            }
            append("موقعي الآن:\n$locationUrl")
        }

        val phoneNumbers = contacts
            .filter { it.receiveSms && it.isActive }
            .map { it.phoneNumber }

        return sendSmsToMultiple(phoneNumbers, message)
    }

    // Send stopped alert (user stopped moving for too long)
    fun sendStoppedAlertSms(
        contacts: List<TrustedContact>,
        currentLocation: String?,
        latitude: Double,
        longitude: Double,
        stoppedDurationMinutes: Int
    ): Map<String, Boolean> {
        val locationUrl = "https://www.google.com/maps?q=$latitude,$longitude"

        val message = buildString {
            append("⚠️ تنبيه توقف - HerSafe\n\n")
            append("توقفت عن الحركة لمدة $stoppedDurationMinutes دقيقة\n\n")
            if (currentLocation != null) {
                append("الموقع: $currentLocation\n\n")
            }
            append("موقعي الآن:\n$locationUrl")
        }

        val phoneNumbers = contacts
            .filter { it.receiveSms && it.isActive }
            .map { it.phoneNumber }

        return sendSmsToMultiple(phoneNumbers, message)
    }

    // Validate phone number format
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // Basic validation: should contain only digits, +, -, (, ), and spaces
        val cleanNumber = phoneNumber.replace(Regex("[\\s\\-()]"), "")
        return cleanNumber.matches(Regex("^\\+?[0-9]{10,15}$"))
    }

    // Format phone number
    fun formatPhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace(Regex("[\\s\\-()]"), "")
    }

    // Get SMS character count info
    fun getSmsInfo(message: String): SmsInfo {
        val length = message.length
        val parts = if (length > MAX_SMS_LENGTH) {
            (length / MAX_SMS_LENGTH) + 1
        } else {
            1
        }
        return SmsInfo(length, parts, MAX_SMS_LENGTH - (length % MAX_SMS_LENGTH))
    }

    data class SmsInfo(
        val characterCount: Int,
        val messageCount: Int,
        val remainingCharacters: Int
    )
}
