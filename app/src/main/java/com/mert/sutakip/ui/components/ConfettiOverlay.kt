package com.mert.sutakip.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.mert.sutakip.ui.theme.CelebrationGold
import com.mert.sutakip.ui.theme.StreakFlame
import com.mert.sutakip.ui.theme.WaterFillColor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Confetti(
    val startX: Float,      // 0f..1f, ekran genişliğine oran
    val angleDeg: Float,
    val speed: Float,       // 0f..1f, düşme hızı çarpanı
    val color: Color,
    val size: Float,        // px
    val rotationSpeed: Float
)

/**
 * Hedef tamamlandığında tetiklenen konfeti benzeri parçacık efekti.
 * `visible` true olduğunda parçacıklar yukarıdan aşağı, hafif sallanarak düşer.
 */
@Composable
fun ConfettiOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val palette = listOf(WaterFillColor, CelebrationGold, StreakFlame, Color(0xFF81D4FA), Color(0xFFFFF176))
        List(40) {
            Confetti(
                startX = Random.nextFloat(),
                angleDeg = Random.nextFloat() * 360f,
                speed = 0.6f + Random.nextFloat() * 0.8f,
                color = palette.random(),
                size = 6f + Random.nextFloat() * 8f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f
            )
        }
    }

    var play by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (play) 1f else 0f,
        animationSpec = tween(durationMillis = 1600, easing = LinearEasing),
        label = "confettiProgress"
    )

    androidx.compose.runtime.LaunchedEffect(visible) {
        if (visible) {
            play = false
            play = true
        }
    }

    if (progress in 0f..1f && (visible || progress > 0f)) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            particles.forEach { p ->
                val fallY = h * progress * p.speed
                val swayX = sin(progress * 6f + p.angleDeg) * 20f
                val cx = w * p.startX + swayX
                val cy = fallY - h * 0.1f
                if (cy in -20f..h + 20f) {
                    rotate(degrees = p.rotationSpeed * progress, pivot = Offset(cx, cy)) {
                        drawRect(
                            color = p.color.copy(alpha = (1f - progress * 0.7f).coerceIn(0f, 1f)),
                            topLeft = Offset(cx - p.size / 2f, cy - p.size / 2f),
                            size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.5f)
                        )
                    }
                }
            }
        }
    }
}
