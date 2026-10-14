# Personal Life Management App

Aplikasi Android pribadi untuk mengelola keuangan dan aktivitas harian
menggunakan Flutter dan SQLite lokal.

## Fitur MVP

-   Finance Tracker: transaksi, saldo, anggaran bulanan, target
    tabungan, grafik dan laporan.
-   Personal Planner: kalender bulanan, tugas, prioritas, deadline,
    catatan harian dan aktivitas.
-   Google Calendar: sinkronisasi manual satu arah untuk event yang
    dikelola aplikasi.
-   PIN dan biometrik.
-   Ekspor dan impor file database SQLite.
-   Light/dark mode dengan desain Neo-Brutalism.
-   Offline-first untuk fungsi lokal.

## Stack

-   Flutter + Dart
-   SQLite
-   Google Calendar API/OAuth untuk sinkronisasi kalender
-   Git + GitHub

Tidak ada backend aplikasi, cloud database, login aplikasi, AI Agent,
sinkronisasi dua arah, transfer antar-dompet, atau transaksi berulang
pada MVP.

## Prasyarat pengembangan

1.  Instal Flutter SDK dan Android SDK yang kompatibel.
2.  Siapkan Android Studio atau IDE pilihan dengan dukungan
    Flutter/Dart.
3.  Pastikan `flutter doctor` tidak menunjukkan masalah kritis untuk
    target Android.
4.  Siapkan konfigurasi OAuth dan Google Calendar API sesuai dokumentasi
    resmi Google sebelum menguji sinkronisasi nyata.

## Setup

Setelah proyek Flutter dibuat:

``` bash
flutter pub get
dart format .
flutter analyze
flutter test
```

## Menjalankan aplikasi

``` bash
flutter run
```

## Build APK release

``` bash
flutter build apk --release
```

Lokasi APK default Flutter:

``` text
build/app/outputs/flutter-apk/app-release.apk
```

Pastikan file tersebut benar-benar ada setelah build berhasil. Untuk
distribusi publik, signing dan pengelolaan keystore harus dikonfigurasi
dengan aman; jangan commit keystore atau password ke Git.

## Workflow milestone

1.  Setup proyek, tema, navigasi, dan CI/analisis.
2.  SQLite, migrasi, dan backup/restore.
3.  Finance Tracker.
4.  Personal Planner.
5.  Google Calendar manual sync.
6.  Penguncian PIN/biometrik.
7.  Testing menyeluruh, optimasi, dan APK release.

Urutan dapat disesuaikan berdasarkan dependency teknis, tetapi setiap
milestone harus diuji sebelum lanjut.

## Dokumentasi proyek

-   `AGENTS.md`: instruksi coding agent.
-   `PRD.md`: kebutuhan produk.
-   `ARCHITECTURE.md`: arsitektur aplikasi.
-   `SCHEMA.md`: desain database.
-   `DESIGN.md`: sistem desain UI.
-   `RULES.md`: aturan bisnis.
-   `TESTING.md`: strategi dan skenario pengujian.

## Keamanan

-   Jangan commit token OAuth, secret, database pribadi, backup, PIN,
    atau keystore.
-   Backup SQLite berisi data sensitif dan tidak otomatis terenkripsi.
-   Uji restore sebelum mengandalkan backup sebagai satu-satunya salinan
    data.
