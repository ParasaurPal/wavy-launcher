package com.wave.app.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import java.util.Locale

enum class AppCategory(
    val title: String,
    val isGame: Boolean,
    val subcategory: String? = null
) {
    COMMUNICATION("Communication", false),
    SOCIAL("Social", false),
    PRODUCTIVITY("Productivity", false),
    PHOTOGRAPHY("Photography", false),
    MUSIC_AUDIO("Music & Audio", false),
    ENTERTAINMENT("Entertainment", false),
    TOOLS("Tools & Utilities", false),
    NAVIGATION("Navigation & Travel", false),
    FINANCE("Finance & Banking", false),
    SHOPPING("Shopping", false),
    NEWS("News & Magazines", false),
    HEALTH_FITNESS("Health & Fitness", false),
    EDUCATION("Education", false),
    LIFESTYLE("Lifestyle", false),
    WEATHER("Weather", false),
    SYSTEM("System & Settings", false),

    // Game Subcategories
    GAME_ACTION("Game", true, "Action"),
    GAME_PUZZLE("Game", true, "Puzzle"),
    GAME_STRATEGY("Game", true, "Strategy"),
    GAME_ARCADE("Game", true, "Arcade"),
    GAME_RPG("Game", true, "Role Playing"),
    GAME_CASUAL("Game", true, "Casual"),
    GAME_SPORTS("Game", true, "Sports"),
    GAME_SIMULATION("Game", true, "Simulation");

    val displayLabel: String
        get() = if (isGame && subcategory != null) {
            "Game: $subcategory"
        } else {
            title
        }

    val icon: ImageVector
        get() = when (this) {
            COMMUNICATION -> Icons.Filled.Call
            SOCIAL -> Icons.Filled.People
            PRODUCTIVITY -> Icons.Filled.Work
            PHOTOGRAPHY -> Icons.Filled.CameraAlt
            MUSIC_AUDIO -> Icons.Filled.MusicNote
            ENTERTAINMENT -> Icons.Filled.PlayArrow
            TOOLS -> Icons.Filled.Calculate
            NAVIGATION -> Icons.Filled.DirectionsCar
            FINANCE -> Icons.Filled.Payments
            SHOPPING -> Icons.Filled.ShoppingBag
            NEWS -> Icons.Filled.Newspaper
            HEALTH_FITNESS -> Icons.Filled.FitnessCenter
            EDUCATION -> Icons.Filled.School
            LIFESTYLE -> Icons.Filled.MenuBook
            WEATHER -> Icons.Filled.Cloud
            SYSTEM -> Icons.Filled.Settings
            // Games
            GAME_ACTION -> Icons.Filled.Gamepad
            GAME_PUZZLE -> Icons.Filled.Psychology
            GAME_STRATEGY -> Icons.Filled.Gamepad
            GAME_ARCADE -> Icons.Filled.Gamepad
            GAME_RPG -> Icons.Filled.Gamepad
            GAME_CASUAL -> Icons.Filled.Gamepad
            GAME_SPORTS -> Icons.Filled.SportsSoccer
            GAME_SIMULATION -> Icons.Filled.Gamepad
        }

    companion object {
        fun detectCategory(packageName: String, label: String): AppCategory {
            val text = (packageName + " " + label).lowercase(Locale.ROOT)

            // Game subcategories detection
            if (text.contains("game") || text.contains("puzzle") || text.contains("sudoku") ||
                text.contains("chess") || text.contains("racer") || text.contains("arcade") ||
                text.contains("rpg") || text.contains("craft") || text.contains("minecraft") ||
                text.contains("roblox") || text.contains("quest") || text.contains("clash")
            ) {
                return when {
                    text.contains("puzzle") || text.contains("sudoku") || text.contains("crossword") || text.contains("word") -> GAME_PUZZLE
                    text.contains("action") || text.contains("strike") || text.contains("shooter") || text.contains("battle") -> GAME_ACTION
                    text.contains("strategy") || text.contains("chess") || text.contains("clash") || text.contains("defense") -> GAME_STRATEGY
                    text.contains("arcade") || text.contains("runner") || text.contains("jump") || text.contains("pac") -> GAME_ARCADE
                    text.contains("rpg") || text.contains("quest") || text.contains("fantasy") -> GAME_RPG
                    text.contains("sport") || text.contains("soccer") || text.contains("football") || text.contains("golf") || text.contains("nba") -> GAME_SPORTS
                    text.contains("sim") || text.contains("tycoon") || text.contains("city") -> GAME_SIMULATION
                    else -> GAME_CASUAL
                }
            }

            return when {
                text.contains("camera") || text.contains("photo") || text.contains("gallery") || text.contains("filter") -> PHOTOGRAPHY
                text.contains("phone") || text.contains("dialer") || text.contains("call") || text.contains("sms") || text.contains("mms") || text.contains("message") || text.contains("telecom") -> COMMUNICATION
                text.contains("chat") || text.contains("whatsapp") || text.contains("telegram") || text.contains("signal") || text.contains("messenger") || text.contains("social") || text.contains("twitter") || text.contains("facebook") || text.contains("instagram") -> SOCIAL
                text.contains("music") || text.contains("audio") || text.contains("sound") || text.contains("spotify") || text.contains("podcast") || text.contains("radio") -> MUSIC_AUDIO
                text.contains("video") || text.contains("youtube") || text.contains("netflix") || text.contains("movie") || text.contains("cinema") || text.contains("tv") -> ENTERTAINMENT
                text.contains("map") || text.contains("nav") || text.contains("gps") || text.contains("waze") || text.contains("uber") || text.contains("transit") -> NAVIGATION
                text.contains("weather") || text.contains("forecast") -> WEATHER
                text.contains("calc") || text.contains("tool") || text.contains("compass") || text.contains("cleaner") || text.contains("scanner") -> TOOLS
                text.contains("bank") || text.contains("pay") || text.contains("wallet") || text.contains("money") || text.contains("finance") || text.contains("crypto") -> FINANCE
                text.contains("shop") || text.contains("store") || text.contains("market") || text.contains("amazon") || text.contains("ebay") || text.contains("cart") -> SHOPPING
                text.contains("news") || text.contains("article") || text.contains("feed") || text.contains("magazine") -> NEWS
                text.contains("fit") || text.contains("health") || text.contains("workout") || text.contains("run") || text.contains("gym") || text.contains("step") -> HEALTH_FITNESS
                text.contains("learn") || text.contains("course") || text.contains("school") || text.contains("dict") || text.contains("edu") -> EDUCATION
                text.contains("note") || text.contains("task") || text.contains("calendar") || text.contains("clock") || text.contains("doc") || text.contains("sheet") || text.contains("pdf") || text.contains("mail") || text.contains("email") -> PRODUCTIVITY
                text.contains("setting") || text.contains("config") || text.contains("system") || text.contains("bluetooth") || text.contains("wifi") || text.contains("file") || text.contains("download") -> SYSTEM
                else -> TOOLS
            }
        }
    }
}
