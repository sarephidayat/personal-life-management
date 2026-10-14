# ARCHITECTURE --- Personal Life Management App

## 1. Prinsip

-   Flutter monolith; tidak menggunakan microservices.
-   Offline-first.
-   SQLite adalah sumber kebenaran untuk data aplikasi.
-   Integrasi Google Calendar terisolasi dari domain dan UI.
-   Pisahkan presentasi, logika bisnis, dan akses data.
-   Hindari abstraksi berlebihan untuk aplikasi pribadi, tetapi tetap
    gunakan batas layer yang jelas.

## 2. Stack

-   Flutter + Dart.
-   SQLite melalui package yang dipilih setelah pemeriksaan
    kompatibilitas dan pemeliharaan.
-   Android OAuth/Google Calendar API.
-   Git/GitHub.
-   Unit test, widget test bila relevan, integration test, analisis
    statis.

## 3. Struktur folder yang disarankan

``` text
lib/
  app/
    app.dart
    router.dart
    theme/
      app_theme.dart
      app_colors.dart
      app_typography.dart
  core/
    database/
      app_database.dart
      migrations/
    security/
      app_lock_service.dart
      secure_storage_service.dart
    backup/
      database_backup_service.dart
    errors/
    utils/
  features/
    dashboard/
      data/
      domain/
      presentation/
    finance/
      data/
      domain/
      presentation/
    planner/
      data/
      domain/
      presentation/
    calendar_sync/
      data/
      domain/
      presentation/
  shared/
    widgets/
    models/
test/
```

Struktur ini adalah baseline; agent boleh menyesuaikan detail kecil
tanpa mengubah pemisahan tanggung jawab.

## 4. Layer dan tanggung jawab

### Presentation

-   Widget dan halaman Flutter.
-   Mengelola state tampilan.
-   Memanggil use case/provider/controller.
-   Tidak menjalankan SQL mentah atau menghitung aturan keuangan secara
    langsung.

### Domain

-   Entitas, aturan bisnis, dan use case.
-   Menghitung saldo, realisasi anggaran, progres target tabungan, serta
    validasi jadwal.
-   Tidak bergantung pada Flutter UI atau implementasi SQLite.

### Data

-   Repository dan implementasi sumber data SQLite.
-   Mapping model/domain ke record database.
-   Query laporan dan pagination.
-   Menjaga transaksi database atomik ketika operasi memengaruhi
    beberapa record.

### Integrasi

-   Google Calendar client.
-   OAuth/token handling.
-   Queue perubahan yang menunggu sinkronisasi.
-   Pemetaan ID event aplikasi dengan ID event Google.

## 5. Sumber data dan aliran data

-   SQLite menyimpan transaksi, kategori, anggaran, target tabungan,
    tugas, event lokal, catatan, dan metadata sinkronisasi.
-   Google Calendar bukan database utama aplikasi.
-   Saat pengguna menekan Sync, aplikasi memproses antrean perubahan
    lokal yang membutuhkan sinkronisasi.
-   Jika jaringan gagal, data lokal tidak dibatalkan; status sync tetap
    pending/failed dan dapat dicoba lagi.
-   Data finansial tidak dikirim ke Google Calendar atau layanan
    eksternal.

## 6. Google Calendar

-   Sinkronisasi satu arah: aplikasi ke kalender utama akun Google.
-   Manual-only; tidak ada background polling.
-   OAuth hanya meminta izin yang diperlukan untuk membuat, membaca
    status event terkait, memperbarui, atau menghapus event yang
    dikelola aplikasi.
-   Simpan `google_event_id` untuk mencegah duplikasi.
-   Simpan status sinkronisasi dan waktu percobaan terakhir.
-   Buat operasi idempotent sejauh API memungkinkan.
-   Jangan menghapus event Google yang tidak memiliki relasi dengan
    event lokal.
-   Saat event lokal dihapus, tandai sebagai pending deletion jika event
    Google masih perlu dihapus. Minta konfirmasi pengguna sebelum
    penghapusan remote.
-   Tangani token kedaluwarsa, pencabutan izin, event yang sudah tidak
    ditemukan, koneksi terputus, dan respons API yang gagal.
-   Event dengan waktu mulai dan selesai; event sepanjang hari di luar
    scope.
-   Jangan mengklaim sinkronisasi berhasil sebelum respons API
    mengonfirmasi operasi.

## 7. Keamanan

-   PIN tidak disimpan plaintext.
-   Gunakan penyimpanan aman untuk secret/token yang didukung platform.
-   Jangan mencatat token, PIN, atau data transaksi sensitif ke log.
-   Autentikasi aplikasi saat dibuka dan setelah terkunci atau melewati
    timeout background.
-   PIN menjadi fallback ketika biometrik tidak tersedia/gagal.
-   File backup SQLite diperlakukan sebagai data sensitif.
-   OAuth harus menggunakan alur yang sesuai untuk aplikasi native;
    jangan menanam client secret privat di aplikasi.
-   Minta scope Google sekecil mungkin sesuai kebutuhan.

## 8. Performa

-   Semua query dan operasi I/O bersifat asynchronous.
-   Gunakan indeks pada kolom yang sering difilter.
-   Agregasi laporan dilakukan di query database bila tepat.
-   Gunakan pagination atau rentang tanggal untuk riwayat transaksi.
-   Hindari rebuild widget seluruh aplikasi untuk perubahan state lokal
    yang kecil.
-   Jangan menjalankan sinkronisasi kalender saat startup secara
    otomatis.
-   Ukur cold start dan warm start menggunakan APK release di perangkat
    uji yang disepakati.

## 9. Backup dan restore

-   Ekspor file SQLite menggunakan mekanisme backup yang konsisten;
    jangan sekadar menyalin file saat transaksi aktif tanpa memastikan
    konsistensinya.
-   Sertakan versi skema dalam metadata pendamping atau format paket
    backup bila diperlukan.
-   Validasi integritas dan kompatibilitas sebelum restore.
-   Buat backup dari database aktif sebelum menggantinya.
-   Restore dilakukan secara atomik atau dengan strategi rollback yang
    aman.
-   Jangan memasukkan database pribadi atau backup ke Git.

## 10. Batasan arsitektur

Tidak ada backend, cloud sync aplikasi, AI, multi-user, atau
sinkronisasi dua arah Google Calendar pada MVP.
