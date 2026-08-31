package com.sutakip.app.util

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
        // Senin eklediklerin:
        "Harikasınn {isim}! 💧",
        "{isim}, tam bir su kraliçesisin.👑",
        "Aferinn {isim}, vücudun ve ben sana teşekkür ediyoruz 💖",
        "Böyle devam {isim}, ödüle daha çok yaklaşıyosunn!",
        "{isim}, bir yudum daha, bir adım daha yakınsın!",
"Her yudum bir adım",
        "Su içmek asla bu kadar keyifli olmamıştı, {isim}!",
        "Tebriklerrr {isim}, kendine iyi bakıyorsun ✨",
        "{isim}, vücudun şu an çok mutlu.",
        "Kaktüs değilsin {isim}, lıkır lıkır devam(benim kaktüsüm olabilirsin)🌵",
        "Böbreklerin şu an halay çekiyor ohoooo {isim}, devammm! 💃",
        "İçelim güzelleşelim {isim}, tabii ki sudan bahsediyorum sen zaten çok güzelsin! 🥰",
        "{isim}, sen içtikçe barajlardaki doluluk oranı düşüyor olabilir ama vücudun bayram ediyor! 🌊",
        "Oksijenin ikizi, hidrojenin can dostu vücuduna giriş yaptı {isim}! 🚰",
        "Lıkır lıkır iç ohhh şifa olsun {isim}, yarasınnn!",
        // Yeni eklenenler:
        "Bir yudum su, bir yudum sevgi... Şifa olsun benimkine! ❤️",
        "Dünyanın en tatlı kızı az önce su içti, kayıtlara geçsin! 📝",
        "Su içişini bile izlemek isterdim {isim}, afiyet olsunn! 😍",
        "Hem sağlığına hem güzelliğine yatırım yapıyorsun, zekisin he! 🧠💧",
        "Yarasın benim prensesime👑"
    )

    /** Günlük hedef tamamlandığında gösterilen kutlama mesajı havuzu. */
    val kutlama = listOf(
        // Senin eklediklerin:
        "{isim}, günün hedefi tamam! Sen bir su kahramanısın ben de senin kahramanınım! 🦸‍♂️🦸‍♀️",
        "Mükemmellsiinnnn {isim}! Bugün de hedefimize ulaştınnn 🌊",
        "Kapanışı şampiyonlara yakışır şekilde yaptın {isim}! Madalyanı elden teslim edicem. 🥇",
        "Bir alkış da böbreklerine gelsin {isim}, bugünkü zorlu mesaiyi başarıyla tamamladılar! 👏",
        "BUGÜNÜ DE BİTİRDİN ALLAMM MÜKEMMELSİN BE 🎉",
        // Yeni eklenenler:
        "YA SEN BİTANESİNN! yine başardınn! 🏆❤️",
        "Bu ne azim, bu ne kararlılık! Gururdan gözlerim yaşardı",
        "Günün yıldızı sensin {isim}! Şimdi gidip kutlama yapabiliriz! 🥳",
        "Hedefi tamamladın! Ödül olarak benden kocaman bir sarılma kazandın! 🤗"
    )

    /**
     * Kullanıcı hedefin belirgin şekilde gerisinde kaldığında,
     * bildirim mesajı tonu için kullanılabilecek "hatırlatıcı" havuz.
     */
    val geriKalmaHatirlatma = listOf(
        // Senin eklediklerin:
        "{isim}, valla bugün çok su içmedin ama hadi neyyseeeee 🙄",
        "{isim}, vücudun su bekliyo, sen yatıyon sadece ohh değme keyfime 😒",
        "Hedefe henüz uzaksın {isim}, hadi bir bardak su daha!",
        "{isim}, kaktüs moduna mı geçtin? Haydi bir bardak su! 🐪",
        "Ohoo ben diyorum deve ol sen diyon ben yohh,daha çok bekliyorum 😤",
        "Kuruduk, sarardık solduk {isim}... Bir yudum su lütfen! 🍂",
        "Böbreklerin greve gitmek üzere {isim}, arabulucu olarak bir bardak su teklif ediyorum. ⚖️",
        "{isim}, içimiz kurudu içimiz! Çöllere düşmeden bir yudum su atıver şu garip vücuduna. 🏜️",
        // Yeni eklenenler:
        "ŞŞ? Orada mısın? Su diyorum su, hani şu içmemiz gereken şey? 🧐",
        "Güzellik uykusuna daldın galiba {isim}, uyan da bi su iç hadi!",
        "Ben sana su iç diyorum, sen... ne yapıyorsun bilmiyorum ama su içmiyorsun! Çabuk bardağa sarıl! 🏃‍♀️",
        "Seni düşünmekten ben susadım {isim}, kalk da beraber bi su içelim bari! 🥺",
        "Böyle gidersen sana en yakın zamanda damacana hediye edicem, ciddiyim! 🎁💧"
    )

    /**
     * Kullanıcı hedefin önünde veya tam zamanında olduğunda,
     * bildirim mesajı tonu için kullanılabilecek "tebrik edici" havuz.
     */
    val onde = listOf(
        // Senin eklediklerin:
        "Harika gidiyorsun {isim}, tam yolundasın! 🚀",
        "{isim}, bugün gayet iyi bir tempodasın, böyle devam!",
        "Mükemmel su içiyorsun sana güveniyoruumm böyle devammm",
        "Hız felakettir derler ama su içerken değil! Uçuyorsun {isim}! ✈️",
        "Şelale gibi akıyorsun maşallah {isim}! Aynen devam. 💦",
        "{isim}, bu hızla gidersen yakında solungaçların çıkacak! Süpersin. 🐟",
        "Tempoya bak! Olimpiyatlara su içme dalı gelse altın madalya banko senin {isim}. 🏅",
        // Yeni eklenenler:
        "Ooo kimleri görüyorum, hız rekorları kırılıyor bugün! Motor takmışsın {isim} 🏍️💨",
        "Bana mısın demiyor, lıkır lıkır içiyor! Nazar değmesin tü tü tü 🧿",
        "İşte benim {isim}'im! Her kulvarda olduğu gibi su içmede de birinci! 🥇",
        "Şov yapıyorsun bugün {isim}, izlerken ben yoruldum valla! 😎"
    )

    fun rastgeleNormal(isim: String): String = normal.random().replace("{isim}", isim)
    fun rastgeleKutlama(isim: String): String = kutlama.random().replace("{isim}", isim)
    fun rastgeleGeriKalma(isim: String): String = geriKalmaHatirlatma.random().replace("{isim}", isim)
    fun rastgeleOnde(isim: String): String = onde.random().replace("{isim}", isim)
}