package com.mert.sutakip.util

import com.mert.sutakip.data.datastore.Cinsiyet
import kotlin.math.roundToInt

/**
 * Günlük su hedefi hesaplama formülü.
 *
 * temel_ml = kilo(kg) x yaş/cinsiyete göre katsayı
 * BMI'a göre +/- 150ml ayarı
 * 50ml'e yuvarlama, 1200-4000ml aralığında sınırlama
 *
 * Bu tıbbi bir tavsiye değildir, kaba bir tahmindir.
 */
object HedefHesaplayici {

    private fun katsayi(cinsiyet: Cinsiyet, yas: Int): Double {
        return when (cinsiyet) {
            Cinsiyet.KADIN -> when {
                yas < 30 -> 35.0
                yas <= 55 -> 30.0
                else -> 25.0
            }
            Cinsiyet.ERKEK -> when {
                yas < 30 -> 40.0
                yas <= 55 -> 35.0
                else -> 30.0
            }
            Cinsiyet.BELIRTILMEDI -> {
                // Kadın ve erkek katsayılarının ortalaması
                val kadin = katsayi(Cinsiyet.KADIN, yas)
                val erkek = katsayi(Cinsiyet.ERKEK, yas)
                (kadin + erkek) / 2.0
            }
        }
    }

    fun hesapla(boyCm: Float, kiloKg: Float, yas: Int, cinsiyet: Cinsiyet): Int {
        val k = katsayi(cinsiyet, yas)
        var temelMl = kiloKg * k

        val boyM = boyCm / 100f
        if (boyM > 0f) {
            val bmi = kiloKg / (boyM * boyM)
            temelMl += when {
                bmi < 18.5f -> -150.0
                bmi > 25f -> 150.0
                else -> 0.0
            }
        }

        // 50ml'e yuvarla
        val yuvarlanmis = (temelMl / 50.0).roundToInt() * 50

        // 1200-4000 aralığında sınırla
        return yuvarlanmis.coerceIn(1200, 4000)
    }

    /** Bardak sayısı: hedef / 200ml, yukarı yuvarlanır. */
    fun bardakSayisi(hedefMl: Int, bardakKapasitesiMl: Int = 200): Int {
        return (hedefMl + bardakKapasitesiMl - 1) / bardakKapasitesiMl
    }
}
