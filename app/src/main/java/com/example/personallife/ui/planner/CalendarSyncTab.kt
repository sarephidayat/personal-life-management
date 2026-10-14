package com.example.personallife.ui.planner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow

@Composable
fun CalendarSyncTab(
    events: List<CalendarEventItem>,
    syncQueueCount: Int,
    lastSyncMessage: String?,
    onPerformManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingEvents = events.filter { it.syncStatus != "synced" && it.syncStatus != "local_only" }
    val syncedEvents = events.filter { it.syncStatus == "synced" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // Sync Status Hero Card
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = NeoYellow,
                cornerRadius = 14.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GOOGLE CALENDAR SYNC",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = LightText
                        )
                        NeoBadge(
                            text = "Manual Satu Arah",
                            color = NeoGreen,
                            textColor = LightText
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Sinkronisasi Manual",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = LightText
                    )
                    Text(
                        text = "Kirim jadwal yang dibuat di aplikasi ke kalender utama akun Google secara aman tanpa polling latar belakang.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightText
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    NeoButton(
                        onClick = onPerformManualSync,
                        text = "Mulai Sinkronisasi (${pendingEvents.size} Menunggu)",
                        backgroundColor = NeoGreen,
                        leadingIcon = Icons.Default.Sync,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "trigger_manual_sync_button"
                    )

                    if (!lastSyncMessage.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = lastSyncMessage,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LightText
                        )
                    }
                }
            }
        }

        // Information card
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    Column {
                        Text(
                            text = "Prinsip Privasi & Arsitektur:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Seluruh data finansial tidak pernah dikirim ke Google.\n• Hanya event kalender yang dikelola aplikasi yang disinkronkan.\n• Event eksternal tidak diimpor ke aplikasi lokal demi performa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section: Antrean Sinkronisasi
        item {
            Text(
                text = "ANTREAN MENUNGGU SINKRONISASI (${pendingEvents.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (pendingEvents.isEmpty()) {
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Tidak ada jadwal yang menunggu sinkronisasi.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(pendingEvents, key = { it.id }) { item ->
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = FormatUtils.formatDateDisplay(item.startAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val (sLabel, sColor) = when (item.syncStatus) {
                            "pending_create" -> Pair("Perlu Dibuat", NeoYellow)
                            "pending_update" -> Pair("Perlu Update", NeoOrange)
                            "pending_delete" -> Pair("Perlu Hapus", NeoRed)
                            else -> Pair("Gagal", NeoRed)
                        }
                        NeoBadge(text = sLabel, color = sColor, textColor = LightText)
                    }
                }
            }
        }

        // Section: Event Tersinkron
        item {
            Text(
                text = "JADWAL SUDAH TERSINKRON (${syncedEvents.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (syncedEvents.isEmpty()) {
            item {
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Text(
                        text = "Belum ada event yang tersinkron ke Google Calendar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(syncedEvents, key = { it.id }) { item ->
                NeoCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = FormatUtils.formatDateDisplay(item.startAt) + " • ID: ${item.googleEventId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        NeoBadge(text = "Tersinkron", color = NeoGreen, textColor = LightText)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
