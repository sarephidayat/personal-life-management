package com.example.personallife.ui.planner

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
import com.example.personallife.data.models.ActivityNoteItem
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.DailyNoteItem
import com.example.personallife.data.models.TaskItem
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.theme.NeoYellow
import com.example.personallife.ui.viewmodel.MainUiState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    state: MainUiState,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    onAddTask: (TaskItem) -> Unit,
    onUpdateTask: (TaskItem) -> Unit,
    onToggleTaskStatus: (id: Long, newStatus: String) -> Unit,
    onDeleteTask: (Long) -> Unit,
    onScheduleTaskToCalendar: (task: TaskItem, startAt: String, endAt: String) -> Unit,
    onAddCalendarEvent: (CalendarEventItem) -> Unit,
    onDeleteCalendarEvent: (Long) -> Unit,
    onSaveDailyNote: (date: String, content: String) -> Unit,
    onSaveActivityNote: (ActivityNoteItem) -> Unit,
    onDeleteActivityNote: (id: Long, date: String) -> Unit,
    onPerformManualSync: () -> Unit,
    onConnectGoogle: () -> Unit,
    onVerifyToken: (String) -> Unit,
    onDisconnectGoogle: () -> Unit
) {
    val subTabs = listOf("Kalender", "Tugas", "Catatan", "Sync Kalender")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Aktivitas",
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
                0 -> CalendarTab(
                    events = state.calendarEvents,
                    selectedDate = state.selectedDate,
                    onSelectDate = onSelectDate,
                    onAddEvent = onAddCalendarEvent,
                    onDeleteEvent = onDeleteCalendarEvent
                )
                1 -> TasksTab(
                    tasks = state.tasks,
                    onAddTask = onAddTask,
                    onUpdateTask = onUpdateTask,
                    onToggleTaskStatus = onToggleTaskStatus,
                    onDeleteTask = onDeleteTask,
                    onScheduleTaskToCalendar = onScheduleTaskToCalendar
                )
                2 -> NotesTab(
                    selectedDate = state.selectedDate,
                    dailyNote = state.dailyNote,
                    activityNotes = state.activityNotes,
                    onSaveDailyNote = onSaveDailyNote,
                    onSaveActivityNote = onSaveActivityNote,
                    onDeleteActivityNote = onDeleteActivityNote,
                    onSelectDate = onSelectDate
                )
                3 -> CalendarSyncTab(
                    events = state.calendarEvents,
                    syncQueueCount = state.syncQueueCount,
                    lastSyncMessage = state.lastSyncMessage,
                    calendarConnectionState = state.calendarConnectionState,
                    onConnectGoogle = onConnectGoogle,
                    onVerifyToken = onVerifyToken,
                    onDisconnectGoogle = onDisconnectGoogle,
                    onPerformManualSync = onPerformManualSync
                )
            }
        }
    }
}
