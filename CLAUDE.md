# Ulak – Proje Bağlamı

Ulak, deprem sonrası internet ve operatör hattı olmadan çalışan bir Android
haberleşme uygulamasıdır. Telefonlar Bluetooth/Wi-Fi (Google Nearby Connections)
ile birbirine bağlanıp mesajları elden ele taşır (epidemic routing: UUID + TTL +
tekrar kontrolü). İnternete erişen ilk telefon mesajları Firebase'e yükler.

## Teknik kararlar (değiştirmeden önce sor)
- Flutter + Dart, SADECE Android hedefleniyor (iOS kapsam dışı)
- Durum yönetimi: Provider
- Yerel veritabanı: sqflite
- Cihazlar arası iletişim: nearby_connections
- Arka plan: flutter_foreground_task, foreground service türü connectedDevice,
  servis SADECE kullanıcı butona bastığında başlar
- İzinler: permission_handler
- Mesh algoritması (paket modeli, TTL, senkronizasyon) takım arkadaşımın
  sorumluluğunda, ayrı bir Dart paketi olarak gelecek. Uygulama tarafında bu
  mantığı yazma; gerekirse geçici, değiştirilebilir bir ara katman kur.

## Ortak mesaj paketi alanları
id (UUID), type (safe | sos | text), senderId, recipientId (veya ALL),
content, latitude, longitude, createdAt, ttl

## Çalışma kuralları
- Ben öğreniyorum. Her değişiklikten sonra ne yaptığını ve NEDEN yaptığını
  Türkçe, kısa ve anlaşılır şekilde açıkla. Kod, değişken adları ve kod içi
  yorumlar İngilizce olsun.
- İşi küçük adımlarla yap. Bir faz bitince DUR, özet ver ve benim "devam"
  dememi bekle. Bir sonraki faza kendi başına geçme.
- Listede olmayan yeni bir paket eklemeden önce bana sor.
- Gereğinden karmaşık mimari kurma. Bu bir bitirme projesi; sade, okunabilir
  kod tercih et.
- feature/* dalında çalış. Her faz sonunda anlamlı bir commit mesajı öner,
  ama push etme ve develop/main dallarına dokunma.
- Her fazın sonunda `flutter analyze` çalıştır, hata bırakma.
