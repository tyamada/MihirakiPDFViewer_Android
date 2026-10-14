package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.util.*
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(vm: ViewerViewModel, back: () -> Unit, reset: () -> Unit) {
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
            SwitchRow(stringResource(R.string.mouse_navigation), state.settings.mouseNavigation) { vm.updateSettings { s -> s.copy(mouseNavigation = it) } }
            SwitchRow(stringResource(R.string.portrait_display), state.settings.portraitSpread) { vm.updateSettings { s -> s.copy(portraitSpread = it) } }
            SelectRow(stringResource(R.string.cover_mode), state.settings.coverMode.name) { vm.updateSettings { s -> s.copy(coverMode = if (s.coverMode == CoverMode.STANDARD) CoverMode.COMPATIBILITY else CoverMode.STANDARD) } }
            TextButton(onClick = reset, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.reset))
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
@Composable fun HelpScreen(back: () -> Unit) {
    val context = LocalContext.current
    val htmlText = remember {
        val lang = java.util.Locale.getDefault().language
        val fullTag = java.util.Locale.getDefault().toLanguageTag()
        val fileName = when {
            fullTag.contains("zh-TW", ignoreCase = true) || fullTag.contains("zh-HK", ignoreCase = true) -> "help_zh-TW.html"
            lang == "zh" -> "help_zh.html"
            lang in listOf("ja", "ko", "de", "fr", "es", "pt", "vi", "ne", "id", "fil") -> "help_$lang.html"
            else -> "help_en.html"
        }
        val assetName = try {
            context.assets.open(fileName).close()
            fileName
        } catch (_: Exception) {
            "help_en.html"
        }
        try {
            context.assets.open(assetName).bufferedReader().use { it.readText() }
        } catch (_: Exception) {
            "<p>Help content not available.</p>"
        }
    }

    val categories = listOf(
        HelpCategory("はじめての使い方 / Getting Started", listOf(
            "PDFを開く", "Open PDF",
            "サンプルPDF", "Sample PDFs",
            "ページ移動", "Navigate Pages",
            "メニュー", "Menu"
        )),
        HelpCategory("読書を快適にする機能 / Reading Features", listOf(
            "検索", "Search",
            "PDF目次", "PDF Table of Contents",
            "サンプルPDF", "Sample PDFs",
            "PDFを開く", "Open PDF",
            "設定", "Settings",
            "ズーム", "Zoom",
            "スライダー", "Slider"
        )),
        HelpCategory("設定とカスタマイズ / Settings Guide", listOf(
            "高画質化", "High quality",
            "シャープネス", "Sharpness",
            "見開き表示", "Two-page view",
            "表紙を表示", "Show cover",
            "ページ方向", "Reading direction",
            "ドキュメント情報", "Document information",
            "マウスクリックでページ切り替え", "Mouse Navigation",
            "縦向きでは単一ページ表示", "Portrait single page",
            "表紙設定", "Cover setting",
            "リセット", "Reset"
        )),
        HelpCategory("診断 / Diagnostics", listOf(
            "実機テストを実行", "Device Test",
            "アプリログを表示・共有", "App Logs",
            "不具合を報告する", "Report Issue",
            "テスト・診断ログのクリア", "Clear Test & Diagnostic Logs"
        ))
    )

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.help)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            categories.forEach { category ->
                var expanded by remember { mutableStateOf(false) }
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expanded = !expanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(category.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(
                                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (expanded) "Collapse" else "Expand"
                                )
                            }
                        }
                        if (expanded) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Box(Modifier.padding(16.dp)) {
                                val filteredHtml = filterHtmlForCategory(htmlText, category.keywords)
                                AndroidView(
                                    factory = { context ->
                                        TextView(context).apply {
                                            textSize = 14f
                                        }
                                    },
                                    update = { view ->
                                        view.text = HtmlCompat.fromHtml(filteredHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)
                                        view.setTextColor(android.graphics.Color.parseColor("#1C1B1F"))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class HelpCategory(val title: String, val keywords: List<String>)

private fun filterHtmlForCategory(html: String, keywords: List<String>): String {
    val paragraphs = html.split("</p>").filter { it.isNotBlank() }
    val orderedParagraphs = mutableListOf<String>()
    keywords.forEach { kw ->
        paragraphs.firstOrNull { p -> p.contains(kw) && !orderedParagraphs.contains(p + "</p>") }?.let { match ->
            orderedParagraphs.add(match + "</p>")
        }
    }
    return orderedParagraphs.joinToString("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun VersionScreen(vm: ViewerViewModel, back: () -> Unit, licenses: () -> Unit, tips: () -> Unit) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var showPromoDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_info)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Section(stringResource(R.string.app_info))
            Info(stringResource(R.string.version), BuildConfig.VERSION_NAME)
            Info(stringResource(R.string.build_number), BuildConfig.VERSION_CODE.toString())
            Info(stringResource(R.string.copyright), "©️ 2026 Takuma Yamada")

            TextButton(onClick = licenses, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.licenses))
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { showPromoDialog = true }, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.promo_code_help_title))
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
                            "KOYOMI" -> R.drawable.ic_tip_koyomi
                            else -> null
                        }
                        res?.let {
                            Image(
                                painter = painterResource(it),
                                contentDescription = "$tier Supporter Badge",
                                modifier = Modifier.height(100.dp).padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }

            if (showPromoDialog) {
                AlertDialog(
                    onDismissRequest = { showPromoDialog = false },
                    title = { Text(stringResource(R.string.promo_code_help_title)) },
                    text = {
                        Text(stringResource(R.string.promo_code_help_message), style = MaterialTheme.typography.bodyMedium)
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showPromoDialog = false
                                tips()
                            }
                        ) {
                            Text(stringResource(R.string.go_to_support))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPromoDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DiagnosticScreen(back: () -> Unit, deviceTest: () -> Unit, logViewer: () -> Unit, diagnostics: () -> Unit, reportIssue: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("診断 (Diagnostics)") },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = deviceTest, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("実機テストを実行 (Device Test)")
                }
            }
            TextButton(onClick = logViewer, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("アプリログを表示・共有 (App Logs)")
                }
            }
            TextButton(onClick = reportIssue, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.report_issue))
                }
            }
            TextButton(onClick = diagnostics, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.testing_diagnostic_logs))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DiagnosticLogsScreen(back: () -> Unit) {
    val logs by AppLogger.logs.collectAsState()
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val maxMemory = runtime.maxMemory() / 1024 / 1024

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.testing_diagnostic_logs)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp).fillMaxSize()) {
            Text("Memory Usage: ${usedMemory}MB / ${maxMemory}MB", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { AppLogger.clear() }) { Text("テスト・診断ログのクリア") }
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
@Composable fun ResetScreen(vm: ViewerViewModel, back: () -> Unit) = Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.reset)) }, navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }) }) { p ->
    Column(Modifier.padding(p).padding(24.dp)) { Text(stringResource(R.string.reset_message), style = MaterialTheme.typography.titleLarge); Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { TextButton(back) { Text(stringResource(R.string.cancel)) }; Spacer(Modifier.width(8.dp)); Button(onClick = { vm.reset(); back() }) { Text(stringResource(R.string.reset)) } } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun DeviceTestScreen(back: () -> Unit) {
    val context = LocalContext.current
    var testResult by remember { mutableStateOf<DeviceTestResult?>(null) }

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
                        "テスト結果を自動的に外部（サーバー等）に送信する機能はありません。データは端末内でのみ処理され、手動で共有する場合を除き外部に出ることはありません。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    testResult = DeviceTestRunner.runTest(context)
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
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, report)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "実機テスト結果の共有")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("テスト結果を共有する")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LogViewerScreen(back: () -> Unit) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf(PersistentLogManager.getAllLogs()) }
    var clearedSignal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("アプリログ管理") },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp).fillMaxSize()) {
            // Privacy Notice
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "🔒 プライバシー保護・データ管理について",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "• ログを自動的に外部（サーバー等）に送信する機能はありません。\n• ユーザーの個人情報やPDFファイルの内容は一切記録されません。\n• 肥大化を防ぐため、24時間以上経過したログファイルは自動的に削除されます。",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val logText = logs.joinToString("\n")
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, logText.ifBlank { "No logs available." })
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "アプリログの共有")
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ログを共有する")
                }

                OutlinedButton(
                    onClick = {
                        PersistentLogManager.clearAllLogs()
                        logs = PersistentLogManager.getAllLogs()
                        clearedSignal = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("ログを削除")
                }
            }

            if (clearedSignal) {
                Spacer(Modifier.height(4.dp))
                Text("すべてのログを削除しました。", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))
            Text("保存されたログ (${logs.size} lines):", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                Column(Modifier.verticalScroll(scrollState)) {
                    if (logs.isEmpty()) {
                        Text("記録されたログはありません。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        logs.forEach { line ->
                            Text(
                                line,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
