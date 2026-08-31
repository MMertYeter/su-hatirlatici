package com.mert.sutakip.ui.settings

import android.os.Build
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mert.sutakip.data.datastore.Cinsiyet
import com.mert.sutakip.data.datastore.TemaModu
import com.mert.sutakip.notification.BatteryOptimizationHelper

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onAktiviteGecmisiTiklandi: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    var hedefDegisiklikOnayDialogAcik by remember { mutableStateOf(false) }
    var bekleyenProfilGuncelleme by remember { mutableStateOf<(() -> Unit)?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Ayarlar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        BolumBasligi("Profil")
        var isim by remember(profile.isim) { mutableStateOf(profile.isim) }
        var boy by remember(profile.boyCm) { mutableStateOf(profile.boyCm) }
        var kilo by remember(profile.kiloKg) { mutableStateOf(profile.kiloKg) }
        var yas by remember(profile.yas) { mutableStateOf(profile.yas) }
        var cinsiyet by remember(profile.cinsiyet) { mutableStateOf(profile.cinsiyet) }
        var uyanma by remember(profile.uyanmaSaatiDk) { mutableStateOf(profile.uyanmaSaatiDk) }
        var uyku by remember(profile.uykuSaatiDk) { mutableStateOf(profile.uykuSaatiDk) }

        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = isim,
                    onValueChange = { isim = it },
                    label = { Text("İsim") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Boy: ${boy.toInt()} cm")
                Slider(value = boy, onValueChange = { boy = it }, valueRange = 120f..220f)

                Text("Kilo: ${kilo.toInt()} kg")
                Slider(value = kilo, onValueChange = { kilo = it }, valueRange = 30f..180f)

                Text("Yaş: $yas")
                Slider(value = yas.toFloat(), onValueChange = { yas = it.toInt() }, valueRange = 10f..100f)

                Column(modifier = Modifier.selectableGroup()) {
                    listOf(
                        Cinsiyet.KADIN to "Kadın",
                        Cinsiyet.ERKEK to "Erkek",
                        Cinsiyet.BELIRTILMEDI to "Belirtmek istemiyorum"
                    ).forEach { (deger, etiket) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(selected = cinsiyet == deger, onClick = { cinsiyet = deger })
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = cinsiyet == deger, onClick = { cinsiyet = deger })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(etiket)
                        }
                    }
                }

                fun saatText(dk: Int) = "%02d:%02d".format(dk / 60, dk % 60)
                Text("Uyanma saati: ${saatText(uyanma)}")
                Slider(value = uyanma.toFloat(), onValueChange = { uyanma = it.toInt() }, valueRange = 0f..1439f, steps = 95)
                Text("Uyku saati: ${saatText(uyku)}")
                Slider(value = uyku.toFloat(), onValueChange = { uyku = it.toInt() }, valueRange = 0f..1439f, steps = 95)

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        bekleyenProfilGuncelleme = {
                            viewModel.profilGuncelle(isim, boy, kilo, yas, cinsiyet, uyanma, uyku, hedefiYenidenHesapla = true)
                        }
                        hedefDegisiklikOnayDialogAcik = true
                    }
                ) {
                    Text("Kaydet")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        BolumBasligi("Günlük Hedef")
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("${profile.gunlukHedefMl} ml", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Slider(
                    value = profile.gunlukHedefMl.toFloat(),
                    onValueChange = { viewModel.hedefiManuelAyarla(it.toInt()) },
                    valueRange = 1500f..4000f,
                    steps = 49 // ~50ml adımlar
                )
                Text(
                    "Bardak boyutu sabit 200ml (gösterim amaçlı, değiştirilemez)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        BolumBasligi("Bildirimler")
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Bildirimleri Aç")
                    Switch(
                        checked = profile.bildirimlerAcik,
                        onCheckedChange = { viewModel.bildirimlerAcikDegistir(it) }
                    )
                }

                if (BatteryOptimizationHelper.agresifPilYonetimiOlabilirMi()) {
                    val context = LocalContext.current
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Bildirimler geç mi geliyor?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} telefonlar, izinler açık olsa bile " +
                                "pil tasarrufu için uygulamayı arka planda kapatabiliyor. Bildirimlerin düzenli " +
                                "gelmesi için aşağıdaki ayarların hepsini kontrol etmen gerekiyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "1. Otomatik başlatma / arka planda çalışma",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Su Hatırlatıcı'nın telefon yeniden başladığında ve arka planda kendi kendine çalışmasına izin ver.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { BatteryOptimizationHelper.ureticiyeOzelAyarlariAc(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Otomatik Başlatma Ayarını Aç")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "2. Pil kullanımında kısıtlama olmasın",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Açılan listeden Su Hatırlatıcı'yı bulup pil kullanımını \"Kısıtlama yok\" / \"Sınırsız\" olarak ayarla.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { BatteryOptimizationHelper.pilKullanimiKisitlamasiniAc(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pil Kullanımı Ayarını Aç")
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "3. Android'in standart pil optimizasyonu",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Yukarıdaki ikisine ek olarak, Android'in kendi \"pil optimizasyonu yok say\" iznini de vermen önerilir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { BatteryOptimizationHelper.pilOptimizasyonuMuafiyetiIste(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pil Optimizasyonundan Muaf Tut")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        BolumBasligi("Aktivite")
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onAktiviteGecmisiTiklandi)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bugünkü Hareketler", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        "Su/kahve ekleme ve azaltma işlemlerinin listesi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        BolumBasligi("Görünüm")
        Card(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp).selectableGroup()) {
                listOf(
                    TemaModu.SISTEM to "Sistem",
                    TemaModu.ACIK to "Açık",
                    TemaModu.KOYU to "Koyu"
                ).forEach { (deger, etiket) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = profile.temaModu == deger,
                                onClick = { viewModel.temaModuDegistir(deger) }
                            )
                            .padding(vertical = 6.dp)
                    ) {
                        RadioButton(selected = profile.temaModu == deger, onClick = { viewModel.temaModuDegistir(deger) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(etiket)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (hedefDegisiklikOnayDialogAcik) {
        AlertDialog(
            onDismissRequest = { hedefDegisiklikOnayDialogAcik = false },
            title = { Text("Hedef yeniden hesaplansın mı?") },
            text = { Text("Profil bilgilerin değişti. Günlük su hedefin yeni bilgilere göre yeniden hesaplanacak. Onaylıyor musun?") },
            confirmButton = {
                TextButton(onClick = {
                    bekleyenProfilGuncelleme?.invoke()
                    hedefDegisiklikOnayDialogAcik = false
                }) { Text("Evet, yeniden hesapla") }
            },
            dismissButton = {
                TextButton(onClick = { hedefDegisiklikOnayDialogAcik = false }) { Text("İptal") }
            }
        )
    }
}

@Composable
private fun BolumBasligi(metin: String) {
    Text(
        text = metin,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
