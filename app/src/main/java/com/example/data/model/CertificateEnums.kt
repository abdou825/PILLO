package com.example.data.model

enum class MilestoneType(val labelArabic: String) {
    DAY_7("التزام 7 أيام"),
    DAY_14("التزام 14 يوماً"),
    DAY_30("التزام شهر كامل"),
    DAY_90("التزام 3 شهور"),
    DAY_180("التزام 6 شهور"),
    DAY_365("التزام سنة كاملة"),
    COMPLETION("إتمام كامل العلاج"),
    EXTENSION_PERIOD("إتمام مرحلة والتمديد")
}

enum class BadgeTier(val labelArabic: String, val colorHex: Long) {
    BRONZE("برونزي", 0xFFCD7F32),
    SILVER("فضي", 0xFFC0C0C0),
    GOLD("ذهبي", 0xFFFFD700),
    DIAMOND("ماسي", 0xFFB9F2FF)
}
