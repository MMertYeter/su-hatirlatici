package com.mert.sutakip.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.math.floor
import kotlin.math.sin
import kotlin.random.Random

private class DusenKalp(
    val xOrani: Float,
    val baslangicGecikmesi: Float,
    val boyut: Float,
    val hiz: Float,
    val renk: Color,
    val sallanmaFazi: Float
)

private fun rastgeleKalpler(adet: Int): List<DusenKalp> {
    val renkler = listOf(
        Color(0xFFFF6B81),
        Color(0xFFFF4D6D),
        Color(0xFFFF8FA3),
        Color(0xFFE63950),
        Color(0xFFFFA1AD)
    )
    val sonuc = ArrayList<DusenKalp>(adet)
    var i = 0
    while (i < adet) {
        sonuc.add(
            DusenKalp(
                xOrani = Random.nextFloat(),
                baslangicGecikmesi = Random.nextFloat() * 0.6f,
                boyut = 14f + Random.nextFloat() * 16f,
                hiz = 0.7f + Random.nextFloat() * 0.6f,
                renk = renkler[Random.nextInt(renkler.size)],
                sallanmaFazi = Random.nextFloat() * 6.28f
            )
        )
        i++
    }
    return sonuc
}

/**
 * Ekranın üstünden aşağı doğru düşen kalpler efekti. `visible` true olduğu sürece
 * sürekli döngüde kalpler üretir; çağıran taraf efekti kaldırmak istediğinde
 * `visible`'ı false yapar (bu composable o an ekrandan kalkar).
 */
@Composable
fun FallingHeartsOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val kalpler = remember { rastgeleKalpler(28) }

    val infiniteTransition = rememberInfiniteTransition(label = "fallingHearts")
    val ilerleme by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing)
        ),
        label = "fallProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val genislik = size.width
        val yukseklik = size.height

        for (kalp in kalpler) {
            var localProgress = (ilerleme - kalp.baslangicGecikmesi) * kalp.hiz
            localProgress -= floor(localProgress)

            val y = -kalp.boyut + localProgress * (yukseklik + kalp.boyut * 2f)
            val sallanma = sin(localProgress * 10f + kalp.sallanmaFazi) * 18f
            val x = kalp.xOrani * genislik + sallanma

            var alpha = 1f
            if (localProgress < 0.08f) {
                alpha = localProgress / 0.08f
            } else if (localProgress > 0.85f) {
                alpha = (1f - localProgress) / 0.15f
            }
            if (alpha < 0f) alpha = 0f
            if (alpha > 1f) alpha = 1f

            val yariBoyut = kalp.boyut / 2f
            val yol = Path()
            yol.moveTo(x, y + yariBoyut)
            yol.cubicTo(
                x - kalp.boyut, y - yariBoyut * 0.3f,
                x - yariBoyut * 0.6f, y - kalp.boyut,
                x, y - yariBoyut * 0.3f
            )
            yol.cubicTo(
                x + yariBoyut * 0.6f, y - kalp.boyut,
                x + kalp.boyut, y - yariBoyut * 0.3f,
                x, y + yariBoyut
            )
            yol.close()

            drawPath(path = yol, color = kalp.renk.copy(alpha = alpha))
        }
    }
}
