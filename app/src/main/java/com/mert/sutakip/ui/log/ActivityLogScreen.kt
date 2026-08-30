package com.mert.sutakip.ui.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mert.sutakip.data.local.entity.IcecekTuru
import com.mert.sutakip.ui.theme.CoffeeFillColorDeep
import com.mert.sutakip.ui.theme.LogAzaltmaKirmizi
import com.mert.sutakip.ui.theme.LogEklemeYesil
import com.mert.sutakip.ui.theme.WaterFillColorDeep

/**
 * Kullanıcının gün içinde yaptığı tüm ekleme/azaltma işlemlerinin saat sırasıyla
 * (en yeni en üstte) listelendiği basit log ekranı. Ayarlar'dan erişilir.
 *
 * Renk kuralı:
 * - Tür (su/kahve) miktarı ve adı her zaman kendi rengiyle gösterilir: su mavi, kahve kahverengi.
 * - "eklendi" ibaresi yeşil, "çıkarıldı" ibaresi kırmızıdır.
 */
@Composable
fun ActivityLogScreen(
    viewModel: ActivityLogViewModel = viewModel()
) {
    val aktiviteler by viewModel.gununAktiviteleri.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Bugünkü Hareketler",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (aktiviteler.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Bugün henüz bir hareket yok.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(aktiviteler, key = { it.id }) { aktivite ->
                    AktiviteSatiriKarti(aktivite)
                }
            }
        }
    }
}

@Composable
private fun AktiviteSatiriKarti(aktivite: AktiviteSatiri) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = aktivite.saat,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(56.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = aktiviteMetni(aktivite),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * "200 ml su eklendi" gibi bir metni, tür kısmı (miktar + tür adı) kendi rengiyle
 * (su mavi, kahve kahverengi), "eklendi"/"çıkarıldı" kısmı da kendi rengiyle
 * (yeşil/kırmızı) olacak şekilde AnnotatedString olarak üretir.
 */
private fun aktiviteMetni(aktivite: AktiviteSatiri) = buildAnnotatedString {
    val turRengi = if (aktivite.tur == IcecekTuru.KAHVE) CoffeeFillColorDeep else WaterFillColorDeep
    val turAdi = if (aktivite.tur == IcecekTuru.KAHVE) "kahve" else "su"
    val eylemMetni = if (aktivite.azaltmaMi) "çıkarıldı" else "eklendi"
    val eylemRengi = if (aktivite.azaltmaMi) LogAzaltmaKirmizi else LogEklemeYesil

    withStyle(SpanStyle(color = turRengi, fontWeight = FontWeight.SemiBold)) {
        append("${aktivite.miktarMl} ml $turAdi")
    }
    append(" ")
    withStyle(SpanStyle(color = eylemRengi, fontWeight = FontWeight.SemiBold)) {
        append(eylemMetni)
    }
}
