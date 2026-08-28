package com.mert.sutakip.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mert.sutakip.data.local.entity.IcecekTuru
import com.mert.sutakip.ui.components.ConfettiOverlay
import com.mert.sutakip.ui.components.DailyProgressHeader
import com.mert.sutakip.ui.components.GlassGrid
import com.mert.sutakip.ui.theme.CoffeeFillColorDeep
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val sonIslemVarMi by viewModel.sonIslemVarMi.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var ozelMiktarDialogAcik by remember { mutableStateOf(false) }
    var ozelAzaltmaDialogAcik by remember { mutableStateOf(false) }
    var ozelKahveDialogAcik by remember { mutableStateOf(false) }

    LaunchedEffect(state.motivasyonMesaji) {
        val mesaj = state.motivasyonMesaji
        if (mesaj != null) {
            scope.launch {
                val sonuc = snackbarHostState.showSnackbar(
                    message = mesaj,
                    actionLabel = if (sonIslemVarMi) "Geri Al" else null,
                    withDismissAction = true
                )
                if (sonuc == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                    viewModel.geriAl()
                }
                viewModel.mesajGosterildi()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                Text(
                    text = if (state.isim.isNotBlank()) "Merhaba, ${state.isim} 👋" else "Merhaba 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 24.dp, top = 20.dp)
                )

                DailyProgressHeader(toplamMl = state.toplamMl, hedefMl = state.hedefMl)

                GlassGrid(
                    hedefMl = state.hedefMl,
                    bardakDolumlari = state.bardakDolumlari,
                    sonDolanBardakIndex = state.sonDolanBardakIndex,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.suEkle(200) },
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
                        Text("+1 Bardak (200ml)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.suEkle(100) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("+ Yarım Bardak (100ml)")
                        }

                        OutlinedButton(
                            onClick = { ozelMiktarDialogAcik = true },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Özel")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Kahve ekleme: kahve de sıvı alımına katkı sağladığı için toplama dahil edilir.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.kahveEkle(150) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CoffeeFillColorDeep
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                CoffeeFillColorDeep.copy(alpha = 0.5f)
                            )
                        ) {
                            Text("☕ Kahve Ekle (150ml)")
                        }

                        OutlinedButton(
                            onClick = { ozelKahveDialogAcik = true },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = CoffeeFillColorDeep
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                CoffeeFillColorDeep.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Özel")
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.suAzalt(200, IcecekTuru.SU) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Su Azalt (200ml)")
                        }

                        OutlinedButton(
                            onClick = { ozelAzaltmaDialogAcik = true },
                            modifier = Modifier
                                .weight(0.6f)
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            )
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("Özel")
                        }
                    }

                    OutlinedButton(
                        onClick = { viewModel.suAzalt(150, IcecekTuru.KAHVE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = CoffeeFillColorDeep
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            CoffeeFillColorDeep.copy(alpha = 0.35f)
                        )
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("Kahve Azalt (150ml)")
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

    if (ozelMiktarDialogAcik) {
        OzelMiktarDialog(
            baslik = "Özel miktar gir",
            onayEtiketi = "Ekle",
            onDismiss = { ozelMiktarDialogAcik = false },
            onConfirm = { ml ->
                viewModel.suEkle(ml)
                ozelMiktarDialogAcik = false
            }
        )
    }

    if (ozelAzaltmaDialogAcik) {
        OzelMiktarDialog(
            baslik = "Ne kadar azaltılsın?",
            onayEtiketi = "Azalt",
            onDismiss = { ozelAzaltmaDialogAcik = false },
            onConfirm = { ml ->
                viewModel.suAzalt(ml)
                ozelAzaltmaDialogAcik = false
            }
        )
    }

    if (ozelKahveDialogAcik) {
        OzelMiktarDialog(
            baslik = "Kaç ml kahve?",
            onayEtiketi = "Ekle",
            onDismiss = { ozelKahveDialogAcik = false },
            onConfirm = { ml ->
                viewModel.kahveEkle(ml)
                ozelKahveDialogAcik = false
            }
        )
    }
}

@Composable
private fun OzelMiktarDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    baslik: String = "Özel miktar gir",
    onayEtiketi: String = "Ekle"
) {
    var metin by remember { mutableStateOf("") }
    val miktar = metin.toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(baslik) },
        text = {
            OutlinedTextField(
                value = metin,
                onValueChange = { metin = it.filter { c -> c.isDigit() } },
                label = { Text("Miktar (ml)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { miktar?.let { if (it > 0) onConfirm(it) } },
                enabled = miktar != null && miktar > 0
            ) {
                Text(onayEtiketi)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İptal") }
        }
    )
}
