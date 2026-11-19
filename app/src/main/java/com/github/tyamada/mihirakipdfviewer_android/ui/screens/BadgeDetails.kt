package com.github.tyamada.mihirakipdfviewer_android.ui.screens

import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.tyamada.mihirakipdfviewer_android.R

@Composable
fun badgeTitle(id: String): String = if (id == "KOYOMI") stringResource(R.string.koyomi_badge_title) else id

fun badgeImage(id: String): Int? = when (id) {
    "BRONZE" -> R.drawable.ic_tip_bronze
    "SILVER" -> R.drawable.ic_tip_silver
    "GOLD" -> R.drawable.ic_tip_gold
    "KOYOMI" -> R.drawable.ic_tip_koyomi
    else -> null
}

/** Save the source's native view ID, not a Compose virtual node ID lost on recreation. */
class BadgeDetailsState(initialBadge: String? = null, initialSource: Int = View.NO_ID, initialPending: Int = View.NO_ID) {
    var badge by mutableStateOf(initialBadge)
        private set
    private val anchors = mutableMapOf<Int, BringIntoViewRequester>()
    fun registerSource(id: Int, requester: BringIntoViewRequester) { anchors[id] = requester }
    fun unregisterSource(id: Int, requester: BringIntoViewRequester) { if (anchors[id] === requester) anchors.remove(id) }
    suspend fun revealSource(id: Int) { anchors[id]?.bringIntoView() }
    private var sourceId = initialSource
    var pendingReturnId by mutableIntStateOf(initialPending)
        private set

    fun open(id: String, source: Int) { sourceId = source; pendingReturnId = View.NO_ID; badge = id }
    fun close() { badge = null; pendingReturnId = sourceId }
    fun finishReturn() { pendingReturnId = View.NO_ID }

    companion object {
        val Saver = listSaver<BadgeDetailsState, Any>(
            save = { listOf(it.badge ?: "", it.sourceId, it.pendingReturnId) },
            restore = { BadgeDetailsState((it[0] as String).ifEmpty { null }, it[1] as Int, it[2] as Int) }
        )
    }
}

@Composable
fun rememberBadgeDetailsState() = rememberSaveable(saver = BadgeDetailsState.Saver) { BadgeDetailsState() }

enum class BadgeDetailsLocation { Support, Version }

fun badgeSourceViewId(location: BadgeDetailsLocation, id: String): Int = when (location) {
    BadgeDetailsLocation.Support -> when (id) {
        "KOYOMI" -> R.id.badge_support_koyomi
        "BRONZE" -> R.id.badge_support_bronze
        "SILVER" -> R.id.badge_support_silver
        "GOLD" -> R.id.badge_support_gold
        else -> error("Unknown badge")
    }
    BadgeDetailsLocation.Version -> when (id) {
        "KOYOMI" -> R.id.badge_version_koyomi
        "BRONZE" -> R.id.badge_version_bronze
        "SILVER" -> R.id.badge_version_silver
        "GOLD" -> R.id.badge_version_gold
        else -> error("Unknown badge")
    }
}

/** The visible Compose button and its native screen-reader target share exactly the same bounds. */
@Composable
private fun BadgeSourceButton(details: BadgeDetailsState, sourceId: Int, label: String, modifier: Modifier = Modifier, onOpen: (Int) -> Unit,
                              fillWidth: Boolean = false, content: @Composable RowScope.() -> Unit) {
    val currentOpen by rememberUpdatedState(onOpen)
    val bringIntoView = remember { BringIntoViewRequester() }
    DisposableEffect(details, sourceId, bringIntoView) {
        details.registerSource(sourceId, bringIntoView)
        onDispose { details.unregisterSource(sourceId, bringIntoView) }
    }
    val ripple = MaterialTheme.colorScheme.primary.copy(alpha = .16f).toArgb()
    Box(modifier.bringIntoViewRequester(bringIntoView)) {
        TextButton(onClick = { currentOpen(sourceId) }, modifier = (if (fillWidth) Modifier.fillMaxWidth() else Modifier).clearAndSetSemantics {}) { content() }
        AndroidView(modifier = Modifier.matchParentSize(), factory = { context ->
            View(context).apply {
                id = sourceId
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                isClickable = true
                isFocusable = false
                accessibilityDelegate = object : View.AccessibilityDelegate() {
                    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
                        super.onInitializeAccessibilityNodeInfo(host, info)
                        info.className = android.widget.Button::class.java.name
                    }
                }
                setOnClickListener { currentOpen(sourceId) }
            }
        }, update = { view ->
            view.contentDescription = label
            if (view.background is RippleDrawable) (view.background as RippleDrawable).setColor(ColorStateList.valueOf(ripple))
            else view.background = RippleDrawable(ColorStateList.valueOf(ripple), null, null)
        })
    }
}

