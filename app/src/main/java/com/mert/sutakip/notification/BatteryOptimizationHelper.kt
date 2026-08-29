package com.mert.sutakip.notification

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Xiaomi (MIUI), Huawei/Honor (EMUI/Magic UI), OPPO/Realme (ColorOS), Vivo (FuntouchOS)
 * gibi üreticiler, standart Android izinleri (POST_NOTIFICATIONS, WorkManager job'ları vb.)
 * doğru verilmiş olsa bile kendi "pil optimizasyonu / otomatik başlatma" sistemleriyle
 * arka plan uygulamalarını agresifçe kapatıp bildirimleri engelleyebiliyor. Bu, uygulama
 * kodundan bağımsız, sadece cihaz ayarlarından kullanıcının izin vermesiyle çözülebilen
 * bilinen bir Android sorunu.
 *
 * Bu yardımcı, cihazın üreticisine göre doğru ayar ekranını açmayı dener; bulamazsa genel
 * pil optimizasyonu istisna ekranına, o da yoksa uygulama ayarlarına düşer.
 */
object BatteryOptimizationHelper {

    /** Cihazın üreticiye özel agresif pil yönetimi olup olmadığını (bilinen liste) tahmin eder. */
    fun agresifPilYonetimiOlabilirMi(): Boolean {
        val uretici = Build.MANUFACTURER.lowercase()
        return listOf("xiaomi", "redmi", "poco", "huawei", "honor", "oppo", "realme", "vivo", "oneplus", "meizu", "asus")
            .any { uretici.contains(it) }
    }

    /** İşletim sistemi seviyesinde pil optimizasyonundan uygulamanın zaten muaf olup olmadığı. */
    fun pilOptimizasyonundanMuafMi(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return true
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Standart Android "pil optimizasyonunu yok say" isteğini gösterir. Kullanıcı bunu
     * onaylarsa sistem WorkManager job'larını daha az kısıtlar. Üretici-özel otomatik
     * başlatma ayarının yerine geçmez, ona ek bir katmandır.
     */
    @SuppressLint("BatteryLife")
    fun pilOptimizasyonuMuafiyetiIste(context: Context) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        runCatching { context.startActivity(intent) }
            .onFailure { genelPilAyarlariniAc(context) }
    }

    /**
     * Üreticiye özel "otomatik başlatma / arka plan çalışma" ayar ekranını açmayı dener.
     * Bu ekranlar üretici sürümüne göre çok değişkenlik gösterdiği için birden fazla
     * bilinen intent sırayla denenir; hiçbiri açılmazsa genel pil ayarlarına düşülür.
     */
    fun ureticiyeOzelAyarlariAc(context: Context) {
        val uretici = Build.MANUFACTURER.lowercase()
        val denenecekIntentler: List<Intent> = when {
            uretici.contains("xiaomi") || uretici.contains("redmi") || uretici.contains("poco") -> listOf(
                Intent().setClassName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                ),
                Intent().setClassName(
                    "com.miui.securitycenter",
                    "com.miui.powercenter.PowerSettings"
                )
            )
            uretici.contains("huawei") || uretici.contains("honor") -> listOf(
                Intent().setClassName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                ),
                Intent().setClassName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            )
            uretici.contains("oppo") || uretici.contains("realme") -> listOf(
                Intent().setClassName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                ),
                Intent().setClassName(
                    "com.coloros.oppoguardelf",
                    "com.coloros.powermanager.fuelgaue.PowerConsumptionActivity"
                )
            )
            uretici.contains("vivo") -> listOf(
                Intent().setClassName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                ),
                Intent().setClassName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            )
            uretici.contains("oneplus") -> listOf(
                Intent().setClassName(
                    "com.oneplus.security",
                    "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"
                )
            )
            uretici.contains("asus") -> listOf(
                Intent().setClassName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.autostart.AutoStartActivity"
                )
            )
            else -> emptyList()
        }

        val acildiMi = denenecekIntentler.any { intent ->
            runCatching {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            }.getOrDefault(false)
        }

        if (!acildiMi) {
            genelPilAyarlariniAc(context)
        }
    }

    /** Hiçbir üretici-özel ekran bulunamazsa: önce pil optimizasyonu istisna isteği, o da olmazsa uygulama ayarları. */
    private fun genelPilAyarlariniAc(context: Context) {
        val muafiyetIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
        val acildiMi = runCatching {
            muafiyetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(muafiyetIntent)
            true
        }.getOrDefault(false)

        if (!acildiMi) {
            uygulamaAyarlariniAc(context)
        }
    }

    private fun uygulamaAyarlariniAc(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
