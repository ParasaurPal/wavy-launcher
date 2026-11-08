package com.wave.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wave.app.data.AppInfoItem
import com.wave.app.data.BundledIconsRegistry
import com.wave.app.data.CustomAppIcon
import com.wave.app.data.IconThemeMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListItem(
    app: AppInfoItem,
    iconBitmap: ImageBitmap?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    iconThemeMode: IconThemeMode = IconThemeMode.CATEGORY_APP_COLOR
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("app_item_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconBadge(
            app = app,
            iconBitmap = iconBitmap,
            size = 46.dp,
            iconThemeMode = iconThemeMode
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = app.category,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier
                .size(44.dp)
                .testTag("fav_button_${app.packageName}")
        ) {
            Icon(
                imageVector = if (app.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = if (app.isFavorite) "Favorited" else "Add to favorites",
                tint = if (app.isFavorite) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridItem(
    app: AppInfoItem,
    iconBitmap: ImageBitmap?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconThemeMode: IconThemeMode = IconThemeMode.CATEGORY_APP_COLOR
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp)
            .testTag("app_grid_item_${app.packageName}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppIconBadge(
            app = app,
            iconBitmap = iconBitmap,
            size = 52.dp,
            iconThemeMode = iconThemeMode
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun AppIconBadge(
    app: AppInfoItem,
    iconBitmap: ImageBitmap?,
    size: Dp,
    modifier: Modifier = Modifier,
    iconThemeMode: IconThemeMode = IconThemeMode.CATEGORY_APP_COLOR
) {
    val context = LocalContext.current

    // Check for custom icon override
    when (val custom = app.customIcon) {
        is CustomAppIcon.Gallery -> {
            val galleryBitmap = remember(custom.uriString) {
                try {
                    val uri = Uri.parse(custom.uriString)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (galleryBitmap != null) {
                Image(
                    bitmap = galleryBitmap,
                    contentDescription = app.label,
                    modifier = modifier
                        .size(size)
                        .clip(RoundedCornerShape(size * 0.28f))
                )
                return
            }
        }
        is CustomAppIcon.Bundled -> {
            val bundledVector = BundledIconsRegistry.getIconByKey(custom.iconKey)
            if (bundledVector != null) {
                val (tileColor, iconTint) = getColorsForMode(app, iconThemeMode)
                Box(
                    modifier = modifier
                        .size(size)
                        .clip(RoundedCornerShape(size * 0.28f))
                        .background(tileColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = bundledVector,
                        contentDescription = app.label,
                        tint = iconTint,
                        modifier = Modifier.size(size * 0.54f)
                    )
                }
                return
            }
        }
        is CustomAppIcon.Original -> {
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap,
                    contentDescription = app.label,
                    modifier = modifier
                        .size(size)
                        .clip(RoundedCornerShape(size * 0.28f))
                )
                return
            }
        }
        is CustomAppIcon.Automatic -> {
            // Proceed to follow iconThemeMode
        }
    }

    // Follow global iconThemeMode
    
        when (iconThemeMode) {
    IconThemeMode.CATEGORY_APP_COLOR -> {
        // Keep whatever was originally here for CATEGORY_APP_COLOR
    }
    IconThemeMode.CATEGORY_WALLPAPER_COLOR -> {
        // Keep whatever was originally here for CATEGORY_WALLPAPER_COLOR
    }
    IconThemeMode.ORIGINAL_APP_ICON, IconThemeMode.ICON_PACK -> {
        // Keep whatever was originally here for ORIGINAL_APP_ICON. 
        // It usually looks like: 
        // Image(bitmap = iconBitmap.asImageBitmap(), ...)
    }
        }   }
    }
}

@Composable
private fun FallbackCategoryBadge(
    app: AppInfoItem,
    size: Dp,
    modifier: Modifier,
    iconThemeMode: IconThemeMode
) {
    val (tileColor, iconTint) = getColorsForMode(app, iconThemeMode)
    val categoryIcon: ImageVector = app.categoryEnum.icon

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(tileColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = categoryIcon,
            contentDescription = "${app.label} (${app.category})",
            tint = iconTint,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
private fun getColorsForMode(
    app: AppInfoItem,
    iconThemeMode: IconThemeMode
): Pair<Color, Color> {
    return when (iconThemeMode) {
        IconThemeMode.CATEGORY_WALLPAPER_COLOR -> {
            // Material You dynamic theme container colors matching wallpaper
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        }
        else -> {
            // Category icon with the app's vibrant accent color
            Color(app.accentColor) to Color.White
        }
    }
}
