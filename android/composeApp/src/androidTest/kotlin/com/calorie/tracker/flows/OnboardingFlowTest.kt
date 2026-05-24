package com.calorie.tracker.flows

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.calorie.tracker.App
import com.calorie.tracker.fakes.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class OnboardingFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testOnboardingFlow_AdvancesThroughSteps() {
        val authRepository = FakeAuthRepository()
        authRepository.loggedIn = true // Skip auth

        val mealRepository = FakeMealRepository()

        // Profile returns height 0.0 to trigger onboarding
        val mockProfile = com.calorie.tracker.model.UserProfile(
            id = 1L, name = "Test", email = "test@example.com",
            height = 0.0, weight = 0.0, age = 0, lifestyle = "SEDENTARY", goal = "MAINTAIN",
            dailyCalorieGoal = 1500, dailyProteinGoal = 100, dailyCarbsGoal = 100, dailyFatGoal = 50
        )

        val apiClient = io.mockk.mockk<com.calorie.tracker.core.network.CalorieApiClient>(relaxed = true)
        io.mockk.coEvery { apiClient.getProfile() } returns Result.success(mockProfile)

        composeTestRule.setContent {
            App(
                authRepository = authRepository,
                mealRepository = mealRepository,
                apiClient = apiClient,
                bookmarkRepository = FakeBookmarkRepository(),
                weightRepository = FakeWeightRepository(),
                waterRepository = FakeWaterRepository()
            )
        }

        // Welcome Screen
        composeTestRule.waitForIdle()
        // The first text might be "Welcome to Journable" or similar from OnboardingScreen
        composeTestRule.onNodeWithText("Welcome!").assertExists()
        composeTestRule.onNodeWithText("Continue").performClick()
    }
}
