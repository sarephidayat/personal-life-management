package com.example.personallife.ui.planner

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoBlue
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun CalendarTab(
    events: List<CalendarEventItem>,
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit,
    onAddEvent: (CalendarEventItem) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentYearMonth by remember { mutableStateOf(YearMonth.of(selectedDate.year, selectedDate.monthValue)) }
    var showAddDialog by remember { mutableStateOf(false) }
    var eventToDelete by remember { mutableStateOf<CalendarEventItem?>(null) }

    val daysInMonth = currentYearMonth.lengthOfMonth()
    val firstDayOfWeek = currentYearMonth.atDay(1).dayOfWeek.value // 1 = Monday ... 7 = Sunday
    val monthTitle = FormatUtils.formatMonthYear(currentYearMonth.year, currentYearMonth.monthValue)

    // Events for selected date
    val selectedDateStr = selectedDate.toString()
    val dayEvents = events.filter { it.startAt.startsWith(selectedDateStr) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Monthly Calendar Card
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 14.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Month header with prev/next buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Bulan Sebelumnya",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Bulan Berikutnya",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day of week headers (Sen, Sel, Rab, Kam, Jum, Sab, Min)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min").forEach { dayLabel ->
                                Text(
                                    text = dayLabel,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Calendar grid
                        val totalCells = ((firstDayOfWeek - 1 + daysInMonth + 6) / 7) * 7
                        for (row in 0 until (totalCells / 7)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                for (col in 1..7) {
                                    val cellIndex = row * 7 + col
                                    val dayNum = cellIndex - (firstDayOfWeek - 1)

                                    if (dayNum in 1..daysInMonth) {
                                        val cellDate = currentYearMonth.atDay(dayNum)
                                        val isSelected = cellDate == selectedDate
                                        val hasEvents = events.any { it.startAt.startsWith(cellDate.toString()) }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp)
                                                .padding(2.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) NeoYellow else Color.Transparent)
                                                .border(
                                                    if (isSelected) 1.5.dp else 0.dp,
                                                    if (isSelected) LightText else Color.Transparent,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable { onSelectDate(cellDate) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                                    color = if (isSelected) LightText else MaterialTheme.colorScheme.onSurface
                                                )
                                                if (hasEvents) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(5.dp)
                                                            .clip(CircleShape)
                                                            .background(if (isSelected) LightText else NeoRed)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events on Selected Date
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AGENDA ${FormatUtils.formatDateIndonesian(selectedDateStr).uppercase()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    NeoBadge(
                        text = "${dayEvents.size} Jadwal",
                        color = NeoBlue,
                        textColor = LightText
                    )
                }
            }

            if (dayEvents.isEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Tidak ada jadwal pada tanggal ini. Klik tombol '+' untuk menambah jadwal baru.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        testTag = "event_item_${event.id}"
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NeoBlue)
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = event.startAt.substringAfter("T").take(5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LightText
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${event.startAt.substringAfter("T").take(5)} - ${event.endAt.substringAfter("T").take(5)}" +
                                            if (!event.location.isNullOrBlank()) " • ${event.location}" else "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (syncLabel, syncColor) = when (event.syncStatus) {
                                    "synced" -> Pair("Google", NeoGreen)
                                    "pending_create" -> Pair("Pending", NeoYellow)
                                    "pending_update" -> Pair("Update", NeoOrange)
                                    "failed" -> Pair("Gagal", NeoRed)
                                    else -> Pair("Lokal", Color.LightGray)
                                }
                                NeoBadge(text = syncLabel, color = syncColor, textColor = LightText)

                                IconButton(
                                    onClick = { eventToDelete = event },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus Jadwal",
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

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_calendar_event_fab"),
            containerColor = NeoBlue,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Jadwal",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Event Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var startTime by remember { mutableStateOf("09:00") }
        var endTime by remember { mutableStateOf("10:00") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Tambah Jadwal Kalender",
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
                        label = "Judul Agenda",
                        placeholder = "Meeting, Olahraga, Kuliah",
                        testTag = "event_title_input"
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeoTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = "Mulai (HH:mm)",
                            placeholder = "09:00",
                            modifier = Modifier.weight(1f),
                            testTag = "event_start_time_input"
                        )
                        NeoTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = "Selesai (HH:mm)",
                            placeholder = "10:00",
                            modifier = Modifier.weight(1f),
                            testTag = "event_end_time_input"
                        )
                    }

                    NeoTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Lokasi (Opsional)",
                        placeholder = "Kantor, Google Meet, Rumah",
                        testTag = "event_location_input"
                    )

                    NeoTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = "Deskripsi (Opsional)",
                        placeholder = "Catatan agenda...",
                        testTag = "event_desc_input"
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
                            errorMsg = "Judul agenda tidak boleh kosong!"
                            return@NeoButton
                        }
                        if (startTime >= endTime) {
                            errorMsg = "Waktu selesai harus lebih besar dari waktu mulai!"
                            return@NeoButton
                        }
                        val startIso = "${selectedDate}T$startTime:00"
                        val endIso = "${selectedDate}T$endTime:00"

                        onAddEvent(
                            CalendarEventItem(
                                title = title,
                                description = description.ifBlank { null },
                                location = location.ifBlank { null },
                                startAt = startIso,
                                endAt = endIso,
                                syncStatus = "pending_create"
                            )
                        )
                        showAddDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_calendar_event_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }

    // Delete Confirmation
    eventToDelete?.let { ev ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Jadwal",
            message = "Yakin ingin menghapus jadwal '${ev.title}'? ${if (ev.syncStatus == "synced") "Event ini juga akan dihapus dari Google Calendar pada sinkronisasi berikutnya." else ""}",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteEvent(ev.id)
                eventToDelete = null
            },
            onDismiss = { eventToDelete = null }
        )
    }
}
