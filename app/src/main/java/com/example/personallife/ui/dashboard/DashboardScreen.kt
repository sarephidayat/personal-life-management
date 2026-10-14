package com.example.personallife.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.data.models.TaskItem
import com.example.personallife.data.models.TransactionItem
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoIconButton
import com.example.personallife.ui.finance.TransactionFormDialog
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoBlue
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoPurple
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import com.example.personallife.ui.viewmodel.MainUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: MainUiState,
    onNavigateToFinance: (subTab: Int) -> Unit,
    onNavigateToPlanner: (subTab: Int) -> Unit,
    onAddTransaction: (TransactionItem) -> Unit,
    onToggleTaskStatus: (id: Long, newStatus: String) -> Unit,
    onOpenSettings: () -> Unit
) {
    val today = LocalDate.now()
    val todayFormatted = FormatUtils.formatDateIndonesian(today.toString())

    // Transaction dialog state
    var showTransactionDialog by remember { mutableStateOf(false) }
    var selectedTransactionType by remember { mutableStateOf("income") } // "income" or "expense"

    // Task counts
    val todoCount = state.tasks.count { it.status == "todo" }
    val inProgressCount = state.tasks.count { it.status == "in_progress" }
    val doneCount = state.tasks.count { it.status == "done" }
    val totalTasks = state.tasks.size
    val completionPercentage = if (totalTasks > 0) ((doneCount.toFloat() / totalTasks) * 100).toInt() else 0
    val progressFloat = if (totalTasks > 0) (doneCount.toFloat() / totalTasks).coerceIn(0f, 1f) else 0f

    // Recent items
    val recentTransactions = state.transactions.take(3)
    val upcomingTasks = state.tasks.filter { it.status != "done" }.take(3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Personal Life",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    NeoIconButton(
                        onClick = onOpenSettings,
                        icon = Icons.Default.Settings,
                        contentDescription = "Pengaturan",
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        testTag = "settings_icon_button"
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // 1. FINANCIAL TRACKER SECTION (Pelacak Keuangan)
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = NeoYellow,
                    cornerRadius = 14.dp,
                    shadowOffset = 4.dp,
                    testTag = "financial_tracker_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PELACAK KEUANGAN",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = LightText
                            )
                            NeoBadge(
                                text = "Bulan Ini",
                                color = Color.White,
                                textColor = LightText
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Saldo Saat Ini",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightText.copy(alpha = 0.85f)
                        )
                        Text(
                            text = FormatUtils.formatRupiah(state.currentBalance),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = LightText,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Income & Expense Summaries
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Total Pemasukan
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, LightText, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "Pemasukan",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Pemasukan",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+ " + FormatUtils.formatRupiah(state.monthlyIncome),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            // Total Pengeluaran
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .border(1.5.dp, LightText, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = "Pengeluaran",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Pengeluaran",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = LightText
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "- " + FormatUtils.formatRupiah(state.monthlyExpense),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // + Add Income & + Add Expense Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            NeoButton(
                                onClick = {
                                    selectedTransactionType = "income"
                                    showTransactionDialog = true
                                },
                                text = "+ Pemasukan",
                                backgroundColor = NeoGreen,
                                leadingIcon = Icons.Default.ArrowDownward,
                                modifier = Modifier.weight(1f),
                                cornerRadius = 8.dp,
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp,
                                testTag = "add_income_button"
                            )

                            NeoButton(
                                onClick = {
                                    selectedTransactionType = "expense"
                                    showTransactionDialog = true
                                },
                                text = "+ Pengeluaran",
                                backgroundColor = NeoRed,
                                leadingIcon = Icons.Default.ArrowUpward,
                                modifier = Modifier.weight(1f),
                                cornerRadius = 8.dp,
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp,
                                testTag = "add_expense_button"
                            )
                        }
                    }
                }
            }

            // 2. COMPACT TASK SUMMARY SECTION (Ringkasan Tugas)
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 14.dp,
                    shadowOffset = 3.dp,
                    testTag = "compact_task_summary_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RINGKASAN TUGAS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$completionPercentage% Selesai",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2E7D32)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3 Compact Metric Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskMetricPill(
                                label = "To Do",
                                count = todoCount,
                                color = NeoYellow,
                                modifier = Modifier.weight(1f)
                            )
                            TaskMetricPill(
                                label = "Berjalan",
                                count = inProgressCount,
                                color = NeoOrange,
                                modifier = Modifier.weight(1f)
                            )
                            TaskMetricPill(
                                label = "Selesai",
                                count = doneCount,
                                color = NeoGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressFloat },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                            color = NeoGreen,
                            trackColor = MaterialTheme.colorScheme.background
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$doneCount dari $totalTasks tugas terselesaikan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = { onNavigateToPlanner(1) }) {
                                Text(
                                    text = "Kelola Tugas ->",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // 3. FEATURE SHORTCUTS SECTION (Akses Cepat Fitur)
            item {
                Text(
                    text = "AKSES CEPAT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardShortcutCard(
                        title = "Riwayat Transaksi",
                        subtitle = "Lihat & cari mutasi",
                        icon = Icons.Default.ReceiptLong,
                        color = NeoYellow,
                        onClick = { onNavigateToFinance(0) },
                        modifier = Modifier.weight(1f),
                        testTag = "shortcut_transactions"
                    )
                    DashboardShortcutCard(
                        title = "Kelola Anggaran",
                        subtitle = "Atur batas bulanan",
                        icon = Icons.Default.Calculate,
                        color = NeoGreen,
                        onClick = { onNavigateToFinance(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "shortcut_budgets"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardShortcutCard(
                        title = "Daftar Tugas",
                        subtitle = "Kanban & prioritas",
                        icon = Icons.Default.Assignment,
                        color = NeoBlue,
                        onClick = { onNavigateToPlanner(1) },
                        modifier = Modifier.weight(1f),
                        testTag = "shortcut_tasks"
                    )
                    DashboardShortcutCard(
                        title = "Buka Planner",
                        subtitle = "Kalender & agenda",
                        icon = Icons.Default.DateRange,
                        color = NeoPurple,
                        onClick = { onNavigateToPlanner(0) },
                        modifier = Modifier.weight(1f),
                        testTag = "shortcut_planner"
                    )
                }
            }

            // 4. SHORT LIST: RECENT TRANSACTIONS (Transaksi Terakhir)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSAKSI TERAKHIR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { onNavigateToFinance(0) }) {
                        Text(
                            text = "Lihat Semua ->",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (recentTransactions.isEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Belum ada transaksi tercatat. Tekan '+ Pemasukan' atau '+ Pengeluaran' di atas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(recentTransactions, key = { it.id }) { item ->
                    val isIncome = item.type == "income"
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        testTag = "dashboard_tx_item_${item.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isIncome) NeoGreen else NeoRed)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = item.type,
                                    tint = LightText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.categoryName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = FormatUtils.formatDateDisplay(item.transactionDate) +
                                            if (!item.note.isNullOrBlank()) " • ${item.note}" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = (if (isIncome) "+" else "-") + FormatUtils.formatRupiah(item.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (isIncome) Color(0xFF2E7D32) else NeoRed
                            )
                        }
                    }
                }
            }

            // 5. SHORT LIST: UPCOMING TASKS (Tugas Mendatang)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TUGAS MENDATANG",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = { onNavigateToPlanner(1) }) {
                        Text(
                            text = "Kelola ->",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            if (upcomingTasks.isEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Semua tugas beres! Tidak ada tugas pending.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(upcomingTasks, key = { it.id }) { task ->
                    val isDone = task.status == "done"

                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        testTag = "dashboard_task_item_${task.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Interactive Checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isDone) NeoGreen else MaterialTheme.colorScheme.background)
                                    .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                                    .clickable {
                                        val nextStatus = when (task.status) {
                                            "todo" -> "in_progress"
                                            "in_progress" -> "done"
                                            else -> "todo"
                                        }
                                        onToggleTaskStatus(task.id, nextStatus)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selesai",
                                        tint = LightText,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                if (!task.deadline.isNullOrBlank()) {
                                    Text(
                                        text = "Tenggat: ${FormatUtils.formatDateDisplay(task.deadline)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (task.deadline == today.toString()) NeoRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (task.deadline == today.toString()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            val (pBadge, pColor) = when (task.priority) {
                                "high" -> Pair("Tinggi", NeoRed)
                                "medium" -> Pair("Sedang", NeoOrange)
                                else -> Pair("Rendah", NeoGreen)
                            }
                            NeoBadge(text = pBadge, color = pColor, textColor = LightText)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Add Income / Expense Dialog
    if (showTransactionDialog) {
        TransactionFormDialog(
            transaction = null,
            categories = state.categories,
            initialType = selectedTransactionType,
            onDismiss = { showTransactionDialog = false },
            onSave = { newTx ->
                onAddTransaction(newTx)
                showTransactionDialog = false
            }
        )
    }
}

@Composable
private fun TaskMetricPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.3f))
            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    NeoCard(
        modifier = modifier,
        backgroundColor = MaterialTheme.colorScheme.surface,
        cornerRadius = 10.dp,
        shadowOffset = 3.dp,
        onClick = onClick,
        testTag = testTag
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color)
                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = LightText,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
