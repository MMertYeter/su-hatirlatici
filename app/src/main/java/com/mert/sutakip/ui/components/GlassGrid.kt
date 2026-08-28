package com.mert.sutakip.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mert.sutakip.ui.home.BardakDolumu
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Bölüm 3: dinamik bardak grid'i. Bardak sayısı = hedef / 200ml (yukarı yuvarlanır).
 * Bardaklar soldan sağa / satır satır sırayla dolar; son bardak kısmi kapasiteyle biter.
 * Kahve eklenen kısımlar kahverengi, su eklenen kısımlar mavi görünür.
 *
 * Kaydırma GEREKTİRMEZ: mevcut genişliğe göre sütun sayısı otomatik hesaplanır ve
 * bardak boyutu, tüm bardaklar tek ekranda (ana ekranın dış scroll'u içinde,
 * kendi başına ayrı bir scrollable olmadan) görünecek şekilde küçültülüp büyütülür.
 *
 * @param hedefMl günlük hedef
 * @param bardakDolumlari her bardağın su/kahve oranını içeren liste (index sırasıyla)
 * @param bardakKapasitesiMl tek bardağın kapasitesi (sabit 200ml)
 * @param sonDolanBardakIndex az önce tamamlanan bardağın indexi (bounce/sparkle tetiklemek için), yoksa -1
 */
@Composable
fun GlassGrid(
    hedefMl: Int,
    bardakDolumlari: List<BardakDolumu>,
    modifier: Modifier = Modifier,
    bardakKapasitesiMl: Int = 200,
    sonDolanBardakIndex: Int = -1
) {
    val bardakSayisi = ceil(hedefMl.toFloat() / bardakKapasitesiMl).toInt().coerceAtLeast(1)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        val minGlassWidth = 32.dp
        val maxGlassWidth = 64.dp
        val spacing = 8.dp
        val maxSatirSayisi = 6 // çok fazla bardak varsa satır sayısını sınırlı tutup sütuna yayarız

        // Mevcut genişliğe göre bir satıra en fazla kaç bardak sığar.
        val genislikteSigan = max(
            1,
            ((maxWidth + spacing) / (minGlassWidth + spacing)).toInt()
        )
        // Satır sayısını maxSatirSayisi ile sınırlamak için gereken minimum sütun sayısı.
        val yukseklikIcinGerekliMin = ceil(bardakSayisi.toFloat() / maxSatirSayisi).toInt().coerceAtLeast(1)

        // İki kısıtı birden sağlayan sütun sayısı: genişliğe sığmalı VE satır sayısını aşırı artırmamalı.
        val columns = bardakSayisi
            .coerceAtMost(genislikteSigan)
            .coerceAtLeast(min(yukseklikIcinGerekliMin, genislikteSigan))
            .coerceAtLeast(1)

        // Sütun sayısına göre gerçek bardak genişliğini hesapla (boşlukları çıkararak),
        // tek satıra sığacak kadar bardak varsa gereksiz büyümesini de sınırla.
        val totalSpacing = spacing * (columns - 1)
        val rawGlassWidth = (maxWidth - totalSpacing) / columns
        val glassWidth = rawGlassWidth.coerceIn(minGlassWidth, maxGlassWidth)

        val rows = ceil(bardakSayisi.toFloat() / columns).toInt()

        Column(
            verticalArrangement = Arrangement.spacedBy(spacing),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(rows) { rowIndex ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rowStart = rowIndex * columns
                    val rowEnd = min(rowStart + columns, bardakSayisi)
                    for (index in rowStart until rowEnd) {
                        val dolum = bardakDolumlari.getOrNull(index) ?: BardakDolumu()

                        AnimatedWaterGlass(
                            suOrani = dolum.suOrani,
                            kahveOrani = dolum.kahveOrani,
                            justCompleted = index == sonDolanBardakIndex,
                            modifier = Modifier.width(glassWidth)
                        )
                    }
                }
            }
        }
    }
}
