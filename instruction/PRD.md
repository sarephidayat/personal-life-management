# PRD --- Personal Life Management App

## 1. Ringkasan produk

**Nama:** Personal Life Management App\
**Platform MVP:** Android\
**Framework:** Flutter + Dart\
**Penyimpanan:** SQLite lokal\
**Integrasi eksternal:** Google Calendar, sinkronisasi manual satu arah\
**Bahasa:** Bahasa Indonesia

Aplikasi ini membantu satu pengguna mengelola pencatatan keuangan
pribadi dan aktivitas harian dari satu aplikasi yang ringan, cepat, dan
dapat digunakan offline untuk fitur lokal.

## 2. Tujuan

-   Mengetahui saldo, arus kas, pengeluaran per kategori, dan progres
    anggaran.
-   Mencatat target tabungan dan progresnya.
-   Mengelola tugas, jadwal, deadline, serta catatan aktivitas.
-   Mengirim event yang dikelola aplikasi ke Google Calendar secara
    manual.
-   Menyimpan data secara lokal tanpa akun aplikasi atau backend.

## 3. Pengguna dan konteks

-   Satu pengguna pada satu perangkat Android.
-   Pengguna ingin pencatatan sederhana dan cepat.
-   Data pribadi harus tetap tersedia tanpa koneksi internet.
-   Koneksi internet hanya dibutuhkan untuk autentikasi/sinkronisasi
    Google Calendar dan proses terkait.

## 4. Scope MVP

### 4.1 Finance Tracker

-   Menampilkan saldo saat ini.
-   Mencatat pemasukan dan pengeluaran secara manual.
-   Mengelola kategori pemasukan dan pengeluaran.
-   Melihat riwayat transaksi.
-   Mengatur anggaran bulanan per kategori.
-   Mengelola target tabungan dan progres.
-   Menampilkan grafik dan laporan bulanan.
-   Memfilter laporan berdasarkan tanggal dan kategori.
-   Mengedit dan menghapus transaksi dengan konfirmasi.

### 4.2 Personal Planner

-   Kalender bulanan.
-   Daftar tugas dan papan status.
-   Prioritas tugas: tinggi, sedang, rendah.
-   Detail tugas dasar: judul, deadline, status; deskripsi dapat
    disediakan sebagai field opsional jika tidak memperumit UI.
-   Status: To Do, In Progress, Done.
-   Catatan harian.
-   Catatan yang terkait dengan aktivitas/jadwal.
-   Pencarian dan filter tugas berdasarkan status, prioritas, dan
    tanggal.
-   Menjadwalkan tugas secara eksplisit.
-   Menyimpan jadwal dengan waktu mulai dan selesai.
-   Mengirim jadwal ke Google Calendar melalui sinkronisasi manual.

### 4.3 Dashboard

-   Ringkasan saldo.
-   Ringkasan pemasukan dan pengeluaran bulan berjalan.
-   Ringkasan anggaran.
-   Target tabungan dan progres.
-   Agenda terdekat.
-   Ringkasan tugas yang belum selesai.
-   Tidak memuat seluruh riwayat transaksi sekaligus.

### 4.4 Keamanan dan data

-   SQLite lokal.
-   PIN dan biometrik.
-   Tidak ada akun aplikasi.
-   Ekspor dan impor file database SQLite.
-   Validasi file backup sebelum pemulihan.
-   Konfirmasi sebelum operasi destruktif.

## 5. Di luar scope MVP

-   AI Life Assistant atau chatbot.
-   Backend aplikasi dan database cloud.
-   Sinkronisasi data lintas perangkat.
-   Login aplikasi.
-   Sinkronisasi Google Calendar dua arah.
-   Impor event eksternal Google Calendar ke Planner.
-   Transfer antar-dompet/rekening.
-   Transaksi berulang.
-   Multi-currency.
-   Event sepanjang hari.
-   Integrasi bank otomatis.
-   Widget home screen, wearable, dan aplikasi iOS.
-   Fitur lain yang belum disetujui pengguna.

## 6. Kebutuhan nonfungsional

-   Offline-first untuk seluruh fungsi lokal.
-   Target pembukaan aplikasi ≤ 2 detik pada perangkat uji kelas
    menengah yang ditentukan, diukur menggunakan build release.
-   Navigasi responsif; pekerjaan database dan jaringan tidak memblokir
    UI.
-   Penggunaan memori dan baterai dijaga dengan menghindari
    polling/background service yang tidak perlu.
-   Data uang menggunakan integer rupiah.
-   Mendukung light mode dan dark mode.
-   UI Neo-Brutalism dengan kontras dan aksesibilitas yang layak.
-   Penanganan error yang jelas dalam Bahasa Indonesia.
-   Backup dan pemulihan tidak boleh merusak database aktif jika
    validasi gagal.

## 7. Asumsi produk

-   Satu saldo gabungan dengan saldo awal yang dapat diatur.
-   Kategori default tersedia dan pengguna dapat menambah/mengubah
    kategori.
-   Target tabungan adalah target nominal dengan pencatatan progres,
    bukan rekening terpisah.
-   Penghapusan transaksi memerlukan konfirmasi.
-   Tugas dan event kalender merupakan entitas berbeda. Tugas hanya
    menjadi event setelah pengguna menjadwalkannya.
-   Pengingat mengandalkan Google Calendar; tidak ada sistem notifikasi
    lokal khusus pada MVP.
-   Bahasa UI hanya Bahasa Indonesia untuk MVP.

## 8. Metrik penerimaan

-   Pengguna dapat membuat, mengedit, dan menghapus transaksi serta
    melihat saldo yang benar.
-   Anggaran, target tabungan, grafik, dan filter laporan bekerja
    konsisten.
-   Pengguna dapat membuat dan mengelola tugas, jadwal, serta catatan.
-   Sinkronisasi manual dapat membuat/memperbarui event terkait tanpa
    duplikasi.
-   Perubahan yang gagal sinkron tetap tersimpan lokal dan dapat dicoba
    ulang.
-   PIN/biometrik, backup, dan pemulihan berfungsi sesuai dokumen.
-   APK release berhasil dibangun dan pengujian wajib lulus.
