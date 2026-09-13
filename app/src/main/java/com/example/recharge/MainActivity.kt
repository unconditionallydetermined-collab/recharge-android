package com.example.recharge

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.recharge.theme.Background
import com.example.recharge.theme.RechargeTheme
import com.example.recharge.tracking.UsageTrackingWorker
import com.example.recharge.sync.SupabaseSyncWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Set to true when a recharge://reset-password deep link is received */
    private val deepLinkResetPassword = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start background workers
        UsageTrackingWorker.enqueuePeriodicWork(this)
        SupabaseSyncWorker.enqueuePeriodicSync(this)

        // Check incoming deep link
        handleDeepLink(intent)

        setContent {
            RechargeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    RechargeNavGraph(deepLinkResetPassword = deepLinkResetPassword.value)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "recharge" && data.host == "reset-password") {
            deepLinkResetPassword.value = true
        }
    }
}
