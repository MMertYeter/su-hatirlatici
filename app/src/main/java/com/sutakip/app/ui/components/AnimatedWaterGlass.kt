package com.sutakip.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.sutakip.app.ui.theme.CoffeeFillColor
import com.sutakip.app.ui.theme.CoffeeFillColorDeep
import com.sutakip.app.ui.theme.WaterFillColor
import com.sutakip.app.ui.theme.WaterFillColorDeep
import com.sutakip.app.ui.theme.WaterGlassEmpty
import kotlin.math.sin

/**
 * Doluluk oranı 0f..1f arasında bir bardak çizer. Su ve kahve ayrı katmanlar halinde
 * gösterilir: kahve dipte (kahverengi), su üstte (mavi) — ekleme sırasına bakılmaksızın
 * sabit bir görsel düzen kullanılır, böylece bardaklar arasında tutarlı görünür.
 * - Su seviyesi alttan yukarı animasyonla yükselir (spring).
 * - Üstte sürekli hafif dalga (wave) efekti oynar.
 * - Doluluk %100'e ulaştığında zıplama (bounce) ve parıltı efekti tetiklenir.
 */
@Composable
fun AnimatedWaterGlass(
    suOrani: Float = 0f,   // 0f..1f
    kahveOrani: Float = 0f, // 0f..1f, suOrani + kahveOrani toplamı 1f'i geçmemeli
    modifier: Modifier = Modifier,
    justCompleted: Boolean = false
) {
    val hedefToplamDoluluk = (suOrani + kahveOrani).coerceIn(0f, 1f)
    val hedefKahveOrani = kahveOrani.coerceIn(0f, 1f)

    val animatedFill by animateFloatAsState(
        targetValue = hedefToplamDoluluk,
        animationSpec = spring(
            dampingRatio = if (justCompleted) Spring.DampingRatioMediumBouncy else Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "glassFill"
    )
    val animatedKahveOrani by animateFloatAsState(
        targetValue = hedefKahveOrani,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "coffeeFill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "waveTransition")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // justCompleted true olduğunda parıltıyı anında 1f'e sıçratıp hemen ardından
    // 0f'e söndürüyoruz (Animatable ile, ki her iki adım da gerçekten çalışsın).
    // Önceki halde hedef değer justCompleted ile ters bağlanmıştı: parıltı hiç
    // görünmeden söner, sonra justCompleted false'a dönünce kalıcı olarak 1f'de
    // takılı kalıyordu — ekranda hep duran gri/beyaz daire buradan geliyordu.
    val sparkle = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(justCompleted) {
        if (justCompleted) {
            sparkle.snapTo(1f)
            sparkle.animateTo(0f, animationSpec = tween(900))
        }
    }
    val animatedSparkle = sparkle.value

    val emptyColor = WaterGlassEmpty

    Canvas(
        modifier = modifier
            .aspectRatio(0.72f)
    ) {
        val w = size.width
        val h = size.height

        // Bardak gövdesi: hafif trapez (üstü altından geniş)
        val topWidth = w
        val bottomWidth = w * 0.82f
        val glassPath = Path().apply {
            moveTo((w - topWidth) / 2f, 0f)
            lineTo((w + topWidth) / 2f, 0f)
            lineTo((w + bottomWidth) / 2f, h)
            lineTo((w - bottomWidth) / 2f, h)
            close()
        }

        clipPath(glassPath) {
            // Boş bardak arka planı
            drawRect(color = emptyColor)

            if (animatedFill > 0f) {
                val waterTopY = h * (1f - animatedFill)
                // Kahve her zaman dipte, su onun üstünde durur. Kahve yüksekliği,
                // toplam bardak yüksekliğine göre kahve oranı kadardır.
                val kahveYuksekligi = h * animatedKahveOrani
                val kahveTopY = (h - kahveYuksekligi).coerceIn(waterTopY, h)

                val waveHeight = 4.dp.toPx()

                // Su katmanı (üstte, dalgalı üst sınır)
                val suPath = Path().apply {
                    moveTo(0f, kahveTopY)
                    lineTo(0f, waterTopY)
                    var x = 0f
                    val step = 4f
                    while (x <= w) {
                        val y = waterTopY + sin((x / w) * 2f * Math.PI.toFloat() + wavePhase) * waveHeight
                        lineTo(x, y)
                        x += step
                    }
                    lineTo(w, waterTopY)
                    lineTo(w, kahveTopY)
                    close()
                }
                drawPath(
                    path = suPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(WaterFillColor, WaterFillColorDeep),
                        startY = waterTopY,
                        endY = kahveTopY.coerceAtLeast(waterTopY + 1f)
                    )
                )

                // Kahve katmanı (dipte, düz üst sınır)
                if (kahveYuksekligi > 0.5f) {
                    drawPath(
                        path = Path().apply {
                            moveTo(0f, h)
                            lineTo(0f, kahveTopY)
                            lineTo(w, kahveTopY)
                            lineTo(w, h)
                            close()
                        },
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(CoffeeFillColor, CoffeeFillColorDeep),
                            startY = kahveTopY,
                            endY = h
                        )
                    )
                }
            }
        }

        // Bardak dış çizgisi
        drawPath(
            path = glassPath,
            color = WaterFillColorDeep.copy(alpha = 0.5f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )

        // Tamamlanma parıltısı: bardağın üstünde kısa süreli parlayan halka
        if (animatedSparkle > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = animatedSparkle * 0.8f),
                radius = w * 0.5f * (1f + (1f - animatedSparkle) * 0.6f),
                center = Offset(w / 2f, h * 0.15f)
            )
        }
    }
}
