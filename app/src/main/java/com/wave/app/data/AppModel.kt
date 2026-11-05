package com.wave.app.data

data class AppInfoItem(
    val id: String,
    val label: String,
    val packageName: String,
    val className: String,
    val firstLetter: Char,
    val isFavorite: Boolean = false,
    val isInstalledApp: Boolean = true,
    val category: String = "Application",
    val accentColor: Long = 0xFF0284C7,
    val iconKey: String = "app",
    val categoryEnum: AppCategory = AppCategory.detectCategory(packageName, label),
    val customIcon: CustomAppIcon = CustomAppIcon.Automatic
)

enum class WaveIntensity(val label: String, val radiusFactor: Float, val maxDisplacementFactor: Float) {
    SUBTLE("Subtle", 70f, 24f),
    MEDIUM("Medium", 95f, 42f),
    DRAMATIC("Dramatic", 130f, 64f)
}

enum class ScrubberSide(val label: String) {
    RIGHT("Right Edge"),
    LEFT("Left Edge")
}

enum class DrawerLayout(val label: String) {
    LIST("Alphabetical List"),
    GRID("Modern Grid")
}

enum class IndexInteractionMode(val label: String) {
    JUMP("Jump to Section"),
    FILTER("Filter by Letter")
}

enum class ThemeMode(val label: String) {
    SYSTEM("Dynamic Material You"),
    DARK("AMOLED Dark"),
    LIGHT("Clean Light")
}

data class LauncherSettings(
    val waveIntensity: WaveIntensity = WaveIntensity.MEDIUM,
    val scrubberSide: ScrubberSide = ScrubberSide.RIGHT,
    val drawerLayout: DrawerLayout = DrawerLayout.LIST,
    val interactionMode: IndexInteractionMode = IndexInteractionMode.JUMP,
    val iconThemeMode: IconThemeMode = IconThemeMode.CATEGORY_APP_COLOR,
    val enableDedicatedWidgetPage: Boolean = true,
    val t9SearchEnabled: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hapticFeedback: Boolean = true,
    val showFavoritesDock: Boolean = true
)
