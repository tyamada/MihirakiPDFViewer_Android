package com.github.tyamada.mihirakipdfviewer_android

import android.app.Application
import com.github.tyamada.mihirakipdfviewer_android.util.cleanupStalePdfCopies
import com.github.tyamada.mihirakipdfviewer_android.util.PersistentLogManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MihirakiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        cleanupStalePdfCopies(cacheDir)
        PDFBoxResourceLoader.init(this)
        PersistentLogManager.init(this)
        PersistentLogManager.log("I", "MihirakiApplication", "App started.")
    }
}
