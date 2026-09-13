package com.example.recharge

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.recharge.handoff.HandoffScreen
import com.example.recharge.home.HomeScreen
import com.example.recharge.quotes.QuoteScreen
import com.example.recharge.settings.SettingsScreen
import com.example.recharge.video.VideoScreen

@Composable
fun RechargeNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AppRoute.Home) {

        composable<AppRoute.Home> {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable<AppRoute.Quotes> {
            QuoteScreen(
                onFinished = {
                    navController.navigate(AppRoute.Video) {
                        popUpTo(AppRoute.Quotes) { inclusive = true }
                    }
                }
            )
        }
        composable<AppRoute.Video> {
            VideoScreen(
                onVideoComplete = {
                    navController.navigate(AppRoute.Handoff) {
                        popUpTo(AppRoute.Video) { inclusive = true }
                    }
                }
            )
        }
        composable<AppRoute.Handoff> {
            HandoffScreen(
                onHandoffComplete = {
                    navController.navigate(AppRoute.Home) {
                        popUpTo(AppRoute.Handoff) { inclusive = true }
                    }
                }
            )
        }
        composable<AppRoute.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }
        composable<AppRoute.QueueManager> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateTo = { route -> navController.navigate(route) }
            )
        }

        
        // Placeholder stubs for tabs
        composable<AppRoute.Habits> {
            HomeScreen(onNavigate = { navController.navigate(it) })
        }
        composable<AppRoute.Sounds> {
            HomeScreen(onNavigate = { navController.navigate(it) })
        }
        composable<AppRoute.Insights> {
            HomeScreen(onNavigate = { navController.navigate(it) })
        }
        composable<AppRoute.Profile> {
            HomeScreen(onNavigate = { navController.navigate(it) })
        }
    }
}
