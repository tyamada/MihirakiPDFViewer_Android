package com.mihiraki.pdfviewer

import android.app.Application
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MihirakiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PDFBoxResourceLoader.init(this)
    }
}
