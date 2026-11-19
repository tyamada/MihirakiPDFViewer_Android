package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import androidx.core.net.toUri
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.tyamada.mihirakipdfviewer_android.R
import com.github.tyamada.mihirakipdfviewer_android.viewmodel.ViewerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialScreen(
    vm: ViewerViewModel,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val isJapanese = LocalLocale.current.language == "ja"
    val introVideoUrl = if (isJapanese) {
        "https://www.youtube.com/watch?v=73bJ0osJYWo"
    } else {
        "https://www.youtube.com/watch?v=I5tp84abV5o"
    }
    val videoGuidePlaylistUrl = if (isJapanese) {
        "https://www.youtube.com/playlist?list=PLWD24JL0mav8"
    } else {
        "https://www.youtube.com/playlist?list=PLBBAqnUOZR-A"
    }
    val errorNoApp = stringResource(R.string.error_no_app_youtube)
    var dontShowAgain by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tutorial_welcome)) },
                actions = {
                    TextButton(onClick = {
                        if (dontShowAgain) {
                            vm.updateSettings { it.copy(showTutorialOnStartup = false) }
                        }
                        onFinished()
                    }) {
                        Text(stringResource(R.string.skip))
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Previous button
                    TextButton(
                        onClick = {
                            if (pagerState.currentPage > 0) {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                }
                            }
                        },
                        enabled = pagerState.currentPage > 0
                    ) {
                        Text(stringResource(R.string.tutorial_previous))
                    }

                    // Pager Indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { iteration ->
                            val color = if (pagerState.currentPage == iteration) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(color)
                                    .size(if (pagerState.currentPage == iteration) 10.dp else 6.dp)
                            )
                        }
                    }

                    // Next / Get Started button
                    if (pagerState.currentPage < 3) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        ) {
                            Text(stringResource(R.string.tutorial_next))
                        }
                    } else {
                        Button(
                            onClick = {
                                if (dontShowAgain) {
                                    vm.updateSettings { it.copy(showTutorialOnStartup = false) }
                                }
                                onFinished()
                            }
                        ) {
                            Text(stringResource(R.string.tutorial_get_started))
                        }
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                when (page) {
                    0 -> {
                        // Page 1: Getting Started
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step1_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step1_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider()
                        TutorialFeatureItem(
                            icon = Icons.Default.FolderOpen,
                            title = stringResource(R.string.open_pdf),
                            description = stringResource(R.string.tutorial_open_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.Default.Download,
                            title = stringResource(R.string.sample_pdfs),
                            description = stringResource(R.string.tutorial_sample_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.Default.Swipe,
                            title = stringResource(R.string.help_navigate),
                            description = stringResource(R.string.tutorial_navigation_desc)
                        )
                    }
                    1 -> {
                        // Page 2: Reading Features
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step2_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step2_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider()
                        TutorialFeatureItem(
                            icon = Icons.Default.AutoStories,
                            title = stringResource(R.string.tutorial_feature_spread_title),
                            description = stringResource(R.string.tutorial_feature_spread_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.AutoMirrored.Filled.List,
                            title = stringResource(R.string.tutorial_feature_outline_title),
                            description = stringResource(R.string.tutorial_feature_outline_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.Default.Search,
                            title = stringResource(R.string.tutorial_feature_search_title),
                            description = stringResource(R.string.tutorial_feature_search_desc)
                        )
                    }
                    2 -> {
                        // Page 3: Settings & Customization
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step3_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step3_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider()
                        TutorialFeatureItem(
                            icon = Icons.Default.HighQuality,
                            title = stringResource(R.string.display_settings),
                            description = stringResource(R.string.tutorial_display_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.Default.Mouse,
                            title = stringResource(R.string.mouse_navigation),
                            description = stringResource(R.string.tutorial_mouse_desc)
                        )
                        TutorialFeatureItem(
                            icon = Icons.Default.Support,
                            title = stringResource(R.string.support),
                            description = stringResource(R.string.tutorial_support_desc)
                        )
                    }
                    3 -> {
                        // Page 4: Summary & Intro Video
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step4_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.tutorial_step4_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // YouTube Intro Video Button
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, introVideoUrl.toUri())
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, errorNoApp, Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.tutorial_youtube_button))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Video Shorts Playlist Button
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, videoGuidePlaylistUrl.toUri())
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, errorNoApp, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.VideoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.tutorial_video_guide_button))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Don't show again checkbox
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Checkbox(
                                checked = dontShowAgain,
                                onCheckedChange = { dontShowAgain = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.tutorial_dont_show_again),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Start Reading Button
                        Button(
                            onClick = {
                                if (dontShowAgain) {
                                    vm.updateSettings { it.copy(showTutorialOnStartup = false) }
                                }
                                onFinished()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text(stringResource(R.string.tutorial_get_started), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TutorialFeatureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
