package com.github.tyamada.mihirakipdfviewer_android

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MihirakiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
    }
}
