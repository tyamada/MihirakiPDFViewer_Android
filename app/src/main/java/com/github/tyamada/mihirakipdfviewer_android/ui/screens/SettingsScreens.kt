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
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.TextView
import android.widget.Toast
import java.util.Locale
import androidx.core.text.HtmlCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.github.tyamada.mihirakipdfviewer_android.BuildConfig
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.data.*
import com.github.tyamada.mihirakipdfviewer_android.util.*
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(
    vm: ViewerViewModel,
    back: () -> Unit,
    reset: () -> Unit
) {
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

            Spacer(Modifier.height(8.dp))
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
    val categories = listOf(
        HelpCategorySpec(stringResource(R.string.help_category_getting_started), listOf(
            R.string.help_open,
            R.string.help_sample_pdfs_desc,
            R.string.help_navigate,
            R.string.help_menu
        )),
        HelpCategorySpec(stringResource(R.string.help_category_reading_features), listOf(
            R.string.help_search,
            R.string.help_outline,
            R.string.help_sample_pdfs_desc,
            R.string.help_open,
            R.string.help_layout,
            R.string.help_zoom,
            R.string.help_slider
        )),
        HelpCategorySpec(stringResource(R.string.help_category_settings_guide), listOf(
            R.string.help_high_quality,
            R.string.help_sharpness,
            R.string.help_two_page,
            R.string.help_show_cover,
            R.string.help_reading_direction,
            R.string.help_document_info,
            R.string.mouse_navigation,
            R.string.help_portrait_display,
            R.string.help_cover_mode
        )),
        HelpCategorySpec(stringResource(R.string.help_category_diagnostics), listOf(
            R.string.help_device_test,
            R.string.help_log_viewer,
            R.string.report_issue_desc,
            R.string.help_clear_logs
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
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                category.items.forEach { resId ->
                                    val htmlText = stringResource(resId)
                                    AndroidView(
                                        factory = { context ->
                                            TextView(context).apply {
                                                textSize = 14f
                                            }
                                        },
                                        update = { view ->
                                            view.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
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
}

data class HelpCategorySpec(val title: String, val items: List<Int>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun VersionScreen(vm: ViewerViewModel, back: () -> Unit, licenses: () -> Unit, tips: () -> Unit, privacyPolicy: () -> Unit) {
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
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(8.dp)) {
                    Info(stringResource(R.string.version), BuildConfig.VERSION_NAME)
                    Info(stringResource(R.string.build_number), BuildConfig.VERSION_CODE.toString())
                    Info(stringResource(R.string.copyright), "©️ 2026 Takuma Yamada")
                }
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = privacyPolicy, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.privacy_policy))
                }
            }

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
                title = { Text(stringResource(R.string.diagnostics)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = deviceTest, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.device_test_menu))
                }
            }
            TextButton(onClick = logViewer, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text(stringResource(R.string.app_logs_menu))
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
                Button(onClick = { AppLogger.clear() }) { Text(stringResource(R.string.testing_diagnostic_logs)) }
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
                        Text(stringResource(R.string.no_logs_recorded_text), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                title = { Text(stringResource(R.string.device_test_screen_title)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).padding(16.dp).verticalScroll(rememberScrollState()).fillMaxSize()) {
            Text(
                stringResource(R.string.device_test_intro),
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
                        stringResource(R.string.privacy_notice_header),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.privacy_notice_body),
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
                Text(stringResource(R.string.run_test_button))
            }

            Spacer(Modifier.height(24.dp))

            testResult?.let { result ->
                Text(stringResource(R.string.test_results_header), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))

                Info(stringResource(R.string.test_timestamp_label), result.timestamp)
                Info(stringResource(R.string.vendor_label), result.manufacturer)
                Info(stringResource(R.string.device_name_label), result.model)
                Info(stringResource(R.string.os_version_label), "Android ${result.osVersion} (SDK ${result.sdkInt})")
                Info(stringResource(R.string.system_locale_label), result.systemLocale)
                Info(stringResource(R.string.screen_orientation_label), result.deviceOrientation)
                Info(stringResource(R.string.theme_mode_label), result.themeMode)
                Info(stringResource(R.string.font_scale_label), "${result.fontScale}x")
                Info(stringResource(R.string.ram_info_label), "${result.totalRamMb} MB / ${result.freeRamMb} MB")
                Info(stringResource(R.string.storage_info_label), String.format(Locale.US, "%.2f GB / %.2f GB", result.totalStorageGb, result.freeStorageGb))
                Info(stringResource(R.string.low_ram_mode_label), if (result.isLowRamDevice) "有効 (Low-RAM Mode)" else "無効 (Normal)")
                Info(stringResource(R.string.cpu_cores_label), "${result.cpuCores} cores")
                Info(stringResource(R.string.screen_resolution_label), result.screenResolution)
                Info(stringResource(R.string.jvm_heap_label), "${result.totalMemoryMb} MB / ${result.freeMemoryMb} MB")
                Info(stringResource(R.string.cache_dir_label), if (result.cacheDirWritable) "書込可能 (OK)" else "エラー")
                Info(stringResource(R.string.pdf_renderer_api_label), if (result.pdfRendererApiWorking) "利用可能 (Functional)" else "エラー")
                Info(stringResource(R.string.billing_api_label), if (result.billingApiAvailable) "利用可能 (Available)" else "利用不可")
                Info(stringResource(R.string.saf_resolver_label), if (result.contentResolverAvailable) "利用可能 (Active)" else "エラー")
                Info(stringResource(R.string.status_label), result.status)

                Spacer(Modifier.height(16.dp))

                val shareChooserTitle = stringResource(R.string.share_test_results_chooser)
                Button(
                    onClick = {
                        val report = DeviceTestRunner.formatResultString(result)
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, report)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, shareChooserTitle)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.share_test_results_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ReportIssueScreen(
    back: () -> Unit,
    deviceTest: () -> Unit,
    logViewer: () -> Unit
) {
    val context = LocalContext.current
    val issueUrl = "https://github.com/tyamada/MihirakiPDFViewer_Android/issues/new?template=bug_report.md"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_issue_screen_title)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(
            Modifier
                .padding(p)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.report_issue_intro),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Section(stringResource(R.string.report_include_header))
            BulletPoint(stringResource(R.string.report_step1))
            BulletPoint(stringResource(R.string.report_step2))
            BulletPoint(stringResource(R.string.report_step3))
            BulletPoint(stringResource(R.string.report_step4))
            BulletPoint(stringResource(R.string.report_step5))
            BulletPoint(stringResource(R.string.report_step6))

            Section(stringResource(R.string.diagnostic_info_header))
            val copyToast = stringResource(R.string.copy_all_diagnostics_toast)
            Button(
                onClick = {
                    val result = DeviceTestRunner.runTest(context)
                    val testReport = DeviceTestRunner.formatResultString(result)
                    val logs = PersistentLogManager.getAllLogs().joinToString("\n")
                    val combined = "$testReport\n\n--- App Logs (${PersistentLogManager.getAllLogs().size} lines) ---\n$logs"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = ClipData.newPlainText("MihirakiPDFViewer Diagnostic Report", combined)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, copyToast, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.copy_all_diagnostics_button))
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = logViewer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.view_app_logs_button))
            }
            OutlinedButton(
                onClick = deviceTest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.run_device_tests_button))
            }

            Section(stringResource(R.string.protect_privacy_header))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.privacy_warning_body_1), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.privacy_warning_body_2), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(issueUrl))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.open_github_issues_button))
            }
            Text(
                stringResource(R.string.github_account_warning),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable private fun BulletPoint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LogViewerScreen(back: () -> Unit) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf(PersistentLogManager.getAllLogs()) }
    var clearedSignal by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_viewer_screen_title)) },
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
                        stringResource(R.string.log_privacy_notice_header),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.log_privacy_notice_body),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val shareLogsTitle = stringResource(R.string.share_logs_title)
                Button(
                    onClick = {
                        val logText = logs.joinToString("\n")
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, logText.ifBlank { "No logs available." })
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, shareLogsTitle)
                        context.startActivity(shareIntent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.share_logs_button))
                }

                OutlinedButton(
                    onClick = {
                        PersistentLogManager.clearAllLogs()
                        logs = PersistentLogManager.getAllLogs()
                        clearedSignal = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.delete_logs_button))
                }
            }

            if (clearedSignal) {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.logs_deleted_message), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.saved_logs_title, logs.size), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val scrollState = rememberScrollState()
                Column(Modifier.verticalScroll(scrollState)) {
                    if (logs.isEmpty()) {
                        Text(stringResource(R.string.no_logs_recorded_text), color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PrivacyPolicyScreen(back: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy_policy)) },
                navigationIcon = { IconButton(back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } }
            )
        }
    ) { p ->
        Column(Modifier.padding(p).verticalScroll(rememberScrollState()).padding(16.dp)) {
            val htmlText = stringResource(R.string.privacy_policy_content)
            AndroidView(
                factory = { context ->
                    TextView(context).apply {
                        textSize = 14f
                    }
                },
                update = { view ->
                    view.text = HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_COMPACT)
                    view.setTextColor(android.graphics.Color.parseColor("#1C1B1F"))
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
