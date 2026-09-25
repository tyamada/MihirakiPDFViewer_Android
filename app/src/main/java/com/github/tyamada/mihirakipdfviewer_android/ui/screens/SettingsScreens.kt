package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(vm: ViewerViewModel, back: () -> Unit, help: () -> Unit, reset: () -> Unit, tips: () -> Unit, licenses: () -> Unit) {
    val state by vm.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Section(stringResource(R.string.display_settings))
            SwitchRow(stringResource(R.string.high_quality), state.settings.highQuality) { vm.updateSettings { s -> s.copy(highQuality = it) } }
            Text(stringResource(R.string.sharpness)); Slider(state.settings.sharpness, { v -> vm.updateSettings { it.copy(sharpness = v) } }, valueRange = 0f..1f)
            SwitchRow(stringResource(R.string.two_page), state.settings.layout == ViewerLayout.SPREAD) { vm.updateSettings { s -> s.copy(layout = if (it) ViewerLayout.SPREAD else ViewerLayout.SINGLE) } }
            SwitchRow(stringResource(R.string.show_cover), state.settings.showCover) { vm.updateSettings { s -> s.copy(showCover = it) } }
            SelectRow(stringResource(R.string.reading_direction), state.settings.direction.name) { vm.updateSettings { s -> s.copy(direction = if (s.direction == ReadingDirection.L2R) ReadingDirection.R2L else ReadingDirection.L2R) } }

            Section(stringResource(R.string.options))
            SelectRow(stringResource(R.string.cover_mode), state.settings.coverMode.name) { vm.updateSettings { s -> s.copy(coverMode = if (s.coverMode == CoverMode.STANDARD) CoverMode.COMPATIBILITY else CoverMode.STANDARD) } }
            TextButton(onClick = reset, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.reset))
                }
            }

            Section(stringResource(R.string.document_info)); Info(stringResource(R.string.title), state.info.title); Info(stringResource(R.string.author), state.info.author); Info(stringResource(R.string.subject), state.info.subject); Info(stringResource(R.string.keywords), state.info.keywords); Info(stringResource(R.string.pdf_version), state.info.version)
            
            Section(stringResource(R.string.help))
            TextButton(onClick = help, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.help))
                }
            }

            Section(stringResource(R.string.app_info))
            ListItem(
                headlineContent = { Text(stringResource(R.string.version)) },
                supportingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(BuildConfig.VERSION_NAME)
                        state.settings.allPurchasedTiers.forEach { tier ->
                            val res = when (tier) {
                                "BRONZE" -> R.drawable.ic_tip_bronze
                                "SILVER" -> R.drawable.ic_tip_silver
                                "GOLD" -> R.drawable.ic_tip_gold
                                else -> null
                            }
                            res?.let {
                                Image(
                                    painter = painterResource(it),
                                    contentDescription = tier,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            )
            Info(stringResource(R.string.build_number), BuildConfig.VERSION_CODE.toString())
            Info(stringResource(R.string.copyright), "©️ 2026 Takuma Yamada")

            state.settings.purchasedTier?.let { tier ->
                val res = when (tier) {
                    "BRONZE" -> R.drawable.ic_tip_bronze
                    "SILVER" -> R.drawable.ic_tip_silver
                    "GOLD" -> R.drawable.ic_tip_gold
                    else -> null
                }
                res?.let {
                    Spacer(Modifier.height(32.dp))
                    Image(
                        painter = painterResource(it),
                        contentDescription = tier,
                        modifier = Modifier.fillMaxWidth().height(120.dp).align(Alignment.CenterHorizontally),
                    )
                }
            }

            TextButton(onClick = licenses, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.licenses))
                }
            }

            Section("開発者を応援")
            TextButton(onClick = tips, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.support))
                }
            }
        }
    }
}
@Composable private fun Section(text: String) { Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) }
@Composable private fun SwitchRow(label: String, checked: Boolean, change: (Boolean) -> Unit) { Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, Modifier.padding(top = 14.dp)); Switch(checked, change) } }
@Composable private fun SelectRow(label: String, value: String, click: () -> Unit) { TextButton(click, Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(label, Modifier.weight(1f)); Text(value) } }
@Composable private fun Info(label: String, value: String) { ListItem(headlineContent = { Text(label) }, supportingContent = { Text(value.ifBlank { "—" }) }) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun HelpScreen(back: () -> Unit) = Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.help)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
    Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(20.dp)) { listOf(R.string.help_open, R.string.help_navigate, R.string.help_menu, R.string.help_zoom, R.string.help_search, R.string.help_layout).forEach { Text(stringResource(it), Modifier.padding(bottom = 16.dp)) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ResetScreen(vm: ViewerViewModel, back: () -> Unit) = Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reset)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
    Column(Modifier.padding(p).padding(24.dp)) { Text(stringResource(R.string.reset_message), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(back) { Text(stringResource(R.string.cancel)) }; Spacer(Modifier.width(8.dp)); Button(onClick = { vm.reset(); back() }) { Text(stringResource(R.string.reset)) } } }
}
