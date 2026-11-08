package com.wave.app.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wave.app.R
import com.wave.app.data.AppInfoItem
import com.wave.app.data.DrawerLayout
import com.wave.app.data.IndexInteractionMode
import com.wave.app.data.ScrubberSide
import androidx.compose.ui.graphics.vector.ImageVector
import com.wave.app.ui.components.AppDetailBottomSheet
import com.wave.app.ui.components.AppGridItem
import com.wave.app.ui.components.AppIconBadge
import com.wave.app.ui.components.AppListItem
import com.wave.app.ui.components.DedicatedWidgetPage
import com.wave.app.ui.components.HomeScreenView
import com.wave.app.ui.components.IconPickerBottomSheet
import com.wave.app.ui.components.PhoneActionItem
import com.wave.app.ui.components.PhoneSearchResult
import com.wave.app.ui.components.SettingsDialog
import com.wave.app.ui.components.T9SearchKeypad
import com.wave.app.ui.components.WaveAlphabetScrubber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filteredApps by viewModel.filteredApps.collectAsState()
    val groupedApps by viewModel.groupedApps.collectAsState()
    val favoriteApps by viewModel.favoriteApps.collectAsState()
    val availableLetters by viewModel.availableLetters.collectAsState()
    val selectedLetter by viewModel.selectedLetter.collectAsState()
    val activeLetterFilter by viewModel.activeLetterFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val t9Digits by viewModel.t9Digits.collectAsState()
    val isT9SearchActive by viewModel.isT9SearchActive.collectAsState()
    val phoneSearchResults by viewModel.phoneSearchResults.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val availableIconPacks by viewModel.availableIconPacks.collectAsState() // New state
    val selectedAppForDetails by viewModel.selectedAppForDetails.collectAsState()
    val selectedAppForIconPicker by viewModel.selectedAppForIconPicker.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Multi-page launcher: Widgets (0), Home (1), App Drawer (2)
    val pageCount = if (settings.enableDedicatedWidgetPage) 3 else 2
    val homePageIndex = if (settings.enableDedicatedWidgetPage) 1 else 0
    val drawerPageIndex = if (settings.enableDedicatedWidgetPage) 2 else 1
    val pagerState = rememberPagerState(initialPage = homePageIndex, pageCount = { pageCount })

    // Real-time Clock for Drawer
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("h:mm", Locale.getDefault())
        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFormat.format(now)
            currentDateString = dateFormat.format(now)
            delay(1000)
        }
    }

    // Back button behavior
    BackHandler(
        enabled = isT9SearchActive || searchQuery.isNotBlank() || activeLetterFilter != null ||
                  isSettingsOpen || selectedAppForDetails != null || selectedAppForIconPicker != null ||
                  pagerState.currentPage != homePageIndex
    ) {
        when {
            selectedAppForIconPicker != null -> viewModel.selectAppForIconPicker(null)
            selectedAppForDetails != null -> viewModel.selectAppForDetails(null)
            isSettingsOpen -> viewModel.setSettingsOpen(false)
            isT9SearchActive -> viewModel.setT9SearchActive(false)
            searchQuery.isNotBlank() -> viewModel.onSearchQueryChange("")
            activeLetterFilter != null -> viewModel.clearLetterFilter()
            pagerState.currentPage != homePageIndex -> {
                scope.launch { pagerState.animateScrollToPage(homePageIndex) }
            }
        }
    }

    // Calculate item indices for alphabet jump
    val letterIndexMap = remember(groupedApps, settings.drawerLayout) {
        val map = mutableMapOf<Char, Int>()
        var currentIndex = 0
        groupedApps.keys.sorted().forEach { letter ->
            map[letter] = currentIndex
            val count = groupedApps[letter]?.size ?: 0
            currentIndex += count + 1
        }
        map
    }

    val onLetterSelected: (Char) -> Unit = { letter ->
        viewModel.onScrubberLetterInput(letter)
        if (settings.interactionMode == IndexInteractionMode.JUMP) {
            val targetIndex = letterIndexMap[letter]
            if (targetIndex != null) {
                scope.launch {
                    if (settings.drawerLayout == DrawerLayout.LIST) {
                        listState.animateScrollToItem(targetIndex)
                    } else {
                        gridState.animateScrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("launcher_screen"),
        contentWindowInsets = WindowInsets.systemBars,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            // Persistent Bottom Navigation Pills
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .testTag("launcher_bottom_navigation"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (settings.enableDedicatedWidgetPage) {
                        val isWidgetPage = pagerState.currentPage == 0
                        NavigationPillButton(
                            icon = Icons.Outlined.Widgets,
                            label = "Widgets",
                            isSelected = isWidgetPage,
                            onClick = { scope.launch { pagerState.animateScrollToPage(0) } }
                        )
                    }

                    val isHomePage = pagerState.currentPage == homePageIndex
                    NavigationPillButton(
                        icon = Icons.Outlined.Home,
                        label = "Home",
                        isSelected = isHomePage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(homePageIndex) } }
                    )

                    val isDrawerPage = pagerState.currentPage == drawerPageIndex
                    NavigationPillButton(
                        icon = Icons.Outlined.Apps,
                        label = "App Wave",
                        isSelected = isDrawerPage,
                        onClick = { scope.launch { pagerState.animateScrollToPage(drawerPageIndex) } }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when {
                    settings.enableDedicatedWidgetPage && page == 0 -> {
                        // Dedicated Vertically Scrolling Widget Page
                        DedicatedWidgetPage(
                            favoriteApps = favoriteApps,
                            onLaunchApp = { app ->
                                if (!viewModel.launchApp(app)) {
                                    Toast.makeText(context, "Opening ${app.label}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    page == homePageIndex -> {
                        // Home Screen with Horizontally Swiping Widget Group & Favorites
                        HomeScreenView(
                            favoriteApps = favoriteApps,
                            onLaunchApp = { app ->
                                if (!viewModel.launchApp(app)) {
                                    Toast.makeText(context, "Opening ${app.label}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onAppLongClick = { app -> viewModel.selectAppForDetails(app) },
                            onOpenSearch = {
                                scope.launch {
                                    pagerState.animateScrollToPage(drawerPageIndex)
                                }
                            },
                            onOpenT9Search = {
                                viewModel.setT9SearchActive(true)
                                scope.launch {
                                    pagerState.animateScrollToPage(drawerPageIndex)
                                }
                            },
                            onOpenSettings = { viewModel.setSettingsOpen(true) },
                            onNavigateToDrawer = {
                                scope.launch {
                                    pagerState.animateScrollToPage(drawerPageIndex)
                                }
                            },
                            onNavigateToWidgets = {
                                if (settings.enableDedicatedWidgetPage) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                }
                            },
                            getCachedIcon = { pkg -> viewModel.getCachedIcon(pkg) },
                            iconThemeMode = settings.iconThemeMode
                        )
                    }

                    page == drawerPageIndex -> {
                        // Main App Drawer with Signature Wavy Alphabet Index & T9 Search
                        AppDrawerContent(
                            viewModel = viewModel,
                            filteredApps = filteredApps,
                            groupedApps = groupedApps,
                            favoriteApps = favoriteApps,
                            availableLetters = availableLetters,
                            selectedLetter = selectedLetter,
                            activeLetterFilter = activeLetterFilter,
                            searchQuery = searchQuery,
                            t9Digits = t9Digits,
                            isT9SearchActive = isT9SearchActive,
                            phoneSearchResults = phoneSearchResults,
                            settings = settings,
                            isLoading = isLoading,
                            currentTimeString = currentTimeString,
                            currentDateString = currentDateString,
                            listState = listState,
                            gridState = gridState,
                            onLetterSelected = onLetterSelected
                        )
                    }
                }
            }
        }
    }

    // App Detail Bottom Sheet
    selectedAppForDetails?.let { app ->
        AppDetailBottomSheet(
            app = app,
            iconBitmap = viewModel.getCachedIcon(app.packageName),
            iconThemeMode = settings.iconThemeMode,
            onDismiss = { viewModel.selectAppForDetails(null) },
            onLaunch = {
                if (!viewModel.launchApp(app)) {
                    Toast.makeText(context, "Opening ${app.label}", Toast.LENGTH_SHORT).show()
                }
            },
            onToggleFavorite = { viewModel.toggleFavorite(app) },
            onOpenAppInfo = { viewModel.openAppInfo(app) },
            onCopyPackageName = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Package Name", app.packageName)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied ${app.packageName}", Toast.LENGTH_SHORT).show()
            },
            onOpenIconPicker = {
                val targetApp = app
                viewModel.selectAppForDetails(null)
                viewModel.selectAppForIconPicker(targetApp)
            }
        )
    }

    // Custom Icon Picker Bottom Sheet
    selectedAppForIconPicker?.let { app ->
        IconPickerBottomSheet(
            app = app,
            iconBitmap = viewModel.getCachedIcon(app.packageName),
            iconThemeMode = settings.iconThemeMode,
            onSelectCustomIcon = { customIcon ->
                viewModel.setCustomIconForApp(app.packageName, customIcon)
            },
            onDismiss = { viewModel.selectAppForIconPicker(null) }
        )
    }

    // Settings Dialog
    if (isSettingsOpen) {
        SettingsDialog(
            settings = settings,
            availableIconPacks = availableIconPacks, // Passed in
            onUpdateSettings = { viewModel.updateSettings(it) },
            onSelectIconPack = { viewModel.selectIconPack(it) }, // Passed in
            onDismiss = { viewModel.setSettingsOpen(false) }
        )
    }
}

@Composable
private fun NavigationPillButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppDrawerContent(
    viewModel: LauncherViewModel,
    filteredApps: List<AppInfoItem>,
    groupedApps: Map<Char, List<AppInfoItem>>,
    favoriteApps: List<AppInfoItem>,
    availableLetters: Set<Char>,
    selectedLetter: Char?,
    activeLetterFilter: Char?,
    searchQuery: String,
    t9Digits: String,
    isT9SearchActive: Boolean,
    phoneSearchResults: List<PhoneSearchResult>,
    settings: com.wave.app.data.LauncherSettings,
    isLoading: Boolean,
    currentTimeString: String,
    currentDateString: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    gridState: androidx.compose.foundation.lazy.grid.LazyGridState,
    onLetterSelected: (Char) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_drawer_content")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (settings.scrubberSide == ScrubberSide.LEFT) 46.dp else 12.dp,
                    end = if (settings.scrubberSide == ScrubberSide.RIGHT) 46.dp else 12.dp
                )
        ) {
            // Header Glance: Clock & Date
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentTimeString.ifEmpty { "Wavy" },
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = currentDateString.ifEmpty { "Launcher" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.setSettingsOpen(true) },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("drawer_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar with QWERTY and T9 toggle
            OutlinedTextField(
                value = if (isT9SearchActive) t9Digits else searchQuery,
                onValueChange = {
                    if (isT9SearchActive) {
                        viewModel.setT9Digits(it)
                    } else {
                        viewModel.onSearchQueryChange(it)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("launcher_search_field"),
                placeholder = {
                    Text(
                        text = if (isT9SearchActive) "T9 Dial: 466 for GOO, 746 for PHO…" else stringResource(R.string.search_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (isT9SearchActive) Icons.Filled.Dialpad else Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (searchQuery.isNotEmpty() || t9Digits.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.onSearchQueryChange("")
                                    viewModel.setT9Digits("")
                                },
                                modifier = Modifier.testTag("search_clear_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Toggle T9 Search Mode
                        IconButton(
                            onClick = { viewModel.setT9SearchActive(!isT9SearchActive) },
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("toggle_t9_mode_button")
                        ) {
                            Icon(
                                imageVector = if (isT9SearchActive) Icons.Filled.Keyboard else Icons.Filled.Dialpad,
                                contentDescription = if (isT9SearchActive) "Switch to Keyboard" else "Switch to T9 Keypad",
                                tint = if (isT9SearchActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Alphabet Index Mode Toggle (Jump vs Filter)
            if (!isT9SearchActive && searchQuery.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredApps.size} apps available",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Compact Mode Pill Selector
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(2.dp)
                            .testTag("mode_toggle_selector"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isJump = settings.interactionMode == IndexInteractionMode.JUMP
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isJump) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setInteractionMode(IndexInteractionMode.JUMP) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("select_jump_mode")
                        ) {
                            Text(
                                text = "Jump 📍",
                                fontSize = 11.sp,
                                fontWeight = if (isJump) FontWeight.Bold else FontWeight.Medium,
                                color = if (isJump) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val isFilter = settings.interactionMode == IndexInteractionMode.FILTER
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isFilter) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewModel.setInteractionMode(IndexInteractionMode.FILTER) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("select_filter_mode")
                        ) {
                            Text(
                                text = "Filter 🔍",
                                fontSize = 11.sp,
                                fontWeight = if (isFilter) FontWeight.Bold else FontWeight.Medium,
                                color = if (isFilter) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Active Letter Filter Notification Chip
            AnimatedVisibility(
                visible = activeLetterFilter != null && searchQuery.isBlank() && !isT9SearchActive,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, bottom = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("letter_filter_active_banner"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Letter \"$activeLetterFilter\" • ${filteredApps.size} apps found",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.clearLetterFilter() }
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Clear",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear letter filter",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main List Area
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("loading_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("app_list_column"),
                    contentPadding = PaddingValues(bottom = if (isT9SearchActive) 280.dp else 48.dp)
                ) {
                    // Cross-phone search actions (Dial, SMS, Web search, Settings)
                    if (isT9SearchActive && phoneSearchResults.isNotEmpty()) {
                        item(key = "phone_search_header") {
                            Text(
                                text = "Phone & System Actions",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        items(phoneSearchResults, key = { "action_${it.title}" }) { action ->
                            PhoneActionItem(result = action)
                        }
                        item(key = "phone_search_divider") {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }

                    if (filteredApps.isEmpty()) {
                        item(key = "empty_state") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.SearchOff,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No apps match your search",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        groupedApps.forEach { (letter, appsInLetter) ->
                            item(key = "header_$letter") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                        .testTag("letter_header_$letter"),
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
                                    Spacer(modifier = Modifier.width(6.dp))
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
                                    iconBitmap = viewModel.getCachedIcon(app.packageName),
                                    iconThemeMode = settings.iconThemeMode,
                                    onClick = {
                                        if (!viewModel.launchApp(app)) {
                                            Toast.makeText(context, "Opening ${app.label}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onLongClick = {
                                        viewModel.selectAppForDetails(app)
                                    },
                                    onToggleFavorite = {
                                        viewModel.toggleFavorite(app)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Side-Bar Wave Alphabet Scrubber (active when not in search)
        if (searchQuery.isBlank() && !isT9SearchActive) {
            WaveAlphabetScrubber(
                availableLetters = availableLetters,
                selectedLetter = if (settings.interactionMode == IndexInteractionMode.FILTER) activeLetterFilter else selectedLetter,
                onLetterSelected = onLetterSelected,
                intensity = settings.waveIntensity,
                side = settings.scrubberSide,
                interactionMode = settings.interactionMode,
                hapticsEnabled = settings.hapticFeedback,
                modifier = Modifier
                    .align(
                        if (settings.scrubberSide == ScrubberSide.RIGHT) Alignment.CenterEnd
                        else Alignment.CenterStart
                    )
                    .padding(vertical = 12.dp)
            )
        }

        // T9 Dialpad Keypad pinned to the bottom when active
        if (isT9SearchActive) {
            T9SearchKeypad(
                queryDigits = t9Digits,
                onDigitsChange = { viewModel.setT9Digits(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag("t9_bottom_keypad")
            )
        }
    }
}
