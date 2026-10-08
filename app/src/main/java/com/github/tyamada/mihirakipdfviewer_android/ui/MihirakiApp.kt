package com.github.tyamada.mihirakipdfviewer_android.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.*
import com.github.tyamada.mihirakipdfviewer_android.ui.theme.MihirakiTheme
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.SamplePdfViewModel
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@Composable fun MihirakiApp(viewer: ViewerViewModel) = MihirakiTheme {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "viewer") {
        composable("viewer") { ViewerScreen(viewer, { nav.navigate("settings") }, { nav.navigate("sample_pdfs") }) }
        composable("settings") { 
            SettingsScreen(
                vm = viewer,
                back = { nav.popBackStack() },
                help = { nav.navigate("help") },
                reset = { nav.navigate("reset") },
                tips = { nav.navigate("tips") },
                licenses = { nav.navigate("licenses") },
                diagnostics = { nav.navigate("diagnostics") },
                deviceTest = { nav.navigate("device_test") },
                logViewer = { nav.navigate("log_viewer") }
            )
        }
        composable("sample_pdfs") {
            val sampleVm: SamplePdfViewModel = viewModel()
            SamplePdfScreen(
                viewModel = sampleVm,
                back = { nav.popBackStack() },
                onOpenPdf = { uri ->
                    viewer.open(uri)
                    nav.popBackStack("viewer", false)
                }
            )
        }
        composable("help") { HelpScreen { nav.popBackStack() } }
        composable("licenses") { LicenseScreen { nav.popBackStack() } }
        composable("reset") { ResetScreen(viewer) { nav.popBackStack() } }
        composable("tips") { TipScreen(viewer) { nav.popBackStack() } }
        composable("diagnostics") { DiagnosticScreen { nav.popBackStack() } }
        composable("device_test") { DeviceTestScreen { nav.popBackStack() } }
        composable("log_viewer") { LogViewerScreen { nav.popBackStack() } }
    }
}
