# AGENTS.md --- Personal Life Management App

## 1. Peran dan tujuan

Kamu adalah coding agent yang mengimplementasikan **Personal Life
Management App**, aplikasi Android pribadi untuk pencatatan keuangan dan
perencanaan aktivitas harian. Ikuti dokumen proyek sebagai sumber
kebenaran. Jangan menambahkan fitur di luar scope tanpa persetujuan
pengguna.

## 2. Sumber kebenaran

Baca dokumen berikut sebelum mengubah kode: - `PRD.md`: kebutuhan produk
dan batasan MVP. - `ARCHITECTURE.md`: arsitektur dan batas layer. -
`SCHEMA.md`: skema SQLite dan aturan migrasi. - `DESIGN.md`: sistem
desain Neo-Brutalism. - `RULES.md`: aturan bisnis. - `TESTING.md`:
standar pengujian dan kriteria penerimaan. - `README.md`: setup dan
workflow.

Jika dokumen bertentangan, jangan menebak. Jelaskan konflik dan minta
keputusan. Jangan mengubah keputusan produk hanya demi memudahkan
implementasi.

## 3. Stack yang ditetapkan

-   Flutter + Dart.
-   SQLite lokal sebagai sumber data utama.
-   Android sebagai target MVP.
-   Google Calendar API untuk sinkronisasi satu arah secara manual.
-   Git dan GitHub untuk version control.
-   Bahasa UI: Bahasa Indonesia.
-   Tema: light dan dark mode.
-   Tidak ada backend aplikasi, cloud database, akun aplikasi, atau AI
    Agent pada MVP.

Gunakan package eksternal hanya jika dibutuhkan, aktif dipelihara,
kompatibel dengan versi Flutter yang dipakai, dan memiliki alasan teknis
jelas. Catat dependency serta alasan pemilihannya.

## 4. Aturan implementasi

1.  Kerjakan per milestone; jangan membangun semua fitur sekaligus.
2.  Sebelum coding, jelaskan tujuan milestone, file yang akan diubah,
    dan acceptance criteria.
3.  Jangan melakukan refactor besar di luar kebutuhan milestone.
4.  Pisahkan UI, logika bisnis, akses data, dan integrasi eksternal.
5.  Jangan menaruh perhitungan saldo atau aturan keuangan di widget UI.
6.  Semua operasi database dan jaringan harus asynchronous dan tidak
    memblokir UI thread.
7.  Jangan menyimpan nominal uang dalam floating point. Gunakan integer
    rupiah.
8.  Jangan menyimpan PIN dalam plaintext. Gunakan mekanisme penyimpanan
    kredensial yang sesuai dan hash PIN dengan algoritma yang tepat.
9.  Token OAuth tidak boleh ditulis ke log atau disimpan dalam
    plaintext.
10. Jangan mengirim data keuangan ke layanan eksternal. Data yang
    dikirim ke Google hanya event kalender yang diperlukan untuk
    sinkronisasi.
11. Jangan menghapus event Google yang tidak dibuat/dikelola aplikasi.
12. Jangan membuat fitur AI, cloud sync, multi-account, transfer
    antar-dompet, atau transaksi berulang.
13. Jangan mengklaim pengujian berhasil jika perintah belum dijalankan
    dan hasilnya belum diperiksa.
14. Jangan menambahkan package hanya untuk menghindari penulisan kode
    kecil yang sederhana.
15. Pertahankan aksesibilitas, kontras, dan dukungan tema light/dark.

## 5. Workflow milestone wajib

Untuk setiap milestone: 1. Baca dokumen relevan. 2. Periksa kondisi
repository dan `git status`. 3. Buat rencana implementasi singkat. 4.
Implementasikan scope milestone saja. 5. Jalankan `dart format`,
`flutter analyze`, dan test relevan. 6. Perbaiki regresi yang disebabkan
perubahan. 7. Laporkan file yang berubah, test yang dijalankan, hasil
aktual, risiko tersisa, dan langkah berikutnya. 8. Jangan lanjut ke
milestone berikutnya sebelum milestone saat ini diterima.

## 6. Git

-   Gunakan commit kecil dengan pesan deskriptif.
-   Jangan menghapus atau menimpa perubahan pengguna.
-   Jangan force-push.
-   Jangan memasukkan secret, token, file konfigurasi OAuth privat,
    database pribadi, atau backup ke Git.
-   Tambahkan file sensitif ke `.gitignore`.

## 7. Definition of done per perubahan

Sebuah perubahan dianggap selesai jika: - Sesuai scope dan dokumen. -
Memiliki validasi/error handling yang wajar. - UI mengikuti
`DESIGN.md`. - Tidak merusak data yang sudah tersimpan. - Test relevan
lulus. - `flutter analyze` tidak menunjukkan error baru yang belum
dijelaskan. - Dokumentasi diperbarui jika perilaku atau setup berubah.

## 8. Ketidakpastian

Jika kebutuhan belum jelas, gunakan asumsi yang sudah tertulis di
dokumen dan tandai asumsi tersebut. Tanyakan hanya ketika keputusan
berdampak pada skema data, keamanan, perilaku sinkronisasi, atau
pengalaman pengguna secara material.
