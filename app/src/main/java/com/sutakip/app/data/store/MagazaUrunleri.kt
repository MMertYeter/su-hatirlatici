package com.sutakip.app.data.store

/**
 * Mağazada satılan ürünler burada, kaynak kodda düzenlenir.
 * Uygulama içinde bu listeyi düzenlemek için bir ekran YOKTUR — Android Studio'da
 * (ya da GitHub üzerinden) bu dosyayı düzenleyip yeniden build alarak ürün
 * ekleyip çıkarabilirsin, tıpkı MotivationMessages.kt gibi.
 *
 * id: benzersiz ve SABİT olmalı (bir kez verildikten sonra değiştirilmemeli) çünkü
 * kullanıcıların envanterinde bu id saklanır; id değişirse envanterdeki eski kayıtlar
 * hangi üründen olduğunu kaybeder.
 */
data class MagazaUrunu(
    val id: String,
    val ad: String,
    val aciklama: String,
    val puanMaliyeti: Int,
    val emoji: String = "🎁"
)

object MagazaUrunleri {
    val urunler = listOf(
        MagazaUrunu(
            id = "surpriz_hediye",
            ad = "Sürpriz Hediye",
            aciklama = "Sürpriz hediye isteme hakkı.",
            puanMaliyeti = 250,
            emoji = "🎁"
        ),
        MagazaUrunu(
            id = "yemek_ismarlama",
            ad = "Yemek Ismarlama",
            aciklama = "Yemek ısmarlatma hakkı.",
            puanMaliyeti = 500,
            emoji = "🍔"
        ),
        MagazaUrunu(
            id = "istek_urun",
            ad = "İstediğin Bir Ürün",
            aciklama = "İstediğin bi ürünü aldırma hakkı.",
            puanMaliyeti = 750,
            emoji = "🛍️"
        ),
        MagazaUrunu(
            id = "odev_yardimi",
            ad = "Ödev Yaptırma Hakkı",
            aciklama = "Ödevlerinden birini baştan sona yaptırma hakkı.",
            puanMaliyeti = 750,
            emoji = "📖"
        )
    )
    

    fun bul(id: String): MagazaUrunu? = urunler.find { it.id == id }
}
