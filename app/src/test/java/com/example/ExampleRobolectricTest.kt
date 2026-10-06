package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pillo", appName)
  }

  @Test
  fun `user profile google login fields validation`() {
    val profile = com.example.data.model.UserProfile(
        displayName = "عبدالرحمن عتريس",
        userEmail = "abdelrahman1atris@gmail.com",
        isGoogleLinked = true
    )
    assertEquals("عبدالرحمن عتريس", profile.displayName)
    assertEquals("abdelrahman1atris@gmail.com", profile.userEmail)
    assertEquals(true, profile.isGoogleLinked)
  }

  @Test
  fun `motivational ai coach generates personalized non-medical message`() {
    val message = com.example.ai.MotivationalAiCoach.generateMotivationalMessage(
        userName = "عبدالرحمن عتريس",
        mood = com.example.ai.MotivationalAiCoach.MotivationMood.STREAK,
        streakDays = 14
    )
    assert(message.contains("عبدالرحمن عتريس"))
    assert(message.contains("14"))
    assert(!message.contains("تشخيص") && !message.contains("علاجك سليم بنسبة"))
  }
}
