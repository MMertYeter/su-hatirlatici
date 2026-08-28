package com.mert.sutakip.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mert.sutakip.data.local.entity.Badge
import com.mert.sutakip.ui.theme.StreakFlame
import com.mert.sutakip.ui.theme.WaterFillColor
import com.mert.sutakip.ui.theme.WaterFillColorDeep
import com.mert.sutakip.ui.theme.WaterGlassEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "İstatistikler",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.gorunum == StatsGorunum.HAFTALIK,
                onClick = { viewModel.gorunumDegistir(StatsGorunum.HAFTALIK) },
                shape = SegmentedButtonDefaults.itemShape(0, 2)
            ) { Text("Haftalık") }
            SegmentedButton(
                selected = state.gorunum == StatsGorunum.AYLIK,
                onClick = { viewModel.gorunumDegistir(StatsGorunum.AYLIK) },
                shape = SegmentedButtonDefaults.itemShape(1, 2)
            ) { Text("Aylık") }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (state.gunler.all { it.toplamMl == 0 }) {
            BosDurum()
        } else {
            BarChart(gunler = state.gunler, modifier = Modifier.fillMaxWidth().height(200.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OzetKart("Günlük Ortalama", "${state.gunlukOrtalamaMl} ml", Modifier.weight(1f))
            OzetKart("En İyi Gün", "${state.enIyiGunMl} ml", Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OzetKart("Hedef Tutturma", "%${state.hedefTutturmaOrani}", Modifier.weight(1f))
            SeriKart(seri = state.mevcutSeri, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Rozetler",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(12.dp))
        RozetGalerisi(rozetler = state.rozetler)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BarChart(gunler: List<com.mert.sutakip.ui.stats.GunlukVeriNoktasi>, modifier: Modifier = Modifier) {
    val maxMl = (gunler.maxOfOrNull { maxOf(it.toplamMl, it.hedefMl) } ?: 1).coerceAtLeast(1)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        gunler.forEach { gun ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                val oran = if (maxMl > 0) gun.toplamMl.toFloat() / maxMl.toFloat() else 0f
                val hedefTutturuldu = gun.hedefMl > 0 && gun.toplamMl >= gun.hedefMl

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(14.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(oran.coerceIn(0.02f, 1f))
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        if (hedefTutturuldu) WaterFillColor else WaterFillColorDeep,
                                        WaterFillColorDeep
                                    )
                                ),
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                            )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = gun.etiket,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun OzetKart(baslik: String, deger: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(baslik, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(deger, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SeriKart(seri: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Seri", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("🔥 $seri gün", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StreakFlame)
        }
    }
}

@Composable
private fun RozetGalerisi(rozetler: List<Badge>) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        items(rozetler) { rozet ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(84.dp)
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .background(
                            color = if (rozet.kazanildiMi) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏅",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (rozet.kazanildiMi) Color.Unspecified else Color.Gray.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = rozet.ad,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (rozet.kazanildiMi) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BosDurum() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💧", style = MaterialTheme.typography.displayLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Henüz veri yok",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Su içmeye başladıkça istatistiklerin burada görünecek",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
