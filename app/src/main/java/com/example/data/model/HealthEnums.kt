package com.example.data.model

enum class HealthReadingType(val labelArabic: String, val unitArabic: String) {
    BLOOD_PRESSURE("ضغط الدم", "ملم زئبق"),
    BLOOD_SUGAR("سكر الدم", "ملجم/ديسيلتر"),
    WEIGHT("الوزن", "كجم"),
    PULSE("النبض", "نبضة/دقيقة"),
    WATER("شرب الماء", "أكواب"),
    SLEEP("ساعات النوم", "ساعات"),
    MOOD("الحالة المزاجية", "تقييم")
}

enum class AppointmentType(val labelArabic: String) {
    DOCTOR("كشف دكتور"),
    TEST("تحليل / أشعة"),
    RECHECK("استشارة / إعادة كشف")
}
