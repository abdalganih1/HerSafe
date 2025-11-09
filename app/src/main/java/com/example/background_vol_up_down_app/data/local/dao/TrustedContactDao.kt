package com.example.background_vol_up_down_app.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.background_vol_up_down_app.data.local.entities.ContactType
import com.example.background_vol_up_down_app.data.local.entities.TrustedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: TrustedContact): Long

    @Update
    suspend fun update(contact: TrustedContact)

    @Delete
    suspend fun delete(contact: TrustedContact)

    @Query("SELECT * FROM trusted_contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Long): TrustedContact?

    @Query("SELECT * FROM trusted_contacts WHERE isActive = 1 ORDER BY priority ASC, name ASC")
    fun getAllActiveContacts(): Flow<List<TrustedContact>>

    @Query("SELECT * FROM trusted_contacts WHERE isActive = 1 ORDER BY priority ASC, name ASC")
    fun getAllActiveContactsLiveData(): LiveData<List<TrustedContact>>

    @Query("SELECT * FROM trusted_contacts ORDER BY priority ASC, name ASC")
    fun getAllContacts(): Flow<List<TrustedContact>>

    @Query("SELECT * FROM trusted_contacts WHERE contactType = :type AND isActive = 1 ORDER BY priority ASC")
    fun getContactsByType(type: ContactType): Flow<List<TrustedContact>>

    @Query("SELECT * FROM trusted_contacts WHERE contactType = 'EMERGENCY' AND isActive = 1 ORDER BY priority ASC LIMIT 1")
    suspend fun getPrimaryEmergencyContact(): TrustedContact?

    @Query("SELECT * FROM trusted_contacts WHERE receiveSms = 1 AND isActive = 1 ORDER BY priority ASC")
    suspend fun getSmsEnabledContacts(): List<TrustedContact>

    @Query("UPDATE trusted_contacts SET lastNotifiedAt = :timestamp WHERE id = :contactId")
    suspend fun updateLastNotified(contactId: Long, timestamp: Long)

    @Query("UPDATE trusted_contacts SET isActive = :isActive WHERE id = :contactId")
    suspend fun updateActiveStatus(contactId: Long, isActive: Boolean)

    @Query("SELECT COUNT(*) FROM trusted_contacts WHERE isActive = 1")
    fun getActiveContactsCount(): LiveData<Int>

    @Query("SELECT * FROM trusted_contacts WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getContactByPhone(phoneNumber: String): TrustedContact?

    @Query("DELETE FROM trusted_contacts WHERE id = :contactId")
    suspend fun deleteById(contactId: Long)
}
