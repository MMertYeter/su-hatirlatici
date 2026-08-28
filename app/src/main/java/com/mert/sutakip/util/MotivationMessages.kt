package com.mert.sutakip.util

/**
 * Motivasyon mesajları burada, kaynak kodda düzenlenir.
 * Uygulama içinde bu listeleri düzenlemek için bir ekran YOKTUR (bkz. spesifikasyon Bölüm 5).
 *
 * {isim} placeholder'ı çalışma zamanında kullanıcının gerçek ismiyle değiştirilir.
 * Bu dosyayı Android Studio'da doğrudan düzenleyip build alarak mesajları
 * istediğin kadar ekleyip çıkarabilirsin.
 */
object MotivationMessages {

    /** Her su ekleme işleminde gösterilen normal mesaj havuzu. */
    val normal = listOf(
        "Harikasın {isim}! 💧",
        "{isim}, tam bir su şampiyonusun!",
        "Aferin {isim}, vücudun sana teşekkür ediyor 🙌",
        "Böyle devam {isim}, harika gidiyorsun!",
        "{isim}, bir yudum daha, bir adım daha yakınsın!",
        "Su içmek asla bu kadar keyifli olmamıştı, {isim}!",
        "Tebrikler {isim}, kendine iyi bakıyorsun 🌊",
        "{isim}, vücudun şu an sana gülümsüyor!"
    )

    /** Günlük hedef tamamlandığında gösterilen kutlama mesajı havuzu. */
    val kutlama = listOf(
        "Bugünkü hedefini tamamladın {isim}, seninle gurur duyuyoruz! 🎉",
        "{isim}, günün hedefi tamam! Sen bir su kahramanısın! 🏆",
        "Harika iş {isim}! Bugün de hedefine ulaştın 💧🎉"
    )

    /**
     * Kullanıcı hedefin belirgin şekilde gerisinde kaldığında,
     * bildirim mesajı tonu için kullanılabilecek "hatırlatıcı" havuz.
     */
    val geriKalmaHatirlatma = listOf(
        "{isim}, bugün biraz geride kaldın, birkaç yudum su içmeye ne dersin?",
        "{isim}, vücudun su bekliyor, küçük bir mola verelim mi?",
        "Hedefe henüz uzaksın {isim}, hadi bir bardak su daha!"
    )

    /**
     * Kullanıcı hedefin önünde veya tam zamanında olduğunda,
     * bildirim mesajı tonu için kullanılabilecek "tebrik edici" havuz.
     */
    val onde = listOf(
        "Harika gidiyorsun {isim}, tam yolundasın! 💧",
        "{isim}, bugün gayet iyi bir tempodasın, böyle devam!"
    )

    fun rastgeleNormal(isim: String): String = normal.random().replace("{isim}", isim)
    fun rastgeleKutlama(isim: String): String = kutlama.random().replace("{isim}", isim)
    fun rastgeleGeriKalma(isim: String): String = geriKalmaHatirlatma.random().replace("{isim}", isim)
    fun rastgeleOnde(isim: String): String = onde.random().replace("{isim}", isim)
}
