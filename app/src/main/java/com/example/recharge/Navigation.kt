package com.example.recharge

import androidx.compose.runtime.*
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.recharge.auth.AuthScreen
import com.example.recharge.auth.ResetPasswordScreen
import com.example.recharge.handoff.HandoffScreen
import com.example.recharge.home.HomeScreen
import com.example.recharge.quotes.QuoteScreen
import com.example.recharge.settings.SettingsScreen
import com.example.recharge.video.VideoScreen

@Composable
fun RechargeNavGraph(deepLinkResetPassword: Boolean = false) {
    val backStack: NavBackStack = rememberNavBackStack(AppRoute.Auth)

    // Handle deep link for password reset
    LaunchedEffect(deepLinkResetPassword) {
        if (deepLinkResetPassword) {
            backStack.add(AppRoute.ResetPassword)
        }
    }

    val currentRoute = backStack.lastOrNull()

    when (currentRoute) {
        is AppRoute.Auth -> AuthScreen(
            onAuthenticated = {
                backStack.removeLastOrNull()
                backStack.add(AppRoute.Home)
            }
        )

        is AppRoute.Home -> HomeScreen(
            onNavigate = { route ->
                when (route) {
                    AppRoute.Home -> {} // already here
                    else -> backStack.add(route)
                }
            }
        )

        is AppRoute.Quotes -> QuoteScreen(
            onFinished = {
                backStack.removeLastOrNull()
                backStack.add(AppRoute.Video)
            }
        )

        is AppRoute.Video -> VideoScreen(
            onVideoComplete = {
                backStack.removeLastOrNull()
                backStack.add(AppRoute.Handoff)
            }
        )

        is AppRoute.Handoff -> HandoffScreen(
            onHandoffComplete = {
                backStack.removeLastOrNull()
                backStack.add(AppRoute.Home)
            }
        )

        is AppRoute.Settings -> SettingsScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateTo = { route -> backStack.add(route) }
        )

        is AppRoute.QueueManager -> SettingsScreen(
            onNavigateBack = { backStack.removeLastOrNull() },
            onNavigateTo = { route -> backStack.add(route) }
        )

        is AppRoute.ResetPassword -> ResetPasswordScreen(
            onBack = { backStack.removeLastOrNull() }
        )

        // Placeholder stubs for tabs
        is AppRoute.Habits, is AppRoute.Sounds, is AppRoute.Insights, is AppRoute.Profile -> {
            HomeScreen(onNavigate = { backStack.add(it) })
        }

        else -> AuthScreen(onAuthenticated = {
            backStack.removeLastOrNull()
            backStack.add(AppRoute.Home)
        })
    }
}
