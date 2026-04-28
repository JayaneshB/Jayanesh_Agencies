package com.chocowholesale.customer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chocowholesale.customer.ui.auth.PhoneEntryScreen
import com.chocowholesale.customer.ui.auth.OtpScreen
import com.chocowholesale.customer.ui.auth.ProfileSetupScreen
import com.chocowholesale.customer.ui.main.MainScreen

object Routes {
    const val PHONE_ENTRY = "phone_entry"
    const val OTP = "otp/{phone}"
    const val PROFILE_SETUP = "profile_setup"
    const val MAIN = "main"

    fun otp(phone: String) = "otp/$phone"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PHONE_ENTRY) {
        composable(Routes.PHONE_ENTRY) {
            PhoneEntryScreen(
                onOtpSent = { phone -> navController.navigate(Routes.otp(phone)) },
                onAlreadyLoggedIn = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PHONE_ENTRY) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Routes.OTP,
            arguments = listOf(navArgument("phone") { type = NavType.StringType })
        ) { backStack ->
            val phone = backStack.arguments?.getString("phone") ?: ""
            OtpScreen(
                phone = phone,
                onVerified = { isNew ->
                    val dest = if (isNew) Routes.PROFILE_SETUP else Routes.MAIN
                    navController.navigate(dest) {
                        popUpTo(Routes.PHONE_ENTRY) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.PROFILE_SETUP) {
            ProfileSetupScreen(
                onDone = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.PROFILE_SETUP) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.MAIN) {
            MainScreen()
        }
    }
}
