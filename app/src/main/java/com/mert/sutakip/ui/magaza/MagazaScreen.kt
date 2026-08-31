package com.mert.sutakip.ui.magaza

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mert.sutakip.data.local.entity.EnvanterOgesi
import com.mert.sutakip.data.store.MagazaUrunu
import com.mert.sutakip.util.SifreKontrol
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Mağaza + Envanter ekranı. Üstte toplam puan, ortada satın alınabilir ürünler,
 * altta envanter (talep edilmiş ama henüz "kaldırılmamış" ürünler). Envanterden
 * bir ürünü kaldırmak (gerçekte teslim edildiğinde kayıttan silmek) özel şifre
 * gerektirir.
 */
@Composable
fun MagazaScreen(
    viewModel: MagazaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var satinAlmaOnayUrunu by remember { mutableStateOf<MagazaUrunu?>(null) }
    var sonucMesaji by remember { mutableStateOf<String?>(null) }
    var kaldirmaHedefi by remember { mutableStateOf<EnvanterOgesi?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Mağaza",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Puanın", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.toplamPuan} 🪙",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Ürünler", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        viewModel.urunler.forEach { urun ->
            val yeterliPuan = state.toplamPuan >= urun.puanMaliyeti
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(urun.emoji, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(urun.ad, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                urun.aciklama,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { satinAlmaOnayUrunu = urun },
                        enabled = yeterliPuan,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (yeterliPuan) "${urun.puanMaliyeti} puana talep et" else "${urun.puanMaliyeti} puan gerekiyor")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Envanterim", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))

        if (state.envanter.isEmpty()) {
            Text(
                "Henüz talep ettiğin bir ürün yok.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val tarihFormat = remember { SimpleDateFormat("d MMM, HH:mm", Locale("tr")) }
            state.envanter.forEach { oge ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(oge.urunEmojiSnapshot, style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(oge.urunAdiSnapshot, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                tarihFormat.format(Date(oge.satinAlmaTarihiEpochMs)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = { kaldirmaHedefi = oge }) {
                            Text("Kaldır")
                        }
                    }
                }
            }
        }
    }

    // Satın alma onayı
    satinAlmaOnayUrunu?.let { urun ->
        AlertDialog(
            onDismissRequest = { satinAlmaOnayUrunu = null },
            title = { Text("Talep edilsin mi?") },
            text = { Text("\"${urun.ad}\" için ${urun.puanMaliyeti} puan harcanacak.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.urunTalepEt(urun.id) { sonuc ->
                        sonucMesaji = when (sonuc) {
                            is SatinAlmaSonucu.Basarili -> "${sonuc.urunAdi} envanterine eklendi 🎉"
                            SatinAlmaSonucu.YetersizPuan -> "Yetersiz puan"
                        }
                    }
                    satinAlmaOnayUrunu = null
                }) { Text("Talep Et") }
            },
            dismissButton = {
                TextButton(onClick = { satinAlmaOnayUrunu = null }) { Text("Vazgeç") }
            }
        )
    }

    // Envanterden kaldırma: özel şifre gerektirir
    kaldirmaHedefi?.let { oge ->
        SifreliKaldirmaDialog(
            urunAdi = oge.urunAdiSnapshot,
            onDismiss = { kaldirmaHedefi = null },
            onDogruSifre = {
                viewModel.envantedenKaldir(oge)
                kaldirmaHedefi = null
            }
        )
    }

    // Kısa sonuç mesajı: birkaç saniye sonra kendiliğinden kaybolur
    sonucMesaji?.let { mesaj ->
        LaunchedEffect(mesaj) {
            kotlinx.coroutines.delay(2500)
            sonucMesaji = null
        }
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                mesaj,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun SifreliKaldirmaDialog(
    urunAdi: String,
    onDismiss: () -> Unit,
    onDogruSifre: () -> Unit
) {
    var sifre by remember { mutableStateOf("") }
    var hataGoster by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\"$urunAdi\" envanterden kaldırılsın mı?") },
        text = {
            Column {
                Text(
                    "Bu ürünü gerçekten teslim ettiysen, onaylamak için özel şifreyi gir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = sifre,
                    onValueChange = {
                        sifre = it.filter { c -> c.isDigit() }
                        hataGoster = false
                    },
                    label = { Text("Şifre") },
                    singleLine = true,
                    isError = hataGoster,
                    modifier = Modifier.fillMaxWidth()
                )
                if (hataGoster) {
                    Text(
                        "Şifre yanlış",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (SifreKontrol.envanterSifresiDogruMu(sifre)) {
                    onDogruSifre()
                } else {
                    hataGoster = true
                }
            }) {
                Text("Onayla")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}
