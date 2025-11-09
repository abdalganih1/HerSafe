package com.example.background_vol_up_down_app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.background_vol_up_down_app.data.local.dao.*
import com.example.background_vol_up_down_app.data.local.entities.*

@Database(
    entities = [
        EmergencyEvent::class,
        TrustedContact::class,
        SafeZone::class,
        LocationPoint::class,
        SafeJourney::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HerSafeDatabase : RoomDatabase() {

    abstract fun emergencyEventDao(): EmergencyEventDao
    abstract fun trustedContactDao(): TrustedContactDao
    abstract fun safeZoneDao(): SafeZoneDao
    abstract fun locationPointDao(): LocationPointDao
    abstract fun safeJourneyDao(): SafeJourneyDao

    companion object {
        @Volatile
        private var INSTANCE: HerSafeDatabase? = null

        fun getDatabase(context: Context): HerSafeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HerSafeDatabase::class.java,
                    "hersafe_database"
                )
                    .fallbackToDestructiveMigration() // For development only
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getInstance(context: Context): HerSafeDatabase {
            return getDatabase(context)
        }
    }
}
