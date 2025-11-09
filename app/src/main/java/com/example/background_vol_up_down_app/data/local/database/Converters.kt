package com.example.background_vol_up_down_app.data.local.database

import androidx.room.TypeConverter
import com.example.background_vol_up_down_app.data.local.entities.*

class Converters {

    @TypeConverter
    fun fromEmergencyType(value: EmergencyType): String {
        return value.name
    }

    @TypeConverter
    fun toEmergencyType(value: String): EmergencyType {
        return EmergencyType.valueOf(value)
    }

    @TypeConverter
    fun fromEmergencyStatus(value: EmergencyStatus): String {
        return value.name
    }

    @TypeConverter
    fun toEmergencyStatus(value: String): EmergencyStatus {
        return EmergencyStatus.valueOf(value)
    }

    @TypeConverter
    fun fromContactType(value: ContactType): String {
        return value.name
    }

    @TypeConverter
    fun toContactType(value: String): ContactType {
        return ContactType.valueOf(value)
    }

    @TypeConverter
    fun fromZoneType(value: ZoneType): String {
        return value.name
    }

    @TypeConverter
    fun toZoneType(value: String): ZoneType {
        return ZoneType.valueOf(value)
    }

    @TypeConverter
    fun fromJourneyStatus(value: JourneyStatus): String {
        return value.name
    }

    @TypeConverter
    fun toJourneyStatus(value: String): JourneyStatus {
        return JourneyStatus.valueOf(value)
    }
}
