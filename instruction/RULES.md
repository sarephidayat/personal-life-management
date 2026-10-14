# RULES --- Business Rules

## 1. Finance Tracker

### 1.1 Tipe transaksi

-   `income`: menambah saldo.
-   `expense`: mengurangi saldo.
-   Nominal harus lebih besar dari nol dan disimpan sebagai integer
    rupiah.
-   Tanggal transaksi wajib ada.
-   Kategori harus sesuai tipe transaksi.

### 1.2 Perhitungan saldo

`current_balance = opening_balance + sum(income) - sum(expense)`

-   Transfer antar-dompet tidak didukung.
-   Transaksi berulang tidak didukung.
-   Saldo dihitung dari sumber data transaksi, bukan nilai cache yang
    tidak tervalidasi.
-   Mengedit/menghapus transaksi harus mengubah hasil saldo dan laporan.

### 1.3 Kategori

-   Kategori memiliki tipe income atau expense.
-   Kategori yang sudah dipakai tidak boleh dihapus dengan cara yang
    menyebabkan transaksi kehilangan relasi.
-   Kategori yang tidak ingin digunakan lagi dapat dinonaktifkan.
-   Filter laporan menggunakan kategori transaksi.

### 1.4 Anggaran bulanan

-   Satu anggaran per kategori pengeluaran untuk satu bulan dan tahun.
-   `spent = sum(expense)` pada kategori dan bulan yang sama.
-   `remaining = budget_limit - spent`.
-   Anggaran terlampaui jika `spent > budget_limit`.
-   Nilai sisa dapat negatif jika anggaran terlampaui; UI harus
    menunjukkan kondisi tersebut dengan jelas.
-   Transaksi yang diedit/dihapus harus memperbarui realisasi.

### 1.5 Target tabungan

-   Setiap target memiliki nama dan nominal target positif.
-   Progres berasal dari nilai `saved_amount` yang dicatat.
-   `remaining = max(target_amount - saved_amount, 0)` untuk tampilan
    sisa target.
-   Target selesai ketika nilai progres mencapai atau melampaui target.
-   Progres target bukan bagian dari saldo secara otomatis dan bukan
    rekening terpisah.
-   Jangan menganggap seluruh saldo sebagai tabungan tanpa tindakan
    pengguna.

### 1.6 Laporan

-   Laporan default menggunakan bulan berjalan.
-   Filter kategori dan rentang tanggal diterapkan konsisten.
-   Pemasukan dan pengeluaran ditampilkan terpisah.
-   Grafik harus sesuai dengan data transaksi yang difilter.
-   Gunakan batas tanggal yang benar untuk bulan lokal, termasuk
    pergantian tahun.

## 2. Personal Planner

### 2.1 Tugas

-   Judul wajib diisi.
-   Prioritas hanya `high`, `medium`, atau `low`.
-   Status hanya `todo`, `in_progress`, atau `done`.
-   Deadline bersifat opsional.
-   Saat tugas ditandai `done`, set `completed_at`; ketika status
    berubah dari `done`, hapus/reset nilai tersebut secara konsisten.
-   Tugas tidak otomatis menjadi event hanya karena memiliki deadline.
-   Pengguna harus menjadwalkan tugas secara eksplisit jika ingin
    membuat event kalender.

### 2.2 Event kalender

-   Judul, waktu mulai, waktu selesai, dan zona waktu wajib ada.
-   Waktu selesai harus lebih besar daripada waktu mulai.
-   Event sepanjang hari tidak didukung MVP.
-   Event lokal tetap tersedia ketika offline.
-   Event yang belum pernah dikirim ke Google tidak memerlukan
    penghapusan remote.
-   Penghapusan event yang telah tersinkron memerlukan konfirmasi
    sebelum menghapus event Google terkait.

### 2.3 Catatan

-   Satu catatan harian per tanggal.
-   Catatan aktivitas dapat dikaitkan dengan event atau tugas.
-   Menghapus event/tugas tidak boleh menghapus catatan secara tidak
    sengaja tanpa kebijakan yang jelas.
-   Operasi hapus meminta konfirmasi.

### 2.4 Pencarian dan filter

-   Pencarian tugas berdasarkan judul dan deskripsi jika tersedia.
-   Filter status, prioritas, dan tanggal harus dapat digabungkan.
-   Filter yang aktif harus terlihat dan dapat dihapus/reset.

## 3. Google Calendar sync

### 3.1 Arah dan trigger

-   Arah: aplikasi → Google Calendar.
-   Trigger: pengguna menekan tombol Sync.
-   Kalender tujuan: kalender utama akun Google.
-   Event dari Google yang tidak dibuat/dikelola aplikasi tidak diimpor
    ke Planner.
-   Tidak ada background sync otomatis.

### 3.2 Status sync

-   `local_only`: belum membutuhkan sinkronisasi.
-   `pending_create`: perlu dibuat di Google.
-   `synced`: berhasil dikaitkan dengan event Google.
-   `pending_update`: perubahan lokal menunggu dikirim.
-   `pending_delete`: event Google terkait perlu dihapus setelah
    konfirmasi.
-   `failed`: operasi terakhir gagal; pengguna dapat mencoba lagi.

Implementasi boleh menggunakan status tambahan bila diperlukan, tetapi
tidak boleh kehilangan arti status di atas.

### 3.3 Idempotensi dan retry

-   Simpan Google event ID setelah create berhasil.
-   Jangan membuat event baru jika event yang sama sudah mempunyai ID
    Google dan hanya perlu update.
-   Jika create request timeout dan hasilnya tidak diketahui, periksa
    kemungkinan event sudah dibuat sebelum mengulang create.
-   Pertahankan data lokal ketika jaringan gagal.
-   Tampilkan status dan pesan error yang dapat dipahami pengguna.
-   Jangan menandai berhasil sebelum hasil operasi diverifikasi.

### 3.4 Penghapusan

-   Hanya hapus event Google yang terhubung ke event lokal yang dikelola
    aplikasi.
-   Minta konfirmasi pengguna sebelum penghapusan remote.
-   Jika offline, simpan penanda pending delete dan proses pada Sync
    berikutnya.
-   Jika event Google sudah tidak ditemukan, tangani sebagai kondisi
    yang sudah terpenuhi dan rapikan relasi lokal secara aman.
-   Jangan pernah menghapus event eksternal yang tidak memiliki relasi
    ke data lokal.

## 4. Keamanan dan backup

-   Autentikasi saat aplikasi dibuka dan setelah terkunci atau melewati
    batas waktu di background.
-   PIN tidak disimpan plaintext.
-   Biometrik menggunakan mekanisme autentikasi platform; PIN menjadi
    fallback.
-   Jangan mencatat PIN, token, atau data keuangan sensitif ke log.
-   Ekspor SQLite harus konsisten.
-   Impor memvalidasi file dan integritas database sebelum mengganti
    data aktif.
-   Buat backup data aktif sebelum restore.
-   Operasi destruktif meminta konfirmasi.
-   File backup mengandung data sensitif dan harus diperlakukan dengan
    hati-hati.
-   Backup SQLite langsung tidak dianggap terenkripsi secara otomatis.

## 5. Lokalisasi

-   Bahasa UI: Bahasa Indonesia.
-   Mata uang: IDR/Rupiah.
-   Format tanggal tampilan: DD/MM/YYYY.
-   Penyimpanan tanggal/waktu harus konsisten dan tidak menimbulkan
    pergeseran tanggal karena konversi zona waktu.
