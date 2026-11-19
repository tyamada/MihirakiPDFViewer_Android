package com.github.tyamada.mihirakipdfviewer_android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import com.github.tyamada.mihirakipdfviewer_android.data.canShowKoyomiNotice
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.*
import com.github.tyamada.mihirakipdfviewer_android.ui.theme.MihirakiTheme
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.SamplePdfViewModel
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@Composable fun MihirakiApp(viewer: ViewerViewModel, documentRequestId: Int = 0) = MihirakiTheme {
    val nav = rememberNavController()
    val context = LocalContext.current
    val state by viewer.state.collectAsState()

    var tutorialDismissed by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.settings.showTutorialOnStartup, tutorialDismissed, documentRequestId) {
        if (documentRequestId == 0 && state.settings.showTutorialOnStartup && !tutorialDismissed) {
            tutorialDismissed = true
            nav.navigate("tutorial")
        }
    }

    // ACTION_VIEW can arrive while settings/help/samples are open. Return to the document.
    LaunchedEffect(documentRequestId) {
        if (documentRequestId > 0) {
            tutorialDismissed = true
            nav.popBackStack("viewer", false)
        }
    }

    val entry by nav.currentBackStackEntryAsState()
    val noticePending by viewer.koyomiNoticePending.collectAsState()
    var showGrantedBadgeDetails by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
    NavHost(nav, startDestination = "viewer", modifier = Modifier.weight(1f)) {
        composable("viewer") { 
            ViewerScreen(
                vm = viewer,
                openSettings = { nav.navigate("settings") },
                openHelp = { nav.navigate("help") },
                openVersion = { nav.navigate("version") },
                openDiagnostics = { nav.navigate("diagnostics") },
                openSupport = { nav.navigate("tips") },
                openPrivacyPolicy = { nav.navigate("privacy_policy") },
                openSamplePdfs = { nav.navigate("sample_pdfs") }
            )
        }
        composable("tutorial") {
            TutorialScreen(
                vm = viewer,
                onFinished = {
                    if (nav.currentBackStackEntry?.destination?.route == "tutorial") {
                        nav.popBackStack()
                    }
                }
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
        composable("help") { HelpScreen(back = { nav.popBackStack() }, openTutorial = { nav.navigate("tutorial") }) }
        composable("version") { VersionScreen(viewer, { nav.popBackStack() }, { nav.navigate("licenses") }, { nav.navigate("tips") }, { nav.navigate("privacy_policy") }) }
        composable("privacy_policy") { PrivacyPolicyScreen { nav.popBackStack() } }
        composable("diagnostics") { 
            DiagnosticScreen(
                back = { nav.popBackStack() },
                deviceTest = { nav.navigate("device_test") },
                logViewer = { nav.navigate("log_viewer") },
                diagnostics = { nav.navigate("diagnostic_logs") },
                reportIssue = { nav.navigate("report_issue") }
            ) 
        }
        composable("report_issue") { 
            ReportIssueScreen(
                back = { nav.popBackStack() },
                deviceTest = { nav.navigate("device_test") },
                logViewer = { nav.navigate("log_viewer") }
            ) 
        }
        composable("diagnostic_logs") { DiagnosticLogsScreen { nav.popBackStack() } }
        composable("licenses") { LicenseScreen { nav.popBackStack() } }
        composable("reset") { ResetScreen(viewer) { nav.popBackStack() } }
        composable("tutorials") { /* placeholder */ }
        composable("tips") { TipScreen(viewer) { nav.popBackStack() } }
        composable("device_test") { DeviceTestScreen { nav.popBackStack() } }
        composable("log_viewer") { LogViewerScreen { nav.popBackStack() } }
    }
    val route = entry?.destination?.route
    if (canShowKoyomiNotice(noticePending, route,
            reading = route == "viewer" && state.source != null,
            blocked = state.loading || state.passwordRequested || state.errorKey != null ||
                (state.settings.showTutorialOnStartup && !tutorialDismissed))) {
        KoyomiGrantNotice(
            onDetails = { viewer.dismissKoyomiNotice(); showGrantedBadgeDetails = true },
            onDismiss = viewer::dismissKoyomiNotice
        )
    }
    }
    if (showGrantedBadgeDetails) BadgeDetailsDialog("KOYOMI") { showGrantedBadgeDetails = false }

}
