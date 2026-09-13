package com.example.recharge

/** Type-safe route sealed class for Compose navigation back-stack */
sealed class AppRoute {
    data object Auth : AppRoute()
    data object Home : AppRoute()
    data object Quotes : AppRoute()
    data object Video : AppRoute()
    data object Handoff : AppRoute()
    data object Settings : AppRoute()
    data object QueueManager : AppRoute()
    data object Habits : AppRoute()
    data object Sounds : AppRoute()
    data object Insights : AppRoute()
    data object Profile : AppRoute()
    data object ResetPassword : AppRoute()
}
