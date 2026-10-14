package com.example.personallife.data.models

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TransactionItem(
    val id: Long = 0,
    val type: String, // "income" or "expense"
    val categoryId: Long,
    val categoryName: String = "",
    val categoryIcon: String = "",
    val categoryColor: String = "",
    val amount: Long, // Integer Rupiah
    val transactionDate: String, // "YYYY-MM-DD"
    val note: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class CategoryItem(
    val id: Long = 0,
    val name: String,
    val type: String, // "income" or "expense"
    val iconKey: String? = null,
    val colorKey: String? = null,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

data class BudgetItem(
    val id: Long = 0,
    val categoryId: Long,
    val categoryName: String = "",
    val year: Int,
    val month: Int, // 1..12
    val amountLimit: Long,
    val spent: Long = 0L
) {
    val remaining: Long get() = amountLimit - spent
    val isOverBudget: Boolean get() = spent > amountLimit
    val progress: Float get() = if (amountLimit > 0) (spent.toFloat() / amountLimit).coerceIn(0f, 1f) else 0f
}

data class SavingsGoalItem(
    val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0L,
    val targetDate: String? = null,
    val note: String? = null,
    val status: String = "active" // "active", "completed", "archived"
) {
    val remaining: Long get() = (targetAmount - savedAmount).coerceAtLeast(0L)
    val progress: Float get() = if (targetAmount > 0) (savedAmount.toFloat() / targetAmount).coerceIn(0f, 1f) else 0f
    val isCompleted: Boolean get() = savedAmount >= targetAmount || status == "completed"
}

data class TaskItem(
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val priority: String = "medium", // "high", "medium", "low"
    val status: String = "todo", // "todo", "in_progress", "done"
    val deadline: String? = null, // "YYYY-MM-DD" or ISO
    val completedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class CalendarEventItem(
    val id: Long = 0,
    val taskId: Long? = null,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startAt: String, // "YYYY-MM-DDTHH:mm:ss"
    val endAt: String,   // "YYYY-MM-DDTHH:mm:ss"
    val timeZone: String = "Asia/Jakarta",
    val googleEventId: String? = null,
    val syncStatus: String = "local_only", // "local_only", "pending_create", "synced", "pending_update", "pending_delete", "failed"
    val lastSyncAt: String? = null,
    val lastSyncError: String? = null
)

data class DailyNoteItem(
    val id: Long = 0,
    val noteDate: String, // "YYYY-MM-DD"
    val content: String,
    val updatedAt: String = ""
)

data class ActivityNoteItem(
    val id: Long = 0,
    val calendarEventId: Long? = null,
    val taskId: Long? = null,
    val title: String? = null,
    val content: String,
    val activityDate: String, // "YYYY-MM-DD"
    val updatedAt: String = ""
)

// Helper formatting utilities
object FormatUtils {
    private val localeId = Locale.forLanguageTag("id-ID")
    private val currencyFormatter = NumberFormat.getCurrencyInstance(localeId).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun formatRupiah(amount: Long): String {
        return currencyFormatter.format(amount).replace("Rp", "Rp ")
    }

    fun formatDateDisplay(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "-"
        return try {
            if (dateStr.length == 10) {
                val date = LocalDate.parse(dateStr)
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", localeId))
            } else {
                val dt = LocalDateTime.parse(dateStr.take(19))
                dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", localeId))
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatDateIndonesian(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return "-"
        return try {
            val date = LocalDate.parse(dateStr.take(10))
            date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", localeId))
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatMonthYear(year: Int, month: Int): String {
        return try {
            val date = LocalDate.of(year, month, 1)
            date.format(DateTimeFormatter.ofPattern("MMMM yyyy", localeId))
        } catch (_: Exception) {
            "$month/$year"
        }
    }
}
