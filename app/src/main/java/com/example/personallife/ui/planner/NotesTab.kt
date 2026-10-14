package com.example.personallife.ui.planner

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.example.personallife.data.models.ActivityNoteItem
import com.example.personallife.data.models.DailyNoteItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoConfirmDialog
import com.example.personallife.ui.components.NeoDatePickerDialog
import com.example.personallife.ui.components.NeoDatePickerField
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import java.time.LocalDate

@Composable
fun NotesTab(
    selectedDate: LocalDate,
    dailyNote: DailyNoteItem?,
    activityNotes: List<ActivityNoteItem>,
    onSaveDailyNote: (date: String, content: String) -> Unit,
    onSaveActivityNote: (ActivityNoteItem) -> Unit,
    onDeleteActivityNote: (id: Long, date: String) -> Unit,
    onSelectDate: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var dailyText by remember { mutableStateOf(dailyNote?.content ?: "") }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<ActivityNoteItem?>(null) }

    LaunchedEffect(dailyNote) {
        dailyText = dailyNote?.content ?: ""
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // 1. CATATAN HARIAN (Daily Note)
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
                    cornerRadius = 14.dp,
                    testTag = "daily_note_card"
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CATATAN HARIAN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(modifier = Modifier.clickable { showDatePickerDialog = true }) {
                                NeoBadge(
                                    text = FormatUtils.formatDateDisplay(selectedDate.toString()),
                                    color = NeoYellow,
                                    textColor = LightText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        NeoTextField(
                            value = dailyText,
                            onValueChange = { dailyText = it },
                            label = "Refleksi / Catatan Hari Ini",
                            placeholder = "Tulis apa saja yang terjadi hari ini...",
                            singleLine = false,
                            maxLines = 4,
                            testTag = "daily_note_input"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        NeoButton(
                            onClick = { onSaveDailyNote(selectedDate.toString(), dailyText) },
                            text = "Simpan Catatan Harian",
                            backgroundColor = NeoGreen,
                            leadingIcon = Icons.Default.Save,
                            testTag = "save_daily_note_button"
                        )
                    }
                }
            }

            // 2. CATATAN AKTIVITAS (Activity Notes)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CATATAN AKTIVITAS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    NeoBadge(
                        text = "${activityNotes.size} Catatan",
                        color = NeoGreen,
                        textColor = LightText
                    )
                }
            }

            if (activityNotes.isEmpty()) {
                item {
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "Belum ada catatan aktivitas khusus hari ini. Tambahkan detail notulensi atau jurnal melalui tombol '+'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(activityNotes, key = { it.id }) { note ->
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface,
                        testTag = "activity_note_${note.id}"
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title ?: "Catatan Aktivitas",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                IconButton(
                                    onClick = { noteToDelete = note },
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

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddActivityDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_activity_note_fab"),
            containerColor = NeoGreen,
            contentColor = LightText
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Catatan Baru",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Add Activity Note Dialog
    if (showAddActivityDialog) {
        var noteDate by remember { mutableStateOf(selectedDate.toString()) }
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddActivityDialog = false },
            title = {
                Text("Tambah Catatan Aktivitas", fontWeight = FontWeight.Black)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NeoDatePickerField(
                        value = noteDate,
                        onDateSelected = { noteDate = it },
                        label = "Tanggal Catatan",
                        testTag = "activity_note_date_input"
                    )
                    NeoTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = "Judul Catatan",
                        placeholder = "Notulensi rapat, ide proyek...",
                        testTag = "activity_note_title_input"
                    )
                    NeoTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = "Isi Catatan",
                        placeholder = "Tulis isi catatan lengkap...",
                        singleLine = false,
                        maxLines = 4,
                        testTag = "activity_note_content_input"
                    )
                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        if (noteContent.isBlank()) {
                            errorMsg = "Isi catatan tidak boleh kosong!"
                            return@NeoButton
                        }
                        onSaveActivityNote(
                            ActivityNoteItem(
                                title = noteTitle.ifBlank { null },
                                content = noteContent,
                                activityDate = noteDate
                            )
                        )
                        showAddActivityDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_activity_note_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddActivityDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Daily Note Date Picker
    if (showDatePickerDialog) {
        NeoDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { date ->
                onSelectDate(date)
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    noteToDelete?.let { note ->
        NeoConfirmDialog(
            show = true,
            title = "Hapus Catatan",
            message = "Yakin ingin menghapus catatan '${note.title ?: "Aktivitas"}'?",
            confirmText = "Hapus",
            confirmColor = NeoRed,
            onConfirm = {
                onDeleteActivityNote(note.id, selectedDate.toString())
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}
