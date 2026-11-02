package com.github.tyamada.mihirakipdfviewer_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import com.github.tyamada.mihirakipdfviewer_android.ui.MihirakiApp
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

class MainActivity : ComponentActivity() {
    private var documentRequestId by mutableIntStateOf(0)
    internal val viewer: ViewerViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        viewer.billing.connect()
        setContent { MihirakiApp(viewer, documentRequestId) }
        // Navigation and the retained ViewModel restore the current screen on recreation.
        if (savedInstanceState == null) handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.takeIf { it.action == Intent.ACTION_VIEW }?.data?.let { uri ->
            documentRequestId++
            viewer.open(uri)
        }
    }
}
