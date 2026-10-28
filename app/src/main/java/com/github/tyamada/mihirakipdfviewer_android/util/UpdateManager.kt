package com.github.tyamada.mihirakipdfviewer_android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

object UpdateManager {
    private var lastCheckTime: Long = 0L
    private const val COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes cooldown

    fun checkForUpdate(context: Context, force: Boolean = false, onResult: (Boolean, String?) -> Unit) {
        val now = System.currentTimeMillis()
        if (!force && (now - lastCheckTime < COOLDOWN_MS)) {
            val remainingSec = ((COOLDOWN_MS - (now - lastCheckTime)) / 1000)
            onResult(false, "Please wait ${remainingSec / 60}m ${remainingSec % 60}s before checking for updates again.")
            return
        }
        lastCheckTime = now

        runCatching {
            val manager = AppUpdateManagerFactory.create(context)
            manager.appUpdateInfo.addOnSuccessListener { info ->
                val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                onResult(available, null)
            }.addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Check failed")
            }
        }.onFailure { e ->
            onResult(false, e.localizedMessage ?: "Check failed")
        }
    }

    fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }
}
