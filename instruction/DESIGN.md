# DESIGN --- Neo-Brutalism UI System

## 1. Arah visual

Gunakan **Neo-Brutalism** yang jelas dan konsisten: border tebal, bentuk
sederhana, kontras tinggi, warna aksen berani, dan hard shadow. Tetap
prioritaskan keterbacaan, kenyamanan penggunaan sehari-hari, serta
performa.

Jangan meniru desain referensi tertentu secara persis. Buat sistem
visual milik aplikasi ini.

## 2. Prinsip

-   Minimal dalam jumlah elemen, tegas dalam bentuk visual.
-   Gunakan border solid dan shadow keras, bukan blur/glassmorphism.
-   Hindari dekorasi yang tidak membantu tugas pengguna.
-   Tombol utama mudah ditemukan dan memiliki label jelas.
-   Status tidak boleh dibedakan hanya melalui warna; sertakan
    teks/icon.
-   Semua komponen harus konsisten dalam light dan dark mode.
-   Hindari animasi kompleks atau terus berjalan.

## 3. Palet warna awal

Nilai ini adalah baseline dan dapat disempurnakan saat implementasi,
tetapi harus tetap konsisten.

### Light mode

-   Background: `#F7F4EC`
-   Surface/card: `#FFFFFF`
-   Text utama: `#171717`
-   Border: `#171717`
-   Primary accent: `#F7D84A`
-   Secondary accent: `#B8F2D0`
-   Tertiary accent: `#A8C7FF`
-   Danger: `#FF8A80`

### Dark mode

-   Background: `#171717`
-   Surface/card: `#242424`
-   Text utama: `#F7F4EC`
-   Border: `#F7F4EC`
-   Primary accent: `#F7D84A`
-   Secondary accent: `#B8F2D0`
-   Tertiary accent: `#A8C7FF`
-   Danger: `#FF8A80`

Pastikan kontras teks dan kontrol diperiksa; warna aksen dapat dipadukan
dengan teks gelap jika lebih terbaca.

## 4. Bentuk dan komponen

-   Border umum: 2 px; komponen yang perlu penekanan dapat menggunakan 3
    px.
-   Sudut: kecil hingga sedang, sekitar 8--14 px; hindari pill
    berlebihan.
-   Hard shadow: offset kecil, tanpa blur, misalnya 3--4 px.
-   Tombol utama: aksen solid, border tegas, label kontras.
-   Kartu: surface solid, border tegas, shadow konsisten.
-   Input: label selalu terlihat, error message jelas, target sentuh
    nyaman.
-   Dialog destruktif harus menyebutkan tindakan secara eksplisit.
-   Gunakan icon yang konsisten dan mudah dikenali.

## 5. Tipografi

-   Gunakan font yang mudah dibaca dan tersedia secara lokal jika
    memungkinkan.
-   Hierarki: heading halaman, section heading, body, caption.
-   Hindari terlalu banyak variasi ukuran dan weight.
-   Nominal uang memakai angka yang mudah dipindai; pertahankan format
    Rupiah Indonesia.

## 6. Navigasi

Bottom navigation dengan tiga tujuan: 1. Home 2. Finance 3. Planner

Pengaturan, backup/restore, keamanan, dan tema dapat diakses dari
halaman Home atau menu pengaturan yang jelas. Jangan menambahkan tab
utama baru tanpa persetujuan.

## 7. Dashboard

Tampilkan: - Saldo saat ini. - Ringkasan pemasukan dan pengeluaran bulan
berjalan. - Ringkasan anggaran. - Target tabungan. - Agenda terdekat. -
Ringkasan tugas belum selesai.

Hindari menampilkan semua transaksi dan semua tugas sekaligus. Gunakan
ringkasan dan tautan menuju halaman detail.

## 8. Finance Tracker

-   Form transaksi manual dengan field esensial.
-   Jenis transaksi income/expense harus jelas.
-   Riwayat transaksi dengan filter tanggal/kategori.
-   Anggaran memperlihatkan limit, terpakai, dan sisa.
-   Target tabungan memperlihatkan target, progres, dan sisa nominal.
-   Grafik harus sederhana dan memiliki label/angka pendamping.

## 9. Personal Planner

-   Tampilan kalender bulanan sebagai tampilan utama.
-   Indikator agenda pada tanggal yang memiliki event.
-   Pemilihan tanggal menampilkan daftar agenda hari tersebut.
-   Daftar tugas dan papan status dapat diakses dari Planner.
-   Prioritas menggunakan label dan warna, bukan warna saja.
-   Catatan harian dan catatan aktivitas dibedakan dengan jelas.
-   Status sinkronisasi kalender terlihat dan dapat dijelaskan kepada
    pengguna.

## 10. Tema dan aksesibilitas

-   Mendukung light dan dark mode.
-   Sediakan pilihan mengikuti sistem atau memilih tema secara eksplisit
    bila tidak menambah kompleksitas berarti.
-   Target sentuh minimal sekitar 44--48 logical pixels untuk kontrol
    utama.
-   Perhatikan text scaling dan overflow.
-   Semua ikon penting memiliki label aksesibilitas.
-   Jangan mengandalkan warna sebagai satu-satunya indikator status.

## 11. Motion dan performa

-   Gunakan transisi sederhana dan singkat.
-   Hormati pengaturan reduced motion bila tersedia.
-   Tidak ada animasi latar, partikel, blur berat, atau efek paralaks.
-   Hindari rebuild besar yang tidak diperlukan.
