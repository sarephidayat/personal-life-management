package com.example.personallife.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personallife.data.models.ActivityNoteItem
import com.example.personallife.data.models.BudgetItem
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.data.models.DailyNoteItem
import com.example.personallife.data.models.SavingsGoalItem
import com.example.personallife.data.models.TaskItem
import com.example.personallife.data.models.TransactionItem
import com.example.personallife.data.repository.FinanceRepository
import com.example.personallife.data.repository.PlannerRepository
import com.example.personallife.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val currentBalance: Long = 0L,
    val openingBalance: Long = 0L,
    val monthlyIncome: Long = 0L,
    val monthlyExpense: Long = 0L,
    val monthlyNet: Long = 0L,
    val transactions: List<TransactionItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val budgets: List<BudgetItem> = emptyList(),
    val savingsGoals: List<SavingsGoalItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val calendarEvents: List<CalendarEventItem> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyNote: DailyNoteItem? = null,
    val activityNotes: List<ActivityNoteItem> = emptyList(),
    val syncQueueCount: Int = 0,
    val lastSyncMessage: String? = null,
    val isPinEnabled: Boolean = false,
    val isAppLocked: Boolean = false,
    val themeMode: String = "system",
    val calendarConnectionState: com.example.personallife.data.google.CalendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.NotConnected,
    val currentNavTab: Int = 0, // 0: Home, 1: Finance, 2: Planner
    val financeSubTab: Int = 0, // 0: Transaksi, 1: Anggaran, 2: Tabungan, 3: Laporan, 4: Kategori
    val plannerSubTab: Int = 0  // 0: Kalender, 1: Tugas, 2: Catatan, 3: Sync
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val financeRepo = FinanceRepository(application)
    private val plannerRepo = PlannerRepository(application)
    private val settingsRepo = SettingsRepository(application)
    val googleCalendarService = com.example.personallife.data.google.GoogleCalendarService(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadSettingsAndSecurity()
        refreshAll()
    }

    private fun loadSettingsAndSecurity() {
        viewModelScope.launch {
            val pinEnabled = settingsRepo.isPinEnabled()
            val theme = settingsRepo.getSetting("theme_mode", "system")
            _uiState.update {
                it.copy(
                    isPinEnabled = pinEnabled,
                    isAppLocked = pinEnabled, // Lock initially if PIN is set
                    themeMode = theme
                )
            }
        }
    }

    fun unlockApp(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val valid = settingsRepo.verifyPin(pin)
            if (valid) {
                _uiState.update { it.copy(isAppLocked = false) }
            }
            onResult(valid)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            settingsRepo.setPin(pin)
            _uiState.update { it.copy(isPinEnabled = true) }
        }
    }

    fun disablePin() {
        viewModelScope.launch {
            settingsRepo.disablePin()
            _uiState.update { it.copy(isPinEnabled = false, isAppLocked = false) }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepo.setSetting("theme_mode", mode)
            _uiState.update { it.copy(themeMode = mode) }
        }
    }

    fun setNavTab(tab: Int) {
        _uiState.update { it.copy(currentNavTab = tab) }
    }

    fun setFinanceSubTab(subTab: Int) {
        _uiState.update { it.copy(financeSubTab = subTab) }
    }

    fun setPlannerSubTab(subTab: Int) {
        _uiState.update { it.copy(plannerSubTab = subTab) }
    }

    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
        loadDailyNote(date.toString())
        loadActivityNotes(date.toString())
    }

    fun refreshAll() {
        val now = LocalDate.now()
        loadFinanceData(now.year, now.monthValue)
        loadPlannerData()
        loadDailyNote(_uiState.value.selectedDate.toString())
        loadActivityNotes(_uiState.value.selectedDate.toString())
    }

    // --- FINANCE ACTIONS ---

    fun loadFinanceData(year: Int = LocalDate.now().year, month: Int = LocalDate.now().monthValue) {
        viewModelScope.launch {
            val balance = financeRepo.getCurrentBalance()
            val opening = financeRepo.getOpeningBalance()
            val summary = financeRepo.getMonthlySummary(year, month)
            val transactions = financeRepo.getTransactions()
            val categories = financeRepo.getCategories()
            val budgets = financeRepo.getBudgets(year, month)
            val savings = financeRepo.getSavingsGoals()

            _uiState.update {
                it.copy(
                    currentBalance = balance,
                    openingBalance = opening,
                    monthlyIncome = summary.first,
                    monthlyExpense = summary.second,
                    monthlyNet = summary.third,
                    transactions = transactions,
                    categories = categories,
                    budgets = budgets,
                    savingsGoals = savings
                )
            }
        }
    }

    fun setOpeningBalance(amount: Long) {
        viewModelScope.launch {
            financeRepo.setOpeningBalance(amount)
            loadFinanceData()
        }
    }

    fun addTransaction(item: TransactionItem) {
        viewModelScope.launch {
            financeRepo.addTransaction(item)
            loadFinanceData()
        }
    }

    fun updateTransaction(item: TransactionItem) {
        viewModelScope.launch {
            financeRepo.updateTransaction(item)
            loadFinanceData()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            financeRepo.deleteTransaction(id)
            loadFinanceData()
        }
    }

    fun setBudget(categoryId: Long, year: Int, month: Int, limit: Long) {
        viewModelScope.launch {
            financeRepo.setBudget(categoryId, year, month, limit)
            loadFinanceData(year, month)
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            financeRepo.deleteBudget(id)
            loadFinanceData()
        }
    }

    fun addSavingsGoal(item: SavingsGoalItem) {
        viewModelScope.launch {
            financeRepo.addSavingsGoal(item)
            loadFinanceData()
        }
    }

    fun updateSavingsGoal(item: SavingsGoalItem) {
        viewModelScope.launch {
            financeRepo.updateSavingsGoal(item)
            loadFinanceData()
        }
    }

    fun addSavingsDeposit(id: Long, amount: Long) {
        viewModelScope.launch {
            financeRepo.updateSavingsAmount(id, amount)
            loadFinanceData()
        }
    }

    fun deleteSavingsGoal(id: Long) {
        viewModelScope.launch {
            financeRepo.deleteSavingsGoal(id)
            loadFinanceData()
        }
    }

    fun addCategory(item: CategoryItem) {
        viewModelScope.launch {
            financeRepo.addCategory(item)
            loadFinanceData()
        }
    }

    fun toggleCategoryActive(id: Long, active: Boolean) {
        viewModelScope.launch {
            financeRepo.toggleCategoryActive(id, active)
            loadFinanceData()
        }
    }

    // --- PLANNER ACTIONS ---

    fun loadPlannerData() {
        viewModelScope.launch {
            val tasks = plannerRepo.getTasks()
            val events = plannerRepo.getCalendarEvents()
            val queue = plannerRepo.getSyncQueue()

            _uiState.update {
                it.copy(
                    tasks = tasks,
                    calendarEvents = events,
                    syncQueueCount = queue.size
                )
            }
        }
    }

    fun addTask(item: TaskItem) {
        viewModelScope.launch {
            plannerRepo.addTask(item)
            loadPlannerData()
        }
    }

    fun updateTask(item: TaskItem) {
        viewModelScope.launch {
            plannerRepo.updateTask(item)
            loadPlannerData()
        }
    }

    fun toggleTaskStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            plannerRepo.toggleTaskStatus(id, newStatus)
            loadPlannerData()
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            plannerRepo.deleteTask(id)
            loadPlannerData()
        }
    }

    fun addCalendarEvent(item: CalendarEventItem) {
        viewModelScope.launch {
            plannerRepo.addCalendarEvent(item)
            loadPlannerData()
        }
    }

    fun updateCalendarEvent(item: CalendarEventItem) {
        viewModelScope.launch {
            plannerRepo.updateCalendarEvent(item)
            loadPlannerData()
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            plannerRepo.deleteCalendarEvent(id)
            loadPlannerData()
        }
    }

    fun scheduleTaskToCalendar(task: TaskItem, startAt: String, endAt: String) {
        viewModelScope.launch {
            val event = CalendarEventItem(
                taskId = task.id,
                title = task.title,
                description = task.description,
                startAt = startAt,
                endAt = endAt,
                syncStatus = "pending_create"
            )
            plannerRepo.addCalendarEvent(event)
            loadPlannerData()
        }
    }

    private fun loadDailyNote(date: String) {
        viewModelScope.launch {
            val note = plannerRepo.getDailyNote(date)
            _uiState.update { it.copy(dailyNote = note) }
        }
    }

    fun saveDailyNote(date: String, content: String) {
        viewModelScope.launch {
            plannerRepo.saveDailyNote(date, content)
            loadDailyNote(date)
        }
    }

    private fun loadActivityNotes(date: String) {
        viewModelScope.launch {
            val notes = plannerRepo.getActivityNotes(date)
            _uiState.update { it.copy(activityNotes = notes) }
        }
    }

    fun saveActivityNote(item: ActivityNoteItem) {
        viewModelScope.launch {
            plannerRepo.saveActivityNote(item)
            loadActivityNotes(item.activityDate)
        }
    }

    fun deleteActivityNote(id: Long, date: String) {
        viewModelScope.launch {
            plannerRepo.deleteActivityNote(id)
            loadActivityNotes(date)
        }
    }

    fun getGoogleOAuthUrl(): String = googleCalendarService.buildOAuthAuthorizationUrl()

    fun handleOAuthRedirectUri(uri: android.net.Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(calendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.Connecting) }
            val (tokenOrCode, error) = googleCalendarService.parseRedirectUri(uri)
            if (!error.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(
                        calendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.Error(
                            message = "Autentikasi ditolak atau dibatalkan",
                            details = "Kode respon: $error"
                        )
                    )
                }
                return@launch
            }
            if (tokenOrCode.isNullOrEmpty()) {
                _uiState.update {
                    it.copy(
                        calendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.Error(
                            message = "Token autentikasi tidak ditemukan",
                            details = "URL redirect tidak memuat parameter access_token."
                        )
                    )
                }
                return@launch
            }

            val verificationResult = googleCalendarService.verifyPrimaryCalendarAccess(tokenOrCode)
            _uiState.update { it.copy(calendarConnectionState = verificationResult) }
        }
    }

    fun verifyCalendarAccessWithToken(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(calendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.Connecting) }
            val verificationResult = googleCalendarService.verifyPrimaryCalendarAccess(token.trim())
            _uiState.update { it.copy(calendarConnectionState = verificationResult) }
        }
    }

    fun disconnectGoogleCalendar() {
        googleCalendarService.clearToken()
        _uiState.update { it.copy(calendarConnectionState = com.example.personallife.data.google.CalendarConnectionState.NotConnected) }
    }

    fun performManualSync() {
        viewModelScope.launch {
            val count = plannerRepo.performManualSync()
            _uiState.update {
                it.copy(
                    lastSyncMessage = "Berhasil menyinkronkan $count event ke Google Calendar.",
                    syncQueueCount = 0
                )
            }
            loadPlannerData()
        }
    }

    suspend fun exportBackupJson(): String {
        return settingsRepo.exportBackupJson()
    }

    suspend fun importBackupJson(json: String): Boolean {
        val success = settingsRepo.importBackupJson(json)
        if (success) {
            refreshAll()
        }
        return success
    }
}