@Composable
fun OwnedBadgeButton(id: String, details: BadgeDetailsState, location: BadgeDetailsLocation) {
    val image = badgeImage(id) ?: return
    val title = badgeTitle(id)
    val action = stringResource(R.string.badge_details_action, title)
    BadgeSourceButton(details, badgeSourceViewId(location, id), action, Modifier.widthIn(max = 160.dp).sizeIn(minWidth = 48.dp, minHeight = 48.dp),
        onOpen = { details.open(id, it) }) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(image), contentDescription = null, modifier = Modifier.size(80.dp))
            Text(title)
        }
    }
}

@Composable
fun BadgeDetailsTextButton(id: String, details: BadgeDetailsState, label: String, sourceId: Int, modifier: Modifier = Modifier) {
    BadgeSourceButton(details, sourceId, stringResource(R.string.badge_details_action, badgeTitle(id)), modifier,
        onOpen = { details.open(id, it) }, fillWidth = true) { Text(label) }
}

@Composable
fun BadgeDetailsHost(details: BadgeDetailsState) {
    val view = LocalView.current
    val accessibility = view.context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    LaunchedEffect(details.pendingReturnId) {
        val targetId = details.pendingReturnId
        if (targetId == View.NO_ID) return@LaunchedEffect
        if (accessibility.isEnabled && accessibility.isTouchExplorationEnabled) {
            withFrameNanos { }
            // The source can move below the viewport when a portrait flow row becomes landscape.
            // Use Compose scrolling rather than a native child-rectangle request across the interop boundary.
            details.revealSource(targetId)
            // Wait for the dialog window to be removed and the recreated parent to regain focus.
            // Never request input/keyboard focus, and never run on ordinary recomposition or rotation.
            for (frame in 0 until 60) {
                withFrameNanos { }
                val target = view.rootView.findViewById<View>(targetId)
                if (target != null && target.isAttachedToWindow && target.hasWindowFocus() && target.width > 0 && target.height > 0) {
                    if (target.isAccessibilityFocused) break
                    if (returnFocusAfterExplicitClose(target)) break
                }
            }
        }
        details.finishReturn()
    }
    details.badge?.let { BadgeDetailsDialog(it, details::close) }
}

/** Entire name/description scroll; the close action stays available even in short landscape windows. */
@Composable
fun BadgeDetailsDialog(id: String, onClose: () -> Unit) {
    val image = badgeImage(id) ?: return
    val title = badgeTitle(id)
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(.94f).fillMaxHeight(.9f), shape = MaterialTheme.shapes.large) {
            Column {
                Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Image(painterResource(image), contentDescription = null, modifier = Modifier.size(180.dp))
                    Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.semantics { heading() })
                    Text(stringResource(if (id == "KOYOMI") R.string.koyomi_badge_description else R.string.support_badge_description))
                }
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 16.dp)) {
                    Text(stringResource(R.string.badge_close))
                }
            }
        }
    }
}

/** No timeout and no focus request. A new grant can wait safely behind a modal or while reading. */
@Composable
fun KoyomiGrantNotice(onDetails: () -> Unit, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.heightIn(max = 180.dp), color = MaterialTheme.colorScheme.secondaryContainer, tonalElevation = 4.dp) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding().padding(12.dp)) {
            Text(stringResource(R.string.koyomi_badge_granted_notice), modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onDetails, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.badge_view_details)) }
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) { Text(stringResource(R.string.badge_close)) }
            }
        }
    }
}

/** Only called for the saved source after an explicit modal close, while our window is focused.
 * This restores the user's place; it never redirects a screen reader on startup or notifications.
 */
@SuppressLint("AccessibilityFocus")
private fun returnFocusAfterExplicitClose(target: View): Boolean =
    target.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
