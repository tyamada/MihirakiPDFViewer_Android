# MihirakiPDFViewer Android App - System Specification & Prompt Context

> This document serves as the comprehensive specification and context prompt for the **MihirakiPDFViewer** Android application. It is designed to be provided to AI assistants or developers to establish a complete understanding of the app's architecture, data models, core business logic, and UI design.

---

## 1. Project Overview & Tech Stack
- **Project Name**: MihirakiPDFViewer
- **Target Platform**: Android (Min SDK: modern Android, target SDK latest)
- **Primary Architecture**: MVVM (Model-View-ViewModel) with Jetpack Compose & Material 3.
- **Key Libraries**:
  - Jetpack Compose & Material 3 (`androidx.compose`)
  - Jetpack Lifecycle & ViewModel (`androidx.lifecycle`)
  - Apache PDFBox (`com.tom_roush.pdfbox`) & Android native `PdfRenderer` (Hybrid PDF rendering engine)
  - Google Play Billing Library (`com.android.billingclient`)

---

## 2. Core Architecture & Component Map

### 2.1 Entry Points & Navigation
- [`MainActivity.kt`](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/MainActivity.kt): Hosts the Compose UI via [`MihirakiApp.kt`](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/MihirakiApp.kt) and handles incoming intents (`Intent.ACTION_VIEW`) to open external PDFs directly into `ViewerViewModel`.
- [`MihirakiApplication.kt`](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/MihirakiApplication.kt): Application-level setup.

### 2.2 Data Models & State ([Models.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/data/Models.kt))
- **`ReadingDirection`**: `L2R` (Left-to-Right, e.g., Western books) / `R2L` (Right-to-Left, e.g., Japanese manga).
- **`CoverMode`**: `STANDARD` vs `COMPATIBILITY`.
- **`ViewerLayout`**: `SINGLE` (Single page) / `SPREAD` (Two-page spread).
- **`ViewerSettings`**: Holds user preferences including reading direction, cover mode, layout, high quality rendering, sharpness, portrait spread, purchased tip tiers, last viewed URI, and last viewed page.
- **`PageSpread`**: Represents a two-page spread `data class PageSpread(val left: Int?, val right: Int?)` where `null` represents a deliberate blank page (e.g., standalone cover).

---

## 3. Core Business Logic & Algorithms

### 3.1 Spread Planning (`SpreadPlanner`)
- Calculates page layouts for double-page spreads taking reading direction (`L2R` vs `R2L`) and cover handling into account.
- In standard mode with cover enabled, page 0 (cover) is placed standalone on the correct side (Right for L2R, Left/Right depending on convention). Subsequent pages are paired in spreads.

### 3.2 Direction & Layout Detection (`DirectionDetector`, `LayoutDetector`)
- Automatically detects reading direction (`R2L` vs `L2R`) and layout preferences from PDF metadata (`ViewerPreferences`, page layout hints).

### 3.3 Hybrid PDF Engine (`PdfDocumentRepository`)
- [`PdfDocumentRepository.kt`](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/pdf/PdfDocumentRepository.kt) copies picked URIs into the private cache to ensure seekable file descriptors.
- Uses a hybrid approach combining Apache PDFBox for text extraction, search, outline, and metadata parsing with Android's native `PdfRenderer` for hardware-accelerated bitmap rendering.
- Robust exception handling for password-protected PDFs (`PasswordRequired`, `WrongPassword`, `PermissionDenied`, `Corrupt`).

---

## 4. UI Screens & Features

### 4.1 Viewer Screen ([ViewerScreen.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/ViewerScreen.kt) & [ViewerViewModel.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/viewmodel/ViewerViewModel.kt))
- Implements immersive PDF reading with spread views, zoom/pan controls, page navigation, table of contents (outline drawer), and search functionality.

### 4.2 Sample PDF Catalog ([SamplePdfScreen.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/SamplePdfScreen.kt), `SamplePdfCatalog.kt`, `SamplePdfRepository.kt`)
- Provides built-in sample PDFs (e.g., test light novels, manga episodes, layout test PDFs) for quick evaluation.

### 4.3 Settings Screens ([SettingsScreens.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/SettingsScreens.kt))
- Configures reading direction, spread behavior, cover display mode, image sharpness, high quality rendering, and mouse navigation.

### 4.4 Billing & Support ([TipScreen.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/ui/screens/TipScreen.kt) & [BillingManager.kt](file:///Users/tyamada22/src/MihirakiPDFViewer_Android/app/src/main/java/com/github/tyamada/mihirakipdfviewer_android/billing/BillingManager.kt))
- Integrates Google Play Billing Library for in-app tipping and support tiers (`TipProducts.kt`).

---

## 5. Development & Testing Guidelines
- **Jetpack Compose Best Practices**: Use immutable state holders, ViewModel state flows (`StateFlow`), and Material 3 design tokens.
- **Coroutines**: Perform heavy file operations and PDF rendering on `Dispatchers.IO`.
- **Testing**: Comprehensive UI and unit tests located in `app/src/androidTest` (including accessibility tests, load performance tests, navigation tests, and screenshot tests).
