package com.sutakip.app.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

enum class Cinsiyet { KADIN, ERKEK, BELIRTILMEDI }
enum class TemaModu { SISTEM, ACIK, KOYU }

/** DataStore'da tutulan kullanıcı profili ve ayarlar. */
data class UserProfile(
    val isim: String = "",
    val boyCm: Float = 170f,
    val kiloKg: Float = 70f,
    val yas: Int = 25,
    val cinsiyet: Cinsiyet = Cinsiyet.BELIRTILMEDI,
    val uyanmaSaatiDk: Int = 7 * 60,   // günün dakikası, 07:00
    val uykuSaatiDk: Int = 23 * 60,    // 23:00
    val gunlukHedefMl: Int = 2000,
    val hedefManuelDegistirildi: Boolean = false,
    val onboardingTamamlandi: Boolean = false,
    val bildirimlerAcik: Boolean = true,
    val temaModu: TemaModu = TemaModu.SISTEM
)

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val ISIM = stringPreferencesKey("isim")
        val BOY = floatPreferencesKey("boy_cm")
        val KILO = floatPreferencesKey("kilo_kg")
        val YAS = intPreferencesKey("yas")
        val CINSIYET = stringPreferencesKey("cinsiyet")
        val UYANMA = intPreferencesKey("uyanma_dk")
        val UYKU = intPreferencesKey("uyku_dk")
        val HEDEF = intPreferencesKey("gunluk_hedef_ml")
        val HEDEF_MANUEL = booleanPreferencesKey("hedef_manuel")
        val ONBOARDING_OK = booleanPreferencesKey("onboarding_tamamlandi")
        val BILDIRIM_ACIK = booleanPreferencesKey("bildirimler_acik")
        val TEMA = stringPreferencesKey("tema_modu")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data.map { prefs ->
        UserProfile(
            isim = prefs[Keys.ISIM] ?: "",
            boyCm = prefs[Keys.BOY] ?: 170f,
            kiloKg = prefs[Keys.KILO] ?: 70f,
            yas = prefs[Keys.YAS] ?: 25,
            cinsiyet = prefs[Keys.CINSIYET]?.let { runCatching { Cinsiyet.valueOf(it) }.getOrNull() }
                ?: Cinsiyet.BELIRTILMEDI,
            uyanmaSaatiDk = prefs[Keys.UYANMA] ?: 7 * 60,
            uykuSaatiDk = prefs[Keys.UYKU] ?: 23 * 60,
            gunlukHedefMl = prefs[Keys.HEDEF] ?: 2000,
            hedefManuelDegistirildi = prefs[Keys.HEDEF_MANUEL] ?: false,
            onboardingTamamlandi = prefs[Keys.ONBOARDING_OK] ?: false,
            bildirimlerAcik = prefs[Keys.BILDIRIM_ACIK] ?: true,
            temaModu = prefs[Keys.TEMA]?.let { runCatching { TemaModu.valueOf(it) }.getOrNull() }
                ?: TemaModu.SISTEM
        )
    }

    suspend fun updateIsim(isim: String) = context.dataStore.edit { it[Keys.ISIM] = isim }

    suspend fun updateFizikselBilgiler(boyCm: Float, kiloKg: Float, yas: Int, cinsiyet: Cinsiyet) {
        context.dataStore.edit {
            it[Keys.BOY] = boyCm
            it[Keys.KILO] = kiloKg
            it[Keys.YAS] = yas
            it[Keys.CINSIYET] = cinsiyet.name
        }
    }

    suspend fun updateUykuUyanma(uyanmaDk: Int, uykuDk: Int) {
        context.dataStore.edit {
            it[Keys.UYANMA] = uyanmaDk
            it[Keys.UYKU] = uykuDk
        }
    }

    suspend fun updateHedef(hedefMl: Int, manuel: Boolean) {
        context.dataStore.edit {
            it[Keys.HEDEF] = hedefMl
            it[Keys.HEDEF_MANUEL] = manuel
        }
    }

    suspend fun setOnboardingTamamlandi() {
        context.dataStore.edit { it[Keys.ONBOARDING_OK] = true }
    }

    suspend fun setBildirimlerAcik(acik: Boolean) {
        context.dataStore.edit { it[Keys.BILDIRIM_ACIK] = acik }
    }

    suspend fun setTemaModu(mod: TemaModu) {
        context.dataStore.edit { it[Keys.TEMA] = mod.name }
    }
}
