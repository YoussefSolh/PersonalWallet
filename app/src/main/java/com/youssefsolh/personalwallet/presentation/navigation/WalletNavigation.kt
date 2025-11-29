package com.youssefsolh.personalwallet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.youssefsolh.personalwallet.presentation.ui.screen.AddCategoryScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.AddTransactionScreenEnhanced
import com.youssefsolh.personalwallet.presentation.ui.screen.AddWalletScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.CategoryListScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.DashboardScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.DebtListScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.LoginScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.ReportsScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.SettingsScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.TransferScreen
import com.youssefsolh.personalwallet.presentation.ui.screen.WalletDetailScreen

@Composable
fun WalletNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNavigateToWalletDetail = { walletId ->
                    navController.navigate("wallet_detail/$walletId")
                },
                onNavigateToAddWallet = {
                    navController.navigate("add_wallet")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable("wallet_detail/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            WalletDetailScreen(
                walletId = walletId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddTransaction = { walletId ->
                    navController.navigate("add_transaction/$walletId")
                },
                onNavigateToTransfer = { walletId ->
                    navController.navigate("transfer/$walletId")
                },
                onNavigateToEditWallet = { walletId ->
                    navController.navigate("edit_wallet/$walletId")
                },
                onNavigateToEditTransaction = { walletId, transactionId ->
                    navController.navigate("edit_transaction/$walletId/$transactionId")
                }
            )
        }

        composable("add_wallet") {
            AddWalletScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_wallet/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            AddWalletScreen(
                walletId = walletId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("add_transaction/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            AddTransactionScreenEnhanced(
                walletId = walletId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCategory = {
                    navController.navigate("add_category")
                }
            )
        }

        composable("edit_transaction/{walletId}/{transactionId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId") ?: ""
            val transactionId = backStackEntry.arguments?.getString("transactionId") ?: ""
            AddTransactionScreenEnhanced(
                walletId = walletId,
                transactionId = transactionId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCategory = {
                    navController.navigate("add_category")
                }
            )
        }

        composable("categories") {
            CategoryListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAddCategory = {
                    navController.navigate("add_category")
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate("edit_category/$categoryId")
                }
            )
        }

        composable("add_category") {
            AddCategoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("edit_category/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            AddCategoryScreen(
                categoryId = categoryId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("transfer/{fromWalletId}") { backStackEntry ->
            val fromWalletId = backStackEntry.arguments?.getString("fromWalletId")
            TransferScreen(
                fromWalletId = fromWalletId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("debts") {
            DebtListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCategories = {
                    navController.navigate("categories")
                },
                onNavigateToDebts = {
                    navController.navigate("debts")
                },
                onNavigateToReports = {
                    navController.navigate("reports")
                },
                onSignOut = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}