package com.example.personallife.ui.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.data.models.TaskItem
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoDatePickerField
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import java.time.LocalDate

@Composable
fun TasksTab(
    tasks: List<TaskItem>,
    onAddTask: (TaskItem) -> Unit,
    onUpdateTask: (TaskItem) -> Unit,
    onToggleTaskStatus: (id: Long, newStatus: String) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onScheduleTaskToCalendar: (task: TaskItem, startAt: String, endAt: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var statusFilter by remember { mutableStateOf("all") } // "all", "todo", "in_progress", "done"
    var priorityFilter by remember { mutableStateOf("all") } // "all", "high", "medium", "low"
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TaskItem?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskItem?>(null) }
    var taskToSchedule by remember { mutableStateOf<TaskItem?>(null) }

    val filteredList = tasks.filter { task ->
        val matchesStatus = if (statusFilter == "all") true else task.status == statusFilter
        val matchesPriority = if (priorityFilter == "all") true else task.priority == priorityFilter
        val matchesSearch = if (searchQuery.isBlank()) true else {
            task.title.contains(searchQuery, ignoreCase = true) ||
            task.description?.contains(searchQuery, ignoreCase = true) == true
        }
        matchesStatus && matchesPriority && matchesSearch
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            NeoTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "",
                placeholder = "Cari tugas...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth(),
                testTag = "task_search_input"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoChip(text = "Semua Status", selected = statusFilter == "all", onClick = { statusFilter = "all" })
                NeoChip(text = "To Do", selected = statusFilter == "todo", onClick = { statusFilter = "todo" }, selectedColor = NeoYellow)
                NeoChip(text = "In Progress", selected = statusFilter == "in_progress", onClick = { statusFilter = "in_progress" }, selectedColor = NeoOrange)
                NeoChip(text = "Done", selected = statusFilter == "done", onClick = { statusFilter = "done" }, selectedColor = NeoGreen)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Priority Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoChip(text = "Semua Prioritas", selected = priorityFilter == "all", onClick = { priorityFilter = "all" })
                NeoChip(text = "Tinggi", selected = priorityFilter == "high", onClick = { priorityFilter = "high" }, selectedColor = NeoRed)
                NeoChip(text = "Sedang", selected = priorityFilter == "medium", onClick = { priorityFilter = "medium" }, selectedColor = NeoOrange)
                NeoChip(text = "Rendah", selected = priorityFilter == "low", onClick = { priorityFilter = "low" }, selectedColor = NeoGreen)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "Tidak ada tugas yang sesuai filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { task ->
                        val isDone = task.status == "done"

                        NeoCard(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            testTag = "task_item_${task.id}"
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Neo Checkbox
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
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
                                                contentDescription = null,
                                                tint = LightText,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!task.description.isNullOrBlank()) {
                                            Text(
                                                text = task.description,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                        if (!task.deadline.isNullOrBlank()) {
                                            Text(
                                                text = "Deadline: ${FormatUtils.formatDateDisplay(task.deadline)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NeoOrange,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val (pLabel, pColor) = when (task.priority) {
                                        "high" -> Pair("Tinggi", NeoRed)
                                        "medium" -> Pair("Sedang", NeoOrange)
                                        else -> Pair("Rendah", NeoGreen)
                                    }
                                    NeoBadge(text = pLabel, color = pColor, textColor = LightText)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Status Badge & Action row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (statusLabel, statusColor) = when (task.status) {
                                        "done" -> Pair("Done", NeoGreen)
                                        "in_progress" -> Pair("In Progress", NeoOrange)
                                        else -> Pair("To Do", NeoYellow)
                                    }
                                    NeoBadge(text = statusLabel, color = statusColor, textColor = LightText)

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Schedule to calendar button
                                        IconButton(
                                            onClick = { taskToSchedule = task },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = "Jadwalkan",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { editingTask = task },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { taskToDelete = task },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus",
                                                tint = NeoRed,
                                                modifier = Modifier.size(18.dp)
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_task_fab"),
            containerColor = NeoYellow,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Tugas",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add / Edit Task Dialog
    if (showAddDialog || editingTask != null) {
        val current = editingTask
        var title by remember { mutableStateOf(current?.title ?: "") }
        var description by remember { mutableStateOf(current?.description ?: "") }
        var priority by remember { mutableStateOf(current?.priority ?: "medium") }
        var status by remember { mutableStateOf(current?.status ?: "todo") }
        var deadline by remember { mutableStateOf(current?.deadline ?: "") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                editingTask = null
            },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = if (current != null) "Edit Tugas" else "Tambah Tugas Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeoTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Judul Tugas",
                        placeholder = "Selesaikan laporan, bayar listrik...",
                        testTag = "task_title_input"
                    )

                    NeoTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Deskripsi (Opsional)",
                        placeholder = "Detail tugas...",
                        singleLine = false,
                        maxLines = 3,
                        testTag = "task_desc_input"
                    )

                    // Priority
                    Text(
                        text = "Prioritas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeoChip(text = "Tinggi", selected = priority == "high", onClick = { priority = "high" }, selectedColor = NeoRed, modifier = Modifier.weight(1f))
                        NeoChip(text = "Sedang", selected = priority == "medium", onClick = { priority = "medium" }, selectedColor = NeoOrange, modifier = Modifier.weight(1f))
                        NeoChip(text = "Rendah", selected = priority == "low", onClick = { priority = "low" }, selectedColor = NeoGreen, modifier = Modifier.weight(1f))
                    }

                    // Status
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        NeoChip(text = "To Do", selected = status == "todo", onClick = { status = "todo" }, selectedColor = NeoYellow, modifier = Modifier.weight(1f))
                        NeoChip(text = "In Progress", selected = status == "in_progress", onClick = { status = "in_progress" }, selectedColor = NeoOrange, modifier = Modifier.weight(1f))
                        NeoChip(text = "Done", selected = status == "done", onClick = { status = "done" }, selectedColor = NeoGreen, modifier = Modifier.weight(1f))
                    }

                    NeoDatePickerField(
                        value = deadline,
                        onDateSelected = { deadline = it },
                        label = "Deadline (Opsional)",
                        optional = true,
                        onClear = { deadline = "" },
                        testTag = "task_deadline_input"
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        if (title.isBlank()) {
                            errorMsg = "Judul tugas tidak boleh kosong!"
                            return@NeoButton
                        }
                        val item = TaskItem(
                            id = current?.id ?: 0L,
                            title = title,
                            description = description.ifBlank { null },
                            priority = priority,
                            status = status,
                            deadline = deadline.ifBlank { null }
                        )
                        if (current != null) {
                            onUpdateTask(item)
                        } else {
                            onAddTask(item)
                        }
                        showAddDialog = false
                        editingTask = null
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_task_button"
                )
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    editingTask = null
                }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Schedule Task To Calendar Dialog
    taskToSchedule?.let { task ->
        var dateText by remember { mutableStateOf(task.deadline ?: LocalDate.now().toString()) }
        var startTime by remember { mutableStateOf("09:00") }
        var endTime by remember { mutableStateOf("10:00") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { taskToSchedule = null },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Jadwalkan Tugas ke Kalender",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Tugas: ${task.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    NeoDatePickerField(
                        value = dateText,
                        onDateSelected = { dateText = it },
                        label = "Tanggal Jadwal",
                        testTag = "schedule_date_input"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeoTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = "Waktu Mulai",
                            placeholder = "09:00",
                            modifier = Modifier.weight(1f)
                        )
                        NeoTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = "Waktu Selesai",
                            placeholder = "10:00",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        if (dateText.isBlank() || startTime >= endTime) {
                            errorMsg = "Waktu selesai harus lebih besar dari mulai!"
                            return@NeoButton
                        }
                        val startAt = "${dateText}T$startTime:00"
                        val endAt = "${dateText}T$endTime:00"
                        onScheduleTaskToCalendar(task, startAt, endAt)
                        taskToSchedule = null
                    },
                    text = "Jadwalkan",
                    backgroundColor = NeoGreen,
                    testTag = "confirm_schedule_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { taskToSchedule = null }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Delete Confirmation
    taskToDelete?.let { task ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Tugas",
            message = "Yakin ingin menghapus tugas '${task.title}'?",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteTask(task.id)
                taskToDelete = null
            },
            onDismiss = { taskToDelete = null }
        )
    }
}
