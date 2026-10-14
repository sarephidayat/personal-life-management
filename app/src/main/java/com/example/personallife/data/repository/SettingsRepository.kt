package com.example.personallife.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.personallife.data.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class SettingsRepository(private val context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    private fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    suspend fun getSetting(key: String, defaultValue: String = ""): String = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT value FROM app_settings WHERE key = ?", arrayOf(key))
        var value = defaultValue
        if (cursor.moveToFirst()) {
            value = cursor.getString(0)
        }
        cursor.close()
        value
    }

    suspend fun setSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("key", key)
            put("value", value)
            put("updated_at", time)
        }
        val updated = db.update("app_settings", cv, "key = ?", arrayOf(key))
        if (updated == 0) {
            db.insert("app_settings", null, cv)
        }
    }

    // --- PIN SECURITY ---

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest((pin + salt).toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun isPinEnabled(): Boolean = withContext(Dispatchers.IO) {
        getSetting("pin_enabled", "false") == "true"
    }

    suspend fun setPin(pin: String) = withContext(Dispatchers.IO) {
        val salt = UUID.randomUUID().toString()
        val hash = hashPin(pin, salt)
        setSetting("pin_salt", salt)
        setSetting("pin_hash", hash)
        setSetting("pin_enabled", "true")
    }

    suspend fun disablePin() = withContext(Dispatchers.IO) {
        setSetting("pin_enabled", "false")
        setSetting("pin_hash", "")
        setSetting("pin_salt", "")
    }

    suspend fun verifyPin(enteredPin: String): Boolean = withContext(Dispatchers.IO) {
        val salt = getSetting("pin_salt", "")
        val expectedHash = getSetting("pin_hash", "")
        if (salt.isEmpty() || expectedHash.isEmpty()) return@withContext false
        val computed = hashPin(enteredPin, salt)
        computed == expectedHash
    }

    // --- BACKUP & RESTORE ---

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("version", DatabaseHelper.DATABASE_VERSION)
        root.put("app", "Personal Life Management")
        root.put("timestamp", now())

        val db = dbHelper.readableDatabase

        // Categories
        val catArray = JSONArray()
        val cCursor = db.rawQuery("SELECT id, name, type, icon_key, color_key, is_default, is_active FROM categories", null)
        while (cCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", cCursor.getLong(0))
                put("name", cCursor.getString(1))
                put("type", cCursor.getString(2))
                put("icon_key", cCursor.getString(3))
                put("color_key", cCursor.getString(4))
                put("is_default", cCursor.getInt(5))
                put("is_active", cCursor.getInt(6))
            }
            catArray.put(obj)
        }
        cCursor.close()
        root.put("categories", catArray)

        // Transactions
        val tArray = JSONArray()
        val tCursor = db.rawQuery("SELECT id, type, category_id, amount, transaction_date, note FROM transactions", null)
        while (tCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", tCursor.getLong(0))
                put("type", tCursor.getString(1))
                put("category_id", tCursor.getLong(2))
                put("amount", tCursor.getLong(3))
                put("transaction_date", tCursor.getString(4))
                put("note", tCursor.getString(5))
            }
            tArray.put(obj)
        }
        tCursor.close()
        root.put("transactions", tArray)

        // Budgets
        val bArray = JSONArray()
        val bCursor = db.rawQuery("SELECT id, category_id, year, month, amount_limit FROM budgets", null)
        while (bCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", bCursor.getLong(0))
                put("category_id", bCursor.getLong(1))
                put("year", bCursor.getInt(2))
                put("month", bCursor.getInt(3))
                put("amount_limit", bCursor.getLong(4))
            }
            bArray.put(obj)
        }
        bCursor.close()
        root.put("budgets", bArray)

        // Savings
        val sArray = JSONArray()
        val sCursor = db.rawQuery("SELECT id, name, target_amount, saved_amount, target_date, note, status FROM savings_goals", null)
        while (sCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", sCursor.getLong(0))
                put("name", sCursor.getString(1))
                put("target_amount", sCursor.getLong(2))
                put("saved_amount", sCursor.getLong(3))
                put("target_date", sCursor.getString(4))
                put("note", sCursor.getString(5))
                put("status", sCursor.getString(6))
            }
            sArray.put(obj)
        }
        sCursor.close()
        root.put("savings_goals", sArray)

        // Tasks
        val taskArray = JSONArray()
        val taskCursor = db.rawQuery("SELECT id, title, description, priority, status, deadline FROM tasks", null)
        while (taskCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", taskCursor.getLong(0))
                put("title", taskCursor.getString(1))
                put("description", taskCursor.getString(2))
                put("priority", taskCursor.getString(3))
                put("status", taskCursor.getString(4))
                put("deadline", taskCursor.getString(5))
            }
            taskArray.put(obj)
        }
        taskCursor.close()
        root.put("tasks", taskArray)

        // Calendar Events
        val eArray = JSONArray()
        val eCursor = db.rawQuery("SELECT id, task_id, title, description, location, start_at, end_at, time_zone, google_event_id, sync_status FROM calendar_events", null)
        while (eCursor.moveToNext()) {
            val obj = JSONObject().apply {
                put("id", eCursor.getLong(0))
                put("task_id", if (eCursor.isNull(1)) JSONObject.NULL else eCursor.getLong(1))
                put("title", eCursor.getString(2))
                put("description", eCursor.getString(3))
                put("location", eCursor.getString(4))
                put("start_at", eCursor.getString(5))
                put("end_at", eCursor.getString(6))
                put("time_zone", eCursor.getString(7))
                put("google_event_id", eCursor.getString(8))
                put("sync_status", eCursor.getString(9))
            }
            eArray.put(obj)
        }
        eCursor.close()
        root.put("calendar_events", eArray)

        root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("app") || !root.has("version")) return@withContext false
            val db = dbHelper.writableDatabase

            db.beginTransaction()
            try {
                // Clear existing
                db.delete("activity_notes", null, null)
                db.delete("daily_notes", null, null)
                db.delete("calendar_events", null, null)
                db.delete("tasks", null, null)
                db.delete("transactions", null, null)
                db.delete("budgets", null, null)
                db.delete("savings_goals", null, null)

                val time = now()

                // Restore savings
                if (root.has("savings_goals")) {
                    val sArr = root.getJSONArray("savings_goals")
                    for (i in 0 until sArr.length()) {
                        val s = sArr.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", s.getLong("id"))
                            put("name", s.getString("name"))
                            put("target_amount", s.getLong("target_amount"))
                            put("saved_amount", s.optLong("saved_amount", 0))
                            put("target_date", s.optString("target_date", null))
                            put("note", s.optString("note", null))
                            put("status", s.optString("status", "active"))
                            put("created_at", time)
                            put("updated_at", time)
                        }
                        db.insertWithOnConflict("savings_goals", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }

                // Restore transactions
                if (root.has("transactions")) {
                    val tArr = root.getJSONArray("transactions")
                    for (i in 0 until tArr.length()) {
                        val t = tArr.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", t.getLong("id"))
                            put("type", t.getString("type"))
                            put("category_id", t.getLong("category_id"))
                            put("amount", t.getLong("amount"))
                            put("transaction_date", t.getString("transaction_date"))
                            put("note", t.optString("note", null))
                            put("created_at", time)
                            put("updated_at", time)
                        }
                        db.insertWithOnConflict("transactions", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }

                // Restore tasks
                if (root.has("tasks")) {
                    val taskArr = root.getJSONArray("tasks")
                    for (i in 0 until taskArr.length()) {
                        val task = taskArr.getJSONObject(i)
                        val cv = ContentValues().apply {
                            put("id", task.getLong("id"))
                            put("title", task.getString("title"))
                            put("description", task.optString("description", null))
                            put("priority", task.optString("priority", "medium"))
                            put("status", task.optString("status", "todo"))
                            put("deadline", task.optString("deadline", null))
                            put("created_at", time)
                            put("updated_at", time)
                        }
                        db.insertWithOnConflict("tasks", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
                    }
                }

                db.setTransactionSuccessful()
                true
            } finally {
                db.endTransaction()
            }
        } catch (_: Exception) {
            false
        }
    }
}
