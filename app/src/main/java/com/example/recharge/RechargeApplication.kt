package com.example.recharge

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RechargeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(FileLoggingTree(this))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private class FileLoggingTree(private val context: android.content.Context) : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            val logFile = java.io.File(context.filesDir, "recharge_logs.txt")
            try {
                val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.getDefault()).format(java.util.Date())
                val logLine = "$timestamp: $message\n"
                logFile.appendText(logLine)
                if (t != null) {
                    logFile.appendText(android.util.Log.getStackTraceString(t) + "\n")
                }
            } catch (e: Exception) {
                // Ignore failure to log to file
            }
        }
    }
}
