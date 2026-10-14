package com.example.personallife.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "personal_life.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        // 1. app_settings
        db.execSQL("""
            CREATE TABLE app_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // 2. finance_profile
        db.execSQL("""
            CREATE TABLE finance_profile (
                id INTEGER PRIMARY KEY,
                opening_balance INTEGER NOT NULL DEFAULT 0,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // 3. categories
        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL CHECK(type IN ('income','expense')),
                icon_key TEXT,
                color_key TEXT,
                is_default INTEGER NOT NULL DEFAULT 0,
                is_active INTEGER NOT NULL DEFAULT 1,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // 4. transactions
        db.execSQL("""
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL CHECK(type IN ('income','expense')),
                category_id INTEGER NOT NULL REFERENCES categories(id),
                amount INTEGER NOT NULL CHECK(amount > 0),
                transaction_date TEXT NOT NULL,
                note TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_trans_date ON transactions(transaction_date);")
        db.execSQL("CREATE INDEX idx_trans_cat_date ON transactions(category_id, transaction_date);")
        db.execSQL("CREATE INDEX idx_trans_type_date ON transactions(type, transaction_date);")

        // 5. budgets
        db.execSQL("""
            CREATE TABLE budgets (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category_id INTEGER NOT NULL REFERENCES categories(id),
                year INTEGER NOT NULL,
                month INTEGER NOT NULL CHECK(month BETWEEN 1 AND 12),
                amount_limit INTEGER NOT NULL CHECK(amount_limit >= 0),
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                UNIQUE (category_id, year, month)
            );
        """.trimIndent())

        // 6. savings_goals
        db.execSQL("""
            CREATE TABLE savings_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                target_amount INTEGER NOT NULL CHECK(target_amount > 0),
                saved_amount INTEGER NOT NULL DEFAULT 0 CHECK(saved_amount >= 0),
                target_date TEXT,
                note TEXT,
                status TEXT NOT NULL DEFAULT 'active' CHECK(status IN ('active','completed','archived')),
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // 7. tasks
        db.execSQL("""
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                priority TEXT NOT NULL CHECK(priority IN ('high','medium','low')),
                status TEXT NOT NULL DEFAULT 'todo' CHECK(status IN ('todo','in_progress','done')),
                deadline TEXT,
                completed_at TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                deleted_at TEXT
            );
        """.trimIndent())

        // 8. calendar_events
        db.execSQL("""
            CREATE TABLE calendar_events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL,
                title TEXT NOT NULL,
                description TEXT,
                location TEXT,
                start_at TEXT NOT NULL,
                end_at TEXT NOT NULL,
                time_zone TEXT NOT NULL,
                google_event_id TEXT UNIQUE,
                sync_status TEXT NOT NULL DEFAULT 'local_only' CHECK(sync_status IN ('local_only','pending_create','synced','pending_update','pending_delete','failed')),
                last_sync_at TEXT,
                last_sync_error TEXT,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL,
                deleted_at TEXT
            );
        """.trimIndent())
        db.execSQL("CREATE INDEX idx_events_start_end ON calendar_events(start_at, end_at);")
        db.execSQL("CREATE INDEX idx_events_sync_status ON calendar_events(sync_status);")
        db.execSQL("CREATE INDEX idx_events_task_id ON calendar_events(task_id);")

        // 9. daily_notes
        db.execSQL("""
            CREATE TABLE daily_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                note_date TEXT NOT NULL UNIQUE,
                content TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // 10. activity_notes
        db.execSQL("""
            CREATE TABLE activity_notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                calendar_event_id INTEGER REFERENCES calendar_events(id) ON DELETE SET NULL,
                task_id INTEGER REFERENCES tasks(id) ON DELETE SET NULL,
                title TEXT,
                content TEXT NOT NULL,
                activity_date TEXT NOT NULL,
                created_at TEXT NOT NULL,
                updated_at TEXT NOT NULL
            );
        """.trimIndent())

        // Seed initial data
        seedDefaults(db, now)
    }

    private fun seedDefaults(db: SQLiteDatabase, now: String) {
        // Finance profile initial
        val profileValues = ContentValues().apply {
            put("id", 1)
            put("opening_balance", 0)
            put("created_at", now)
            put("updated_at", now)
        }
        db.insert("finance_profile", null, profileValues)

        // Default categories
        val defaultCategories = listOf(
            // Incomes
            Triple("Gaji", "income", "payments"),
            Triple("Bisnis", "income", "storefront"),
            Triple("Investasi", "income", "trending_up"),
            Triple("Pendapatan Lain", "income", "more_horiz"),
            // Expenses
            Triple("Makanan & Minuman", "expense", "restaurant"),
            Triple("Transportasi", "expense", "directions_car"),
            Triple("Belanja & Kebutuhan", "expense", "shopping_cart"),
            Triple("Tagihan & Utilitas", "expense", "receipt_long"),
            Triple("Kesehatan", "expense", "favorite"),
            Triple("Hiburan & Hobi", "expense", "sports_esports"),
            Triple("Pendidikan", "expense", "school"),
            Triple("Pengeluaran Lain", "expense", "category")
        )

        for ((name, type, icon) in defaultCategories) {
            val cv = ContentValues().apply {
                put("name", name)
                put("type", type)
                put("icon_key", icon)
                put("color_key", if (type == "income") "#B8F2D0" else "#FF8A80")
                put("is_default", 1)
                put("is_active", 1)
                put("created_at", now)
                put("updated_at", now)
            }
            db.insert("categories", null, cv)
        }

        seedSampleDataIfEmpty(db, now)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        seedSampleDataIfEmpty(db, now)
    }

    private fun seedSampleDataIfEmpty(db: SQLiteDatabase, now: String) {
        val countCursor = db.rawQuery("SELECT COUNT(*) FROM transactions", null)
        var count = 0
        if (countCursor.moveToFirst()) {
            count = countCursor.getInt(0)
        }
        countCursor.close()

        if (count == 0) {
            val today = java.time.LocalDate.now().toString()
            val yesterday = java.time.LocalDate.now().minusDays(1).toString()
            val twoDaysAgo = java.time.LocalDate.now().minusDays(2).toString()

            // Find categories
            val catCursor = db.rawQuery("SELECT id, name, type FROM categories", null)
            val catMap = mutableMapOf<String, Long>()
            while (catCursor.moveToNext()) {
                catMap[catCursor.getString(1)] = catCursor.getLong(0)
            }
            catCursor.close()

            val gajiId = catMap["Gaji"] ?: 1L
            val belanjaId = catMap["Belanja & Kebutuhan"] ?: 7L
            val makanId = catMap["Makanan & Minuman"] ?: 5L
            val transportId = catMap["Transportasi"] ?: 6L
            val tagihanId = catMap["Tagihan & Utilitas"] ?: 8L

            // Sample Transactions
            val sampleTx = listOf(
                arrayOf("income", gajiId.toString(), "8500000", twoDaysAgo, "Gaji Bulanan Pekerjaan"),
                arrayOf("expense", belanjaId.toString(), "750000", yesterday, "Belanja Kebutuhan Dapur"),
                arrayOf("expense", makanId.toString(), "45000", today, "Makan Siang & Kopi"),
                arrayOf("expense", transportId.toString(), "120000", yesterday, "Isi Bensin Kendaraan"),
                arrayOf("expense", tagihanId.toString(), "480000", twoDaysAgo, "Tagihan Listrik & WiFi")
            )

            for (tx in sampleTx) {
                val cv = ContentValues().apply {
                    put("type", tx[0])
                    put("category_id", tx[1].toLong())
                    put("amount", tx[2].toLong())
                    put("transaction_date", tx[3])
                    put("note", tx[4])
                    put("created_at", now)
                    put("updated_at", now)
                }
                db.insert("transactions", null, cv)
            }

            // Sample Tasks
            val sampleTasks = listOf(
                arrayOf("Review draft laporan anggaran Q4", "high", "in_progress", today),
                arrayOf("Bayar tagihan listrik bulanan", "medium", "todo", java.time.LocalDate.now().plusDays(1).toString()),
                arrayOf("Siapkan presentasi evaluasi mingguan", "high", "todo", java.time.LocalDate.now().plusDays(2).toString()),
                arrayOf("Beli perlengkapan kerja & buku", "low", "done", yesterday),
                arrayOf("Cek target tabungan dana darurat", "medium", "done", twoDaysAgo)
            )

            for (t in sampleTasks) {
                val cv = ContentValues().apply {
                    put("title", t[0])
                    put("priority", t[1])
                    put("status", t[2])
                    put("deadline", t[3])
                    put("completed_at", if (t[2] == "done") now else null)
                    put("created_at", now)
                    put("updated_at", now)
                }
                db.insert("tasks", null, cv)
            }
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations will go here
    }
}
