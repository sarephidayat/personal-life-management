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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.data.models.TransactionItem
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import java.time.LocalDate

@Composable
fun TransactionsTab(
    transactions: List<TransactionItem>,
    categories: List<CategoryItem>,
    onAddTransaction: (TransactionItem) -> Unit,
    onUpdateTransaction: (TransactionItem) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterType by remember { mutableStateOf("all") } // "all", "income", "expense"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionItem?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionItem?>(null) }

    val filteredList = transactions.filter { item ->
        val matchesType = when (filterType) {
            "income" -> item.type == "income"
            "expense" -> item.type == "expense"
            else -> true
        }
        val matchesQuery = if (searchQuery.isBlank()) true else {
            (item.note?.contains(searchQuery, ignoreCase = true) == true) ||
            item.categoryName.contains(searchQuery, ignoreCase = true)
        }
        matchesType && matchesQuery
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Box
            NeoTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "",
                placeholder = "Cari transaksi atau kategori...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth(),
                testTag = "transaction_search_input"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoChip(
                    text = "Semua",
                    selected = filterType == "all",
                    onClick = { filterType = "all" },
                    testTag = "filter_chip_all"
                )
                NeoChip(
                    text = "Pemasukan",
                    selected = filterType == "income",
                    onClick = { filterType = "income" },
                    selectedColor = NeoGreen,
                    testTag = "filter_chip_income"
                )
                NeoChip(
                    text = "Pengeluaran",
                    selected = filterType == "expense",
                    onClick = { filterType = "expense" },
                    selectedColor = NeoRed,
                    testTag = "filter_chip_expense"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Transactions
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Tidak ada transaksi yang sesuai.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        val isIncome = item.type == "income"

                        NeoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            testTag = "transaction_item_${item.id}"
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Type icon box
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isIncome) NeoGreen else NeoRed)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                        contentDescription = item.type,
                                        tint = LightText,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

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

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isIncome) "+" else "-") + FormatUtils.formatRupiah(item.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = if (isIncome) Color(0xFF2E7D32) else NeoRed
                                    )

                                    Row {
                                        IconButton(
                                            onClick = { editingTransaction = item },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { transactionToDelete = item },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = NeoRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Floating Action Button to Add Transaction
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_transaction_fab"),
            containerColor = NeoYellow,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Transaksi",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add / Edit Transaction Dialog
    if (showAddDialog || editingTransaction != null) {
        TransactionFormDialog(
            transaction = editingTransaction,
            categories = categories,
            onDismiss = {
                showAddDialog = false
                editingTransaction = null
            },
            onSave = { result ->
                if (editingTransaction != null) {
                    onUpdateTransaction(result)
                } else {
                    onAddTransaction(result)
                }
                showAddDialog = false
                editingTransaction = null
            }
        )
    }

    // Delete Confirmation Dialog (Mandatory by PRD & RULES)
    transactionToDelete?.let { item ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Transaksi",
            message = "Yakin ingin menghapus transaksi ${item.categoryName} sebesar ${FormatUtils.formatRupiah(item.amount)}? Tindakan ini tidak dapat dibatalkan.",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteTransaction(item.id)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@Composable
fun TransactionFormDialog(
    transaction: TransactionItem?,
    categories: List<CategoryItem>,
    initialType: String = "expense",
    onDismiss: () -> Unit,
    onSave: (TransactionItem) -> Unit
) {
    var type by remember { mutableStateOf(transaction?.type ?: initialType) }
    var amountText by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var note by remember { mutableStateOf(transaction?.note ?: "") }
    var date by remember { mutableStateOf(transaction?.transactionDate ?: LocalDate.now().toString()) }
    var selectedCategoryId by remember {
        val initialCatId = transaction?.categoryId ?: categories.firstOrNull { it.type == (transaction?.type ?: initialType) }?.id ?: 1L
        mutableStateOf(initialCatId)
    }
    var errorMessage by remember { mutableStateOf("") }

    val filteredCategories = categories.filter { it.type == type && it.isActive }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        title = {
            Text(
                text = if (transaction != null) "Edit Transaksi" else "Tambah Transaksi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NeoChip(
                        text = "Pengeluaran",
                        selected = type == "expense",
                        onClick = {
                            type = "expense"
                            categories.firstOrNull { it.type == "expense" }?.let { selectedCategoryId = it.id }
                        },
                        selectedColor = NeoRed,
                        modifier = Modifier.weight(1f),
                        testTag = "type_expense_chip"
                    )
                    NeoChip(
                        text = "Pemasukan",
                        selected = type == "income",
                        onClick = {
                            type = "income"
                            categories.firstOrNull { it.type == "income" }?.let { selectedCategoryId = it.id }
                        },
                        selectedColor = NeoGreen,
                        modifier = Modifier.weight(1f),
                        testTag = "type_income_chip"
                    )
                }

                // Nominal (Integer Rupiah)
                NeoTextField(
                    value = amountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) amountText = input
                    },
                    label = "Nominal (Rupiah)",
                    placeholder = "Contoh: 50000",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "transaction_amount_input"
                )

                // Kategori Selector
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredCategories, key = { it.id }) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeoYellow else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isSelected) LightText else Color.Transparent, RoundedCornerShape(6.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) LightText else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Tanggal
                NeoTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Tanggal (YYYY-MM-DD)",
                    placeholder = "2026-10-14",
                    testTag = "transaction_date_input"
                )

                // Catatan
                NeoTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Catatan (Opsional)",
                    placeholder = "Makan siang, bensin, dll",
                    testTag = "transaction_note_input"
                )

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = NeoRed,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            NeoButton(
                onClick = {
                    val amountLong = amountText.toLongOrNull() ?: 0L
                    if (amountLong <= 0) {
                        errorMessage = "Nominal harus lebih besar dari 0!"
                        return@NeoButton
                    }
                    if (date.isBlank()) {
                        errorMessage = "Tanggal wajib diisi!"
                        return@NeoButton
                    }
                    val item = TransactionItem(
                        id = transaction?.id ?: 0L,
                        type = type,
                        categoryId = selectedCategoryId,
                        amount = amountLong,
                        transactionDate = date,
                        note = note.ifBlank { null }
                    )
                    onSave(item)
                },
                text = "Simpan",
                backgroundColor = NeoYellow,
                testTag = "save_transaction_button"
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
