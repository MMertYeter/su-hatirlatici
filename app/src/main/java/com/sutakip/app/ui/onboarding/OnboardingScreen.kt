package com.sutakip.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sutakip.app.data.datastore.Cinsiyet
import com.sutakip.app.ui.components.FallingHeartsOverlay
import com.sutakip.app.ui.theme.WaterFillColor
import com.sutakip.app.ui.theme.WaterFillColorDeep
import com.sutakip.app.util.OzelIsimKontrol
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    onTamamlandi: () -> Unit,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // İsim adımından ayrılırken, girilen isim "Rümeysa" ve yakın varyasyonlarından
    // biriyle eşleşiyorsa bir kereye mahsus özel bir karşılama ekranı gösterilir.
    // Bu ekran otomatik olarak birkaç saniye sonra kapanıp bir sonraki adıma geçer.
    var ozelKarsilamaGoster by remember { mutableStateOf(false) }

    if (ozelKarsilamaGoster) {
        OzelKarsilamaOverlay(
            isim = state.isim.trim(),
            onTamamlandi = {
                ozelKarsilamaGoster = false
                viewModel.ileriGit()
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // İlerleme göstergesi
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .background(
                                color = if (i <= state.adim) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }
            Text(
                text = "Adım ${state.adim + 1} / 4",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = state.adim,
                    transitionSpec = {
                        (slideInHorizontally { it } togetherWith slideOutHorizontally { -it })
                            .using(SizeTransform(clip = false))
                    },
                    label = "onboardingStep"
                ) { adim ->
                    when (adim) {
                        0 -> IsimAdimi(
                            isim = state.isim,
                            onIsimChange = viewModel::isimGuncelle
                        )
                        1 -> FizikselAdimi(
                            boyCm = state.boyCm,
                            kiloKg = state.kiloKg,
                            yas = state.yas,
                            cinsiyet = state.cinsiyet,
                            onDegisiklik = viewModel::fizikselGuncelle
                        )
                        2 -> UykuUyanmaAdimi(
                            uyanmaDk = state.uyanmaSaatiDk,
                            uykuDk = state.uykuSaatiDk,
                            onDegisiklik = viewModel::uykuUyanmaGuncelle
                        )
                        else -> HedefAdimi(
                            hesaplananHedefMl = state.hesaplananHedefMl,
                            etkinHedefMl = state.etkinHedefMl,
                            onHedefDegistir = viewModel::hedefiElleAyarla
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.adim > 0) {
                    OutlinedButton(
                        onClick = viewModel::geriGit,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("Geri")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                val ilerleyebilir = when (state.adim) {
                    0 -> state.isim.isNotBlank()
                    else -> true
                }

                Button(
                    onClick = {
                        if (state.adim == 3) {
                            viewModel.tamamla(onTamamlandi)
                        } else if (state.adim == 0 && OzelIsimKontrol.rumeysaVaryasyonuMu(state.isim)) {
                            ozelKarsilamaGoster = true
                        } else {
                            viewModel.ileriGit()
                        }
                    },
                    enabled = ilerleyebilir,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .width(160.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        if (state.adim == 3) "Başla 💧" else "Devam Et",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdimBasligi(baslik: String, aciklama: String? = null) {
    Text(
        text = baslik,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    if (aciklama != null) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = aciklama,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OzelKarsilamaOverlay(
    isim: String,
    onTamamlandi: () -> Unit
) {
    // Yazı biraz daha erken belirir, kalpler hemen başlar; toplam ekran süresi ~2.8sn
    // ki mesaj rahatça okunsun (kısa bir cümle için yeterli, aceleye getirmiyor).
    LaunchedEffect(Unit) {
        delay(2800)
        onTamamlandi()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFE3E8),
                        Color(0xFFFFF5F7)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$isim, özel kullanıcı. Bu uygulama kimse bilmese de aslında sadece senin için tasarlandı🥰",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = Color(0xFFAD1457),
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        FallingHeartsOverlay(
            visible = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun OnboardingKart(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
private fun IsimAdimi(isim: String, onIsimChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(
                    brush = Brush.linearGradient(listOf(WaterFillColor, WaterFillColorDeep)),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        AdimBasligi(
            baslik = "Sana nasıl seslenelim?",
            aciklama = "İsmini motivasyon mesajlarında ve bildirimlerde kullanacağız."
        )
        Spacer(modifier = Modifier.height(24.dp))

        OnboardingKart {
            OutlinedTextField(
                value = isim,
                onValueChange = onIsimChange,
                label = { Text("İsmin") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun FizikselAdimi(
    boyCm: Float,
    kiloKg: Float,
    yas: Int,
    cinsiyet: Cinsiyet,
    onDegisiklik: (Float, Float, Int, Cinsiyet) -> Unit
) {
    var boy by remember { mutableStateOf(boyCm) }
    var kilo by remember { mutableStateOf(kiloKg) }
    var yasLokal by remember { mutableStateOf(yas) }
    var cinsiyetLokal by remember { mutableStateOf(cinsiyet) }

    fun bildir() = onDegisiklik(boy, kilo, yasLokal, cinsiyetLokal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AdimBasligi(
            baslik = "Biraz kendinden bahset",
            aciklama = "Bu bilgiler günlük su hedefini kişiselleştirmek için kullanılır."
        )
        Spacer(modifier = Modifier.height(20.dp))

        OnboardingKart {
            EtiketliSlider(
                etiket = "Boy",
                deger = "${boy.toInt()} cm",
                sliderValue = boy,
                onValueChange = { boy = it; bildir() },
                valueRange = 120f..220f
            )
            Spacer(modifier = Modifier.height(18.dp))
            EtiketliSlider(
                etiket = "Kilo",
                deger = "${kilo.toInt()} kg",
                sliderValue = kilo,
                onValueChange = { kilo = it; bildir() },
                valueRange = 30f..180f
            )
            Spacer(modifier = Modifier.height(18.dp))
            EtiketliSlider(
                etiket = "Yaş",
                deger = "$yasLokal",
                sliderValue = yasLokal.toFloat(),
                onValueChange = { yasLokal = it.toInt(); bildir() },
                valueRange = 10f..100f
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OnboardingKart {
            Text(
                "Cinsiyet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                            .selectable(
                                selected = cinsiyetLokal == deger,
                                onClick = { cinsiyetLokal = deger; bildir() }
                            )
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = cinsiyetLokal == deger, onClick = { cinsiyetLokal = deger; bildir() })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(etiket, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EtiketliSlider(
    etiket: String,
    deger: String,
    sliderValue: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiket, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(deger, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
    Slider(
        value = sliderValue,
        onValueChange = onValueChange,
        valueRange = valueRange,
        steps = steps
    )
}

@Composable
private fun UykuUyanmaAdimi(
    uyanmaDk: Int,
    uykuDk: Int,
    onDegisiklik: (Int, Int) -> Unit
) {
    var uyanma by remember { mutableStateOf(uyanmaDk) }
    var uyku by remember { mutableStateOf(uykuDk) }

    fun saatText(dk: Int) = "%02d:%02d".format(dk / 60, dk % 60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AdimBasligi(
            baslik = "Uyku düzenin nasıl?",
            aciklama = "Bu bilgi bildirim zamanlamasını uyanık olduğun saatlere göre ayarlamak için kullanılır."
        )
        Spacer(modifier = Modifier.height(20.dp))

        OnboardingKart {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uyanma saati", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Text(saatText(uyanma), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = uyanma.toFloat(),
                onValueChange = { uyanma = it.toInt(); onDegisiklik(uyanma, uyku) },
                valueRange = 0f..1439f,
                steps = 95
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Uyku saati", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                Text(saatText(uyku), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = uyku.toFloat(),
                onValueChange = { uyku = it.toInt(); onDegisiklik(uyanma, uyku) },
                valueRange = 0f..1439f,
                steps = 95
            )
        }
    }
}

@Composable
private fun HedefAdimi(
    hesaplananHedefMl: Int,
    etkinHedefMl: Int,
    onHedefDegistir: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AdimBasligi(baslik = "Günlük hedefin")
        Spacer(modifier = Modifier.height(24.dp))

        OnboardingKart {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onHedefDegistir((etkinHedefMl - 100).coerceAtLeast(500)) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = "Azalt")
                }
                Text(
                    text = "$etkinHedefMl ml",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                IconButton(
                    onClick = { onHedefDegistir((etkinHedefMl + 100).coerceAtMost(6000)) },
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Artır")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Bu yaklaşık bir değerdir, dilediğin zaman ayarlardan değiştirebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
