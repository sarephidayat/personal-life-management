package com.example.personallife.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.personallife.data.DatabaseHelper
import com.example.personallife.data.models.BudgetItem
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.data.models.SavingsGoalItem
import com.example.personallife.data.models.TransactionItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FinanceRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    private fun now(): String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

    suspend fun getOpeningBalance(): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT opening_balance FROM finance_profile WHERE id = 1", null)
        var balance = 0L
        if (cursor.moveToFirst()) {
            balance = cursor.getLong(0)
        }
        cursor.close()
        balance
    }

    suspend fun setOpeningBalance(newBalance: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("opening_balance", newBalance)
            put("updated_at", now())
        }
        db.update("finance_profile", cv, "id = 1", null)
    }

    suspend fun getCurrentBalance(): Long = withContext(Dispatchers.IO) {
        val opening = getOpeningBalance()
        val db = dbHelper.readableDatabase

        var totalIncome = 0L
        var totalExpense = 0L

        val incomeCursor = db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income'", null)
        if (incomeCursor.moveToFirst()) {
            totalIncome = incomeCursor.getLong(0)
        }
        incomeCursor.close()

        val expenseCursor = db.rawQuery("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense'", null)
        if (expenseCursor.moveToFirst()) {
            totalExpense = expenseCursor.getLong(0)
        }
        expenseCursor.close()

        opening + totalIncome - totalExpense
    }

    suspend fun getMonthlySummary(year: Int, month: Int): Triple<Long, Long, Long> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val monthStr = String.format("%04d-%02d", year, month)

        var income = 0L
        var expense = 0L

        val incomeCursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'income' AND transaction_date LIKE '$monthStr%'",
            null
        )
        if (incomeCursor.moveToFirst()) income = incomeCursor.getLong(0)
        incomeCursor.close()

        val expenseCursor = db.rawQuery(
            "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'expense' AND transaction_date LIKE '$monthStr%'",
            null
        )
        if (expenseCursor.moveToFirst()) expense = expenseCursor.getLong(0)
        expenseCursor.close()

        Triple(income, expense, income - expense)
    }

    suspend fun getTransactions(
        categoryId: Long? = null,
        startDate: String? = null,
        endDate: String? = null,
        type: String? = null,
        searchQuery: String? = null
    ): List<TransactionItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = StringBuilder("""
            SELECT t.id, t.type, t.category_id, t.amount, t.transaction_date, t.note, t.created_at, t.updated_at,
                   c.name, c.icon_key, c.color_key
            FROM transactions t
            LEFT JOIN categories c ON t.category_id = c.id
            WHERE 1=1
        """)
        val args = mutableListOf<String>()

        if (categoryId != null && categoryId > 0) {
            query.append(" AND t.category_id = ?")
            args.add(categoryId.toString())
        }
        if (!type.isNullOrEmpty()) {
            query.append(" AND t.type = ?")
            args.add(type)
        }
        if (!startDate.isNullOrEmpty()) {
            query.append(" AND t.transaction_date >= ?")
            args.add(startDate)
        }
        if (!endDate.isNullOrEmpty()) {
            query.append(" AND t.transaction_date <= ?")
            args.add(endDate)
        }
        if (!searchQuery.isNullOrEmpty()) {
            query.append(" AND (t.note LIKE ? OR c.name LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        query.append(" ORDER BY t.transaction_date DESC, t.id DESC")

        val list = mutableListOf<TransactionItem>()
        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        while (cursor.moveToNext()) {
            list.add(
                TransactionItem(
                    id = cursor.getLong(0),
                    type = cursor.getString(1),
                    categoryId = cursor.getLong(2),
                    amount = cursor.getLong(3),
                    transactionDate = cursor.getString(4),
                    note = cursor.getString(5),
                    createdAt = cursor.getString(6),
                    updatedAt = cursor.getString(7),
                    categoryName = cursor.getString(8) ?: "Tanpa Kategori",
                    categoryIcon = cursor.getString(9) ?: "category",
                    categoryColor = cursor.getString(10) ?: "#B8F2D0"
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addTransaction(item: TransactionItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("type", item.type)
            put("category_id", item.categoryId)
            put("amount", item.amount)
            put("transaction_date", item.transactionDate)
            put("note", item.note)
            put("created_at", time)
            put("updated_at", time)
        }
        db.insert("transactions", null, cv)
    }

    suspend fun updateTransaction(item: TransactionItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("type", item.type)
            put("category_id", item.categoryId)
            put("amount", item.amount)
            put("transaction_date", item.transactionDate)
            put("note", item.note)
            put("updated_at", now())
        }
        db.update("transactions", cv, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("transactions", "id = ?", arrayOf(id.toString()))
    }

    suspend fun getCategories(type: String? = null, activeOnly: Boolean = true): List<CategoryItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = StringBuilder("SELECT id, name, type, icon_key, color_key, is_default, is_active FROM categories WHERE 1=1")
        val args = mutableListOf<String>()

        if (!type.isNullOrEmpty()) {
            query.append(" AND type = ?")
            args.add(type)
        }
        if (activeOnly) {
            query.append(" AND is_active = 1")
        }
        query.append(" ORDER BY is_default DESC, name ASC")

        val list = mutableListOf<CategoryItem>()
        val cursor = db.rawQuery(query.toString(), args.toTypedArray())
        while (cursor.moveToNext()) {
            list.add(
                CategoryItem(
                    id = cursor.getLong(0),
                    name = cursor.getString(1),
                    type = cursor.getString(2),
                    iconKey = cursor.getString(3),
                    colorKey = cursor.getString(4),
                    isDefault = cursor.getInt(5) == 1,
                    isActive = cursor.getInt(6) == 1
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addCategory(item: CategoryItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("name", item.name)
            put("type", item.type)
            put("icon_key", item.iconKey ?: "category")
            put("color_key", item.colorKey ?: "#B8F2D0")
            put("is_default", if (item.isDefault) 1 else 0)
            put("is_active", if (item.isActive) 1 else 0)
            put("created_at", time)
            put("updated_at", time)
        }
        db.insert("categories", null, cv)
    }

    suspend fun updateCategory(item: CategoryItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", item.name)
            put("icon_key", item.iconKey)
            put("color_key", item.colorKey)
            put("is_active", if (item.isActive) 1 else 0)
            put("updated_at", now())
        }
        db.update("categories", cv, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun toggleCategoryActive(id: Long, active: Boolean) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("is_active", if (active) 1 else 0)
            put("updated_at", now())
        }
        db.update("categories", cv, "id = ?", arrayOf(id.toString()))
    }

    suspend fun getBudgets(year: Int, month: Int): List<BudgetItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val monthStr = String.format("%04d-%02d", year, month)

        val query = """
            SELECT b.id, b.category_id, b.year, b.month, b.amount_limit, c.name,
                   (SELECT COALESCE(SUM(t.amount), 0) FROM transactions t 
                    WHERE t.category_id = b.category_id 
                      AND t.type = 'expense' 
                      AND t.transaction_date LIKE '$monthStr%') AS spent
            FROM budgets b
            JOIN categories c ON b.category_id = c.id
            WHERE b.year = ? AND b.month = ?
            ORDER BY b.amount_limit DESC
        """.trimIndent()

        val list = mutableListOf<BudgetItem>()
        val cursor = db.rawQuery(query, arrayOf(year.toString(), month.toString()))
        while (cursor.moveToNext()) {
            list.add(
                BudgetItem(
                    id = cursor.getLong(0),
                    categoryId = cursor.getLong(1),
                    year = cursor.getInt(2),
                    month = cursor.getInt(3),
                    amountLimit = cursor.getLong(4),
                    categoryName = cursor.getString(5),
                    spent = cursor.getLong(6)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun setBudget(categoryId: Long, year: Int, month: Int, amountLimit: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("category_id", categoryId)
            put("year", year)
            put("month", month)
            put("amount_limit", amountLimit)
            put("updated_at", time)
        }

        val updated = db.update("budgets", cv, "category_id = ? AND year = ? AND month = ?",
            arrayOf(categoryId.toString(), year.toString(), month.toString()))
        if (updated == 0) {
            cv.put("created_at", time)
            db.insert("budgets", null, cv)
        }
    }

    suspend fun deleteBudget(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("budgets", "id = ?", arrayOf(id.toString()))
    }

    suspend fun getSavingsGoals(status: String? = null): List<SavingsGoalItem> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val query = if (status != null) {
            "SELECT id, name, target_amount, saved_amount, target_date, note, status FROM savings_goals WHERE status = ? ORDER BY id DESC"
        } else {
            "SELECT id, name, target_amount, saved_amount, target_date, note, status FROM savings_goals ORDER BY (status = 'active') DESC, id DESC"
        }
        val args = if (status != null) arrayOf(status) else null

        val list = mutableListOf<SavingsGoalItem>()
        val cursor = db.rawQuery(query, args)
        while (cursor.moveToNext()) {
            list.add(
                SavingsGoalItem(
                    id = cursor.getLong(0),
                    name = cursor.getString(1),
                    targetAmount = cursor.getLong(2),
                    savedAmount = cursor.getLong(3),
                    targetDate = cursor.getString(4),
                    note = cursor.getString(5),
                    status = cursor.getString(6)
                )
            )
        }
        cursor.close()
        list
    }

    suspend fun addSavingsGoal(item: SavingsGoalItem): Long = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val time = now()
        val cv = ContentValues().apply {
            put("name", item.name)
            put("target_amount", item.targetAmount)
            put("saved_amount", item.savedAmount)
            put("target_date", item.targetDate)
            put("note", item.note)
            put("status", item.status)
            put("created_at", time)
            put("updated_at", time)
        }
        db.insert("savings_goals", null, cv)
    }

    suspend fun updateSavingsGoal(item: SavingsGoalItem) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val cv = ContentValues().apply {
            put("name", item.name)
            put("target_amount", item.targetAmount)
            put("saved_amount", item.savedAmount)
            put("target_date", item.targetDate)
            put("note", item.note)
            put("status", item.status)
            put("updated_at", now())
        }
        db.update("savings_goals", cv, "id = ?", arrayOf(item.id.toString()))
    }

    suspend fun updateSavingsAmount(id: Long, addAmount: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.execSQL("""
            UPDATE savings_goals 
            SET saved_amount = MAX(0, saved_amount + ?),
                status = CASE WHEN (saved_amount + ?) >= target_amount THEN 'completed' ELSE 'active' END,
                updated_at = ?
            WHERE id = ?
        """.trimIndent(), arrayOf(addAmount, addAmount, now(), id))
    }

    suspend fun deleteSavingsGoal(id: Long) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        db.delete("savings_goals", "id = ?", arrayOf(id.toString()))
    }

    suspend fun getMonthlyCategoryBreakdown(year: Int, month: Int, type: String): List<Pair<CategoryItem, Long>> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val monthStr = String.format("%04d-%02d", year, month)

        val query = """
            SELECT c.id, c.name, c.type, c.icon_key, c.color_key, SUM(t.amount) as total
            FROM transactions t
            JOIN categories c ON t.category_id = c.id
            WHERE t.type = ? AND t.transaction_date LIKE '$monthStr%'
            GROUP BY c.id
            ORDER BY total DESC
        """.trimIndent()

        val list = mutableListOf<Pair<CategoryItem, Long>>()
        val cursor = db.rawQuery(query, arrayOf(type))
        while (cursor.moveToNext()) {
            val cat = CategoryItem(
                id = cursor.getLong(0),
                name = cursor.getString(1),
                type = cursor.getString(2),
                iconKey = cursor.getString(3),
                colorKey = cursor.getString(4)
            )
            val total = cursor.getLong(5)
            list.add(Pair(cat, total))
        }
        cursor.close()
        list
    }
}
