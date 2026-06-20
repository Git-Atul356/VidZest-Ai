package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.VideoEntity
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
    assertEquals("vidZest ai", appName)
  }

  @Test
  fun `verify VideoEntity handles shared platforms default and updates securely`() {
    val defaultVideo = VideoEntity(
        id = 1L,
        niche = "Space Trivia",
        trendingTopic = "Mars Colonization Mystery",
        status = "PUBLISHED"
    )

    // Verify default value of multi-channel shared string
    assertEquals("", defaultVideo.sharedPlatforms)

    // Verify copy works successfully
    val updatedVideo = defaultVideo.copy(sharedPlatforms = "Twitter/X, Facebook")
    assertEquals("Twitter/X, Facebook", updatedVideo.sharedPlatforms)
    assertEquals("Space Trivia", updatedVideo.niche)
  }
}
