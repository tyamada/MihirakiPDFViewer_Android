package com.mihiraki.pdfviewer.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mihiraki.pdfviewer.R
import com.mihiraki.pdfviewer.data.ReadingDirection
import com.mihiraki.pdfviewer.viewmodel.ViewerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ViewerScreen(vm: ViewerViewModel, openSettings: () -> Unit, openTips: () -> Unit) {
    val state by vm.state.collectAsState(); val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let {
        runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; vm.open(it)
    } }
    Scaffold(
        topBar = {
            if (state.chromeVisible) {
                Column {
                    TopAppBar(
                        title = {
                            if (state.source != null) {
                                OutlinedTextField(
                                    value = state.searchQuery,
                                    onValueChange = vm::search,
                                    singleLine = true,
                                    placeholder = { Text(stringResource(R.string.search), style = MaterialTheme.typography.bodySmall) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                                    trailingIcon = {
                                        if (state.searchResults.isNotEmpty()) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${state.currentSearchIndex + 1}/${state.searchResults.size}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                )
                                                IconButton(onClick = { vm.navigateSearch(-1) }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowUp, null)
                                                }
                                                IconButton(onClick = { vm.navigateSearch(1) }, modifier = Modifier.size(32.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowDown, null)
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                                    )
                                )
                            } else {
                                Text(state.info.title.ifBlank { stringResource(R.string.app_name) })
                            }
                        },
                        actions = {
                            IconButton(onClick = { picker.launch(arrayOf("application/pdf")) }) {
                                Icon(Icons.Default.FolderOpen, stringResource(R.string.open_pdf))
                            }
                            val tipColor = when (state.settings.purchasedTier) {
                                "BRONZE" -> androidx.compose.ui.graphics.Color(0xFFCD7F32)
                                "SILVER" -> androidx.compose.ui.graphics.Color(0xFFC0C0C0)
                                "GOLD" -> androidx.compose.ui.graphics.Color(0xFFFFD700)
                                else -> LocalContentColor.current
                            }
                            IconButton(onClick = openTips) {
                                Icon(Icons.Default.Favorite, stringResource(R.string.support), tint = tipColor)
                            }
                            IconButton(onClick = openSettings) {
                                Icon(Icons.Default.Settings, stringResource(R.string.settings))
                            }
                        },
                    )
                }
            }
        },
        bottomBar = {
            if (state.chromeVisible && (state.source != null)) {
                BottomAppBar {
                    val direction = state.settings.direction
                    val maxPage = (state.pageCount - 1).coerceAtLeast(0).toFloat()

                    Text("${state.currentPage + 1} / ${state.pageCount}", modifier = Modifier.padding(horizontal = 12.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides if (direction == ReadingDirection.L2R) LayoutDirection.Ltr else LayoutDirection.Rtl) {
                        Slider(
                            value = state.currentPage.toFloat(),
                            onValueChange = { vm.render(it.toInt()) },
                            valueRange = 0f..maxPage,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    )
 { padding -> Box(Modifier.fillMaxSize().padding(padding).background(androidx.compose.ui.graphics.Color(0xFF202124))) {
        if ((state.source == null) && !state.loading) Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(20.dp)); Button(onClick = { picker.launch(arrayOf("application/pdf")) }, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.open_pdf)) }
        }
        if (state.source == null && !state.loading) IconButton(openSettings, Modifier.align(Alignment.TopEnd).padding(top = 36.dp, end = 8.dp).size(48.dp)) { Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = androidx.compose.ui.graphics.Color.White) }
        if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        if (state.bitmap != null || state.secondBitmap != null) {
            val direction = state.settings.direction
            ZoomablePage(
                Modifier.fillMaxSize(),
                onTap = vm::toggleChrome,
                onPrevious = { vm.move(-1) },
                onNext = { vm.move(1) },
                direction = direction,
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                    if (state.settings.layout == com.mihiraki.pdfviewer.data.ViewerLayout.SPREAD) {
                        val images = listOf(state.bitmap, state.secondBitmap)
                        val count = images.count { it != null }
                        if (count == 1) {
                            val image = images.first { it != null }
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(image!!.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            }
                        } else {
                            images.forEachIndexed { index, image ->
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    image?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit,
                                            alignment = if (index == 0) Alignment.CenterEnd else Alignment.CenterStart
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        state.bitmap?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                    }
                }
            }
        }
        state.errorKey?.let { key -> Snackbar(Modifier.align(Alignment.BottomCenter), action = { TextButton(onClick = vm::dismissError) { Text(stringResource(R.string.ok)) } }) { Text(errorText(key)) } }
    } }
    if (state.passwordRequested) AlertDialog(onDismissRequest = {}, title = { Text(stringResource(R.string.password_required)) }, text = {
        Column { OutlinedTextField(password, { password = it }, label = { Text(stringResource(R.string.password)) }, singleLine = true); state.errorKey?.let { Text(errorText(it), color = MaterialTheme.colorScheme.error) } }
    }, confirmButton = { TextButton(onClick = { state.uri?.let { vm.open(it, password) } }) { Text(stringResource(R.string.open)) } }, dismissButton = { TextButton(onClick = vm::closeDocument) { Text(stringResource(R.string.cancel)) } })
}

@Composable private fun errorText(key: String) = stringResource(when (key) { "wrong_password" -> R.string.wrong_password; "permission_denied" -> R.string.permission_denied; "no_results" -> R.string.no_results; else -> R.string.cannot_open_pdf })

@Composable private fun ZoomablePage(
    modifier: Modifier,
    onTap: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    direction: ReadingDirection,
    content: @Composable () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    var totalPan = Offset.Zero
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pan = event.calculatePan()
                        val zoom = event.calculateZoom()

                        if (zoom != 1f || pan != Offset.Zero) {
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            if (newScale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                            scale = newScale
                            totalPan += pan
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale <= 1.05f && kotlin.math.abs(totalPan.x) > 60f) {
                        val isForward = if (direction == ReadingDirection.L2R) totalPan.x < 0 else totalPan.x > 0
                        if (isForward) onNext() else onPrevious()
                    }
                }
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y,
            ),
    ) {
        content()
    }
}
