# HerSafe - Quick Start Guide

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android device or emulator with API 24+ (Android 7.0+)
- **Important**: For location features, use a real device as GPS doesn't work well on emulators

### Installation Steps

#### Option 1: Install Pre-built APK (Fastest)
```bash
# The APK is already built at:
app/build/outputs/apk/debug/app-debug.apk

# Install to connected device:
adb install app/build/outputs/apk/debug/app-debug.apk

# Or rebuild and install:
./gradlew installDebug
```

#### Option 2: Open in Android Studio
1. Open Android Studio
2. Click "Open an Existing Project"
3. Navigate to this folder
4. Wait for Gradle sync to complete
5. Click Run ▶️

### First Launch

When you first open HerSafe:

1. **Welcome Dialog** will appear
   - Click "ابدأ" to continue

2. **Grant Permissions**
   - Location: Required for emergency location
   - SMS: Required to send emergency messages
   - Camera: For emergency recording
   - Audio: For emergency recording
   - Notifications: For alerts

3. **Add Trusted Contact** (Feature coming soon)
   - For testing, you can trigger emergency without contacts
   - SMS won't be sent if no contacts are configured

## 📱 Testing the App

### Test Emergency Button

1. **Grant all permissions** first
2. Click the large red "اضغط للطوارئ" button
3. Confirm in the dialog
4. You should see:
   - Toast message "جاري إرسال تنبيه الطوارئ..."
   - Success dialog "تم إرسال التنبيه"
   - Notification appears

### Test Monitoring Service

1. Toggle the "تفعيل المراقبة في الخلفية" switch
2. A persistent notification should appear
3. The service continues running even if you close the app
4. Toggle off to stop the service

### Test Location

Emergency button will:
1. Get your current GPS location
2. Convert it to an address (if possible)
3. Save to database
4. Would send SMS if contacts are configured

## 🔧 Development

### Project Structure

```
app/src/main/java/com/example/background_vol_up_down_app/
├── data/
│   ├── local/
│   │   ├── entities/     # Database models
│   │   ├── dao/         # Database queries
│   │   └── database/    # Database setup
│   └── repository/      # Data management
├── services/            # Background services
├── utils/              # Helper classes
└── MainActivity.kt     # Main screen
```

### Build Commands

```bash
# Clean project
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Install on device
./gradlew installDebug

# Run lint
./gradlew lint
```

### Debugging

#### Enable Debug Logs
Logs are tagged by class:
```bash
# View all HerSafe logs
adb logcat | grep -E "(MonitoringService|EmergencyManager|LocationTracking)"

# View Room database queries
adb logcat | grep -E "Room"

# View all app logs
adb logcat | grep "com.example.background_vol_up_down_app"
```

#### Check Database
```bash
# Pull database from device
adb pull /data/data/com.example.background_vol_up_down_app/databases/hersafe_database

# View database (requires sqlite3)
sqlite3 hersafe_database
.tables
SELECT * FROM emergency_events;
```

## ⚠️ Important Notes

### Battery Optimization
On some devices, you may need to disable battery optimization for the app to work properly in the background:

1. Go to Settings → Apps → HerSafe
2. Battery → Unrestricted
3. Or add to "Never sleeping apps" list

### Permissions
The app will not work properly without essential permissions:
- **Location**: Critical - without this, no location can be sent
- **SMS**: Critical - without this, no emergency messages
- **Background Location**: Important for safe journey feature

### Testing on Emulator
- Location can be simulated but is not reliable
- SMS sending will fail (no SIM card)
- Best to test on real device

## 🐛 Troubleshooting

### Build Fails
```bash
# Clear Gradle cache
./gradlew clean
rm -rf .gradle build

# Sync Gradle again
./gradlew build
```

### App Crashes on Start
1. Check if all permissions are granted
2. Check logcat for errors
3. Try uninstall and reinstall

### Location Not Working
1. Enable GPS on device
2. Grant Location permissions
3. Check if location services are enabled
4. On emulator: Send location from Extended Controls

### SMS Not Sending
1. Ensure SMS permission is granted
2. Add at least one trusted contact (feature in development)
3. Check device has SMS capability

## 📝 Adding Test Contacts (Manual)

Until the Contacts UI is ready, you can add test contacts via database:

```kotlin
// Add this code temporarily in MainActivity onCreate():
lifecycleScope.launch {
    val database = HerSafeDatabase.getDatabase(this@MainActivity)
    val contactDao = database.trustedContactDao()

    // Add test contact
    val contact = TrustedContact(
        name = "Test Contact",
        phoneNumber = "+1234567890", // Replace with real number
        contactType = ContactType.EMERGENCY,
        receiveSms = true
    )
    contactDao.insert(contact)
}
```

## 🎯 What Works Now

✅ Emergency button triggers full emergency flow
✅ Location is captured and saved
✅ Database stores all events
✅ Background service runs
✅ Notifications work
✅ SMS helper ready (needs contacts)
✅ Recording service infrastructure ready

## 🔜 Coming Soon

- [ ] UI for managing trusted contacts
- [ ] UI for safe journey feature
- [ ] UI for viewing emergency history
- [ ] Safe zones map view
- [ ] Settings screen
- [ ] Emergency widget
- [ ] Cloud sync

## 📞 Support

For issues or questions:
1. Check CLAUDE.md for technical details
2. Check README.md for feature documentation
3. Check DEVELOPMENT_SUMMARY.md for complete project status

## 🎉 You're Ready!

The app is fully functional for basic emergency alerting. Install it, grant permissions, and test the emergency button!

---

**Version**: 1.0.0 (MVP)
**Status**: Ready for Testing
**Last Updated**: November 2025
