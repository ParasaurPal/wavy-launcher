package com.wave.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wave.app.data.AppInfoItem
import com.wave.app.data.AppRepository
import com.wave.app.data.CustomAppIcon
import com.wave.app.data.IconPackManager
import com.wave.app.data.IconThemeMode
import com.wave.app.data.IndexInteractionMode
import com.wave.app.data.InstalledIconPack
import com.wave.app.data.LauncherSettings
import com.wave.app.ui.components.PhoneSearchResult
import com.wave.app.ui.components.T9SearchHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application.applicationContext)

    private val _rawApps = MutableStateFlow<List<AppInfoItem>>(emptyList())
    private val _customIcons = MutableStateFlow<Map<String, CustomAppIcon>>(emptyMap())
    
    // New: State to hold installed icon packs
    private val _availableIconPacks = MutableStateFlow<List<InstalledIconPack>>(emptyList())
    val availableIconPacks: StateFlow<List<InstalledIconPack>> = _availableIconPacks.asStateFlow()

    val allApps: StateFlow<List<AppInfoItem>> = combine(_rawApps, _customIcons) { raw, custom ->
        raw.map { app ->
            val override = custom[app.packageName]
            if (override != null) app.copy(customIcon = override) else app
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _t9Digits = MutableStateFlow("")
    val t9Digits: StateFlow<String> = _t9Digits.asStateFlow()

    private val _isT9SearchActive = MutableStateFlow(false)
    val isT9SearchActive: StateFlow<Boolean> = _isT9SearchActive.asStateFlow()

    private val _activeLetterFilter = MutableStateFlow<Char?>(null)
    val activeLetterFilter: StateFlow<Char?> = _activeLetterFilter.asStateFlow()

    private val _settings = MutableStateFlow(LauncherSettings())
    val settings: StateFlow<LauncherSettings> = _settings.asStateFlow()

    private val _selectedLetter = MutableStateFlow<Char?>(null)
    val selectedLetter: StateFlow<Char?> = _selectedLetter.asStateFlow()

    private val _selectedAppForDetails = MutableStateFlow<AppInfoItem?>(null)
    val selectedAppForDetails: StateFlow<AppInfoItem?> = _selectedAppForDetails.asStateFlow()

    private val _selectedAppForIconPicker = MutableStateFlow<AppInfoItem?>(null)
    val selectedAppForIconPicker: StateFlow<AppInfoItem?> = _selectedAppForIconPicker.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filtered apps based on query, T9 digits, or letter filter
    val filteredApps: StateFlow<List<AppInfoItem>> = combine(
        allApps,
        _searchQuery,
        _t9Digits,
        _isT9SearchActive,
        _activeLetterFilter
    ) { apps, query, t9, isT9, letterFilter ->
        when {
            isT9 && t9.isNotBlank() -> {
                apps.filter { T9SearchHelper.matchesApp(it, t9) }
            }
            query.isNotBlank() -> {
                val q = query.trim().lowercase(Locale.ROOT)
                apps.filter {
                    it.label.lowercase(Locale.ROOT).contains(q) ||
                    it.packageName.lowercase(Locale.ROOT).contains(q) ||
                    it.category.lowercase(Locale.ROOT).contains(q)
                }
            }
            letterFilter != null -> {
                apps.filter { it.firstLetter == letterFilter }
            }
            else -> apps
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Cross-phone search actions for T9 search
    val phoneSearchResults: StateFlow<List<PhoneSearchResult>> = combine(
        _t9Digits,
        _isT9SearchActive
    ) { digits, isT9 ->
        if (isT9 && digits.isNotBlank()) {
            T9SearchHelper.getPhoneActions(application.applicationContext, digits)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Grouped apps by letter
    val groupedApps: StateFlow<Map<Char, List<AppInfoItem>>> = filteredApps.combine(allApps) { filtered, _ ->
        filtered.groupBy { it.firstLetter }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Favorites
    val favoriteApps: StateFlow<List<AppInfoItem>> = allApps.combine(_settings) { apps, _ ->
        apps.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Set of letters present in the app list
    val availableLetters: StateFlow<Set<Char>> = allApps.combine(allApps) { apps, _ ->
        apps.map { it.firstLetter }.toSet()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    init {
        loadIconPacks() // New
        refreshApps()
    }

    fun loadIconPacks() {
        viewModelScope.launch {
            val packs = withContext(Dispatchers.IO) {
                IconPackManager.getInstalledPacks(getApplication())
            }
            _availableIconPacks.value = packs
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _isLoading.value = true
            // Updated to pass the settings
            val loaded = repository.loadApps(_settings.value)
            _rawApps.value = loaded
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            _activeLetterFilter.value = null
            _isT9SearchActive.value = false
        }
    }

    fun setT9Digits(digits: String) {
        _t9Digits.value = digits
        if (digits.isNotBlank()) {
            _isT9SearchActive.value = true
            _searchQuery.value = ""
            _activeLetterFilter.value = null
        }
    }

    fun setT9SearchActive(active: Boolean) {
        _isT9SearchActive.value = active
        if (!active) {
            _t9Digits.value = ""
        }
    }

    fun setInteractionMode(mode: IndexInteractionMode) {
        _settings.value = _settings.value.copy(interactionMode = mode)
        if (mode == IndexInteractionMode.JUMP) {
            _activeLetterFilter.value = null
        }
    }

    fun onScrubberLetterInput(letter: Char) {
        _selectedLetter.value = letter
        if (_settings.value.interactionMode == IndexInteractionMode.FILTER) {
            _activeLetterFilter.value = letter
        }
    }

    fun clearLetterFilter() {
        _activeLetterFilter.value = null
        _selectedLetter.value = null
    }

    fun toggleFavorite(app: AppInfoItem) {
        _rawApps.value = _rawApps.value.map {
            if (it.id == app.id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    fun setCustomIconForApp(packageName: String, customIcon: CustomAppIcon) {
        _customIcons.value = _customIcons.value + (packageName to customIcon)
    }

    fun setIconThemeMode(mode: IconThemeMode) {
        _settings.value = _settings.value.copy(iconThemeMode = mode)
        refreshApps() // Reload icons to apply the theme
    }

    // New: Function to select an icon pack
    fun selectIconPack(packPackageName: String?) {
        _settings.value = _settings.value.copy(
            iconThemeMode = if (packPackageName != null) IconThemeMode.ICON_PACK else IconThemeMode.CATEGORY_APP_COLOR,
            selectedIconPackPackage = packPackageName
        )
        refreshApps()
    }

    fun launchApp(app: AppInfoItem): Boolean {
        return repository.launchApp(app)
    }

    fun openAppInfo(app: AppInfoItem) {
        repository.openAppDetails(app.packageName)
    }

    fun selectAppForDetails(app: AppInfoItem?) {
        _selectedAppForDetails.value = app
    }

    fun selectAppForIconPicker(app: AppInfoItem?) {
        _selectedAppForIconPicker.value = app
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _isSettingsOpen.value = isOpen
    }

    fun updateSettings(newSettings: LauncherSettings) {
        _settings.value = newSettings
    }

    fun getCachedIcon(packageName: String) = repository.getCachedIcon(packageName)
}
