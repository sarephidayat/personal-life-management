# Personal Life Management

Aplikasi manajemen kehidupan pribadi (Personal Life Management) offline-first berbasis Android yang dibangun dengan **Kotlin** dan **Jetpack Compose**, mengadopsi bahasa desain **Neo-Brutalism** dengan kontras tinggi, border tebal, warna aksen berani, dan hard shadow.

---

## 🚀 Fitur Utama

### 1. Dashboard (Beranda)
- **Saldo Saat Ini**: Perhitungan dinamis dari saldo awal + total pemasukan - total pengeluaran.
- **Ringkasan Arus Kas**: Pemasukan vs. pengeluaran bulan berjalan.
- **Ringkasan Anggaran**: Realisasi vs. batas anggaran bulanan dengan indikator over-budget.
- **Target Tabungan**: Progress bar persentase pencapaian impian finansial.
- **Agenda Terdekat**: Jadwal dan rapat mendatang untuk hari ini dan minggu ini.
- **Ringkasan Tugas**: Jumlah tugas yang belum selesai (To Do & In Progress) dengan tanda prioritas.
- **Aksi Cepat**: Tombol pintas untuk menambah transaksi atau tugas seketika.

### 2. Finance Tracker (Keuangan)
- **Pencatatan Transaksi**: Pemasukan dan pengeluaran manual (nominal integer Rupiah, tanggal, kategori, dan catatan opsional).
- **Pencarian & Filter**: Filter berdasarkan jenis transaksi (Semua, Pemasukan, Pengeluaran) dan pencarian kata kunci.
- **Anggaran Bulanan**: Atur batas anggaran per kategori pengeluaran per bulan.
- **Target Tabungan**: Tetapkan target nominal tabungan dan lakukan setoran berkala.
- **Laporan & Grafik Arus Kas**: Laporan bulanan, breakdown persentase pengeluaran & pemasukan per kategori.
- **Manajemen Kategori**: Kategori default bawaan dan kemampuan menambah kategori kustom baru.

### 3. Personal Planner (Aktivitas)
- **Kalender Bulanan**: Grid kalender interaktif dengan indikator titik penanda hari yang memiliki agenda.
- **Manajemen Tugas**: Pelacakan status (To Do, In Progress, Done), tingkat prioritas (Tinggi, Sedang, Rendah), tenggat waktu (deadline), dan fitur jadwal ke kalender.
- **Catatan Harian & Notulensi Aktivitas**: Catatan refleksi per tanggal serta catatan khusus yang melekat pada tugas/jadwal.
- **Sinkronisasi Google Calendar**: Antrean sinkronisasi manual satu arah dari aplikasi ke Google Calendar (status: `local_only`, `pending_create`, `synced`, `pending_update`, `pending_delete`, `failed`).

### 4. Keamanan & Pengaturan
- **Kunci Aplikasi (PIN)**: PIN 4-digit dengan proteksi enkripsi SHA-256 + Salt.
- **Tema Visual**: Mendukung mode Terang, Gelap, dan Mengikuti Sistem.
- **Saldo Awal**: Pengaturan dan penyesuaian saldo awal akun.
- **Cadangan & Pemulihan (Backup & Restore)**: Ekspor dan impor data SQLite/JSON lengkap dengan validasi integritas skema.

---

## 🛠️ Arsitektur & Teknologi

- **Bahasa**: Kotlin (JVM Toolchain 21)
- **UI Framework**: Jetpack Compose dengan Material 3 & Custom Neo-Brutalism System
- **Penyimpanan**: SQLite lokal offline-first (`SQLiteOpenHelper`) dengan penegakan Foreign Keys (`PRAGMA foreign_keys = ON;`)
- **State Management**: Android `ViewModel`, Kotlin Coroutines, dan `StateFlow`
- **Build System**: Gradle 9.3.1 (Kotlin DSL) dengan Android Gradle Plugin (AGP) 9.1.1