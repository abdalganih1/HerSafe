بناءً على تحليل الصورة (تطبيق HerSafe) والجداول التي اقترحتها، قمت بإعادة تصميم وتوسيع قاعدة البيانات لتكون **احترافية، قابلة للتوسع، وتدعم ميزات الذكاء الاصطناعي والـ API**.

لقد أضفت جداول ضرورية لتغطية ميزات "الرحلة الآمنة"، "تحليل المناطق"، و"الوسائط المتعددة" (صوت/صورة).

إليك المخطط المقترح بلغة **DBML** الجاهزة للنسخ واللصق في موقع **dbdiagram.io**:

```dbml
// جدول المستخدمين
// يراعي البيانات الشخصية وحقول التوثيق المطلوبة
Table users {
  id big_integer [pk, increment]
  first_name varchar
  last_name varchar [note: 'اختياري']
  username varchar [unique]
  email varchar [unique]
  phone_number varchar [unique]
  password_hash varchar
  two_factor_secret varchar [note: 'رمز التحقق بخطوتين']
  is_email_verified boolean [default: false]
  is_phone_verified boolean [default: false]
  fcm_token varchar [note: 'رمز استقبال الإشعارات للاندرويد']
  
  // البيانات الديموغرافية (كما طلبت)
  birth_date date
  gender enum('male', 'female')
  residence_address text
  birth_place varchar
  father_name varchar
  mother_name varchar
  
  created_at timestamp
  updated_at timestamp
}

// جدول جهات اتصال الطوارئ
// الأشخاص الذين سيتم إشعارهم عند الخطر
Table emergency_contacts {
  id big_integer [pk, increment]
  user_id big_integer [ref: > users.id]
  contact_name varchar
  contact_phone varchar
  is_default boolean [default: false]
  relationship varchar [note: 'أب، أخ، صديق...']
  created_at timestamp
}

// جدول المناطق والمواقع الجغرافية
// يستخدمه الذكاء الاصطناعي لتصنيف المناطق (آمنة/خطرة)
Table safety_zones {
  id big_integer [pk, increment]
  name varchar [note: 'اسم المنطقة مثل: حي الجلاء']
  city varchar
  latitude decimal(10, 8)
  longitude decimal(11, 8)
  radius_meters integer [default: 500]
  safety_score integer [note: 'يتم حسابه عبر AI من 1 إلى 10']
  report_count integer [default: 0, note: 'عدد البلاغات التاريخية']
  description text
  created_at timestamp
}

// جدول الرحلات الآمنة
// خاص بميزة "تفعيل وضع الرحلة الآمنة" المذكورة في الصورة
Table safe_journeys {
  id big_integer [pk, increment]
  user_id big_integer [ref: > users.id]
  start_lat decimal(10, 8)
  start_lng decimal(11, 8)
  dest_lat decimal(10, 8)
  dest_lng decimal(11, 8)
  status enum('active', 'completed', 'panic') [default: 'active']
  started_at timestamp
  ended_at timestamp
}

// سجل الطوارئ والبلاغات (الجدول الرئيسي)
// يربط البلاغ بالموقع والمستخدم والحالة
Table incidents {
  id big_integer [pk, increment]
  user_id big_integer [ref: > users.id]
  safety_zone_id big_integer [ref: > safety_zones.id, note: 'المنطقة التي حدث فيها البلاغ']
  
  // تفاصيل البلاغ
  type enum('sos_button', 'auto_detect', 'voice_alert', 'journey_anomaly') [note: 'نوع التنبيه: زر، تلقائي، صوتي، انحراف رحلة']
  description text [note: 'ملاحظة المستخدم']
  
  // الموقع الدقيق لحظة البلاغ
  gps_lat decimal(10, 8)
  gps_lng decimal(11, 8)
  
  // حالة البلاغ للتحقق (صادق/كاذب) وتدريب الذكاء الاصطناعي
  is_verified boolean [default: null, note: 'هل البلاغ حقيقي؟']
  admin_notes text
  
  created_at timestamp
}

// جدول الوسائط (الأدلة)
// خاص بميزة الزر السري الذي يسجل الصوت أو يشغل الكاميرا
Table incident_media {
  id big_integer [pk, increment]
  incident_id big_integer [ref: > incidents.id]
  media_type enum('audio', 'image', 'video')
  file_path varchar [note: 'مسار الملف على السيرفر']
  duration_seconds integer [note: 'للصوت والفيديو']
  created_at timestamp
}

// جدول تقييمات المناطق
// خاص بميزة "التعليقات من المستخدمين" لتحسين دقة المناطق
Table zone_reviews {
  id big_integer [pk, increment]
  user_id big_integer [ref: > users.id]
  safety_zone_id big_integer [ref: > safety_zones.id]
  rating integer [note: 'تقييم من 1-5']
  comment text
  created_at timestamp
}
```

### شرح التحسينات والميزات التقنية في هذا المخطط:

1.  **فصل الوسائط (`incident_media`):**
    *   الصورة تذكر "تسجيل صوت" و "تشغيل كاميرا". وضع رابط واحد في جدول البلاغات غير كافٍ، فقد يكون هناك تسجيل صوتي وصورة معاً. هذا الجدول يسمح بتخزين عدة ملفات لنفس البلاغ.

2.  **جدول الرحلات (`safe_journeys`):**
    *   لتغطية ميزة **"الرحلة الآمنة"** التي تبدأ عند المشي وتغلق عند الوصول. هذا الجدول سيسمح للنظام (Backend) بمراقبة حالة الرحلة، وإذا لم يتم إغلاقها (`completed`) في الوقت المتوقع، يقوم النظام تلقائياً بإنشاء بلاغ.

3.  **تحسين جدول المناطق (`safety_zones`):**
    *   أضفت `safety_score` (درجة الأمان) و `radius_meters` (نطاق المنطقة).
    *   **دور الذكاء الاصطناعي هنا:** الـ Backend سيقوم بتحليل جدول `incidents` (البلاغات) وجدول `zone_reviews` (التقييمات) وبناءً عليها يقوم بتحديث `safety_score` لهذه المنطقة تلقائياً.

4.  **أنواع البلاغات (`incidents.type`):**
    *   تم تحديد النوع (زر طوارئ، اكتشاف تلقائي، تنبيه صوتي) ليتوافق مع ميزات الصورة. هذا يساعد في تحليل البيانات لاحقاً (مثلاً: هل معظم الحالات هي تحرش لفظي "صوتي" أم خطر جسدي؟).

5.  **التوافق مع Laravel & Android:**
    *   استخدام `created_at` و `updated_at` هو المعيار في Laravel (Timestamps).
    *   حقل `fcm_token` في جدول المستخدمين ضروري جداً لإرسال إشعارات (Notifications) لهاتف الأندرويد.
    *   حقل `password_hash` لتخزين كلمة المرور مشفرة (Bcrypt) كما هو متبع في Laravel.

### نصيحة للتطبيق (SQLite vs MySQL):
*   في **Android (SQLite)**: ستحتاج لنسخة مصغرة من هذه الجداول (خاصة `users`, `emergency_contacts`, `safe_journeys`) لتعمل أوفلاين.
*   في **Web (Laravel - MySQL)**: ستكون قاعدة البيانات الكاملة هذه موجودة، ويتم عمل مزامنة (Sync) عند توفر الإنترنت لرفع البلاغات وتحديث المناطق الخطرة.
