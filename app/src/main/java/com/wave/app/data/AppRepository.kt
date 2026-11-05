package com.wave.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AppRepository(private val context: Context) {

    private val iconCache = mutableMapOf<String, ImageBitmap>()

    suspend fun loadApps(): List<AppInfoItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(intent, 0)
            }
        } catch (_: Exception) {
            emptyList()
        }

        val processedPackages = mutableSetOf<String>()
        val realApps = mutableListOf<AppInfoItem>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) continue // Skip wavy launcher itself
            if (!processedPackages.add(pkg)) continue

            val label = resolveInfo.loadLabel(pm)?.toString()?.trim() ?: pkg
            val className = resolveInfo.activityInfo.name
            val letter = label.firstOrNull()?.uppercaseChar()?.let {
                if (it in 'A'..'Z') it else '#'
            } ?: '#'

            try {
                val iconDrawable = resolveInfo.loadIcon(pm)
                val bitmap = drawableToBitmap(iconDrawable)
                iconCache[pkg] = bitmap.asImageBitmap()
            } catch (_: Exception) {
            }

            val category = AppCategory.detectCategory(pkg, label)

            realApps.add(
                AppInfoItem(
                    id = "$pkg/$className",
                    label = label,
                    packageName = pkg,
                    className = className,
                    firstLetter = letter,
                    isFavorite = false,
                    isInstalledApp = true,
                    category = category.displayLabel,
                    accentColor = getColorForPackage(pkg),
                    iconKey = getIconKeyForPackage(pkg, label),
                    categoryEnum = category
                )
            )
        }

        // Also query installed applications to ensure system tools with launcher entry are captured
        try {
            val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.getInstalledApplications(0)
            }
            for (appInfo in installedApps) {
                val pkg = appInfo.packageName
                if (pkg == context.packageName || processedPackages.contains(pkg)) continue
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null) {
                    processedPackages.add(pkg)
                    val label = appInfo.loadLabel(pm)?.toString()?.trim() ?: pkg
                    val letter = label.firstOrNull()?.uppercaseChar()?.let {
                        if (it in 'A'..'Z') it else '#'
                    } ?: '#'
                    try {
                        val iconDrawable = appInfo.loadIcon(pm)
                        val bitmap = drawableToBitmap(iconDrawable)
                        iconCache[pkg] = bitmap.asImageBitmap()
                    } catch (_: Exception) {
                    }
                    val category = AppCategory.detectCategory(pkg, label)
                    realApps.add(
                        AppInfoItem(
                            id = "$pkg/main",
                            label = label,
                            packageName = pkg,
                            className = "",
                            firstLetter = letter,
                            isFavorite = false,
                            isInstalledApp = true,
                            category = category.displayLabel,
                            accentColor = getColorForPackage(pkg),
                            iconKey = getIconKeyForPackage(pkg, label),
                            categoryEnum = category
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }

        // If running in a minimal emulator or container environment with few apps,
        // complement with standard apps so the entire A-Z wavy index has rich items to test
        val sampleApps = if (realApps.size < 12) {
            getComplementaryApps(processedPackages)
        } else {
            emptyList()
        }

        val combined = (realApps + sampleApps).sortedWith(
            compareBy<AppInfoItem> {
                if (it.firstLetter == '#') 'Z' + 1 else it.firstLetter
            }.thenBy { it.label.lowercase(Locale.ROOT) }
        )

        combined
    }

    fun getCachedIcon(packageName: String): ImageBitmap? {
        return iconCache[packageName]
    }

    fun launchApp(app: AppInfoItem): Boolean {
        return try {
            val pm = context.packageManager
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                val fallbackIntent = when (app.iconKey) {
                    "phone" -> Intent(Intent.ACTION_DIAL)
                    "camera" -> Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                    "settings" -> Intent(Settings.ACTION_SETTINGS)
                    "browser" -> Intent(Intent.ACTION_VIEW, Uri.parse("https://google.com"))
                    else -> null
                }
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                    true
                } else {
                    false
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val genericIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(genericIntent)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getColorForPackage(pkg: String): Long {
        val hash = kotlin.math.abs(pkg.hashCode())
        val colors = longArrayOf(
            0xFF0284C7, 0xFF059669, 0xFF7C3AED, 0xFFDB2777,
            0xFFD97706, 0xFF2563EB, 0xFF0D9488, 0xFFE11D48
        )
        return colors[hash % colors.size]
    }

    private fun getIconKeyForPackage(pkg: String, label: String): String {
        val lower = (pkg + label).lowercase(Locale.ROOT)
        return when {
            lower.contains("phone") || lower.contains("dialer") -> "phone"
            lower.contains("message") || lower.contains("sms") -> "message"
            lower.contains("browser") || lower.contains("chrome") -> "browser"
            lower.contains("camera") -> "camera"
            lower.contains("photo") || lower.contains("gallery") -> "gallery"
            lower.contains("clock") || lower.contains("alarm") -> "clock"
            lower.contains("calendar") -> "calendar"
            lower.contains("calc") -> "calculator"
            lower.contains("setting") -> "settings"
            lower.contains("music") || lower.contains("audio") -> "music"
            lower.contains("file") -> "files"
            lower.contains("map") -> "maps"
            lower.contains("mail") || lower.contains("email") -> "email"
            else -> "app"
        }
    }

    private fun getComplementaryApps(existingPackages: Set<String>): List<AppInfoItem> {
        val raw = listOf(
            Triple("com.android.calculator2", "Calculator", "calculator"),
            Triple("com.android.calendar", "Calendar", "calendar"),
            Triple("com.android.camera2", "Camera", "camera"),
            Triple("com.android.deskclock", "Clock", "clock"),
            Triple("com.android.contacts", "Contacts", "contacts"),
            Triple("com.android.documentsui", "Files", "files"),
            Triple("com.android.gallery3d", "Gallery", "gallery"),
            Triple("com.google.android.apps.maps", "Maps", "maps"),
            Triple("com.android.mms", "Messages", "message"),
            Triple("com.android.music", "Music Player", "music"),
            Triple("com.android.notes", "Notes & Tasks", "notes"),
            Triple("com.android.dialer", "Phone", "phone"),
            Triple("com.android.settings", "Settings", "settings"),
            Triple("com.android.vending", "Store", "store"),
            Triple("com.google.android.apps.weather", "Weather", "weather"),
            Triple("com.google.android.youtube", "YouTube", "video"),
            Triple("com.wave.chess", "Chess Master", "puzzle"),
            Triple("com.wave.arcade", "Speed Runner", "arcade")
        )

        return raw.filterNot { existingPackages.contains(it.first) }.map { (pkg, label, key) ->
            val cat = AppCategory.detectCategory(pkg, label)
            AppInfoItem(
                id = pkg,
                label = label,
                packageName = pkg,
                className = "",
                firstLetter = label.first().uppercaseChar(),
                isFavorite = pkg in listOf("com.android.dialer", "com.android.mms", "com.android.camera2", "com.android.settings"),
                isInstalledApp = false,
                category = cat.displayLabel,
                accentColor = getColorForPackage(pkg),
                iconKey = key,
                categoryEnum = cat
            )
        }
    }
}
