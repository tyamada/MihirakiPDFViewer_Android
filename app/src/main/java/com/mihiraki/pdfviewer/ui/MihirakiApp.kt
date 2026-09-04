package com.mihiraki.pdfviewer.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.mihiraki.pdfviewer.ui.screens.*
import com.mihiraki.pdfviewer.ui.theme.MihirakiTheme
import com.mihiraki.pdfviewer.viewmodel.ViewerViewModel

@Composable fun MihirakiApp(viewer: ViewerViewModel) = MihirakiTheme {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "viewer") {
        composable("viewer") { ViewerScreen(viewer, { nav.navigate("settings") }, { nav.navigate("tips") }) }
        composable("settings") { SettingsScreen(viewer, { nav.popBackStack() }, { nav.navigate("help") }, { nav.navigate("reset") }, { nav.navigate("tips") }) }
        composable("help") { HelpScreen { nav.popBackStack() } }
        composable("reset") { ResetScreen(viewer) { nav.popBackStack() } }
        composable("tips") { TipScreen(viewer) { nav.popBackStack() } }
    }
}
