package com.github.tyamada.mihirakipdfviewer_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.github.tyamada.mihirakipdfviewer_android.ui.MihirakiApp
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

class MainActivity : ComponentActivity() {
    internal val viewer: ViewerViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState); enableEdgeToEdge()
        viewer.billing.connect()
        setContent { MihirakiApp(viewer) }
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        intent.takeIf { it.action == Intent.ACTION_VIEW }?.data?.let(viewer::open)
    }
}
