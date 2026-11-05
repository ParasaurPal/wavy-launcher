package com.wave.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wave.app.data.AppInfoItem
import com.wave.app.data.AppRepository
import com.wave.app.data.IndexInteractionMode
import com.wave.app.data.ScrubberSide
import com.wave.app.data.WaveIntensity
import kotlinx.coroutines.launch

/**
 * A standalone composable that retrieves all installed applications on the device
 * and displays them in a scrollable list, accompanied by the interactive side-bar
 * 'wavy' alphabet index component for fast jumping or filtering.
 */
@Composable
fun InstalledAppsList(
    modifier: Modifier = Modifier,
    onAppClick: ((AppInfoItem) -> Unit)? = null,
    onAppLongClick: ((AppInfoItem) -> Unit)? = null,
    onToggleFavorite: ((AppInfoItem) -> Unit)? = null,
    side: ScrubberSide = ScrubberSide.RIGHT,
    waveIntensity: WaveIntensity = WaveIntensity.MEDIUM,
    hapticsEnabled: Boolean = true,
    initialInteractionMode: IndexInteractionMode = IndexInteractionMode.JUMP,
    iconThemeMode: com.wave.app.data.IconThemeMode = com.wave.app.data.IconThemeMode.CATEGORY_APP_COLOR
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AppRepository(context) }

    var apps by remember { mutableStateOf<List<AppInfoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var interactionMode by remember { mutableStateOf(initialInteractionMode) }
    var selectedLetterFilter by remember { mutableStateOf<Char?>(null) }
    var highlightedLetter by remember { mutableStateOf<Char?>(null) }

    val listState = rememberLazyListState()

    fun refreshApps() {
        scope.launch {
            isLoading = true
            apps = repository.loadApps()
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    // Filter or group apps
    val displayedApps = remember(apps, selectedLetterFilter, interactionMode) {
        if (interactionMode == IndexInteractionMode.FILTER && selectedLetterFilter != null) {
            apps.filter { it.firstLetter == selectedLetterFilter }
        } else {
            apps
        }
    }

    val groupedApps = remember(displayedApps) {
        displayedApps.groupBy { it.firstLetter }
    }

    val availableLetters = remember(apps) {
        apps.map { it.firstLetter }.toSet()
    }

    // Map letter to list position index for Jump mode
    val letterIndexMap = remember(groupedApps) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 0
        groupedApps.keys.sorted().forEach { letter ->
            map[letter] = currentIndex
            val count = groupedApps[letter]?.size ?: 0
            currentIndex += count + 1 // 1 for header + items
        }
        map
    }

    val handleLetterInput: (Char) -> Unit = { letter ->
        highlightedLetter = letter
        if (interactionMode == IndexInteractionMode.FILTER) {
            selectedLetterFilter = if (selectedLetterFilter == letter) null else letter
        } else {
            // Jump mode
            val targetIdx = letterIndexMap[letter]
            if (targetIdx != null) {
                scope.launch {
                    listState.animateScrollToItem(targetIdx)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("installed_apps_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (side == ScrubberSide.LEFT) 48.dp else 8.dp,
                    end = if (side == ScrubberSide.RIGHT) 48.dp else 8.dp
                )
        ) {
            // Top Mode & Count Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${apps.size} Installed Apps",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Interaction Mode Pill: Jump vs Filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                interactionMode = if (interactionMode == IndexInteractionMode.JUMP) {
                                    IndexInteractionMode.FILTER
                                } else {
                                    selectedLetterFilter = null
                                    IndexInteractionMode.JUMP
                                }
                            }
                            .testTag("toggle_interaction_mode_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = if (interactionMode == IndexInteractionMode.JUMP) "Mode: Jump 📍" else "Mode: Filter 🔍",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { refreshApps() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refresh_installed_apps_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh apps",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Filter status banner if active
            if (interactionMode == IndexInteractionMode.FILTER && selectedLetterFilter != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filtered by \"$selectedLetterFilter\" • ${displayedApps.size} apps found",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { selectedLetterFilter = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear filter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Scrollable List Content
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("apps_loading"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            } else if (displayedApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("apps_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedLetterFilter != null) "No installed apps starting with '$selectedLetterFilter'" else "No installed applications found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("installed_apps_list"),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    groupedApps.forEach { (letter, appsInLetter) ->
                        item(key = "section_$letter") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("letter_section_$letter"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = letter.toString(),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${appsInLetter.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }

                        items(appsInLetter, key = { it.id }) { app ->
                            AppListItem(
                                app = app,
                                iconBitmap = repository.getCachedIcon(app.packageName),
                                iconThemeMode = iconThemeMode,
                                onClick = {
                                    if (onAppClick != null) {
                                        onAppClick(app)
                                    } else {
                                        val launched = repository.launchApp(app)
                                        if (!launched) {
                                            Toast.makeText(context, "Opening ${app.label}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (onAppLongClick != null) {
                                        onAppLongClick(app)
                                    } else {
                                        repository.openAppDetails(app.packageName)
                                    }
                                },
                                onToggleFavorite = {
                                    onToggleFavorite?.invoke(app)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Side-Bar Wavy Alphabet Index Component
        WaveAlphabetScrubber(
            availableLetters = availableLetters,
            selectedLetter = if (interactionMode == IndexInteractionMode.FILTER) selectedLetterFilter else highlightedLetter,
            onLetterSelected = handleLetterInput,
            intensity = waveIntensity,
            side = side,
            interactionMode = interactionMode,
            hapticsEnabled = hapticsEnabled,
            modifier = Modifier
                .align(if (side == ScrubberSide.RIGHT) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(vertical = 8.dp)
        )
    }
}
