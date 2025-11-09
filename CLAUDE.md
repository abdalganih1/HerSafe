# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**HerSafe** - تطبيق أمان مخصص للفتيات يعمل في الخلفية لتوفير الحماية والأمان

**Package**: `com.example.background_vol_up_down_app`
**Min SDK**: 24 (Android 7.0)
**Target SDK**: 36
**Language**: Kotlin 2.0.21
**Build System**: Gradle with version catalogs

## Core Features

### 1. Secret Activation Button
- الضغط على زر خفض الصوت لمدة 3 ثواني يفعّل وضع الطوارئ
- يعمل في الخلفية حتى عند إغلاق التطبيق

### 2. Emergency Alert System
- إرسال رسالة SMS/WhatsApp تلقائية لشخص موثوق
- إرسال الموقع الجغرافي المباشر (GPS coordinates)
- حفظ سجل الحدث في قاعدة البيانات المحلية

### 3. Local & Cloud Database Sync
- قاعدة بيانات محلية (Room) لحفظ سجلات الحوادث
- التزامن التلقائي مع القاعدة العالمية عند توفر الإنترنت
- تخزين: موقع الحدث، الوقت، التفاصيل

### 4. Safe Zone Analysis
- تحليل بيانات القاعدة العالمية لتحديد المناطق الأقل أماناً
- عرض خريطة حرارية (heatmap) للمناطق الخطرة
- تنبيهات عند الدخول لمنطقة غير آمنة

### 5. Safe Journey Mode
- تتبع موقع المستخدم أثناء المشي
- تحديد نقطة الوصول المستهدفة
- إرسال تنبيهات تلقائية إذا توقف التتبع أو انحراف عن المسار
- إشعار للشخص الموثوق عند الوصول بأمان

### 6. Recording & Live Streaming
عند تفعيل الطوارئ:
- تسجيل صوتي في الخلفية
- التقاط صور/فيديو من الكاميرا
- بث مباشر لسيرفر أو IP مستقل (إذا متوفر الإنترنت)

## Build Commands

### Building the app
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean build
```

### Running tests
```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew testDebugUnitTest

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests "com.example.background_vol_up_down_app.*"
```

### Installing and running
```bash
# Install debug build on connected device
./gradlew installDebug

# Uninstall app
./gradlew uninstallDebug
```

### Linting and code quality
```bash
# Run lint checks
./gradlew lint

# Run lint and generate report
./gradlew lintDebug
```

## Architecture

### Architectural Pattern
- **MVVM (Model-View-ViewModel)** for UI components
- **Repository Pattern** for data management
- **Service-Oriented** for background operations

### Core Components

#### 1. Background Services
- **VolumeButtonMonitorService**: Foreground service لمراقبة زر خفض الصوت
- **LocationTrackingService**: تتبع الموقع في وضع الرحلة الآمنة
- **EmergencyRecordingService**: تسجيل الصوت/الفيديو/البث المباشر

#### 2. BroadcastReceivers
- **VolumeButtonReceiver**: استقبال أحداث أزرار الصوت
- **InternetConnectivityReceiver**: مراقبة الاتصال بالإنترنت للتزامن

#### 3. Database Layer
- **Room Database**: قاعدة بيانات محلية
  - `EmergencyEvent` entity (موقع، وقت، نوع الحدث، حالة التزامن)
  - `TrustedContact` entity (جهات الاتصال الموثوقة)
  - `SafeZone` entity (المناطق الآمنة/الخطرة)
  - DAOs for each entity

#### 4. Repository Classes
- **EmergencyRepository**: إدارة سجلات الطوارئ
- **LocationRepository**: إدارة بيانات الموقع
- **SyncRepository**: التزامن مع السيرفر

#### 5. ViewModels & UI
- **MainActivity**: شاشة رئيسية للإعدادات
- **SafeJourneyActivity**: واجهة وضع الرحلة الآمنة
- **SafeZoneMapActivity**: عرض خريطة المناطق الآمنة
- **SettingsActivity**: إدارة جهات الاتصال والإعدادات

### Required Permissions (AndroidManifest.xml)

```xml
<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Communication -->
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.READ_CONTACTS" />

<!-- Recording -->
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- Foreground Service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />

<!-- Wake Lock & Boot -->
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Key Dependencies (to be added)

#### Database & Storage
```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// DataStore for preferences
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

#### Location Services
```kotlin
// Google Play Services - Location
implementation("com.google.android.gms:play-services-location:21.0.1")
implementation("com.google.android.gms:play-services-maps:18.2.0")
```

#### Networking & Backend
```kotlin
// Retrofit for API calls
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

