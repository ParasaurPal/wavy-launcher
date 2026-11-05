# Wavy Launcher

A fluid, tactile, and modern Android home screen launcher built with **Kotlin** and **Jetpack Compose**, featuring a signature **Wavy Alphabet Index** for fast, intuitive application navigation.

## ✨ Features

- **🌊 Signature Wavy Alphabet Index**:
  - Tactile vertical A–Z index along the screen edge with a physical wave distortion effect that magnifies letters under the finger.
  - Smooth Gaussian/cosine displacement and scaling for nearby letters.
  - Integrated haptic feedback ticks on letter transitions.
  - Programmatic smooth auto-scrolling to selected letter sections.
  - Floating high-contrast lens badge during touch scrubbing.

- **⚡ Instant Search & Filtering**:
  - Real-time search across installed application labels, package names, and categories.
  - Quick clear button and instant keyboard integration.

- **⭐ Favorites Dock**:
  - Quick-access dock for your most-used applications (Phone, Messages, Camera, Settings, etc.).
  - Easily pin or unpin apps directly from the drawer or app details sheet.

- **📱 App Management & Context Actions**:
  - Long-press any application to open the app bottom sheet.
  - Direct launch, pin/unpin favorites, jump to system App Info & Permissions, and copy package identifiers.

- **🎨 Modern Customization**:
  - **Wave Intensity**: Choose between *Subtle*, *Medium*, or *Dramatic* wave curvature.
  - **Hand Orientation**: Position the alphabet scrubber on the *Right Edge* or *Left Edge*.
  - **Layout Modes**: Switch between *Alphabetical List* and *Modern Grid*.
  - **Themes**: Supports *Dynamic Material You* (Android 12+), *AMOLED Dark*, and *Clean Light*.
  - **System Launcher**: Easy shortcut to set Wavy Launcher as the default Android home app.

## 🛠 Tech Stack

- **Language**: Kotlin 2.2
- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Android ViewModel and StateFlow
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Gradle**: Gradle 9.3.1 with Android Gradle Plugin 9.1.1