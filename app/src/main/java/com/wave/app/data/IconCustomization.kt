package com.wave.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

enum class IconThemeMode(val label: String, val description: String) {
    CATEGORY_APP_COLOR("Category (App Colors)", "Play Store category icon tinted with each app's vibrant color"),
    CATEGORY_WALLPAPER_COLOR("Category (Wallpaper Tint)", "Play Store category icon matching dynamic system / wallpaper palette"),
    ORIGINAL_APP_ICON("Original App Icons", "Displays the device's native application icons")
}

sealed class CustomAppIcon {
    data object Automatic : CustomAppIcon()
    data class Bundled(val iconKey: String) : CustomAppIcon()
    data class Gallery(val uriString: String) : CustomAppIcon()
    data object Original : CustomAppIcon()
}

data class BundledIconItem(
    val key: String,
    val name: String,
    val vector: ImageVector
)

object BundledIconsRegistry {
    val icons: List<BundledIconItem> = listOf(
        BundledIconItem("call", "Phone", Icons.Filled.Call),
        BundledIconItem("message", "Message", Icons.Filled.Email),
        BundledIconItem("camera", "Camera", Icons.Filled.CameraAlt),
        BundledIconItem("browser", "Browser", Icons.Filled.Language),
        BundledIconItem("music", "Music", Icons.Filled.MusicNote),
        BundledIconItem("gamepad", "Game", Icons.Filled.Gamepad),
        BundledIconItem("puzzle", "Brain", Icons.Filled.Psychology),
        BundledIconItem("video", "Video", Icons.Filled.Videocam),
        BundledIconItem("play", "Player", Icons.Filled.PlayArrow),
        BundledIconItem("folder", "Files", Icons.Filled.Folder),
        BundledIconItem("settings", "Settings", Icons.Filled.Settings),
        BundledIconItem("people", "Social", Icons.Filled.People),
        BundledIconItem("work", "Briefcase", Icons.Filled.Work),
        BundledIconItem("car", "Drive", Icons.Filled.DirectionsCar),
        BundledIconItem("map", "Compass", Icons.Filled.Explore),
        BundledIconItem("calculator", "Calculator", Icons.Filled.Calculate),
        BundledIconItem("pay", "Finance", Icons.Filled.Payments),
        BundledIconItem("shop", "Shopping", Icons.Filled.ShoppingBag),
        BundledIconItem("news", "News", Icons.Filled.Newspaper),
        BundledIconItem("fitness", "Gym", Icons.Filled.FitnessCenter),
        BundledIconItem("ball", "Sports", Icons.Filled.SportsSoccer),
        BundledIconItem("book", "Education", Icons.Filled.School),
        BundledIconItem("read", "Reader", Icons.Filled.MenuBook),
        BundledIconItem("cloud", "Weather", Icons.Filled.Cloud),
        BundledIconItem("sparkles", "Magic", Icons.Filled.AutoAwesome),
        BundledIconItem("star", "Favorite", Icons.Filled.Star),
        BundledIconItem("heart", "Heart", Icons.Filled.Favorite),
        BundledIconItem("bookmark", "Bookmark", Icons.Filled.Bookmark),
        BundledIconItem("palette", "Creative", Icons.Filled.Palette),
        BundledIconItem("bulb", "Idea", Icons.Filled.Lightbulb),
        BundledIconItem("flame", "Fire", Icons.Filled.LocalFireDepartment),
        BundledIconItem("rocket", "Rocket", Icons.Filled.RocketLaunch),
        BundledIconItem("terminal", "Dev", Icons.Filled.Terminal),
        BundledIconItem("diamond", "Gem", Icons.Filled.Diamond),
        BundledIconItem("headphones", "Audio", Icons.Filled.Headphones),
        BundledIconItem("flash", "Lightning", Icons.Filled.FlashOn),
        BundledIconItem("moon", "Night", Icons.Filled.DarkMode),
        BundledIconItem("lock", "Lock", Icons.Filled.Lock),
        BundledIconItem("key", "Key", Icons.Filled.Key)
    )

    fun getIconByKey(key: String): ImageVector? {
        return icons.firstOrNull { it.key == key }?.vector
    }
}
