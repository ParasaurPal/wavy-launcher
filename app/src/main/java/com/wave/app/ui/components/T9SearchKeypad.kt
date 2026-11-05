package com.wave.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wave.app.data.AppInfoItem

data class PhoneSearchResult(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconBgColor: Color,
    val onClick: (Context) -> Unit
)

object T9SearchHelper {
    private val charToT9 = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9'
    )

    fun toT9Digits(text: String): String {
        return text.lowercase().map { charToT9[it] ?: it }.joinToString("")
    }

    fun matchesApp(app: AppInfoItem, t9Digits: String): Boolean {
        if (t9Digits.isBlank()) return true
        val labelDigits = toT9Digits(app.label)
        val packageDigits = toT9Digits(app.packageName)
        val initials = app.label.split(" ", "-", "_")
            .mapNotNull { it.firstOrNull()?.lowercaseChar() }
            .mapNotNull { charToT9[it] }
            .joinToString("")

        return labelDigits.contains(t9Digits) ||
               packageDigits.contains(t9Digits) ||
               initials.contains(t9Digits) ||
               app.label.lowercase().contains(t9Digits.lowercase())
    }

    fun getPhoneActions(context: Context, digits: String): List<PhoneSearchResult> {
        if (digits.isBlank()) return emptyList()
        val results = mutableListOf<PhoneSearchResult>()

        // 1. Dial phone number action
        results.add(
            PhoneSearchResult(
                title = "Dial $digits",
                subtitle = "Place call via device dialer",
                icon = Icons.Filled.Call,
                iconBgColor = Color(0xFF10B981),
                onClick = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ctx.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        )

        // 2. Send SMS action
        results.add(
            PhoneSearchResult(
                title = "Message $digits",
                subtitle = "Send text message",
                icon = Icons.Filled.Message,
                iconBgColor = Color(0xFF3B82F6),
                onClick = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$digits")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ctx.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        )

        // 3. Search web action
        results.add(
            PhoneSearchResult(
                title = "Search Web for \"$digits\"",
                subtitle = "Google Search",
                icon = Icons.Filled.Language,
                iconBgColor = Color(0xFF8B5CF6),
                onClick = { ctx ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$digits")).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ctx.startActivity(intent)
                    } catch (_: Exception) {}
                }
            )
        )

        // 4. System Settings Shortcuts
        val queryT9 = digits
        if ("9434".startsWith(queryT9) || queryT9.startsWith("9434")) { // WIFI
            results.add(
                PhoneSearchResult(
                    title = "Wi-Fi Settings",
                    subtitle = "System Network",
                    icon = Icons.Filled.Settings,
                    iconBgColor = Color(0xFF64748B),
                    onClick = { ctx ->
                        try {
                            ctx.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
            )
        }

        if ("2583".startsWith(queryT9) || queryT9.startsWith("2583")) { // BLUE
            results.add(
                PhoneSearchResult(
                    title = "Bluetooth Settings",
                    subtitle = "Connected Devices",
                    icon = Icons.Filled.Settings,
                    iconBgColor = Color(0xFF64748B),
                    onClick = { ctx ->
                        try {
                            ctx.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } catch (_: Exception) {}
                    }
                )
            )
        }

        return results
    }
}

@Composable
fun T9SearchKeypad(
    queryDigits: String,
    onDigitsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    val keys = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("C" to "CLR", "0" to "+", "⌫" to "DEL")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("t9_keypad"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Digit display preview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (queryDigits.isEmpty()) "Enter digits to search apps & phone…" else queryDigits,
                fontSize = if (queryDigits.isEmpty()) 13.sp else 22.sp,
                fontWeight = if (queryDigits.isEmpty()) FontWeight.Normal else FontWeight.Black,
                color = if (queryDigits.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("t9_query_display")
            )

            if (queryDigits.isNotEmpty()) {
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onDigitsChange("")
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear digits",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 4x3 Keypad Matrix
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowKeys.forEach { (mainText, subText) ->
                    T9KeyButton(
                        mainText = mainText,
                        subText = subText,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            when (mainText) {
                                "C" -> onDigitsChange("")
                                "⌫" -> {
                                    if (queryDigits.isNotEmpty()) {
                                        onDigitsChange(queryDigits.dropLast(1))
                                    }
                                }
                                else -> onDigitsChange(queryDigits + mainText)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun T9KeyButton(
    mainText: String,
    subText: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("t9_key_$mainText"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = mainText,
                fontSize = if (mainText.length > 1) 16.sp else 19.sp,
                fontWeight = FontWeight.Bold,
                color = if (mainText in listOf("C", "⌫")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            if (subText.isNotEmpty()) {
                Text(
                    text = subText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PhoneActionItem(
    result: PhoneSearchResult,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { result.onClick(context) }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("phone_action_${result.title}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(result.iconBgColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = result.icon,
                contentDescription = null,
                tint = result.iconBgColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
