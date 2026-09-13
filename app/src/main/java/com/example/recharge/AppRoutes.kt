package com.example.recharge

import kotlinx.serialization.Serializable

/** Type-safe route sealed class for Compose navigation back-stack */
@Serializable
sealed class AppRoute {

    @Serializable data object Home : AppRoute()
    @Serializable data object Quotes : AppRoute()
    @Serializable data object Video : AppRoute()
    @Serializable data object Handoff : AppRoute()
    @Serializable data object Settings : AppRoute()
    @Serializable data object QueueManager : AppRoute()
    @Serializable data object Habits : AppRoute()
    @Serializable data object Sounds : AppRoute()
    @Serializable data object Insights : AppRoute()
    @Serializable data object Profile : AppRoute()

}
