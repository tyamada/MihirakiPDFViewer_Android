package com.github.tyamada.mihirakipdfviewer_android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.*
import com.github.tyamada.mihirakipdfviewer_android.ui.theme.MihirakiTheme
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.SamplePdfViewModel
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@Composable fun MihirakiApp(viewer: ViewerViewModel) = MihirakiTheme {
    val nav = rememberNavController()
    val context = LocalContext.current
    NavHost(nav, startDestination = "viewer") {
        composable("viewer") { 
            ViewerScreen(
                vm = viewer,
                openSettings = { nav.navigate("settings") },
                openHelp = { nav.navigate("help") },
                openVersion = { nav.navigate("version") },
                openDiagnostics = { nav.navigate("diagnostics") },
                openSupport = { nav.navigate("tips") },
                openSamplePdfs = { nav.navigate("sample_pdfs") }
            )
        }
        composable("settings") { 
            SettingsScreen(
                vm = viewer,
                back = { nav.popBackStack() },
                reset = { nav.navigate("reset") }
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
        composable("version") { VersionScreen(viewer, { nav.popBackStack() }, { nav.navigate("licenses") }, { nav.navigate("tips") }) }
        composable("diagnostics") { 
            DiagnosticScreen(
                back = { nav.popBackStack() },
                deviceTest = { nav.navigate("device_test") },
                logViewer = { nav.navigate("log_viewer") },
                diagnostics = { nav.navigate("diagnostic_logs") },
                reportIssue = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tyamada/MihirakiPDFViewer_Android/issues"))
                    context.startActivity(intent)
                }
            ) 
        }
        composable("diagnostic_logs") { DiagnosticLogsScreen { nav.popBackStack() } }
        composable("licenses") { LicenseScreen { nav.popBackStack() } }
        composable("reset") { ResetScreen(viewer) { nav.popBackStack() } }
        composable("tips") { TipScreen(viewer) { nav.popBackStack() } }
        composable("device_test") { DeviceTestScreen { nav.popBackStack() } }
        composable("log_viewer") { LogViewerScreen { nav.popBackStack() } }
    }
}
