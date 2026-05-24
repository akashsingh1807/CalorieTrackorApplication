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
class WaterAndWeightFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigationToWaterTracker() {
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
                bookmarkRepository = FakeBookmarkRepository(),
                weightRepository = FakeWeightRepository(),
                waterRepository = FakeWaterRepository()
            )
        }

        composeTestRule.waitForIdle()

        // Open drawer
        // The menu icon is used to open the drawer
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        
        // Click on Water Tracker
        composeTestRule.onNodeWithText("Water Tracker").performClick()

        // Verify we are on Water Tracker screen
        composeTestRule.onNodeWithText("Daily Goal: 8 glasses").assertExists()
    }

    @Test
    fun testNavigationToWeightTracker() {
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
                bookmarkRepository = FakeBookmarkRepository(),
                weightRepository = FakeWeightRepository(),
                waterRepository = FakeWaterRepository()
            )
        }

        composeTestRule.waitForIdle()

        // Open drawer
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        
        // Click on Weight Tracker
        composeTestRule.onNodeWithText("Weight Tracker").performClick()

        // Verify we are on Weight Tracker screen
        composeTestRule.onNodeWithText("Log Weight").assertExists()
    }
}
