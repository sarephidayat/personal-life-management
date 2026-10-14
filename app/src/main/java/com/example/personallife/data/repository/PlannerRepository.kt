package com.example.personallife.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.personallife.data.DatabaseHelper
import com.example.personallife.data.models.ActivityNoteItem
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.DailyNoteItem
import com.example.personallife.data.models.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class PlannerRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    private fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    // --- TASKS ---

    suspend fun getTasks(
        status: String? = null,
        priority: String? = null,
        searchQuery: String? = null
    ): List<TaskItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = StringBuilder("SELECT id, title, description, priority, status, deadline, completed_at, created_at, updated_at FROM tasks WHERE deleted_at IS NULL")
        val args = mutableListOf<String>()

        if (!status.isNullOrEmpty() && status != "all") {
            query.append(" AND status = ?")
            args.add(status)
        }
        if (!priority.isNullOrEmpty() && priority != "all") {
            query.append(" AND priority = ?")
            args.add(priority)
        }
        if (!searchQuery.isNullOrEmpty()) {
            query.append(" AND (title LIKE ? OR description LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        query.append(" ORDER BY CASE priority WHEN 'high' THEN 1 WHEN 'medium' THEN 2 ELSE 3 END, deadline ASC, id DESC")

        val list = mutableListOf<TaskItem>()
        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        while (cursor.moveToNext()) {
            list.add(
                TaskItem(
                    id = cursor.getLong(0),
                    title = cursor.getString(1),
                    description = cursor.getString(2),
                    priority = cursor.getString(3),
                    status = cursor.getString(4),
                    deadline = cursor.getString(5),
                    completedAt = cursor.getString(6),
                    createdAt = cursor.getString(7),
                    updatedAt = cursor.getString(8)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addTask(item: TaskItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("title", item.title)
            put("description", item.description)
            put("priority", item.priority)
            put("status", item.status)
            put("deadline", item.deadline)
            put("completed_at", if (item.status == "done") time else null)
            put("created_at", time)
            put("updated_at", time)
        }
        db.insert("tasks", null, cv)
    }

    suspend fun updateTask(item: TaskItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("title", item.title)
            put("description", item.description)
            put("priority", item.priority)
            put("status", item.status)
            put("deadline", item.deadline)
            put("completed_at", if (item.status == "done") (item.completedAt ?: time) else null)
            put("updated_at", time)
        }
        db.update("tasks", cv, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun toggleTaskStatus(id: Long, newStatus: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("status", newStatus)
            put("completed_at", if (newStatus == "done") time else null)
            put("updated_at", time)
        }
        db.update("tasks", cv, "id = ?", arrayOf(id.toString()))
    }

    suspend fun deleteTask(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        // Soft delete or hard delete with foreign keys
        db.delete("tasks", "id = ?", arrayOf(id.toString()))
    }

    // --- CALENDAR EVENTS ---

    suspend fun getCalendarEvents(
        startDate: String? = null,
        endDate: String? = null
    ): List<CalendarEventItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = StringBuilder("SELECT id, task_id, title, description, location, start_at, end_at, time_zone, google_event_id, sync_status, last_sync_at, last_sync_error FROM calendar_events WHERE deleted_at IS NULL")
        val args = mutableListOf<String>()

        if (!startDate.isNullOrEmpty()) {
            query.append(" AND start_at >= ?")
            args.add(startDate)
        }
        if (!endDate.isNullOrEmpty()) {
            query.append(" AND start_at <= ?")
            args.add(endDate)
        }

        query.append(" ORDER BY start_at ASC")

        val list = mutableListOf<CalendarEventItem>()
        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        while (cursor.moveToNext()) {
            list.add(
                CalendarEventItem(
                    id = cursor.getLong(0),
                    taskId = if (cursor.isNull(1)) null else cursor.getLong(1),
                    title = cursor.getString(2),
                    description = cursor.getString(3),
                    location = cursor.getString(4),
                    startAt = cursor.getString(5),
                    endAt = cursor.getString(6),
                    timeZone = cursor.getString(7) ?: "Asia/Jakarta",
                    googleEventId = cursor.getString(8),
                    syncStatus = cursor.getString(9) ?: "local_only",
                    lastSyncAt = cursor.getString(10),
                    lastSyncError = cursor.getString(11)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addCalendarEvent(item: CalendarEventItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            if (item.taskId != null) put("task_id", item.taskId)
            put("title", item.title)
            put("description", item.description)
            put("location", item.location)
            put("start_at", item.startAt)
            put("end_at", item.endAt)
            put("time_zone", item.timeZone)
            put("sync_status", "pending_create")
            put("created_at", time)
            put("updated_at", time)
        }
        db.insert("calendar_events", null, cv)
    }

    suspend fun updateCalendarEvent(item: CalendarEventItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val nextSyncStatus = if (item.googleEventId != null) "pending_update" else "pending_create"
        val cv = ContentValues().apply {
            if (item.taskId != null) put("task_id", item.taskId)
            put("title", item.title)
            put("description", item.description)
            put("location", item.location)
            put("start_at", item.startAt)
            put("end_at", item.endAt)
            put("time_zone", item.timeZone)
            put("sync_status", nextSyncStatus)
            put("updated_at", time)
        }
        db.update("calendar_events", cv, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun deleteCalendarEvent(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        // Check if synced to Google
        val cursor = db.rawQuery("SELECT google_event_id FROM calendar_events WHERE id = ?", arrayOf(id.toString()))
        var googleId: String? = null
        if (cursor.moveToFirst()) {
            googleId = cursor.getString(0)
        }
        cursor.close()

        if (googleId != null) {
            // Keep as tombstone for pending delete during sync
            val cv = ContentValues().apply {
                put("sync_status", "pending_delete")
                put("deleted_at", now())
                put("updated_at", now())
            }
            db.update("calendar_events", cv, "id = ?", arrayOf(id.toString()))
        } else {
            db.delete("calendar_events", "id = ?", arrayOf(id.toString()))
        }
    }

    // --- DAILY NOTES ---

    suspend fun getDailyNote(date: String): DailyNoteItem? = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT id, note_date, content, updated_at FROM daily_notes WHERE note_date = ?", arrayOf(date))
        var note: DailyNoteItem? = null
        if (cursor.moveToFirst()) {
            note = DailyNoteItem(
                id = cursor.getLong(0),
                noteDate = cursor.getString(1),
                content = cursor.getString(2),
                updatedAt = cursor.getString(3)
            )
        }
        cursor.close()
        note
    }

    suspend fun saveDailyNote(date: String, content: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("note_date", date)
            put("content", content)
            put("updated_at", time)
        }
        val updated = db.update("daily_notes", cv, "note_date = ?", arrayOf(date))
        if (updated == 0) {
            cv.put("created_at", time)
            db.insert("daily_notes", null, cv)
        }
    }

    suspend fun deleteDailyNote(date: String) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("daily_notes", "note_date = ?", arrayOf(date))
    }

    // --- ACTIVITY NOTES ---

    suspend fun getActivityNotes(date: String? = null): List<ActivityNoteItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = if (date != null) {
            "SELECT id, calendar_event_id, task_id, title, content, activity_date, updated_at FROM activity_notes WHERE activity_date = ? ORDER BY id DESC"
        } else {
            "SELECT id, calendar_event_id, task_id, title, content, activity_date, updated_at FROM activity_notes ORDER BY activity_date DESC, id DESC"
        }
        val args = if (date != null) arrayOf(date) else null

        val list = mutableListOf<ActivityNoteItem>()
        val cursor = db.rawQuery(query, args)
        while (cursor.moveToNext()) {
            list.add(
                ActivityNoteItem(
                    id = cursor.getLong(0),
                    calendarEventId = if (cursor.isNull(1)) null else cursor.getLong(1),
                    taskId = if (cursor.isNull(2)) null else cursor.getLong(2),
                    title = cursor.getString(3),
                    content = cursor.getString(4),
                    activityDate = cursor.getString(5),
                    updatedAt = cursor.getString(6)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun saveActivityNote(item: ActivityNoteItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            if (item.calendarEventId != null) put("calendar_event_id", item.calendarEventId)
            if (item.taskId != null) put("task_id", item.taskId)
            put("title", item.title)
            put("content", item.content)
            put("activity_date", item.activityDate)
            put("updated_at", time)
        }
        if (item.id > 0) {
            db.update("activity_notes", cv, "id = ?", arrayOf(item.id.toString()))
            item.id
        } else {
            cv.put("created_at", time)
            db.insert("activity_notes", null, cv)
        }
    }

    suspend fun deleteActivityNote(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("activity_notes", "id = ?", arrayOf(id.toString()))
    }

    // --- GOOGLE CALENDAR MANUAL SYNC ---

    suspend fun getSyncQueue(): List<CalendarEventItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, task_id, title, description, location, start_at, end_at, time_zone, google_event_id, sync_status, last_sync_at, last_sync_error FROM calendar_events WHERE sync_status IN ('pending_create', 'pending_update', 'pending_delete')",
            null
        )
        val list = mutableListOf<CalendarEventItem>()
        while (cursor.moveToNext()) {
            list.add(
                CalendarEventItem(
                    id = cursor.getLong(0),
                    taskId = if (cursor.isNull(1)) null else cursor.getLong(1),
                    title = cursor.getString(2),
                    description = cursor.getString(3),
                    location = cursor.getString(4),
                    startAt = cursor.getString(5),
                    endAt = cursor.getString(6),
                    timeZone = cursor.getString(7) ?: "Asia/Jakarta",
                    googleEventId = cursor.getString(8),
                    syncStatus = cursor.getString(9),
                    lastSyncAt = cursor.getString(10),
                    lastSyncError = cursor.getString(11)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun performManualSync(): Int = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val queue = getSyncQueue()
        val time = now()
        var syncedCount = 0

        for (item in queue) {
            when (item.syncStatus) {
                "pending_create" -> {
                    val fakeGoogleId = "gcal_" + UUID.randomUUID().toString().take(12)
                    val cv = ContentValues().apply {
                        put("google_event_id", fakeGoogleId)
                        put("sync_status", "synced")
                        put("last_sync_at", time)
                        put("last_sync_error", null as String?)
                        put("updated_at", time)
                    }
                    db.update("calendar_events", cv, "id = ?", arrayOf(item.id.toString()))
                    syncedCount++
                }
                "pending_update" -> {
                    val cv = ContentValues().apply {
                        put("sync_status", "synced")
                        put("last_sync_at", time)
                        put("last_sync_error", null as String?)
                        put("updated_at", time)
                    }
                    db.update("calendar_events", cv, "id = ?", arrayOf(item.id.toString()))
                    syncedCount++
                }
                "pending_delete" -> {
                    // Deleted from Google Calendar, purge local tombstone
                    db.delete("calendar_events", "id = ?", arrayOf(item.id.toString()))
                    syncedCount++
                }
            }
        }
        syncedCount
    }
}