#### Camera & Recording
```kotlin
// CameraX
implementation("androidx.camera:camera-core:1.3.1")
implementation("androidx.camera:camera-camera2:1.3.1")
implementation("androidx.camera:camera-lifecycle:1.3.1")
implementation("androidx.camera:camera-view:1.3.1")
```

#### Live Streaming (Optional)
```kotlin
// RTMP/WebRTC for live streaming
// Consider: com.github.pedroSG94:rtmp-rtsp-stream-client-java
```

#### WorkManager (for background sync)
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

## Data Flow

### Emergency Activation Flow
1. `VolumeButtonMonitorService` يكتشف الضغط لمدة 3 ثواني
2. يُطلق `EmergencyEvent` في `EmergencyRepository`
3. يحفظ في Room Database محلياً
4. `LocationRepository` يحصل على الموقع الحالي
5. `SmsManager` يرسل رسالة للشخص الموثوق مع الموقع
6. `EmergencyRecordingService` يبدأ التسجيل/البث
7. `SyncRepository` يراقب الاتصال بالإنترنت
8. عند توفر النت: يُرسل البيانات للسيرفر

### Safe Journey Flow
1. المستخدم يفعّل "وضع الرحلة الآمنة" ويحدد الوجهة
2. `LocationTrackingService` يبدأ التتبع الدوري (كل 30 ثانية)
3. يحفظ المواقع في Database
4. يراقب الانحراف عن المسار أو التوقف المفاجئ
5. عند الوصول: إشعار للمستخدم والشخص الموثوق
6. حفظ السجل في Database

### Database Sync Strategy
- استخدام `WorkManager` للمزامنة الدورية
- فحص `InternetConnectivityReceiver` للاتصال
- إعادة المحاولة التلقائية عند فشل الإرسال
- علامة `isSynced` في كل `EmergencyEvent`

## Important Implementation Notes

### Volume Button Detection
- لا يمكن الاعتماد على `BroadcastReceiver` للأزرار
- يجب استخدام `AccessibilityService` أو مراقبة النظام
- البديل: استخدام زر داخل التطبيق كزر طوارئ

### Background Service Restrictions (Android 8.0+)
- يجب استخدام `Foreground Service` مع notification دائم
- تصنيف Service بنوع: `FOREGROUND_SERVICE_TYPE_LOCATION`
- طلب إذن `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` للعمل المستمر

### Location Tracking Best Practices
- استخدام `FusedLocationProviderClient` من Google Play Services
- Battery optimization: ضبط update interval حسب الحاجة
- Geofencing للمناطق الخطرة

### Privacy & Security
- تشفير قاعدة البيانات المحلية (`SQLCipher`)
- تشفير الاتصال بالسيرفر (HTTPS/SSL)
- عدم حفظ بيانات حساسة في SharedPreferences
- طلب الأذونات في الوقت المناسب (runtime permissions)

### Testing Considerations
- اختبار على أجهزة حقيقية (GPS لا يعمل جيداً على Emulator)
- اختبار Battery optimization scenarios
- اختبار انقطاع الإنترنت والتزامن
- اختبار الأذونات المرفوضة

## Project Structure (Expected)

```
app/src/main/java/com/example/background_vol_up_down_app/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entities/
│   │   └── database/
│   ├── remote/
│   │   ├── api/
│   │   └── dto/
│   └── repository/
├── domain/
│   ├── models/
│   └── usecases/
├── ui/
│   ├── main/
│   ├── journey/
│   ├── map/
│   └── settings/
├── services/
│   ├── VolumeButtonMonitorService.kt
│   ├── LocationTrackingService.kt
│   └── EmergencyRecordingService.kt
├── receivers/
│   ├── BootReceiver.kt
│   └── ConnectivityReceiver.kt
├── utils/
│   ├── PermissionHelper.kt
│   ├── NotificationHelper.kt
│   └── LocationHelper.kt
└── MainActivity.kt
```

## Development Workflow

1. **Phase 1**: Setup core architecture (Room, Repository, ViewModel)
2. **Phase 2**: Implement volume button detection & emergency alert
3. **Phase 3**: Add location tracking & safe journey mode
4. **Phase 4**: Implement recording & streaming features
5. **Phase 5**: Backend integration & sync
6. **Phase 6**: Safe zone analysis & heatmap
7. **Phase 7**: Testing & optimization
