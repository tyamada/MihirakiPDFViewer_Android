package com.github.tyamada.mihirakipdfviewer_android.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.github.tyamada.mihirakipdfviewer_android.ui.screens.*
import com.github.tyamada.mihirakipdfviewer_android.ui.theme.MihirakiTheme
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@Composable fun MihirakiApp(viewer: ViewerViewModel) = MihirakiTheme {
    val nav = rememberNavController()
    NavHost(nav, startDestination = "viewer") {
        composable("viewer") { ViewerScreen(viewer, { nav.navigate("settings") }, { nav.navigate("tips") }) }
        composable("settings") { SettingsScreen(viewer, { nav.popBackStack() }, { nav.navigate("help") }, { nav.navigate("reset") }, { nav.navigate("tips") }, { nav.navigate("licenses") }) }
        composable("help") { HelpScreen { nav.popBackStack() } }
        composable("licenses") { LicenseScreen { nav.popBackStack() } }
        composable("reset") { ResetScreen(viewer) { nav.popBackStack() } }
        composable("tips") { TipScreen(viewer) { nav.popBackStack() } }
    }
}
