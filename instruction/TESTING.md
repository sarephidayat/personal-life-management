# TESTING --- Personal Life Management App

## 1. Prinsip

-   Test mengikuti perilaku yang disepakati di `PRD.md` dan `RULES.md`.
-   Jalankan test otomatis dan manual.
-   Jangan mengklaim test lulus tanpa menjalankan perintah dan memeriksa
    hasilnya.
-   Uji build release pada perangkat nyata atau emulator yang
    representatif.

## 2. Perintah dasar

Perintah yang relevan:

``` bash
dart format --output=none --set-exit-if-changed .
flutter analyze
flutter test
flutter test integration_test
flutter build apk --release
```

Sesuaikan perintah integration test dengan struktur proyek. Catat hasil
aktual, termasuk test yang tidak dapat dijalankan.

## 3. Unit test Finance Tracker

-   Saldo awal nol tanpa transaksi.
-   Pemasukan menambah saldo.
-   Pengeluaran mengurangi saldo.
-   Kombinasi transaksi menghasilkan saldo yang benar.
-   Mengedit transaksi memperbarui saldo.
-   Menghapus transaksi memperbarui saldo.
-   Nilai nol/negatif ditolak.
-   Nominal besar tidak mengalami overflow pada batas yang ditetapkan.
-   Kategori tidak sesuai tipe transaksi ditolak.
-   Anggaran bulanan menghitung transaksi pada kategori dan bulan yang
    tepat.
-   Transaksi di batas pergantian bulan masuk periode yang benar.
-   Anggaran terlampaui menampilkan sisa negatif dengan benar.
-   Progres target tabungan dan sisa target dihitung dengan benar.
-   Filter tanggal/kategori mengubah laporan dan grafik sesuai data.

## 4. Unit/widget test Personal Planner

-   Tugas dapat dibuat, diedit, dicari, difilter, dan dihapus.
-   Prioritas dan status hanya menerima nilai valid.
-   `completed_at` konsisten ketika status berubah.
-   Deadline opsional ditampilkan dengan benar.
-   Event menolak waktu selesai yang sama dengan atau lebih awal dari
    waktu mulai.
-   Kalender bulanan menampilkan event pada tanggal yang tepat.
-   Pergantian bulan/tahun tidak menggeser event.
-   Catatan harian unik per tanggal.
-   Catatan aktivitas dapat dikaitkan ke event/tugas.
-   UI light/dark mode tidak overflow pada ukuran layar yang diuji.

## 5. Test Google Calendar

Gunakan mock/fake API untuk unit test; lakukan tes integrasi OAuth/API
dengan konfigurasi pengujian yang aman. - Create event lokal
menghasilkan status pending. - Sync membuat event Google dan menyimpan
`google_event_id`. - Sync ulang tidak membuat duplikat. - Update lokal
memperbarui event terkait. - Delete lokal meminta konfirmasi sebelum
remote delete. - Event eksternal tanpa relasi lokal tidak pernah
dihapus. - Offline mempertahankan perubahan lokal. - Retry memproses
operasi yang tertunda. - Token kedaluwarsa meminta autentikasi ulang
dengan pesan yang jelas. - Izin dicabut ditangani tanpa crash. - Event
yang sudah dihapus di Google tidak membuat aplikasi macet. - Timeout
setelah create tidak menyebabkan duplikasi saat retry. - Status UI
membedakan pending, synced, dan failed.

## 6. Test backup/restore

-   Ekspor menghasilkan file SQLite yang valid.
-   Ekspor saat ada operasi aktif tidak menghasilkan backup korup.
-   File non-SQLite ditolak.
-   Database korup ditolak.
-   Backup dengan skema lebih baru/inkompatibel tidak mengganti database
    aktif.
-   Restore sukses memulihkan transaksi, anggaran, target, tugas,
    jadwal, dan catatan.
-   Jika restore gagal, data aktif tetap tersedia.
-   Pengguna menerima konfirmasi sebelum data aktif diganti.
-   File backup tidak tercatat ke Git.

## 7. Test keamanan

-   PIN tidak tersimpan plaintext.
-   Aplikasi meminta autentikasi pada kondisi yang ditentukan.
-   PIN fallback bekerja jika biometrik tidak tersedia/gagal.
-   Token tidak muncul di log.
-   Logout/revoke akses Google ditangani dengan aman.
-   Database dan backup tidak dibagikan ke log atau layanan eksternal.

## 8. Test performa

Uji dengan APK release pada perangkat Android kelas menengah yang
dicatat spesifikasinya. - Cold start dan warm start; target ≤ 2 detik
pada perangkat uji yang memadai. - Navigasi Home/Finance/Planner
responsif. - Scroll riwayat transaksi tetap lancar dengan dataset
pengujian yang realistis. - Grafik tidak memblokir UI. - Tidak ada
operasi jaringan di UI thread. - Tidak ada background polling tanpa
kebutuhan. - Catat waktu, perangkat, ukuran dataset, build type, dan
hasil; jangan melaporkan target tercapai tanpa pengukuran.

## 9. Smoke test APK

-   APK release berhasil dibangun.
-   APK dapat di-install dan dibuka.
-   PIN/biometrik dapat dikonfigurasi dan digunakan.
-   Finance Tracker berfungsi offline.
-   Planner berfungsi offline.
-   Sinkronisasi kalender bekerja saat internet tersedia.
-   Backup/restore berhasil.
-   Tema light/dark dapat digunakan.
-   Tidak ada crash pada alur utama.

## 10. Definition of done

Sebuah milestone lulus jika test relevan berhasil, analisis statis tidak
memiliki error baru yang tidak dijelaskan, dan acceptance criteria
milestone terpenuhi. Semua kegagalan yang diketahui harus dicatat
sebelum melanjutkan.
