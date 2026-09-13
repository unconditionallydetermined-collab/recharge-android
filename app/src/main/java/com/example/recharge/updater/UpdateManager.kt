package com.example.recharge.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.recharge.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File
import java.net.URL
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val versionUrl = "https://api.github.com/repos/unconditionallydetermined-collab/recharge-android/releases/latest"

    // Result sealed class
    sealed class UpdateResult {
        object NoUpdate : UpdateResult()
        object Downloading : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    suspend fun checkForUpdates(): UpdateResult = withContext(Dispatchers.IO) {
        try {
            // Fetch latest release from GitHub
            val response = URL(versionUrl).readText()
            val json = JSONObject(response)
            
            val tagName = json.optString("tag_name", "")
            val latestVersion = tagName.removePrefix("v").trim()
            val currentVersion = BuildConfig.VERSION_NAME.trim()

            val assets = json.optJSONArray("assets")
            var apkUrl = ""
            if (assets != null && assets.length() > 0) {
                apkUrl = assets.getJSONObject(0).optString("browser_download_url", "")
            }
            
            val latestFloat = latestVersion.toFloatOrNull() ?: 0f
            val currentFloat = currentVersion.toFloatOrNull() ?: 0f

            if (latestFloat > currentFloat && apkUrl.isNotEmpty()) {
                startDownload(apkUrl)
                return@withContext UpdateResult.Downloading
            } else {
                return@withContext UpdateResult.NoUpdate
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check for updates")
            return@withContext UpdateResult.Error("Could not connect to GitHub to check for updates.")
        }
    }

    private fun startDownload(apkUrl: String) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(apkUrl)

        val request = DownloadManager.Request(uri)
            .setTitle("Recharge Update")
            .setDescription("Downloading latest version of Recharge...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Recharge_Update.apk")

        val downloadId = downloadManager.enqueue(request)
        
        // Register receiver to install when complete
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId && context != null) {
                    installApk(context, downloadId)
                    context.unregisterReceiver(this)
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun installApk(context: Context, downloadId: Long) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = downloadManager.getUriForDownloadedFile(downloadId) ?: return

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to install APK")
        }
    }
}
