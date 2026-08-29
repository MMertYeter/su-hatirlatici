# Su Hatırlatıcı — Su İçme Takip ve Hatırlatma Uygulaması

Kotlin + Jetpack Compose (Material 3) ile yazılmış, MVVM + Repository mimarisinde bir Android uygulaması.

## Açma

1. Android Studio'da **File → Open** ile bu klasörü aç.
2. Gradle senkronizasyonunu bekle (IDE, `gradle-wrapper.properties`'te belirtilen Gradle 8.7'yi otomatik indirir).
3. Bir emülatör veya gerçek cihaz (Android 8.0+) seç, Run'a bas.

## Proje Yapısı

```
data/
  local/          Room entity'leri, DAO'lar, veritabanı
  datastore/       DataStore ile kullanıcı profili/ayarlar
  repository/       WaterRepository — su ekleme, günlük kayıt, rozet mantığı
di/                 Basit elle yazılmış bağımlılık konteyneri (Hilt kullanılmadı)
notification/       WorkManager tabanlı adaptif bildirim sistemi
ui/
  onboarding/      4 adımlı ilk açılış akışı
  home/            Ana ekran: bardak grid'i, ilerleme, hızlı ekleme
  stats/           Haftalık/aylık istatistik, streak, rozet galerisi
  settings/         Profil düzenleme, hedef override, tema, bildirimler
  components/       AnimatedWaterGlass, GlassGrid, ConfettiOverlay, DailyProgressHeader
  theme/            Material 3 renk paleti (su/mavi tonları) + karanlık mod
util/
  HedefHesaplayici.kt     Günlük hedef formülü (spesifikasyon Bölüm 2.4)
  MotivationMessages.kt   Motivasyon mesajları — BURADAN DÜZENLE (bkz. aşağı)
```

## Motivasyon Mesajlarını Düzenleme

Spesifikasyona uygun olarak, motivasyon mesajları uygulama içinde değil
`util/MotivationMessages.kt` dosyasında düz Kotlin listeleri olarak tutuluyor.
Mesaj eklemek/çıkarmak için bu dosyayı Android Studio'da açıp `normal`,
`kutlama`, `geriKalmaHatirlatma` ve `onde` listelerini düzenle, sonra tekrar
build al. Uygulama içinde bu listeleri düzenleyen bir ekran yok — spesifikasyon
bunu kasıtlı olarak istemiyordu.

## Bildirim Sistemi Nasıl Çalışıyor

`notification/AdaptiveReminderWorker.kt`, her 15 dakikada bir çalışan bir
WorkManager job'udur. Her çalıştığında:

1. Uyku saatleri içindeyse hiçbir şey yapmaz.
2. Son bildirimden bu yana en az 45 dakika geçmediyse çıkar.
3. Uyanma saatinden bu yana geçen süre oranına göre "beklenen" tüketimi hesaplar.
4. Gerçek tüketimle karşılaştırıp %20+ gerideyse daha sık/"hatırlatıcı" tonda,
   önde/tam zamanındaysa daha seyrek/"tebrik edici" tonda bildirim gönderir.
5. Son 2 saat içinde ciddi açık varsa sıklığı artırır (yine min. 45dk kuralıyla).

Bu yaklaşım, WorkManager'ın 15 dakikalık minimum periyodik aralık kısıtı
nedeniyle exact alarm karmaşıklığına girmeden spesifikasyondaki davranışı
sağlar. Gerekirse `AlarmManager.setExactAndAllowWhileIdle` ile değiştirilebilir.

## Bilinmesi Gerekenler / Sonraki Adımlar

- **Font:** Şu an sistem sans-serif kullanılıyor. Poppins/Nunito gibi yuvarlak
  hatlı bir font eklemek için `.ttf` dosyalarını `res/font/`'a koyup
  `ui/theme/Type.kt` içindeki `RoundedFontFamily`'yi güncelle.
- **Launcher ikonu:** Basit bir damla placeholder'ı var
  (`res/drawable/ic_launcher_foreground.xml`). İstersen Android Studio'nun
  Image Asset Studio'suyla daha detaylı bir ikon oluşturabilirsin.
- **Pil optimizasyonu istisnası:** Spesifikasyonda opsiyonel olarak
  belirtilmişti; şu an istenmiyor. Eklemek istersen
  `Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` intent'ini
  `SettingsScreen`'e bir buton olarak ekleyebilirsin.
- **minSdk 26 / targetSdk 35** olarak ayarlandı; tüm izin istekleri
  `Build.VERSION.SDK_INT` kontrolleriyle sarmalandı (Bölüm 9'daki gibi).

Test için: uygulamayı ilk açtığında onboarding akışı çıkacak, tamamladıktan
sonra ana ekrana yönlenecek. Su eklerken bardakların dolma animasyonunu ve
hedefi tamamladığında konfeti efektini görebilirsin.

## Release APK İmzalama

GitHub Actions workflow'u (`.github/workflows/build-apk.yml`), repo
Secrets'ında aşağıdaki 4 secret tanımlıysa release APK'yı otomatik olarak
imzalar; tanımlı değilse imzasız derlemeye devam eder (build bozulmaz):

- `KEYSTORE_BASE64` — keystore dosyasının base64 hali
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Yeni bir keystore oluşturmak için:

```
keytool -genkeypair -v -keystore su-hatirlatici-release.keystore \
  -alias su-hatirlatici -keyalg RSA -keysize 2048 -validity 10000
base64 -i su-hatirlatici-release.keystore -o keystore-base64.txt
```

`keystore-base64.txt` içeriğini `KEYSTORE_BASE64` secret'ına, keytool'un
sorduğu şifreleri de ilgili secret'lara yapıştır (Settings → Secrets and
variables → Actions).

Yerel makinede (Android Studio'dan) imzalı build almak için proje kökünde
`keystore.properties` dosyası oluştur (bu dosya `.gitignore`'da, asla repoya
gitmez):

```
storeFile=/tam/yol/su-hatirlatici-release.keystore
storePassword=...
keyAlias=su-hatirlatici
keyPassword=...
```

**Keystore dosyasını ve şifrelerini kaybetme** — kaybedersen aynı imzayla
güncelleme yayınlayamazsın, bu geri dönüşü olmayan bir durumdur.
