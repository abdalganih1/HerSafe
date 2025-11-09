package com.example.background_vol_up_down_app.data.repository

import androidx.lifecycle.LiveData
import com.example.background_vol_up_down_app.data.local.dao.TrustedContactDao
import com.example.background_vol_up_down_app.data.local.entities.ContactType
import com.example.background_vol_up_down_app.data.local.entities.TrustedContact
import kotlinx.coroutines.flow.Flow

class TrustedContactRepository(
    private val trustedContactDao: TrustedContactDao
) {

    val allActiveContacts: Flow<List<TrustedContact>> = trustedContactDao.getAllActiveContacts()
    val allActiveContactsLiveData: LiveData<List<TrustedContact>> = trustedContactDao.getAllActiveContactsLiveData()
    val allContacts: Flow<List<TrustedContact>> = trustedContactDao.getAllContacts()
    val activeContactsCount: LiveData<Int> = trustedContactDao.getActiveContactsCount()

    suspend fun insertContact(contact: TrustedContact): Long {
        return trustedContactDao.insert(contact)
    }

    suspend fun updateContact(contact: TrustedContact) {
        trustedContactDao.update(contact)
    }

    suspend fun deleteContact(contact: TrustedContact) {
        trustedContactDao.delete(contact)
    }

    suspend fun deleteContactById(contactId: Long) {
        trustedContactDao.deleteById(contactId)
    }

    suspend fun getContactById(contactId: Long): TrustedContact? {
        return trustedContactDao.getContactById(contactId)
    }

    fun getContactsByType(type: ContactType): Flow<List<TrustedContact>> {
        return trustedContactDao.getContactsByType(type)
    }

    suspend fun getPrimaryEmergencyContact(): TrustedContact? {
        return trustedContactDao.getPrimaryEmergencyContact()
    }

    suspend fun getSmsEnabledContacts(): List<TrustedContact> {
        return trustedContactDao.getSmsEnabledContacts()
    }

    suspend fun updateLastNotified(contactId: Long, timestamp: Long = System.currentTimeMillis()) {
        trustedContactDao.updateLastNotified(contactId, timestamp)
    }

    suspend fun updateActiveStatus(contactId: Long, isActive: Boolean) {
        trustedContactDao.updateActiveStatus(contactId, isActive)
    }

    suspend fun getContactByPhone(phoneNumber: String): TrustedContact? {
        return trustedContactDao.getContactByPhone(phoneNumber)
    }

    // Helper function to add a new trusted contact
    suspend fun addTrustedContact(
        name: String,
        phoneNumber: String,
        email: String? = null,
        contactType: ContactType = ContactType.EMERGENCY,
        priority: Int = 1,
        relationship: String? = null
    ): Long {
        val contact = TrustedContact(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            contactType = contactType,
            priority = priority,
            relationship = relationship
        )
        return insertContact(contact)
    }

    // Get contacts for emergency notification
    suspend fun getEmergencyNotificationContacts(): List<TrustedContact> {
        return getSmsEnabledContacts()
    }
}
