package com.example.recharge.tracking

import android.content.Context
import android.content.Intent

object UsageTrackingBroadcast {
    const val ACTION_FOREGROUND_APP = "com.example.recharge.FOREGROUND_APP"
    const val EXTRA_PACKAGE = "package_name"

    fun notifyForegroundApp(context: Context, packageName: String) {
        val intent = Intent(ACTION_FOREGROUND_APP).apply {
            putExtra(EXTRA_PACKAGE, packageName)
            setPackage(context.packageName)
        }
        context.sendBroadcast(intent)
    }
}
