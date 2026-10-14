package com.example.personallife.ui.finance

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.data.models.BudgetItem
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import java.time.LocalDate

@Composable
fun BudgetsTab(
    budgets: List<BudgetItem>,
    categories: List<CategoryItem>,
    onSetBudget: (categoryId: Long, year: Int, month: Int, limit: Long) -> Unit,
    onDeleteBudget: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = LocalDate.now().year
    val currentMonth = LocalDate.now().monthValue

    var showAddDialog by remember { mutableStateOf(false) }
    var budgetToDelete by remember { mutableStateOf<BudgetItem?>(null) }

    val expenseCategories = categories.filter { it.type == "expense" && it.isActive }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Month indicator card
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NeoYellow,
                cornerRadius = 10.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ANGGARAN BULAN INI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                        Text(
                            text = FormatUtils.formatMonthYear(currentYear, currentMonth),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                    }
                    NeoBadge(
                        text = "${budgets.size} Anggaran",
                        color = Color.White,
                        textColor = LightText
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (budgets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum ada anggaran yang diatur.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { showAddDialog = true },
                            text = "+ Atur Anggaran",
                            backgroundColor = NeoGreen,
                            testTag = "empty_state_add_budget_button"
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(budgets, key = { it.id }) { b ->
                        NeoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            testTag = "budget_item_${b.id}"
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = b.categoryName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (b.isOverBudget) {
                                            NeoBadge(text = "TERLAMPAUI", color = NeoRed, textColor = LightText)
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { budgetToDelete = b },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Anggaran",
                                                tint = NeoRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Terpakai: ${FormatUtils.formatRupiah(b.spent)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (b.isOverBudget) NeoRed else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Batas: ${FormatUtils.formatRupiah(b.amountLimit)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { b.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(5.dp)),
                                    color = if (b.isOverBudget) NeoRed else NeoGreen,
                                    trackColor = MaterialTheme.colorScheme.background
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (b.isOverBudget)
                                        "Terlampaui: -${FormatUtils.formatRupiah(b.spent - b.amountLimit)}"
                                    else
                                        "Sisa: ${FormatUtils.formatRupiah(b.remaining)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = if (b.isOverBudget) NeoRed else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_budget_fab"),
            containerColor = NeoGreen,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Atur Anggaran",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Set Budget Dialog
    if (showAddDialog) {
        var selectedCatId by remember { mutableStateOf(expenseCategories.firstOrNull()?.id ?: 1L) }
        var limitText by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Atur Anggaran Bulanan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Kategori Pengeluaran",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        items(expenseCategories, key = { it.id }) { cat ->
                            val isSelected = cat.id == selectedCatId
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) LightText else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) NeoYellow else Color.Transparent)
                                    .clickable { selectedCatId = cat.id }
                                    .padding(8.dp)
                            )
                        }
                    }

                    NeoTextField(
                        value = limitText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) limitText = it },
                        label = "Batas Anggaran (Rp)",
                        placeholder = "Contoh: 1500000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "budget_limit_input"
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        val limit = limitText.toLongOrNull() ?: 0L
                        if (limit <= 0) {
                            errorMsg = "Batas anggaran harus lebih besar dari 0!"
                            return@NeoButton
                        }
                        onSetBudget(selectedCatId, currentYear, currentMonth, limit)
                        showAddDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_budget_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    budgetToDelete?.let { b ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Anggaran",
            message = "Yakin ingin menghapus batas anggaran kategori ${b.categoryName}?",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteBudget(b.id)
                budgetToDelete = null
            },
            onDismiss = { budgetToDelete = null }
        )
    }
}
