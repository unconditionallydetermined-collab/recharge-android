package com.example.recharge.tracking

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import timber.log.Timber

/**
 * Optional accessibility service for foreground-app tracking (user opt-in only).
 * Provides more reliable foreground app detection than UsageStatsManager polling.
 * User must explicitly enable in Settings > Accessibility — clear disclosure shown in UI.
 */
class RechargeAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        serviceInfo = serviceInfo.also { info ->
            info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            info.notificationTimeout = 100
        }
        Timber.d("Accessibility service connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            Timber.d("Foreground app (accessibility): $packageName")
            // Broadcast to app for passive queue completion tracking
            UsageTrackingBroadcast.notifyForegroundApp(this, packageName)
        }
    }

    override fun onInterrupt() {
        Timber.d("Accessibility service interrupted")
    }
}
