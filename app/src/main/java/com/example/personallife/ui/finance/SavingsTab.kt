package com.example.personallife.ui.finance

import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Savings
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
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.data.models.SavingsGoalItem
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow

@Composable
fun SavingsTab(
    savingsGoals: List<SavingsGoalItem>,
    onAddSavingsGoal: (SavingsGoalItem) -> Unit,
    onAddDeposit: (id: Long, amount: Long) -> Unit,
    onDeleteSavingsGoal: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var depositGoal by remember { mutableStateOf<SavingsGoalItem?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalItem?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            if (savingsGoals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Belum ada target tabungan.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { showAddDialog = true },
                            text = "+ Buat Target Tabungan",
                            backgroundColor = NeoYellow,
                            testTag = "empty_state_add_savings_button"
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(savingsGoals, key = { it.id }) { goal ->
                        NeoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            testTag = "savings_item_${goal.id}"
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = goal.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (goal.isCompleted) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Tercapai",
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        NeoBadge(
                                            text = "${(goal.progress * 100).toInt()}%",
                                            color = if (goal.isCompleted) NeoGreen else NeoYellow,
                                            textColor = LightText
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { goalToDelete = goal },
                                            modifier = Modifier.size(24.dp)
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

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Terkumpul: ${FormatUtils.formatRupiah(goal.savedAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Target: ${FormatUtils.formatRupiah(goal.targetAmount)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { goal.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(5.dp)),
                                    color = if (goal.isCompleted) NeoGreen else NeoYellow,
                                    trackColor = MaterialTheme.colorScheme.background
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (goal.isCompleted) "Target Tercapai!" else "Sisa: ${FormatUtils.formatRupiah(goal.remaining)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goal.isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!goal.isCompleted) {
                                        NeoButton(
                                            onClick = { depositGoal = goal },
                                            text = "+ Nabung",
                                            backgroundColor = NeoGreen,
                                            cornerRadius = 6.dp,
                                            borderWidth = 1.5.dp,
                                            shadowOffset = 2.dp,
                                            testTag = "deposit_button_${goal.id}"
                                        )
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_savings_fab"),
            containerColor = NeoYellow,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Target Baru",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Goal Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("") }
        var initialSavedText by remember { mutableStateOf("") }
        var targetDate by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Buat Target Tabungan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nama Target",
                        placeholder = "Contoh: Dana Darurat, Liburan Bali",
                        testTag = "savings_name_input"
                    )

                    NeoTextField(
                        value = targetText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) targetText = it },
                        label = "Nominal Target (Rp)",
                        placeholder = "Contoh: 10000000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "savings_target_input"
                    )

                    NeoTextField(
                        value = initialSavedText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) initialSavedText = it },
                        label = "Saldo Tabungan Awal (Opsional)",
                        placeholder = "0",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "savings_initial_input"
                    )

                    NeoTextField(
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        label = "Target Tanggal (Opsional)",
                        placeholder = "YYYY-MM-DD",
                        testTag = "savings_date_input"
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        val target = targetText.toLongOrNull() ?: 0L
                        if (name.isBlank()) {
                            errorMsg = "Nama target tidak boleh kosong!"
                            return@NeoButton
                        }
                        if (target <= 0) {
                            errorMsg = "Nominal target harus lebih besar dari 0!"
                            return@NeoButton
                        }
                        val initial = initialSavedText.toLongOrNull() ?: 0L
                        onAddSavingsGoal(
                            SavingsGoalItem(
                                name = name,
                                targetAmount = target,
                                savedAmount = initial,
                                targetDate = targetDate.ifBlank { null },
                                note = note.ifBlank { null }
                            )
                        )
                        showAddDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_savings_goal_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Deposit Dialog
    depositGoal?.let { goal ->
        var depositText by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { depositGoal = null },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Tambah Tabungan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Setor ke: ${goal.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sisa untuk mencapai target: ${FormatUtils.formatRupiah(goal.remaining)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    NeoTextField(
                        value = depositText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) depositText = it },
                        label = "Jumlah Setoran (Rp)",
                        placeholder = "Contoh: 250000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        testTag = "deposit_amount_input"
                    )
                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        val amount = depositText.toLongOrNull() ?: 0L
                        if (amount <= 0) {
                            errorMsg = "Nominal harus lebih dari 0!"
                            return@NeoButton
                        }
                        onAddDeposit(goal.id, amount)
                        depositGoal = null
                    },
                    text = "Tambah",
                    backgroundColor = NeoGreen,
                    testTag = "submit_deposit_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { depositGoal = null }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Delete Confirmation
    goalToDelete?.let { goal ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Target Tabungan",
            message = "Yakin ingin menghapus target tabungan '${goal.name}'?",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteSavingsGoal(goal.id)
                goalToDelete = null
            },
            onDismiss = { goalToDelete = null }
        )
    }
}
