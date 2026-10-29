package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import android.content.Intent
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.data.OutlineNode
import com.github.tyamada.mihirakipdfviewer_android.data.ReadingDirection
import com.github.tyamada.mihirakipdfviewer_android.data.ViewerLayout
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ViewerScreen(
    vm: ViewerViewModel,
    openSettings: () -> Unit,
    openHelp: () -> Unit,
    openVersion: () -> Unit,
    openDiagnostics: () -> Unit,
    openSupport: () -> Unit,
    openPrivacyPolicy: () -> Unit,
    openSamplePdfs: () -> Unit
) {
    val state by vm.state.collectAsState(); val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT
    LaunchedEffect(isPortrait) {
        vm.setOrientation(isPortrait)
    }
    val isDesktop = state.settings.mouseNavigation
    val focusRequester = remember { FocusRequester() }
    var password by remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> uri?.let {
        runCatching { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }; vm.open(it)
    } }

    var showSwipeHint by remember { mutableStateOf(false) }
    var showOutlineDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state.source) {
        if (state.source != null) {
            showSwipeHint = true
            delay(1000)
            showSwipeHint = false
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

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
                                                    modifier = Modifier.semantics {
                                                        contentDescription = "Search result ${state.currentSearchIndex + 1} of ${state.searchResults.size}"
                                                    }
                                                )
                                                IconButton(onClick = { vm.navigateSearch(-1) }, modifier = Modifier.size(48.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(R.string.previous_search))
                                                }
                                                IconButton(onClick = { vm.navigateSearch(1) }, modifier = Modifier.size(48.dp)) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(R.string.next_search))
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    textStyle = MaterialTheme.typography.bodySmall,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                    )
                                )
                            } else {
                                Text(state.info.title.ifBlank { stringResource(R.string.app_name) })
                            }
                        },
                        actions = {
                            if (state.updateAvailable) {
                                FilledTonalButton(
                                    onClick = {
                                        com.github.tyamada.mihirakipdfviewer_android.util.UpdateManager.openPlayStore(context)
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp).testTag("btn_update_now")
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Update Now", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (state.outline.isNotEmpty()) {
                                IconButton(onClick = { showOutlineDialog = true }, modifier = Modifier.testTag("btn_outline")) {
                                    Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = stringResource(R.string.table_of_contents))
                                }
                            }
                            IconButton(onClick = openSamplePdfs, modifier = Modifier.testTag("btn_top_sample_pdfs")) {
                                Icon(Icons.Default.Download, contentDescription = stringResource(R.string.sample_pdfs))
                            }
                            IconButton(onClick = { picker.launch(arrayOf("application/pdf")) }) {
                                Icon(Icons.Default.FolderOpen, stringResource(R.string.open_pdf))
                            }
                            var showMenu by remember { mutableStateOf(false) }
                            Box {
                                IconButton(onClick = { showMenu = true }, modifier = Modifier.testTag("btn_settings")) {
                                    Icon(Icons.Default.Settings, stringResource(R.string.settings))
                                }
                                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.settings)) },
                                        onClick = { showMenu = false; openSettings() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.help)) },
                                        onClick = { showMenu = false; openHelp() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.version)) },
                                        onClick = { showMenu = false; openVersion() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.diagnostics_menu)) },
                                        onClick = { showMenu = false; openDiagnostics() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.support)) },
                                        onClick = { showMenu = false; openSupport() }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.privacy_policy)) },
                                        onClick = { showMenu = false; openPrivacyPolicy() }
                                    )
                                }
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

                    Text(
                        text = "${state.currentPage + 1} / ${state.pageCount}",
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .semantics {
                                contentDescription = "Page ${state.currentPage + 1} of ${state.pageCount}"
                            }
                    )

                    CompositionLocalProvider(LocalLayoutDirection provides if (direction == ReadingDirection.L2R) LayoutDirection.Ltr else LayoutDirection.Rtl) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(onClick = { vm.movePage(-1) }) {
                                Icon(Icons.Default.ChevronLeft, stringResource(R.string.previous_page))
                            }
                            Slider(
                                value = state.currentPage.toFloat(),
                                onValueChange = { vm.render(it.toInt()) },
                                valueRange = 0f..maxPage,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { vm.movePage(1) }) {
                                Icon(Icons.Default.ChevronRight, stringResource(R.string.next_page))
                            }
                        }
                    }
                }
            }
        },
    )
 { padding -> BoxWithConstraints(
     Modifier
         .fillMaxSize()
         .padding(padding)
         .background(androidx.compose.ui.graphics.Color(0xFF202124))
         .testTag("viewer_content")
         .focusRequester(focusRequester)
         .focusable()
         .onKeyEvent { event ->
             if (event.type == KeyEventType.KeyDown) {
                 when (event.key) {
                     Key.DirectionLeft -> {
                         vm.move(if (state.settings.direction == ReadingDirection.L2R) -1 else 1)
                         true
                     }
                     Key.DirectionRight -> {
                         vm.move(if (state.settings.direction == ReadingDirection.L2R) 1 else -1)
                         true
                     }
                     Key.Spacebar -> {
                         vm.move(1)
                         true
                     }
                     Key.Escape -> {
                         if (state.chromeVisible) vm.toggleChrome()
                         true
                     }
                     Key.F -> {
                         if (event.isCtrlPressed) {
                             if (!state.chromeVisible) vm.toggleChrome()
                             true
                         } else false
                     }
                     else -> false
                 }
             } else false
         }
 ) {
        val isTabletOrFoldable = maxWidth >= 600.dp
        if ((state.source == null) && !state.loading) Column(
            Modifier
                .align(Alignment.Center)
                .then(if (isTabletOrFoldable) Modifier.widthIn(max = 520.dp) else Modifier.fillMaxWidth())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = Color.White, modifier = Modifier.size(80.dp))
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = openSamplePdfs,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("btn_center_sample_pdfs"),
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.sample_pdfs))
                }
                Button(
                    onClick = { picker.launch(arrayOf("application/pdf")) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.open_pdf))
                }
            }
        }
        if ((state.source == null) && !state.loading) {
            Box(Modifier.align(Alignment.TopEnd).padding(top = 36.dp, end = 8.dp)) {
                var showMenu by remember { mutableStateOf(false) }
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(48.dp).testTag("btn_settings")) {
                    Icon(Icons.Default.Settings, stringResource(R.string.settings), tint = Color.White)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        onClick = { showMenu = false; openSettings() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.help)) },
                        onClick = { showMenu = false; openHelp() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.version)) },
                        onClick = { showMenu = false; openVersion() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.diagnostics)) },
                        onClick = { showMenu = false; openDiagnostics() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.support)) },
                        onClick = { showMenu = false; openSupport() }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.privacy_policy)) },
                        onClick = { showMenu = false; openPrivacyPolicy() }
                    )
                }
            }
        }
        if (state.loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
        if ((state.bitmap != null) || (state.secondBitmap != null)) {
            val direction = state.settings.direction
            ZoomablePage(
                Modifier.fillMaxSize(),
                onTap = vm::toggleChrome,
                onPrevious = { vm.move(-1) },
                onNext = { vm.move(1) },
                direction = direction,
                currentPage = state.currentPage,
                isDesktop = isDesktop,
            ) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                    if (state.effectiveLayout == ViewerLayout.SPREAD) {
                        val images = listOfNotNull(state.bitmap, state.secondBitmap)
                        if (images.size == 1) {
                            val image = images.first()
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Image(image.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                            }
                        } else if (images.size == 2) {
                            images.forEachIndexed { index, image ->
                                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                    Image(
                                        bitmap = image.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
                                        alignment = if (index == 0) Alignment.CenterEnd else Alignment.CenterStart
                                    )
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

        if (showSwipeHint && state.source != null) {
            val isR2L = state.settings.direction == ReadingDirection.R2L
            val infiniteTransition = rememberInfiniteTransition(label = "EdgeHint")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "EdgePulse"
            )

            Box(modifier = Modifier.fillMaxSize().testTag("swipe_hint")) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .graphicsLayer(alpha = pulseAlpha)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = if (isR2L) stringResource(R.string.open) else stringResource(R.string.back),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .graphicsLayer(alpha = pulseAlpha)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isR2L) stringResource(R.string.back) else stringResource(R.string.open),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    } }
    if (state.passwordRequested) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.password_required)) },
            text = {
                Column {
                    OutlinedTextField(
                        password,
                        { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        singleLine = true
                    )
                    state.errorKey?.let { Text(errorText(it), color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = { state.uri?.let { vm.open(it, password = password) } }) {
                    Text(stringResource(R.string.open))
                }
            },
            dismissButton = {
                TextButton(onClick = vm::closeDocument) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showOutlineDialog) {
        AlertDialog(
            onDismissRequest = { showOutlineDialog = false },
            title = { Text(stringResource(R.string.table_of_contents)) },
            text = {
                androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                    items(state.outline.size) { index ->
                        OutlineItemView(state.outline[index], onSelect = { page ->
                            vm.render(page)
                            showOutlineDialog = false
                        })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOutlineDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
}

@Composable
private fun OutlineItemView(node: OutlineNode, onSelect: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        TextButton(onClick = { onSelect(node.pageIndex) }, modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(node.title, modifier = Modifier.weight(1f))
                Text("p.${node.pageIndex + 1}", style = MaterialTheme.typography.labelSmall)
            }
        }
        if (node.children.isNotEmpty()) {
            Column(modifier = Modifier.padding(start = 16.dp)) {
                node.children.forEach { child ->
                    OutlineItemView(child, onSelect)
                }
            }
        }
    }
}

@Composable private fun errorText(key: String) = stringResource(when (key) { "wrong_password" -> R.string.wrong_password; "permission_denied" -> R.string.permission_denied; "no_results" -> R.string.no_results; else -> R.string.cannot_open_pdf })

@Composable private fun ZoomablePage(
    modifier: Modifier,
    onTap: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    direction: ReadingDirection,
    currentPage: Int,
    isDesktop: Boolean,
    content: @Composable () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(currentPage) {
        scale = 1f
        offset = Offset.Zero
    }

    Box(
        modifier
            .pointerInput(isDesktop, direction) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalPan = Offset.Zero
                    var hasMoved = false
                    val startTime = System.currentTimeMillis()

                    do {
                        val event = awaitPointerEvent()
                        
                        if (event.type == PointerEventType.Scroll && scale <= 1.05f) {
                            val delta = event.changes.firstOrNull()?.scrollDelta?.y ?: 0f
                            if (delta > 0.5f) { onNext(); return@awaitEachGesture }
                            else if (delta < -0.5f) { onPrevious(); return@awaitEachGesture }
                        }

                        val pan = event.calculatePan()
                        val zoom = event.calculateZoom()

                        if (zoom != 1f || pan != Offset.Zero) {
                            hasMoved = true
                            val newScale = (scale * zoom).coerceIn(1f, 5f)
                            if (newScale > 1f) {
                                offset += pan
                            } else {
                                offset = Offset.Zero
                            }
                            scale = newScale
                            totalPan += pan
                            event.changes.forEach { it.consume() }
                        } else {
                            val currentPos = event.changes.firstOrNull()?.position ?: down.position
                            if ((currentPos - down.position).getDistance() > 15f) {
                                hasMoved = true
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    val duration = System.currentTimeMillis() - startTime
                    if (!hasMoved && duration < 350f) {
                        val secondDown = withTimeoutOrNull(300L) {
                            awaitFirstDown(requireUnconsumed = false)
                        }
                        if (secondDown != null) {
                            var secondMoved = false
                            do {
                                val secondEvent = awaitPointerEvent()
                                val currentPos = secondEvent.changes.firstOrNull()?.position ?: secondDown.position
                                if ((currentPos - secondDown.position).getDistance() > 15f) {
                                    secondMoved = true
                                }
                            } while (secondEvent.changes.any { it.pressed })

                            if (!secondMoved) {
                                scale = 1f
                                offset = Offset.Zero
                            }
                        } else {
                            val boxWidth = size.width.toFloat()
                            if (isDesktop && scale <= 1.05f && boxWidth > 0f) {
                                val x = down.position.x
                                val leftBoundary = boxWidth * 0.2f
                                val rightBoundary = boxWidth * 0.8f
                                if (x < leftBoundary) {
                                    if (direction == ReadingDirection.L2R) onPrevious() else onNext()
                                } else if (x > rightBoundary) {
                                    if (direction == ReadingDirection.L2R) onNext() else onPrevious()
                                } else {
                                    onTap()
                                }
                            } else {
                                onTap()
                            }
                        }
                    } else if (scale <= 1.05f && abs(totalPan.x) > 60f) {
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
