package com.example.background_vol_up_down_app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trusted_contacts")
data class TrustedContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val phoneNumber: String,
    val email: String? = null,

    // Contact type
    val contactType: ContactType = ContactType.EMERGENCY,

    // Priority (lower number = higher priority)
    val priority: Int = 1,

    // Notification preferences
    val receiveSms: Boolean = true,
    val receiveWhatsApp: Boolean = false,
    val receiveEmail: Boolean = false,

    // Metadata
    val isActive: Boolean = true,
    val addedAt: Long = System.currentTimeMillis(),
    val lastNotifiedAt: Long? = null,

    // Profile
    val relationship: String? = null, // e.g., "Mother", "Friend", "Police"
    val photoUri: String? = null
)

enum class ContactType {
    EMERGENCY,      // Primary emergency contact
    SAFE_JOURNEY,   // Notified during safe journey
    BACKUP,         // Secondary contact
    AUTHORITY      // Police, emergency services
}
