package com.example.personallife.ui.finance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personallife.data.models.BudgetItem
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.data.models.SavingsGoalItem
import com.example.personallife.data.models.TransactionItem
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.theme.NeoYellow
import com.example.personallife.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    state: MainUiState,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onAddTransaction: (TransactionItem) -> Unit,
    onUpdateTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onSetBudget: (categoryId: Long, year: Int, month: Int, limit: Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    onAddSavingsGoal: (SavingsGoalItem) -> Unit,
    onAddDeposit: (id: Long, amount: Long) -> Unit,
    onDeleteSavingsGoal: (Long) -> Unit,
    onAddCategory: (CategoryItem) -> Unit,
    onToggleCategoryActive: (id: Long, active: Boolean) -> Unit
) {
    val subTabs = listOf("Transaksi", "Anggaran", "Tabungan", "Laporan", "Kategori")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Keuangan",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Horizontal scrollable subtabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subTabs.forEachIndexed { index, title ->
                    NeoChip(
                        text = title,
                        selected = selectedSubTab == index,
                        onClick = { onSelectSubTab(index) },
                        selectedColor = NeoYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            when (selectedSubTab) {
                0 -> TransactionsTab(
                    transactions = state.transactions,
                    categories = state.categories,
                    onAddTransaction = onAddTransaction,
                    onUpdateTransaction = onUpdateTransaction,
                    onDeleteTransaction = onDeleteTransaction
                )
                1 -> BudgetsTab(
                    budgets = state.budgets,
                    categories = state.categories,
                    onSetBudget = onSetBudget,
                    onDeleteBudget = onDeleteBudget
                )
                2 -> SavingsTab(
                    savingsGoals = state.savingsGoals,
                    onAddSavingsGoal = onAddSavingsGoal,
                    onAddDeposit = onAddDeposit,
                    onDeleteSavingsGoal = onDeleteSavingsGoal
                )
                3 -> ReportsTab(
                    transactions = state.transactions
                )
                4 -> CategoriesTab(
                    categories = state.categories,
                    onAddCategory = onAddCategory,
                    onToggleCategoryActive = onToggleCategoryActive
                )
            }
        }
    }
}
