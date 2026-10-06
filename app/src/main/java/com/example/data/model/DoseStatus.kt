package com.example.data.model

enum class DoseStatus(val labelArabic: String) {
    PENDING("في انتظار الميعاد"),
    TAKEN("أخدت الدوا"),
    SKIPPED("تم التخطي"),
    SNOOZED("تأجيل"),
    MISSED("فاتتك الجرعة")
}
