package com.sutakip.app.util

import java.security.MessageDigest

/**
 * Envanterden ürün kaldırma (bkz. MagazaScreen) için özel şifre kontrolü.
 * Şifrenin kendisi kod içinde düz metin olarak tutulmaz — sadece SHA-256 hash'i
 * saklanır, kullanıcının girdiği metin aynı şekilde hash'lenip karşılaştırılır.
 * Bu, APK decompile edilse bile şifrenin doğrudan okunmasını engeller (tam
 * güvenlik değildir, ama düz metinden çok daha iyidir).
 */
object SifreKontrol {

    // SHA-256("909220026405")
    private const val ENVANTER_SIFRE_HASH =
        "9aff116e3c34923eb75eaa9ef86a913a9df11a82a119669267e37c7bd3da67dd"

    fun envanterSifresiDogruMu(girilenSifre: String): Boolean {
        if (girilenSifre.isBlank()) return false
        val hash = sha256(girilenSifre)
        return hash == ENVANTER_SIFRE_HASH
    }

    private fun sha256(metin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(metin.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { byte -> "%02x".format(byte.toInt() and 0xFF) }
    }
}
