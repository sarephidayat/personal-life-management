package com.example.personallife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personallife.ui.dashboard.DashboardScreen
import com.example.personallife.ui.finance.FinanceScreen
import com.example.personallife.ui.finance.TransactionFormDialog
import com.example.personallife.ui.planner.PlannerScreen
import com.example.personallife.ui.settings.PinLockScreen
import com.example.personallife.ui.settings.SettingsDialog
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoYellow
import com.example.personallife.ui.theme.PersonalLifeTheme
import com.example.personallife.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsState()

            PersonalLifeTheme(themeMode = state.themeMode) {
                if (state.isAppLocked) {
                    PinLockScreen(
                        onUnlock = { pin, callback ->
                            viewModel.unlockApp(pin, callback)
                        }
                    )
                } else {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val state by viewModel.uiState.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showQuickTransactionDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.outline)
                    .height(72.dp)
            ) {
                NavigationBarItem(
                    selected = state.currentNavTab == 0,
                    onClick = { viewModel.setNavTab(0) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LightText,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = NeoYellow,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = state.currentNavTab == 1,
                    onClick = { viewModel.setNavTab(1) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Keuangan") },
                    label = { Text("Keuangan", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LightText,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = NeoYellow,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_finance")
                )

                NavigationBarItem(
                    selected = state.currentNavTab == 2,
                    onClick = { viewModel.setNavTab(2) },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Aktivitas") },
                    label = { Text("Aktivitas", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LightText,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = NeoYellow,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_planner")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.currentNavTab) {
                0 -> DashboardScreen(
                    state = state,
                    onNavigateToFinance = { subTab ->
                        viewModel.setFinanceSubTab(subTab)
                        viewModel.setNavTab(1)
                    },
                    onNavigateToPlanner = { subTab ->
                        viewModel.setPlannerSubTab(subTab)
                        viewModel.setNavTab(2)
                    },
                    onAddTransaction = { tx ->
                        viewModel.addTransaction(tx)
                    },
                    onToggleTaskStatus = { id, newStatus ->
                        viewModel.toggleTaskStatus(id, newStatus)
                    },
                    onOpenSettings = { showSettingsDialog = true }
                )
                1 -> FinanceScreen(
                    state = state,
                    selectedSubTab = state.financeSubTab,
                    onSelectSubTab = { viewModel.setFinanceSubTab(it) },
                    onAddTransaction = { viewModel.addTransaction(it) },
                    onUpdateTransaction = { viewModel.updateTransaction(it) },
                    onDeleteTransaction = { viewModel.deleteTransaction(it) },
                    onSetBudget = { catId, year, month, limit ->
                        viewModel.setBudget(catId, year, month, limit)
                    },
                    onDeleteBudget = { viewModel.deleteBudget(it) },
                    onAddSavingsGoal = { viewModel.addSavingsGoal(it) },
                    onAddDeposit = { id, amount -> viewModel.addSavingsDeposit(id, amount) },
                    onDeleteSavingsGoal = { viewModel.deleteSavingsGoal(it) },
                    onAddCategory = { viewModel.addCategory(it) },
                    onToggleCategoryActive = { id, active -> viewModel.toggleCategoryActive(id, active) }
                )
                2 -> PlannerScreen(
                    state = state,
                    selectedSubTab = state.plannerSubTab,
                    onSelectSubTab = { viewModel.setPlannerSubTab(it) },
                    onSelectDate = { viewModel.selectDate(it) },
                    onAddTask = { viewModel.addTask(it) },
                    onUpdateTask = { viewModel.updateTask(it) },
                    onToggleTaskStatus = { id, newStatus -> viewModel.toggleTaskStatus(id, newStatus) },
                    onDeleteTask = { viewModel.deleteTask(it) },
                    onScheduleTaskToCalendar = { task, start, end ->
                        viewModel.scheduleTaskToCalendar(task, start, end)
                    },
                    onAddCalendarEvent = { viewModel.addCalendarEvent(it) },
                    onDeleteCalendarEvent = { viewModel.deleteCalendarEvent(it) },
                    onSaveDailyNote = { date, content -> viewModel.saveDailyNote(date, content) },
                    onSaveActivityNote = { viewModel.saveActivityNote(it) },
                    onDeleteActivityNote = { id, date -> viewModel.deleteActivityNote(id, date) },
                    onPerformManualSync = { viewModel.performManualSync() }
                )
            }
        }
    }

    // Quick Add Transaction Dialog
    if (showQuickTransactionDialog) {
        TransactionFormDialog(
            transaction = null,
            categories = state.categories,
            onDismiss = { showQuickTransactionDialog = false },
            onSave = { newTx ->
                viewModel.addTransaction(newTx)
                showQuickTransactionDialog = false
            }
        )
    }

    // Settings Dialog
    SettingsDialog(
        show = showSettingsDialog,
        openingBalance = state.openingBalance,
        isPinEnabled = state.isPinEnabled,
        currentTheme = state.themeMode,
        onDismiss = { showSettingsDialog = false },
        onSetOpeningBalance = { viewModel.setOpeningBalance(it) },
        onSetPin = { viewModel.setPin(it) },
        onDisablePin = { viewModel.disablePin() },
        onSetThemeMode = { viewModel.setThemeMode(it) },
        onExportBackup = { viewModel.exportBackupJson() },
        onImportBackup = { viewModel.importBackupJson(it) }
    )
}
