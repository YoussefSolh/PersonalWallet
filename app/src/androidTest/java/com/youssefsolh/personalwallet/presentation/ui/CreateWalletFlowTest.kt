package com.youssefsolh.personalwallet.presentation.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.youssefsolh.personalwallet.presentation.navigation.WalletNavigation
import com.youssefsolh.personalwallet.ui.theme.PersonalWalletTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateWalletFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCompleteWalletCreationFlow() {
        composeTestRule.setContent {
            PersonalWalletTheme {
                WalletNavigation()
            }
        }

        // Login as guest
        composeTestRule
            .onNodeWithText("Continue as Guest")
            .performClick()

        // Wait for dashboard
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithText("Personal Wallet", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click add wallet FAB
        composeTestRule
            .onNodeWithContentDescription("Add Wallet")
            .performClick()

        // Fill wallet form
        composeTestRule
            .onNodeWithText("Wallet Name")
            .performTextInput("Test Wallet")

        composeTestRule
            .onNodeWithText("Initial Balance")
            .performTextInput("1000")

        composeTestRule
            .onNodeWithText("Currency")
            .performTextInput("USD")

        // Submit
        composeTestRule
            .onNodeWithText("Create Wallet")
            .performClick()

        // Verify wallet appears on dashboard
        composeTestRule
            .onNodeWithText("Test Wallet")
            .assertIsDisplayed()
    }

    @Test
    fun testAddTransactionFlow() {
        composeTestRule.setContent {
            PersonalWalletTheme {
                WalletNavigation()
            }
        }

        // Login as guest
        composeTestRule
            .onNodeWithText("Continue as Guest")
            .performClick()

        // Assume wallet exists, click on it
        composeTestRule
            .onAllNodesWithTag("walletItem")
            .onFirst()
            .performClick()

        // Click add transaction FAB
        composeTestRule
            .onNodeWithContentDescription("Add Transaction")
            .performClick()

        // Select expense
        composeTestRule
            .onNodeWithText("Expense")
            .performClick()

        // Fill transaction form
        composeTestRule
            .onNodeWithText("Amount")
            .performTextInput("50")

        composeTestRule
            .onNodeWithText("Description")
            .performTextInput("Test Expense")

        // Submit
        composeTestRule
            .onNodeWithText("Add Transaction")
            .performClick()

        // Verify transaction appears
        composeTestRule
            .onNodeWithText("Test Expense")
            .assertIsDisplayed()
    }
}
