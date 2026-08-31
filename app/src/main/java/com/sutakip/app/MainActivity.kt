package com.sutakip.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sutakip.app.data.datastore.TemaModu
import com.sutakip.app.notification.ReminderScheduler
import com.sutakip.app.ui.home.HomeScreen
import com.sutakip.app.ui.log.ActivityLogScreen
import com.sutakip.app.ui.magaza.MagazaScreen
import com.sutakip.app.ui.onboarding.OnboardingScreen
import com.sutakip.app.ui.settings.SettingsScreen
import com.sutakip.app.ui.stats.StatsScreen
import com.sutakip.app.ui.theme.SuTakipTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* İzin sonucu; kullanıcı reddetse de uygulama normal çalışmaya devam eder. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bildirimIzniIsteYaGerekliyse()
        ReminderScheduler.planlamayiBaslat(this)

        setContent {
            val app = application as SuTakipApp
            val profile by app.container.userPreferencesRepository.userProfileFlow.collectAsState(initial = null)

            val karanlikMod = when (profile?.temaModu) {
                TemaModu.ACIK -> false
                TemaModu.KOYU -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            SuTakipTheme(darkTheme = karanlikMod) {
                if (profile == null) {
                    // Profil yükleniyor; boş bir yüzey göster.
                    return@SuTakipTheme
                }
                if (profile?.onboardingTamamlandi == true) {
                    AnaNavigasyon()
                } else {
                    OnboardingScreen(onTamamlandi = { /* state akışıyla otomatik yönlenir */ })
                }
            }
        }
    }

    private fun bildirimIzniIsteYaGerekliyse() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val izinVar = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!izinVar) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        // Android 13 öncesinde bildirimler varsayılan olarak açıktır, izin istemeye gerek yok.
    }
}

/** Alt navigasyon bar'ında görünen sekmeler. Sadece bunlar 'when (hedef)' bloklarında ele alınır. */
private sealed class AltBarHedefi(val route: String, val etiket: String) {
    object Ana : AltBarHedefi("ana", "Ana Sayfa")
    object Istatistik : AltBarHedefi("istatistik", "İstatistik")
    object Magaza : AltBarHedefi("magaza", "Mağaza")
    object Ayarlar : AltBarHedefi("ayarlar", "Ayarlar")
}

/** Alt bar'da görünmeyen, sadece başka bir ekrandan navigate edilen "detay" route'ları. */
private object DetayRota {
    const val AKTIVITE_GECMISI = "aktivite_gecmisi"
}

@Composable
private fun AnaNavigasyon() {
    val navController = rememberNavController()
    val hedefler = listOf(AltBarHedefi.Ana, AltBarHedefi.Istatistik, AltBarHedefi.Magaza, AltBarHedefi.Ayarlar)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                hedefler.forEach { hedef ->
                    val secili = currentDestination?.hierarchy?.any { it.route == hedef.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = when (hedef) {
                                    AltBarHedefi.Ana -> Icons.Filled.Home
                                    AltBarHedefi.Istatistik -> Icons.Filled.BarChart
                                    AltBarHedefi.Magaza -> Icons.Filled.ShoppingCart
                                    AltBarHedefi.Ayarlar -> Icons.Filled.Settings
                                },
                                contentDescription = hedef.etiket
                            )
                        },
                        label = { Text(hedef.etiket) },
                        selected = secili,
                        onClick = {
                            navController.navigate(hedef.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = AltBarHedefi.Ana.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(AltBarHedefi.Ana.route) { HomeScreen() }
            composable(AltBarHedefi.Istatistik.route) { StatsScreen() }
            composable(AltBarHedefi.Magaza.route) { MagazaScreen() }
            composable(AltBarHedefi.Ayarlar.route) {
                SettingsScreen(
                    onAktiviteGecmisiTiklandi = { navController.navigate(DetayRota.AKTIVITE_GECMISI) }
                )
            }
            composable(DetayRota.AKTIVITE_GECMISI) {
                ActivityLogScreen()
            }
        }
    }
}
