package com.mert.sutakip.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.sin
import kotlin.random.Random

private data class DusenKalp(
    val xOrani: Float,      // 0f..1f, ekran genişliğine göre yatay konum
    val baslangicGecikmesi: Float, // 0f..1f, düşüşün ne kadar geç başlayacağı
    val boyut: Float,       // dp cinsinden kalp boyutu
    val hiz: Float,         // görece düşüş hızı çarpanı
    val renk: Color,
    val sallanmaFazi: Float // yana sallanma animasyonu için faz kayması
)

/**
 * Ekranın üstünden aşağı doğru düşen kalpler efekti. `visible` true olduğu sürece
 * sürekli döngüde kalpler üretir; çağıran taraf (örn. LaunchedEffect ile 2-3 saniye
 * sonra) `visible`'ı false yaparak efekti durdurur/kaldırır.
 */
@Composable
fun FallingHeartsOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val kalpRenkleri = listOf(
        Color(0xFFFF6B81),
        Color(0xFFFF4D6D),
        Color(0xFFFF8FA3),
        Color(0xFFE63950),
        Color(0xFFFFA1AD)
    )

    val kalpler = remember {
        List(28) {
            DusenKalp(
                xOrani = Random.nextFloat(),
                baslangicGecikmesi = Random.nextFloat() * 0.6f,
                boyut = Random.nextInt(14, 30).toFloat(),
                hiz = 0.7f + Random.nextFloat() * 0.6f,
                renk = kalpRenkleri.random(),
                sallanmaFazi = Random.nextFloat() * 6.28f
            )
        }
    }

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

        kalpler.forEach { kalp ->
            // Her kalp kendi gecikmesiyle başlar, döngü boyunca yukarıdan aşağı iner.
            var localProgress = (ilerleme - kalp.baslangicGecikmesi) * kalp.hiz
            localProgress = localProgress - kotlin.math.floor(localProgress) // 0f..1f döngüsü

            val y = -kalp.boyut + localProgress * (yukseklik + kalp.boyut * 2)
            val sallanma = sin(localProgress * 10f + kalp.sallanmaFazi) * 18f
            val x = kalp.xOrani * genislik + sallanma

            val alpha = when {
                localProgress < 0.08f -> localProgress / 0.08f
                localProgress > 0.85f -> (1f - localProgress) / 0.15f
                else -> 1f
            }.coerceIn(0f, 1f)

            rotate(degrees = sallanma, pivot = Offset(x, y)) {
                drawKalp(
                    merkez = Offset(x, y),
                    boyut = kalp.boyut,
                    renk = kalp.renk.copy(alpha = kalp.renk.alpha * alpha)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawKalp(
    merkez: Offset,
    boyut: Float,
    renk: Color
) {
    val yariBoyut = boyut / 2f
    val yol = Path().apply {
        moveTo(merkez.x, merkez.y + yariBoyut)
        cubicTo(
            merkez.x - boyut, merkez.y - yariBoyut * 0.3f,
            merkez.x - yariBoyut * 0.6f, merkez.y - boyut,
            merkez.x, merkez.y - yariBoyut * 0.3f
        )
        cubicTo(
            merkez.x + yariBoyut * 0.6f, merkez.y - boyut,
            merkez.x + boyut, merkez.y - yariBoyut * 0.3f,
            merkez.x, merkez.y + yariBoyut
        )
        close()
    }
    drawPath(path = yol, color = renk)
}
