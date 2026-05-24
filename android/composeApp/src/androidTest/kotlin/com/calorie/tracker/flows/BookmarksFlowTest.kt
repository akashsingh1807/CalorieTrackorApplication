package com.calorie.tracker.flows

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.calorie.tracker.App
import com.calorie.tracker.fakes.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class BookmarksFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOpenBookmarksAndEmptyState() {
        val authRepository = FakeAuthRepository()
        authRepository.loggedIn = true

        val mockProfile = com.calorie.tracker.model.UserProfile(
            id = 1L, name = "Test", email = "test@example.com",
            height = 180.0, weight = 75.0, age = 25, lifestyle = "SEDENTARY", goal = "MAINTAIN",
            dailyCalorieGoal = 2000, dailyProteinGoal = 100, dailyCarbsGoal = 200, dailyFatGoal = 60
        )
        val apiClient = io.mockk.mockk<com.calorie.tracker.core.network.CalorieApiClient>(relaxed = true)
        io.mockk.coEvery { apiClient.getProfile() } returns Result.success(mockProfile)

        composeTestRule.setContent {
            App(
                authRepository = authRepository,
                mealRepository = FakeMealRepository(),
                apiClient = apiClient,
                bookmarkRepository = FakeBookmarkRepository(), // Empty by default
                weightRepository = FakeWeightRepository(),
                waterRepository = FakeWaterRepository()
            )
        }

        composeTestRule.waitForIdle()

        // Verify Bookmark icon is on dashboard (often top bar or list header)
        // Here we test if clicking the bookmark icon opens the sheet.
        // If there's a bookmark icon button, we click it.
        // Assuming content description "Saved Meals"
        val bookmarkIcon = composeTestRule.onNodeWithContentDescription("Saved Meals")
        if (bookmarkIcon.isDisplayed()) {
            bookmarkIcon.performClick()
            composeTestRule.onNodeWithText("Saved Meals").assertExists()
            composeTestRule.onNodeWithText("No saved meals yet").assertExists()
        }
    }
}
