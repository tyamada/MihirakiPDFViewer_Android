package com.github.tyamada.mihirakipdfviewer_android.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Scheme = lightColorScheme(primary = Color(0xFF435E91), secondary = Color(0xFF565F71), background = Color(0xFFF9F9FF), surface = Color(0xFFF9F9FF))
@Composable fun MihirakiTheme(content: @Composable () -> Unit) = MaterialTheme(colorScheme = Scheme, typography = Typography(), content = content)
