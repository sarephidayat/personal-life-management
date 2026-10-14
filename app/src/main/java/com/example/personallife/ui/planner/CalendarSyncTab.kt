package com.example.personallife.ui.planner

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.data.google.CalendarConnectionState
import com.example.personallife.data.google.GoogleCalendarService
import com.example.personallife.data.models.CalendarEventItem
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoBlue
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoPurple
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow

@Composable
fun CalendarSyncTab(
    events: List<CalendarEventItem>,
    syncQueueCount: Int,
    lastSyncMessage: String?,
    calendarConnectionState: CalendarConnectionState,
    onConnectGoogle: () -> Unit,
    onVerifyToken: (String) -> Unit,
    onDisconnectGoogle: () -> Unit,
    onPerformManualSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showManualTokenDialog by remember { mutableStateOf(false) }

    val pendingEvents = events.filter { it.syncStatus != "synced" && it.syncStatus != "local_only" }
    val syncedEvents = events.filter { it.syncStatus == "synced" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        // --- 1. PHASE 1 GOOGLE OAUTH & READ-ONLY VERIFICATION CARD ---
        item {
            NeoCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surface,
                cornerRadius = 14.dp,
                shadowOffset = 4.dp,
                testTag = "google_auth_card"
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KONEKSI GOOGLE CALENDAR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val (statusBadgeText, statusBadgeColor) = when (calendarConnectionState) {
                            is CalendarConnectionState.Connected -> Pair("Terhubung", NeoGreen)
                            is CalendarConnectionState.Connecting -> Pair("Memverifikasi", NeoYellow)
                            is CalendarConnectionState.Error -> Pair("Gagal", NeoRed)
                            is CalendarConnectionState.NotConnected -> Pair("Belum Terhubung", Color.LightGray)
                        }
                        NeoBadge(text = statusBadgeText, color = statusBadgeColor, textColor = LightText)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Account & Scope Specifications
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Akun Target: ${GoogleCalendarService.TARGET_ACCOUNT}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Scope: ${GoogleCalendarService.SCOPE_CALENDAR_READONLY}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Dynamic State Presentation
                    when (calendarConnectionState) {
                        is CalendarConnectionState.Connected -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Verifikasi Akses Read-Only Berhasil!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF2E7D32)
                                    )
                                }

                                Text(
                                    text = "• Akun Terverifikasi: ${calendarConnectionState.email}\n" +
                                            "• Nama Kalender: ${calendarConnectionState.calendarSummary}\n" +
                                            "• Zona Waktu: ${calendarConnectionState.timeZone}\n" +
                                            "• Jumlah Event Primer: ${calendarConnectionState.eventCount} item\n" +
                                            "• Terakhir Diperiksa: ${calendarConnectionState.lastVerifiedAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeoButton(
                                        onClick = onConnectGoogle,
                                        text = "Uji Ulang",
                                        leadingIcon = Icons.Default.Refresh,
                                        backgroundColor = NeoGreen,
                                        modifier = Modifier.weight(1f),
                                        testTag = "retest_connection_button"
                                    )
                                    NeoButton(
                                        onClick = onDisconnectGoogle,
                                        text = "Putuskan",
                                        backgroundColor = NeoRed,
                                        modifier = Modifier.weight(1f),
                                        testTag = "disconnect_calendar_button"
                                    )
                                }
                            }
                        }

                        is CalendarConnectionState.Connecting -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Menghubungi Google Calendar API...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        is CalendarConnectionState.Error -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = null,
                                        tint = NeoRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = calendarConnectionState.message,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NeoRed
                                    )
                                }

                                if (!calendarConnectionState.details.isNullOrEmpty()) {
                                    Text(
                                        text = calendarConnectionState.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeoButton(
                                        onClick = onConnectGoogle,
                                        text = "Coba Lagi",
                                        backgroundColor = NeoYellow,
                                        modifier = Modifier.weight(1f),
                                        testTag = "retry_oauth_button"
                                    )
                                    NeoButton(
                                        onClick = { showManualTokenDialog = true },
                                        text = "Input Token",
                                        leadingIcon = Icons.Default.VpnKey,
                                        backgroundColor = NeoBlue,
                                        modifier = Modifier.weight(1f),
                                        testTag = "manual_token_button"
                                    )
                                }
                            }
                        }

                        is CalendarConnectionState.NotConnected -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Aplikasi belum terhubung ke Google Calendar akun Anda. Lakukan otentikasi izin membaca primary calendar secara aman.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NeoButton(
                                        onClick = onConnectGoogle,
                                        text = "Hubungkan Akun Google",
                                        leadingIcon = Icons.Default.Link,
                                        backgroundColor = NeoYellow,
                                        modifier = Modifier.weight(1f),
                                        testTag = "connect_google_button"
                                    )
                                    NeoButton(
                                        onClick = { showManualTokenDialog = true },
                                        text = "Uji via Token",
                                        leadingIcon = Icons.Default.VpnKey,
                                        backgroundColor = NeoBlue,
                                        testTag = "test_token_button"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. PRIVACY & SAFETY PRINCIPLE CARD ---
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
                            text = "Ketentuan Fase 1 (Read-Only):",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Scope yang diminta hanya 'calendar.events.readonly'.\n" +
                                    "• Tidak ada event kalender yang dibuat, diubah, atau dihapus.\n" +
                                    "• Token akses disimpan di memori dan tidak disimpan plaintext.\n" +
                                    "• Data keuangan tetap 100% lokal dan tidak dikirim ke Google.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // --- 3. EXISTING LOCAL SYNC QUEUE SECTION (PRESERVED) ---
        item {
            Text(
                text = "ANTREAN JADWAL LOKAL (${pendingEvents.size})",
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

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Manual Token Input / Quick Test Dialog
    if (showManualTokenDialog) {
        var tokenInput by remember { mutableStateOf("") }
        var inputError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showManualTokenDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Verifikasi Akses Google Calendar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Masukkan Google OAuth Access Token yang memiliki scope 'calendar.events.readonly' untuk melakukan pengujian GET request langsung ke API primary calendar:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    NeoTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = "OAuth Access Token (Bearer)",
                        placeholder = "ya29.a0Ac...",
                        singleLine = false,
                        maxLines = 4,
                        testTag = "access_token_input"
                    )
                    if (inputError.isNotEmpty()) {
                        Text(
                            text = inputError,
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
                        if (tokenInput.isBlank()) {
                            inputError = "Access token tidak boleh kosong!"
                            return@NeoButton
                        }
                        onVerifyToken(tokenInput.trim())
                        showManualTokenDialog = false
                    },
                    text = "Verifikasi Sekarang",
                    backgroundColor = NeoGreen,
                    testTag = "submit_verify_token_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showManualTokenDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
