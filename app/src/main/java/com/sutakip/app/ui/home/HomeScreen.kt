package com.sutakip.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sutakip.app.data.local.entity.IcecekTuru
import com.sutakip.app.ui.components.ConfettiOverlay
import com.sutakip.app.ui.components.DailyProgressHeader
import com.sutakip.app.ui.components.GlassGrid
import com.sutakip.app.ui.theme.CoffeeFillColorDeep

/** Ana ekranda hangi popup'ın açık olduğunu tutar. Aynı anda en fazla biri açık olabilir. */
private enum class AcikPopup {
    YOK, SU_EKLE, KAHVE_EKLE, SIVI_AZALT
}

/** Özel miktar giriş ekranının hangi işlem için açıldığını (ekleme/azaltma + tür) tutar. */
private data class OzelMiktarIstegi(
    val tur: IcecekTuru,
    val azaltmaMi: Boolean
)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    var acikPopup by remember { mutableStateOf(AcikPopup.YOK) }
    var ozelMiktarIstegi by remember { mutableStateOf<OzelMiktarIstegi?>(null) }

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 20.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (state.isim.isNotBlank()) "Merhaba, ${state.isim} 👋" else "Merhaba 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${state.toplamPuan} 🪙",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Motivasyon mesajı: en üstte sabit bir alan, kapatmaya gerek yok.
                // Yeni ekleme geldiğinde otomatik olarak bir öncekinin yerini alır.
                // Kutlama mesajı ViewModel tarafında en az 5 saniye korunur.
                MotivationBanner(
                    mesaj = state.motivasyonMesaji,
                    kutlamaMi = state.kutlamaGoster,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )

                DailyProgressHeader(toplamMl = state.toplamMl, hedefMl = state.hedefMl)

                GlassGrid(
                    hedefMl = state.hedefMl,
                    bardakDolumlari = state.bardakDolumlari,
                    sonDolanBardakIndex = state.sonDolanBardakIndex,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Minimal ana menü: sadece 3 buton. Her biri kendi popup'ını açar.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { acikPopup = AcikPopup.SU_EKLE },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.LocalDrink, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Su Ekle", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }

                    Button(
                        onClick = { acikPopup = AcikPopup.KAHVE_EKLE },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CoffeeFillColorDeep,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.LocalCafe, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Kahve Ekle", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }

                    OutlinedButton(
                        onClick = { acikPopup = AcikPopup.SIVI_AZALT },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Sıvı Azalt")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            ConfettiOverlay(
                visible = state.kutlamaGoster,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // "Su Ekle" popup'ı: 200ml, 100ml, Özel miktar
    if (acikPopup == AcikPopup.SU_EKLE) {
        SecenekPopup(
            baslik = "Su Ekle",
            secenekler = listOf(
                "200 ml" to { viewModel.suEkle(200); acikPopup = AcikPopup.YOK },
                "100 ml" to { viewModel.suEkle(100); acikPopup = AcikPopup.YOK },
                "Özel miktar" to {
                    acikPopup = AcikPopup.YOK
                    ozelMiktarIstegi = OzelMiktarIstegi(IcecekTuru.SU, azaltmaMi = false)
                }
            ),
            onDismiss = { acikPopup = AcikPopup.YOK }
        )
    }

    // "Kahve Ekle" popup'ı: 200ml, Özel miktar
    if (acikPopup == AcikPopup.KAHVE_EKLE) {
        SecenekPopup(
            baslik = "Kahve Ekle",
            secenekler = listOf(
                "200 ml" to { viewModel.kahveEkle(200); acikPopup = AcikPopup.YOK },
                "Özel miktar" to {
                    acikPopup = AcikPopup.YOK
                    ozelMiktarIstegi = OzelMiktarIstegi(IcecekTuru.KAHVE, azaltmaMi = false)
                }
            ),
            onDismiss = { acikPopup = AcikPopup.YOK }
        )
    }

    // "Sıvı Azalt" popup'ı: Kahve Azalt / Su Azalt -> her ikisi de özel değer ekranına gider
    if (acikPopup == AcikPopup.SIVI_AZALT) {
        SecenekPopup(
            baslik = "Neyi azaltmak istersin?",
            secenekler = listOf(
                "Su Azalt" to {
                    acikPopup = AcikPopup.YOK
                    ozelMiktarIstegi = OzelMiktarIstegi(IcecekTuru.SU, azaltmaMi = true)
                },
                "Kahve Azalt" to {
                    acikPopup = AcikPopup.YOK
                    ozelMiktarIstegi = OzelMiktarIstegi(IcecekTuru.KAHVE, azaltmaMi = true)
                }
            ),
            onDismiss = { acikPopup = AcikPopup.YOK }
        )
    }

    // Özel değer giriş ekranı: hem ekleme hem azaltma için ortak, canlı önizleme metniyle.
    ozelMiktarIstegi?.let { istek ->
        OzelMiktarDialog(
            istek = istek,
            gunlukSuMl = state.gunlukSuMl,
            gunlukKahveMl = state.gunlukKahveMl,
            onDismiss = { ozelMiktarIstegi = null },
            onConfirm = { ml ->
                if (istek.azaltmaMi) {
                    viewModel.suAzalt(ml, istek.tur)
                } else if (istek.tur == IcecekTuru.KAHVE) {
                    viewModel.kahveEkle(ml)
                } else {
                    viewModel.suEkle(ml)
                }
                ozelMiktarIstegi = null
            }
        )
    }
}

/**
 * Ana ekranın üstünde sabit duran motivasyon mesajı alanı. Kapatma tuşu yok — yeni bir
 * ekleme/azaltma geldiğinde bir öncekinin yerini otomatik olarak alır. Mesaj yoksa (uygulama
 * daha yeni açıldıysa) hiçbir şey göstermez, boşluk kaplamaz.
 */
@Composable
private fun MotivationBanner(
    mesaj: String?,
    kutlamaMi: Boolean,
    modifier: Modifier = Modifier
) {
    if (mesaj == null) return

    val arkaplanRengi = if (kutlamaMi) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = arkaplanRengi,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = mesaj,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (kutlamaMi) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

/**
 * Bir butonun altından açılan, birkaç kısa seçenek sunan basit popup.
 * Her seçenek tek dokunuşla kendi aksiyonunu tetikler ve popup'ı kapatır.
 */
@Composable
private fun SecenekPopup(
    baslik: String,
    secenekler: List<Pair<String, () -> Unit>>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(baslik) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                secenekler.forEach { (etiket, aksiyon) ->
                    OutlinedButton(
                        onClick = aksiyon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(etiket, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Vazgeç") }
        }
    )
}

/**
 * Özel miktar giriş ekranı. Hem ekleme hem azaltma için kullanılır; başlık ve
 * onay metni işleme göre değişir. Girilen değer canlı olarak "Bugün X ml
 * su/kahve içtiniz" önizlemesine yansır (azaltmada da aynı cümle, azaltma
 * sonrası günün toplamını gösterir).
 */
@Composable
private fun OzelMiktarDialog(
    istek: OzelMiktarIstegi,
    gunlukSuMl: Int,
    gunlukKahveMl: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var metin by remember { mutableStateOf("") }
    val girilenMiktar = metin.toIntOrNull() ?: 0

    val turAdi = if (istek.tur == IcecekTuru.KAHVE) "kahve" else "su"
    val mevcutMl = if (istek.tur == IcecekTuru.KAHVE) gunlukKahveMl else gunlukSuMl

    val onizlemeMl = if (istek.azaltmaMi) {
        (mevcutMl - girilenMiktar).coerceAtLeast(0)
    } else {
        mevcutMl + girilenMiktar
    }

    val baslik = when {
        istek.azaltmaMi && istek.tur == IcecekTuru.KAHVE -> "Kahve azalt"
        istek.azaltmaMi -> "Su azalt"
        istek.tur == IcecekTuru.KAHVE -> "Özel kahve miktarı"
        else -> "Özel su miktarı"
    }
    val onayEtiketi = if (istek.azaltmaMi) "Azalt" else "Ekle"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(baslik) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = metin,
                    onValueChange = { metin = it.filter { c -> c.isDigit() } },
                    label = { Text("Miktar (ml)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Bugün $onizlemeMl ml $turAdi içtiniz",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (girilenMiktar > 0) onConfirm(girilenMiktar) },
                enabled = girilenMiktar > 0
            ) {
                Text(onayEtiketi)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
