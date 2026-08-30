package com.mert.sutakip

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
import com.mert.sutakip.data.datastore.TemaModu
import com.mert.sutakip.notification.ReminderScheduler
import com.mert.sutakip.ui.home.HomeScreen
import com.mert.sutakip.ui.log.ActivityLogScreen
import com.mert.sutakip.ui.onboarding.OnboardingScreen
import com.mert.sutakip.ui.settings.SettingsScreen
import com.mert.sutakip.ui.stats.StatsScreen
import com.mert.sutakip.ui.theme.SuTakipTheme

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

private sealed class NavHedef(val route: String, val etiket: String) {
    object Ana : NavHedef("ana", "Ana Sayfa")
    object Istatistik : NavHedef("istatistik", "İstatistik")
    object Ayarlar : NavHedef("ayarlar", "Ayarlar")
    object AktiviteGecmisi : NavHedef("aktivite_gecmisi", "Bugünkü Hareketler")
}

@Composable
private fun AnaNavigasyon() {
    val navController = rememberNavController()
    val hedefler = listOf(NavHedef.Ana, NavHedef.Istatistik, NavHedef.Ayarlar)

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
                                    NavHedef.Ana -> Icons.Filled.Home
                                    NavHedef.Istatistik -> Icons.Filled.BarChart
                                    NavHedef.Ayarlar -> Icons.Filled.Settings
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
            startDestination = NavHedef.Ana.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavHedef.Ana.route) { HomeScreen() }
            composable(NavHedef.Istatistik.route) { StatsScreen() }
            composable(NavHedef.Ayarlar.route) {
                SettingsScreen(
                    onAktiviteGecmisiTiklandi = { navController.navigate(NavHedef.AktiviteGecmisi.route) }
                )
            }
            composable(NavHedef.AktiviteGecmisi.route) {
                ActivityLogScreen()
            }
        }
    }
}
