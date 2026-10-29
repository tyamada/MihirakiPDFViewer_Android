package com.github.tyamada.mihirakipdfviewer_android.util

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.github.tyamada.mihirakipdfviewer_android.R
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

object UpdateManager {
    private var lastCheckTime: Long = 0L
    private const val COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes cooldown

    fun checkForUpdate(context: Context, force: Boolean = false, onResult: (Boolean, String?) -> Unit) {
        val now = System.currentTimeMillis()
        if (!force && ((now - lastCheckTime) < COOLDOWN_MS)) {
            val remainingSec = ((COOLDOWN_MS - (now - lastCheckTime)) / 1000)
            val msg = context.getString(R.string.update_cooldown, remainingSec / 60, remainingSec % 60)
            onResult(false, msg)
            return
        }
        lastCheckTime = now

        runCatching {
            val manager = AppUpdateManagerFactory.create(context)
            manager.appUpdateInfo.addOnSuccessListener { info ->
                val available = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                onResult(available, null)
            }.addOnFailureListener { e ->
                val errorMsg = e.localizedMessage ?: context.getString(R.string.check_failed)
                onResult(false, errorMsg)
            }
        }.onFailure { e ->
            val errorMsg = e.localizedMessage ?: context.getString(R.string.check_failed)
            onResult(false, errorMsg)
        }
    }

    fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri()))
        } catch (_: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
        }
    }
}
