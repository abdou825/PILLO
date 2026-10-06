package com.example

import com.example.data.model.MealRelation
import com.example.data.model.UserProfile
import com.example.domain.ScheduleCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testScheduleCalculator_beforeMeal() {
        val profile = UserProfile(
            wakeTime = "07:00",
            sleepTime = "23:00",
            breakfastTime = "08:00",
            lunchTime = "14:00",
            dinnerTime = "20:00"
        )

        // 1 dose = 30 min before breakfast (07:30)
        val times1 = ScheduleCalculator.calculateDoseTimes(1, MealRelation.BEFORE_MEAL, profile)
        assertEquals(1, times1.size)
        assertEquals("07:30", times1[0])

        // 2 doses = breakfast - 30m, dinner - 30m
        val times2 = ScheduleCalculator.calculateDoseTimes(2, MealRelation.BEFORE_MEAL, profile)
        assertEquals(2, times2.size)
        assertEquals("07:30", times2[0])
        assertEquals("19:30", times2[1])

        // 3 doses = breakfast - 30m, lunch - 30m, dinner - 30m
        val times3 = ScheduleCalculator.calculateDoseTimes(3, MealRelation.BEFORE_MEAL, profile)
        assertEquals(3, times3.size)
        assertEquals("07:30", times3[0])
        assertEquals("13:30", times3[1])
        assertEquals("19:30", times3[2])
    }

    @Test
    fun testScheduleCalculator_afterMeal() {
        val profile = UserProfile(
            wakeTime = "07:00",
            sleepTime = "23:00",
            breakfastTime = "08:00",
            lunchTime = "14:00",
            dinnerTime = "20:00"
        )

        // 1 dose = 30 min after breakfast (08:30)
        val times1 = ScheduleCalculator.calculateDoseTimes(1, MealRelation.AFTER_MEAL, profile)
        assertEquals(1, times1.size)
        assertEquals("08:30", times1[0])

        // 2 doses = breakfast + 30m, dinner + 30m
        val times2 = ScheduleCalculator.calculateDoseTimes(2, MealRelation.AFTER_MEAL, profile)
        assertEquals(2, times2.size)
        assertEquals("08:30", times2[0])
        assertEquals("20:30", times2[1])
    }

    @Test
    fun testScheduleCalculator_anyTime() {
        val profile = UserProfile(
            wakeTime = "07:00",
            sleepTime = "23:00"
        )
        val times2 = ScheduleCalculator.calculateDoseTimes(2, MealRelation.ANY_TIME, profile)
        assertEquals(2, times2.size)
        // awakeStart + 30m = 07:30, awakeEnd - 60m = 22:00
        assertEquals("07:30", times2[0])
        assertEquals("22:00", times2[1])
    }

    @Test
    fun testUserProfile_accountAndFatherPhoneDefaults() {
        val profile = UserProfile(
            displayName = "أحمد البطل",
            userEmail = "ahmed@pillo.app",
            fatherPhone = "01012345678",
            avatarId = "avatar_2"
        )
        assertEquals("أحمد البطل", profile.displayName)
        assertEquals("ahmed@pillo.app", profile.userEmail)
        assertEquals("01012345678", profile.fatherPhone)
        assertEquals("avatar_2", profile.avatarId)
    }
}
