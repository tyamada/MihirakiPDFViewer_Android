# PDFBox-Android
-keep class com.tom_roush.pdfbox.** { *; }
-dontwarn org.bouncycastle.**
-dontwarn com.gemalto.jp2.**
-dontwarn android.graphics.Bitmap$Config

# Google Play Billing
-keep class com.android.billingclient.** { *; }

# Google Play App Update
-keep class com.google.android.play.core.appupdate.** { *; }
-keep class com.google.android.play.core.install.** { *; }

# Kotlin / Coroutines / DataStore
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
-dontwarn kotlinx.coroutines.**
-dontwarn androidx.datastore.**

# App Data Models & Repositories (R8 Full Mode safety)
-keep class com.github.tyamada.mihirakipdfviewer_android.data.** { *; }
