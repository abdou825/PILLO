package com.example.data.model

enum class AgeMode(val labelArabic: String, val ageRangeArabic: String) {
    KIDS("أطفال", "5 - 12 سنة"),
    TEENS("مراهقين", "13 - 17 سنة"),
    YOUNG_ADULTS("شباب", "18 - 40 سنة"),
    ADULTS("كبار", "40 - 60 سنة"),
    SENIORS("كبار السن", "+60 سنة")
}
