package com.example.ai

import kotlin.random.Random

/**
 * Pillo Motivational AI Engine.
 * 
 * STRICT RULES:
 * - 100% Motivational, uplifting, psychological encouragement only.
 * - NEVER provides medical advice, diagnosis, or treatment recommendations.
 * - Warm, enthusiastic Egyptian colloquial Arabic.
 */
object MotivationalAiCoach {

    private val generalQuotes = listOf(
        "يا {name}، كل ميعاد دوا بتلتزم بيه هو خطوة كبيرة لصحتك وراحتك.. استمر يا بطل! 🌟",
        "الالتزام مش مجرد روتين، ده دليل على قوة إرادتك واهتمامك بنفسك وبحبايبك يا {name}! 💪",
        "عاش يا فنان! سجل التزامك النهارده مشرف جداً، استمر بنفس الروح! 🔥",
        "صحتك هي أغلى كنز.. وبيلّو فخور بيك وبإصرارك كل يوم يا {name}! 👑",
        "كل يوم بتعديه بانتظام بيقربك من شهادة التميز الملكية.. كمل يا بطل! 🏅"
    )

    private val morningQuotes = listOf(
        "صباح الخير والنشاط يا {name}! بداية يوم جديدة بنشاط والتزام حديدي مع بيلّو! ☀️",
        "صباح الورد يا {name}! رشفة مية مع جرعة الصباح هي سر اليوم المظبوط! 🌿",
        "يوم جديد وإنجاز جديد مستنيك يا {name}.. يلا نبدأ يومنا بالالتزام التام! 🚀"
    )

    private val eveningQuotes = listOf(
        "مساء الفل يا {name}.. يوم جميل مليان التزام وصحة، نورت بيلّو النهارده! 🌙",
        "مساء الراحة يا {name}.. فخورين بيك وبكل جرعة أخدتها في ميعادها النهارده! ✨",
        "نوم هني وصحة دايمة يا {name}، جاهزين بكرة لمواصلة سلسلة الالتزام! 💤"
    )

    private val streakQuotes = listOf(
        "سلسلة التزام {streak} يوم متواصلة بدون أي تأخير! أنت قدوة حقيقية يا {name}! 🔥",
        "ما شاء الله يا {name}! {streak} يوم التزام مستمر.. أليفك في بيلّو بيحتفل بيك وعملاتك بتزيد! 🐾",
        "الاستمرارية هي سر النجاح.. {streak} يوم التزام يثبتوا إنك بطل من طراز خاص يا {name}! 🏆"
    )

    private val lazyBoostQuotes = listOf(
        "عارفين إن الالتزام ساعات بيكون تقيل، بس تذكر: لحظة التزام بتوفر عليك كتير بعدين! شد حيلك يا {name}! 🛡️",
        "لما تحس بكسل يا {name}، افتكر إنك قطعت مشوار رائع ووصلت لإنجاز كبير.. متخليش حبة تفوتك! 💥",
        "دقيقة واحدة تاخد فيها جرعتك وتشرب كوباية مية يا {name}.. وهترجع ليومك بكل طاقة! ⚡"
    )

    private val doseCompletedQuotes = listOf(
        "برافو عليك يا {name}! تم تسجيل الجرعة بنجاح والعداد بينقص بكل سلاسة! 🎯",
        "تسلم إيدك يا {name}! جرعة في ميعادها يعني صحة أحسن وراحة بال! 🌟",
        "ألف هنا وشفا يا بطل {name}! خطوة تانية ناجحة في كورس علاجك! 🥇"
    )

    enum class MotivationMood(val title: String, val icon: String) {
        GENERAL("تحفيز شامل", "✨"),
        MORNING("صباح النشاط", "☀️"),
        EVENING("مساء الهدوء", "🌙"),
        STREAK("شعلة الالتزام", "🔥"),
        LAZY_BOOST("تغلب على الكسل", "⚡"),
        DOSE_COMPLETED("احتفال بالجرعة", "🎉")
    }

    fun generateMotivationalMessage(
        userName: String,
        mood: MotivationMood = MotivationMood.GENERAL,
        streakDays: Int = 1,
        activeMedCount: Int = 1
    ): String {
        val safeName = userName.ifEmpty { "يا بطل" }
        val pool = when (mood) {
            MotivationMood.GENERAL -> generalQuotes
            MotivationMood.MORNING -> morningQuotes
            MotivationMood.EVENING -> eveningQuotes
            MotivationMood.STREAK -> streakQuotes
            MotivationMood.LAZY_BOOST -> lazyBoostQuotes
            MotivationMood.DOSE_COMPLETED -> doseCompletedQuotes
        }

        val raw = pool[Random.nextInt(pool.size)]
        return raw.replace("{name}", safeName)
            .replace("{streak}", streakDays.toString())
            .replace("{count}", activeMedCount.toString())
    }
}
