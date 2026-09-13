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

import dagger.hilt.android.AndroidEntryPoint

import com.example.recharge.updater.UpdateManager
import javax.inject.Inject
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var updateManager: UpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        UsageTrackingWorker.enqueuePeriodicWork(this)
        
        // Silently check for updates
        lifecycleScope.launch {
            updateManager.checkForUpdates()
        }

        setContent {
            RechargeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Background
                ) {
                    RechargeNavGraph()
                }
            }
        }
    }


}
