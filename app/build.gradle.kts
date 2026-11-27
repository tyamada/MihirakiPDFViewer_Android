import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.github.tyamada.mihirakipdfviewer_android"
    compileSdk = 37
    buildToolsVersion = "36.0.0"

    defaultConfig {
        applicationId = "com.github.tyamada.MihirakiPDFViewer_Android"
        minSdk = 26
        targetSdk = 37
        versionCode = 53
        versionName = "2.1.10"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        val props = Properties()
        val propFile = file("../local.properties")
        if (propFile.exists()) {
            propFile.inputStream().use { props.load(it) }
        }

        create("release") {
            storeFile = props.getProperty("signing.storeFile")?.let { file(it) }
            storePassword = props.getProperty("signing.storePassword")
            keyAlias = props.getProperty("signing.keyAlias")
            keyPassword = props.getProperty("signing.keyPassword")
        }

        create("sideloadRelease") {
            storeFile = (props.getProperty("sideload.storeFile") ?: props.getProperty("signing.storeFile"))?.let { file(it) }
            storePassword = props.getProperty("sideload.storePassword") ?: props.getProperty("signing.storePassword")
            keyAlias = props.getProperty("sideload.keyAlias") ?: props.getProperty("signing.keyAlias")
            keyPassword = props.getProperty("sideload.keyPassword") ?: props.getProperty("signing.keyPassword")
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("play") {
            dimension = "distribution"
            isDefault = true
            buildConfigField("boolean", "IS_SIDELOAD", "false")
            signingConfig = signingConfigs.getByName("release")
        }
        create("sideload") {
            dimension = "distribution"
            applicationIdSuffix = ".sideload"
            versionNameSuffix = "-sideload"
            buildConfigField("boolean", "IS_SIDELOAD", "true")
            signingConfig = signingConfigs.getByName("sideloadRelease")
        }
    }

    buildFeatures { compose = true; buildConfig = true }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    testOptions { unitTests.isIncludeAndroidResources = true }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(platform("androidx.compose:compose-bom:2026.08.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2026.08.00"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.fragment:fragment-ktx:1.9.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
    implementation("androidx.navigation:navigation-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.google.android.play:app-update-ktx:2.1.0")
    implementation("com.tom-roush:pdfbox-android:2.0.27.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.7.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
