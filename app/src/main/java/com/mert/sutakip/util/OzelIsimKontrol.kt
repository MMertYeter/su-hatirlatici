package com.mert.sutakip.util

/**
 * Onboarding'de isim adımında, girilen ismin "Rümeysa" ve çok yakın
 * varyasyonlarından (Rumeysa, Rumi, Rümi vb.) biri olup olmadığını kontrol
 * eder. Bilinçli olarak DAR tutulmuştur: sadece bu isme has, Türkçe karakter
 * (ü/u), yaygın kısaltma (Rumi/Rümi) ve boşluk/büyük-küçük harf farklarını
 * tolere eder — başka hiçbir isimle (örn. "Rüya", "Ruken", "Emre") eşleşmez.
 */
object OzelIsimKontrol {

    // Normalize edilmiş (küçük harf, ü->u, boşluksuz) hedef varyasyonlar.
    // Sadece "Rümeysa, Rumeysa, Rumi, Rümi" ve normalize sonrası bunlarla
    // birebir aynı olan yazımlar (büyük/küçük harf, ü/u farkı) kapsanır.
    private val ozelVaryasyonlar = setOf(
        "rumeysa",
        "rumi"
    )

    /** Türkçe karakterleri sadeleştirir, küçük harfe çevirir, baştaki/sondaki boşlukları atar. */
    private fun normalize(metin: String): String =
        metin.trim()
            .lowercase()
            .replace("ü", "u")
            .replace("i̇", "i") // Türkçe büyük İ'nin lowercase'i
            .replace("ı", "i")
            .replace(" ", "")

    /**
     * Girilen ismin (tek kelime ya da birden fazla kelimeden oluşan) "Rümeysa"
     * varyasyonlarından biriyle eşleşip eşleşmediğini kontrol eder. İsim alanına
     * birden fazla kelime girilmişse (örn. "Rümeysa Yılmaz"), her kelime ayrı
     * ayrı da kontrol edilir ki soyadı eklense bile ilk isim yakalansın.
     */
    fun rumeysaVaryasyonuMu(girilenIsim: String): Boolean {
        val normalizedTam = normalize(girilenIsim)
        if (normalizedTam.isBlank()) return false

        if (normalizedTam in ozelVaryasyonlar) return true

        // Birden fazla kelimeyse (örn. "Rümeysa K."), her kelimeyi ayrı kontrol et.
        val kelimeler = girilenIsim.trim().split(Regex("\\s+"))
        return kelimeler.any { normalize(it) in ozelVaryasyonlar }
    }
}
