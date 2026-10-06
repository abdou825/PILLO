package com.example.domain

import com.example.data.model.MealRelation
import com.example.data.model.UserProfile
import java.util.Locale

object ScheduleCalculator {

    fun parseTimeToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return 480 // fallback 08:00
        val hour = parts[0].toIntOrNull() ?: 8
        val min = parts[1].toIntOrNull() ?: 0
        return hour * 60 + min
    }

    fun formatMinutesToTime(totalMinutes: Int): String {
        val normalized = ((totalMinutes % 1440) + 1440) % 1440
        val hour = normalized / 60
        val min = normalized % 60
        return String.format(Locale.US, "%02d:%02d", hour, min)
    }

    /**
     * Calculates dose times as a sorted list of "HH:mm" strings based on user profile routine
     * and medicine parameters.
     */
    fun calculateDoseTimes(
        timesPerDay: Int,
        mealRelation: MealRelation,
        profile: UserProfile
    ): List<String> {
        if (profile.ramadanModeEnabled) {
            return calculateRamadanDoseTimes(
                timesPerDay = timesPerDay,
                mealRelation = mealRelation,
                suhoorTime = profile.suhoorTime,
                iftarTime = profile.iftarTime
            )
        }

        val count = timesPerDay.coerceIn(1, 6)
        val wakeMinutes = parseTimeToMinutes(profile.wakeTime)
        val sleepMinutes = parseTimeToMinutes(profile.sleepTime)
        val breakfastMinutes = parseTimeToMinutes(profile.breakfastTime)
        val lunchMinutes = parseTimeToMinutes(profile.lunchTime)
        val dinnerMinutes = parseTimeToMinutes(profile.dinnerTime)

        val awakeStart = wakeMinutes
        val awakeEnd = if (sleepMinutes > wakeMinutes) sleepMinutes else sleepMinutes + 1440

        fun clampAwake(minVal: Int): Int {
            return minVal.coerceIn(awakeStart, awakeEnd - 15)
        }

        val calculatedMinutes = mutableListOf<Int>()

        when (mealRelation) {
            MealRelation.BEFORE_MEAL -> {
                val bDose = clampAwake(breakfastMinutes - 30)
                val lDose = clampAwake(lunchMinutes - 30)
                val dDose = clampAwake(dinnerMinutes - 30)

                when (count) {
                    1 -> calculatedMinutes.add(bDose)
                    2 -> calculatedMinutes.addAll(listOf(bDose, dDose))
                    3 -> calculatedMinutes.addAll(listOf(bDose, lDose, dDose))
                    else -> {
                        calculatedMinutes.addAll(listOf(bDose, lDose, dDose))
                        val extraCount = count - 3
                        val step = (awakeEnd - awakeStart) / (extraCount + 1)
                        for (i in 1..extraCount) {
                            calculatedMinutes.add(clampAwake(awakeStart + i * step))
                        }
                    }
                }
            }

            MealRelation.AFTER_MEAL -> {
                val bDose = clampAwake(breakfastMinutes + 30)
                val lDose = clampAwake(lunchMinutes + 30)
                val dDose = clampAwake(dinnerMinutes + 30)

                when (count) {
                    1 -> calculatedMinutes.add(bDose)
                    2 -> calculatedMinutes.addAll(listOf(bDose, dDose))
                    3 -> calculatedMinutes.addAll(listOf(bDose, lDose, dDose))
                    else -> {
                        calculatedMinutes.addAll(listOf(bDose, lDose, dDose))
                        val extraCount = count - 3
                        val step = (awakeEnd - awakeStart) / (extraCount + 1)
                        for (i in 1..extraCount) {
                            calculatedMinutes.add(clampAwake(awakeStart + i * step))
                        }
                    }
                }
            }

            MealRelation.ANY_TIME -> {
                val windowStart = clampAwake(awakeStart + 30)
                val windowEnd = clampAwake(awakeEnd - 60)
                if (count == 1) {
                    calculatedMinutes.add((windowStart + windowEnd) / 2)
                } else {
                    val step = (windowEnd - windowStart) / (count - 1)
                    for (i in 0 until count) {
                        calculatedMinutes.add(clampAwake(windowStart + i * step))
                    }
                }
            }
        }

        return calculatedMinutes
            .map { it % 1440 }
            .distinct()
            .sorted()
            .map { formatMinutesToTime(it) }
    }

    /**
     * Calculates Ramadan schedule: doses spread strictly between Iftar and Suhoor (non-fasting hours).
     */
    fun calculateRamadanDoseTimes(
        timesPerDay: Int,
        mealRelation: MealRelation,
        suhoorTime: String,
        iftarTime: String
    ): List<String> {
        val count = timesPerDay.coerceIn(1, 6)
        val suhoorMin = parseTimeToMinutes(suhoorTime) // e.g. 03:30 (210)
        val iftarMin = parseTimeToMinutes(iftarTime)   // e.g. 18:15 (1095)

        // Non-fasting window spans from iftarMin to next day's suhoorMin (over midnight)
        // Duration = (1440 - iftarMin) + suhoorMin
        val nonFastingDuration = (1440 - iftarMin) + suhoorMin

        val result = mutableListOf<Int>()

        when (mealRelation) {
            MealRelation.BEFORE_MEAL -> {
                // Before Iftar or Before Suhoor
                val beforeIftar = (iftarMin - 15 + 1440) % 1440
                val beforeSuhoor = (suhoorMin - 30 + 1440) % 1440
                when (count) {
                    1 -> result.add(beforeIftar)
                    2 -> result.addAll(listOf(beforeIftar, beforeSuhoor))
                    else -> {
                        result.addAll(listOf(beforeIftar, beforeSuhoor))
                        val extra = count - 2
                        val step = nonFastingDuration / (extra + 1)
                        for (i in 1..extra) {
                            result.add((iftarMin + i * step) % 1440)
                        }
                    }
                }
            }
            MealRelation.AFTER_MEAL -> {
                val afterIftar = (iftarMin + 30) % 1440
                val afterSuhoor = (suhoorMin - 10 + 1440) % 1440 // slightly before fasting begins
                when (count) {
                    1 -> result.add(afterIftar)
                    2 -> result.addAll(listOf(afterIftar, afterSuhoor))
                    else -> {
                        result.addAll(listOf(afterIftar, afterSuhoor))
                        val extra = count - 2
                        val step = nonFastingDuration / (extra + 1)
                        for (i in 1..extra) {
                            result.add((iftarMin + i * step) % 1440)
                        }
                    }
                }
            }
            MealRelation.ANY_TIME -> {
                if (count == 1) {
                    result.add((iftarMin + nonFastingDuration / 2) % 1440)
                } else {
                    val step = nonFastingDuration / (count + 1)
                    for (i in 1..count) {
                        result.add((iftarMin + i * step) % 1440)
                    }
                }
            }
        }

        return result.distinct().sorted().map { formatMinutesToTime(it) }
    }
}
