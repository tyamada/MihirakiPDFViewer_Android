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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.util.*
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(vm: ViewerViewModel, back: () -> Unit, help: () -> Unit, reset: () -> Unit, tips: () -> Unit, licenses: () -> Unit, diagnostics: () -> Unit, deviceTest: () -> Unit) {
    val state by vm.state.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Section(stringResource(R.string.display_settings))
            SwitchRow(stringResource(R.string.high_quality), state.settings.highQuality) { vm.updateSettings { s -> s.copy(highQuality = it) } }
            Text(stringResource(R.string.sharpness)); Slider(state.settings.sharpness, { v -> vm.updateSettings { it.copy(sharpness = v) } }, valueRange = 0f..1f)
            SwitchRow(stringResource(R.string.two_page), state.settings.layout == ViewerLayout.SPREAD) { vm.updateSettings { s -> s.copy(layout = if (it) ViewerLayout.SPREAD else ViewerLayout.SINGLE) } }
            SwitchRow(stringResource(R.string.show_cover), state.settings.showCover) { vm.updateSettings { s -> s.copy(showCover = it) } }
            SelectRow(stringResource(R.string.reading_direction), state.settings.direction.name) { vm.updateSettings { s -> s.copy(direction = if (s.direction == ReadingDirection.L2R) ReadingDirection.R2L else ReadingDirection.L2R) } }

            Section(stringResource(R.string.document_info)); Info(stringResource(R.string.title), state.info.title); Info(stringResource(R.string.author), state.info.author); Info(stringResource(R.string.subject), state.info.subject); Info(stringResource(R.string.keywords), state.info.keywords); Info(stringResource(R.string.pdf_version), state.info.version)

            Section(stringResource(R.string.options))
            SelectRow(stringResource(R.string.cover_mode), state.settings.coverMode.name) { vm.updateSettings { s -> s.copy(coverMode = if (s.coverMode == CoverMode.STANDARD) CoverMode.COMPATIBILITY else CoverMode.STANDARD) } }
            TextButton(onClick = reset, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.reset))
                }
            }
            
            Section("Testing & Diagnostics")
            TextButton(onClick = diagnostics, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("Testing & Diagnostic Logs")
                }
            }

            Section(stringResource(R.string.help))
            TextButton(onClick = help, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.help))
                }
            }

            Section(stringResource(R.string.app_info))
            Info(stringResource(R.string.version), BuildConfig.VERSION_NAME)
            Info(stringResource(R.string.build_number), BuildConfig.VERSION_CODE.toString())
            Info(stringResource(R.string.copyright), "©️ 2026 Takuma Yamada")

            TextButton(onClick = licenses, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.licenses))
                }
            }

            TextButton(onClick = deviceTest, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("実機テストを実行 (Device Test)")
                }
            }

            if (state.settings.allPurchasedTiers.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.purchased_badges_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                modifier = Modifier.height(100.dp).padding(horizontal = 8.dp)
                            )
                        }
                    }
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
    Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(20.dp)) {
        val originalItems = listOf(
            R.string.help_open,
            R.string.help_navigate,
            R.string.help_menu,
            R.string.help_zoom,
            R.string.help_search
        )
        val subItems = listOf(
            R.string.help_layout,
            R.string.help_high_quality,
            R.string.help_sharpness,
            R.string.help_two_page,
            R.string.help_show_cover,
            R.string.help_reading_direction,
            R.string.help_cover_mode,
            R.string.help_reset,
            R.string.help_document_info,
            R.string.help_help,
            R.string.help_app_info,
            R.string.help_support
        )
        originalItems.forEach {
            Text(stringResource(it), Modifier.padding(bottom = 16.dp))
        }
        subItems.forEach {
            Text(stringResource(it), Modifier.padding(start = 24.dp, bottom = 16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ResetScreen(vm: ViewerViewModel, back: () -> Unit) = Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reset)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
    Column(Modifier.padding(p).padding(24.dp)) { Text(stringResource(R.string.reset_message), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(back) { Text(stringResource(R.string.cancel)) }; Spacer(Modifier.width(8.dp)); Button(onClick = { vm.reset(); back() }) { Text(stringResource(R.string.reset)) } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DiagnosticScreen(back: () -> Unit) {
    val logs by AppLogger.logs.collectAsState()
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val maxMemory = runtime.maxMemory() / 1024 / 1024

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Testing & Diagnostics") },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp).fillMaxSize()) {
            Text("Memory Usage: ${usedMemory}MB / ${maxMemory}MB", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { AppLogger.clear() }) { Text("Clear Logs") }
            }
            Spacer(Modifier.height(12.dp))
            Text("Application Logs (${logs.size}):", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                Column(Modifier.verticalScroll(scrollState)) {
                    if (logs.isEmpty()) {
                        Text("No logs recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        logs.forEach { entry ->
                            Text(
                                "[${entry.timestamp}] ${entry.level}/${entry.tag}: ${entry.message}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DeviceTestScreen(back: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var testResult by remember { mutableStateOf<DeviceTestResult?>(null) }
    var copiedMessage by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("実機テスト・端末診断") },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp).verticalScroll(rememberScrollState()).fillMaxSize()) {
            Text(
                "この画面では、お使いの端末でビューアの動作確認テスト（メモリ、キャッシュ、PDFエンジン環境）を実行できます。",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))

            // Privacy Notice
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "🔒 プライバシー保護に関するお知らせ",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "テスト結果を自動的に外部（サーバー等）に送信する機能はありません。データは端末内でのみ処理され、手動でコピーして共有する場合を除き外部に出ることはありません。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    testResult = DeviceTestRunner.runTest(context)
                    copiedMessage = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("テストを実行する")
            }

            Spacer(Modifier.height(24.dp))

            testResult?.let { result ->
                Text("📊 テスト結果", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))

                Info("テスト日時", result.timestamp)
                Info("ベンダー名", result.manufacturer)
                Info("端末名", result.model)
                Info("OS バージョン", "Android ${result.osVersion} (SDK ${result.sdkInt})")
                Info("総メモリ / 空きメモリ", "${result.totalMemoryMb} MB / ${result.freeMemoryMb} MB")
                Info("キャッシュ領域", if (result.cacheDirWritable) "書込可能 (OK)" else "エラー")
                Info("PDF エンジン", if (result.pdfEngineAvailable) "利用可能 (OK)" else "エラー")
                Info("ステータス", result.status)

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val report = DeviceTestRunner.formatResultString(result)
                        clipboardManager.setText(AnnotatedString(report))
                        copiedMessage = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("テスト結果をクリップボードにコピー")
                }

                if (copiedMessage) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "クリップボードにコピーしました！",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}


