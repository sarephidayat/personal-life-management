# SCHEMA --- SQLite

## 1. Prinsip skema

-   Gunakan SQLite dengan foreign key aktif.
-   Gunakan integer rupiah untuk semua nilai uang.
-   Simpan tanggal/waktu dalam format ISO-8601 yang konsisten; event
    bertanggal/waktu menyimpan zona waktu atau offset yang diperlukan.
-   Semua tabel memiliki primary key stabil.
-   Terapkan migrasi bernomor; jangan mengubah database pengguna secara
    destruktif tanpa migrasi.
-   Nama tabel/kolom dapat menggunakan `snake_case`.

## 2. Tabel

### `app_settings`

Menyimpan konfigurasi aplikasi yang bukan secret. -
`key TEXT PRIMARY KEY` - `value TEXT NOT NULL` -
`updated_at TEXT NOT NULL`

Jangan menyimpan PIN atau token OAuth pada tabel ini.

### `finance_profile`

Satu record profil keuangan untuk saldo awal. -
`id INTEGER PRIMARY KEY` -
`opening_balance INTEGER NOT NULL DEFAULT 0` -
`created_at TEXT NOT NULL` - `updated_at TEXT NOT NULL`

Validasi nilai dan aturan koreksi saldo awal berada di domain layer.

### `categories`

-   `id INTEGER PRIMARY KEY`
-   `name TEXT NOT NULL`
-   `type TEXT NOT NULL CHECK(type IN ('income','expense'))`
-   `icon_key TEXT`
-   `color_key TEXT`
-   `is_default INTEGER NOT NULL DEFAULT 0`
-   `is_active INTEGER NOT NULL DEFAULT 1`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`

Jangan menghapus kategori yang sudah dipakai transaksi secara cascading;
gunakan `is_active = 0` atau aturan migrasi yang aman.

### `transactions`

-   `id INTEGER PRIMARY KEY`
-   `type TEXT NOT NULL CHECK(type IN ('income','expense'))`
-   `category_id INTEGER NOT NULL REFERENCES categories(id)`
-   `amount INTEGER NOT NULL CHECK(amount > 0)`
-   `transaction_date TEXT NOT NULL`
-   `note TEXT`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`

Indeks: - `(transaction_date)` - `(category_id, transaction_date)` -
`(type, transaction_date)`

Nominal selalu positif; tipe menentukan apakah nominal menambah atau
mengurangi saldo.

### `budgets`

-   `id INTEGER PRIMARY KEY`
-   `category_id INTEGER NOT NULL REFERENCES categories(id)`
-   `year INTEGER NOT NULL`
-   `month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12)`
-   `amount_limit INTEGER NOT NULL CHECK(amount_limit >= 0)`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`
-   UNIQUE `(category_id, year, month)`

Anggaran MVP berlaku per bulan dan kategori pengeluaran.

### `savings_goals`

-   `id INTEGER PRIMARY KEY`
-   `name TEXT NOT NULL`
-   `target_amount INTEGER NOT NULL CHECK(target_amount > 0)`
-   `saved_amount INTEGER NOT NULL DEFAULT 0 CHECK(saved_amount >= 0)`
-   `target_date TEXT`
-   `note TEXT`
-   `status TEXT NOT NULL DEFAULT 'active' CHECK(status IN ('active','completed','archived'))`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`

Asumsi MVP: progres disimpan sebagai nilai yang dicatat pengguna; target
bukan rekening terpisah. Jika kelak diperlukan audit alokasi tabungan,
buat tabel ledger khusus melalui keputusan produk baru.

### `tasks`

-   `id INTEGER PRIMARY KEY`
-   `title TEXT NOT NULL`
-   `description TEXT`
-   `priority TEXT NOT NULL CHECK(priority IN ('high','medium','low'))`
-   `status TEXT NOT NULL DEFAULT 'todo' CHECK(status IN ('todo','in_progress','done'))`
-   `deadline TEXT`
-   `completed_at TEXT`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`
-   `deleted_at TEXT`

`deleted_at` dapat digunakan untuk soft-delete internal jika dibutuhkan
sinkronisasi atau pemulihan; UI tetap meminta konfirmasi penghapusan.

### `calendar_events`

-   `id INTEGER PRIMARY KEY`
-   `task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL`
-   `title TEXT NOT NULL`
-   `description TEXT`
-   `location TEXT`
-   `start_at TEXT NOT NULL`
-   `end_at TEXT NOT NULL`
-   `time_zone TEXT NOT NULL`
-   `google_event_id TEXT UNIQUE`
-   `sync_status TEXT NOT NULL DEFAULT 'local_only' CHECK(sync_status IN ('local_only','pending_create','synced','pending_update','pending_delete','failed'))`
-   `last_sync_at TEXT`
-   `last_sync_error TEXT`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`
-   `deleted_at TEXT`

Aturan: - `end_at` harus lebih besar dari `start_at`. - Event yang
dihapus tetapi belum berhasil dihapus dari Google dipertahankan sebagai
tombstone (`deleted_at`) sampai operasi sync selesai. - Event sepanjang
hari tidak didukung MVP. - Hanya event yang dibuat/dikelola aplikasi
yang ditautkan dengan `google_event_id`.

Indeks: - `(start_at, end_at)` - `(sync_status)` - `(task_id)`

### `daily_notes`

-   `id INTEGER PRIMARY KEY`
-   `note_date TEXT NOT NULL UNIQUE`
-   `content TEXT NOT NULL`
-   `created_at TEXT NOT NULL`
-   `updated_at TEXT NOT NULL`

### `activity_notes`

Catatan yang melekat pada jadwal/aktivitas. - `id INTEGER PRIMARY KEY` -
`calendar_event_id INTEGER REFERENCES calendar_events(id) ON DELETE SET NULL` -
`task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL` -
`title TEXT` - `content TEXT NOT NULL` - `activity_date TEXT NOT NULL` -
`created_at TEXT NOT NULL` - `updated_at TEXT NOT NULL`

Setidaknya salah satu relasi aktivitas dapat diisi; validasi tambahan
dilakukan di domain layer.

## 3. Data turunan

-   Saldo saat ini = saldo awal + total pemasukan - total pengeluaran.
-   Total anggaran terpakai dihitung dari transaksi expense pada
    kategori dan bulan yang sama.
-   Sisa anggaran = batas anggaran - realisasi pengeluaran.
-   Progres target tabungan = `saved_amount / target_amount`, dibatasi
    untuk tampilan progress bar 0--100%; nilai tersimpan tidak boleh
    berubah diam-diam.
-   Laporan menggunakan query dengan batas tanggal yang eksplisit.

## 4. Transaksi database

Operasi multi-record yang harus konsisten memakai transaction SQL.
Perubahan transaksi wajib membuat dashboard/laporan mencerminkan data
terbaru setelah commit.

## 5. Migrasi dan backup

-   Simpan `user_version` SQLite atau mekanisme versi migrasi yang
    setara.
-   Setiap migrasi memiliki tes.
-   Restore database harus memeriksa integritas dan versi skema sebelum
    menggantikan database aktif.
-   Jika backup berasal dari versi yang lebih baru atau tidak
    kompatibel, batalkan restore dan jelaskan alasannya.
