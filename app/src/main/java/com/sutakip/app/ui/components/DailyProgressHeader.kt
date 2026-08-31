package com.sutakip.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sutakip.app.ui.theme.WaterFillColor
import com.sutakip.app.ui.theme.WaterFillColorDeep
import com.sutakip.app.ui.theme.WaterGlassEmpty

@Composable
fun DailyProgressHeader(
    toplamMl: Int,
    hedefMl: Int,
    modifier: Modifier = Modifier
) {
    val oran = if (hedefMl > 0) (toplamMl.toFloat() / hedefMl.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedOran by animateFloatAsState(
        targetValue = oran,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow),
        label = "progressRing"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
            .aspectRatio(1.6f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1.6f)) {
            val strokeWidth = 22.dp.toPx()
            val diameter = kotlin.math.min(size.width, size.height * 1.6f) - strokeWidth
            val topLeft = androidx.compose.ui.geometry.Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter / 1.6f) / 2f
            )

            drawArc(
                color = WaterGlassEmpty,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                brush = Brush.horizontalGradient(listOf(WaterFillColorDeep, WaterFillColor)),
                startAngle = 180f,
                sweepAngle = 180f * animatedOran,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier.padding(top = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$toplamMl / $hedefMl ml",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(animatedOran * 100).toInt()}% tamamlandı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
