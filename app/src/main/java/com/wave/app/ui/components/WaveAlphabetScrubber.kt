package com.wave.app.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wave.app.data.IndexInteractionMode
import com.wave.app.data.ScrubberSide
import com.wave.app.data.WaveIntensity
import com.wave.app.ui.theme.WaveGlowEnd
import com.wave.app.ui.theme.WaveGlowStart
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt

@Composable
fun WaveAlphabetScrubber(
    availableLetters: Set<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    intensity: WaveIntensity,
    side: ScrubberSide,
    interactionMode: IndexInteractionMode,
    hapticsEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val alphabet = remember { ('A'..'Z').toList() + listOf('#') }
    val view = LocalView.current
    val density = LocalDensity.current

    var isTouching by remember { mutableStateOf(false) }
    var touchY by remember { mutableFloatStateOf(0f) }
    var currentHoverLetter by remember { mutableStateOf<Char?>(null) }

    // Spring animation for smooth return when finger lifts
    val touchFactor = remember { Animatable(0f) }

    LaunchedEffect(isTouching) {
        touchFactor.animateTo(
            targetValue = if (isTouching) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxHeight()
            .width(54.dp)
            .testTag("wave_alphabet_scrubber")
            .pointerInput(side, intensity, interactionMode) {
                detectTapGestures(
                    onPress = { offset ->
                        isTouching = true
                        touchY = offset.y
                        val totalHeight = size.height.toFloat()
                        val letterSlot = totalHeight / alphabet.size
                        val index = (offset.y / letterSlot).toInt().coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != currentHoverLetter) {
                            currentHoverLetter = letter
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onLetterSelected(letter)
                        }
                        tryAwaitRelease()
                        isTouching = false
                        currentHoverLetter = null
                    }
                )
            }
            .pointerInput(side, intensity, interactionMode) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isTouching = true
                        touchY = offset.y
                        val totalHeight = size.height.toFloat()
                        val letterSlot = totalHeight / alphabet.size
                        val index = (offset.y / letterSlot).toInt().coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != currentHoverLetter) {
                            currentHoverLetter = letter
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            }
                            onLetterSelected(letter)
                        }
                    },
                    onDragEnd = {
                        isTouching = false
                        currentHoverLetter = null
                    },
                    onDragCancel = {
                        isTouching = false
                        currentHoverLetter = null
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        touchY = change.position.y
                        val totalHeight = size.height.toFloat()
                        val letterSlot = totalHeight / alphabet.size
                        val index = (touchY / letterSlot).toInt().coerceIn(0, alphabet.lastIndex)
                        val letter = alphabet[index]
                        if (letter != currentHoverLetter) {
                            currentHoverLetter = letter
                            if (hapticsEnabled) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                            }
                            onLetterSelected(letter)
                        }
                    }
                )
            }
    ) {
        val totalHeightPx = with(density) { maxHeight.toPx() }
        val letterSlotPx = totalHeightPx / alphabet.size
        val waveRadiusPx = with(density) { intensity.radiusFactor.dp.toPx() }
        val maxDispPx = with(density) { intensity.maxDisplacementFactor.dp.toPx() }

        // Subtle background rail
        Box(
            modifier = Modifier
                .align(if (side == ScrubberSide.RIGHT) Alignment.CenterEnd else Alignment.CenterStart)
                .width(22.dp)
                .fillMaxHeight(0.94f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(11.dp)
                )
        )

        // Fluid Wave Spine Curve drawn on Canvas when active
        if (touchFactor.value > 0.05f) {
            val railX = if (side == ScrubberSide.RIGHT) {
                with(density) { 54.dp.toPx() - 11.dp.toPx() }
            } else {
                with(density) { 11.dp.toPx() }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val wavePath = Path()
                var first = true

                for (i in alphabet.indices) {
                    val y = i * letterSlotPx + (letterSlotPx / 2f)
                    val dist = abs(y - touchY)
                    val factor = if (dist < waveRadiusPx) {
                        val ratio = dist / waveRadiusPx
                        ((cos(ratio * Math.PI.toFloat()) + 1f) / 2f) * touchFactor.value
                    } else {
                        0f
                    }
                    val disp = maxDispPx * factor
                    val x = if (side == ScrubberSide.RIGHT) railX - disp else railX + disp

                    if (first) {
                        wavePath.moveTo(x, y)
                        first = false
                    } else {
                        wavePath.lineTo(x, y)
                    }
                }

                drawPath(
                    path = wavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            WaveGlowStart.copy(alpha = 0.2f),
                            WaveGlowStart.copy(alpha = 0.85f * touchFactor.value),
                            WaveGlowEnd.copy(alpha = 0.85f * touchFactor.value),
                            WaveGlowEnd.copy(alpha = 0.2f)
                        )
                    ),
                    style = Stroke(
                        width = with(density) { (2.5f * touchFactor.value).dp.toPx() },
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Render alphabet letters with individual wave displacement and scaling
        alphabet.forEachIndexed { index, letter ->
            val letterCenterY = index * letterSlotPx + (letterSlotPx / 2f)
            val dist = abs(letterCenterY - touchY)

            val inWave = dist < waveRadiusPx
            val waveProfile = if (inWave) {
                val ratio = dist / waveRadiusPx
                (cos(ratio * Math.PI.toFloat()) + 1f) / 2f
            } else {
                0f
            }

            val currentFactor = waveProfile * touchFactor.value
            val displacementPx = maxDispPx * currentFactor
            val xDisp = if (side == ScrubberSide.RIGHT) -displacementPx else displacementPx
            val scale = 1f + (currentFactor * 0.95f)

            val isAvailable = availableLetters.contains(letter)
            val isCurrentHover = currentHoverLetter == letter
            val isSelected = selectedLetter == letter

            val letterColor = when {
                isCurrentHover -> WaveGlowStart
                isSelected -> MaterialTheme.colorScheme.primary
                isAvailable -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            }

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = xDisp.roundToInt(),
                            y = (index * letterSlotPx).roundToInt()
                        )
                    }
                    .size(
                        width = 46.dp,
                        height = with(density) { letterSlotPx.toDp() }
                    )
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                if (currentFactor > 0.35f) {
                    // Glow badge behind waving letter
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        WaveGlowStart.copy(alpha = currentFactor * 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = letter.toString(),
                    fontSize = if (scale > 1.3f) 14.sp else 10.sp,
                    fontWeight = if (isCurrentHover || isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = letterColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Large floating magnifying preview badge when touching/dragging
        if (isTouching && currentHoverLetter != null) {
            val badgeY = (touchY - with(density) { 34.dp.toPx() })
                .coerceIn(0f, totalHeightPx - with(density) { 68.dp.toPx() })

            val badgeXOffset = if (side == ScrubberSide.RIGHT) {
                -with(density) { (intensity.maxDisplacementFactor + 68).dp.toPx() }
            } else {
                with(density) { (intensity.maxDisplacementFactor + 68).dp.toPx() }
            }

            Surface(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = badgeXOffset.roundToInt(),
                            y = badgeY.roundToInt()
                        )
                    }
                    .size(66.dp)
                    .shadow(16.dp, CircleShape)
                    .testTag("scrubber_magnify_badge"),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp,
                shadowElevation = 10.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    WaveGlowStart.copy(alpha = 0.22f),
                                    WaveGlowEnd.copy(alpha = 0.22f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentHoverLetter.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
