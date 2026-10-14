package com.example.personallife.ui.settings

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.personallife.data.models.FormatUtils
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    show: Boolean,
    openingBalance: Long,
    isPinEnabled: Boolean,
    currentTheme: String,
    onDismiss: () -> Unit,
    onSetOpeningBalance: (Long) -> Unit,
    onSetPin: (String) -> Unit,
    onDisablePin: () -> Unit,
    onSetThemeMode: (String) -> Unit,
    onExportBackup: suspend () -> String,
    onImportBackup: suspend (String) -> Boolean
) {
    if (!show) return

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var showBalanceDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupResultDialog by remember { mutableStateOf(false) }
    var backupJsonString by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var importStatusMsg by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
        title = {
            Text(
                text = "Pengaturan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. SALDO AWAL (Opening Balance)
                NeoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Wallet, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.padding(start = 6.dp))
                                Text("Saldo Awal", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Text(
                                text = FormatUtils.formatRupiah(openingBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        NeoButton(
                            onClick = { showBalanceDialog = true },
                            text = "Ubah Saldo Awal",
                            backgroundColor = NeoYellow,
                            borderWidth = 1.5.dp,
                            shadowOffset = 2.dp,
                            testTag = "edit_opening_balance_button"
                        )
                    }
                }

                // 2. KEAMANAN PIN
                NeoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.padding(start = 6.dp))
                                Text("Kunci Aplikasi (PIN)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            NeoBadge(
                                text = if (isPinEnabled) "Aktif" else "Nonaktif",
                                color = if (isPinEnabled) NeoGreen else NeoRed,
                                textColor = LightText
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NeoButton(
                                onClick = { showPinDialog = true },
                                text = if (isPinEnabled) "Ubah PIN" else "Pasang PIN",
                                backgroundColor = NeoYellow,
                                modifier = Modifier.weight(1f),
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp,
                                testTag = "setup_pin_button"
                            )
                            if (isPinEnabled) {
                                NeoButton(
                                    onClick = onDisablePin,
                                    text = "Matikan PIN",
                                    backgroundColor = NeoRed,
                                    modifier = Modifier.weight(1f),
                                    borderWidth = 1.5.dp,
                                    shadowOffset = 2.dp,
                                    testTag = "disable_pin_button"
                                )
                            }
                        }
                    }
                }

                // 3. TEMA TAMPILAN
                NeoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.padding(start = 6.dp))
                            Text("Tema Tampilan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            NeoChip(text = "Sistem", selected = currentTheme == "system", onClick = { onSetThemeMode("system") }, modifier = Modifier.weight(1f))
                            NeoChip(text = "Terang", selected = currentTheme == "light", onClick = { onSetThemeMode("light") }, selectedColor = NeoYellow, modifier = Modifier.weight(1f))
                            NeoChip(text = "Gelap", selected = currentTheme == "dark", onClick = { onSetThemeMode("dark") }, selectedColor = NeoGreen, modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 4. CADANGAN & PEMULIHAN (Backup & Restore)
                NeoCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Cadangan & Pemulihan Data", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ekspor dan impor basis data SQLite / JSON terstruktur secara lokal dan aman.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            NeoButton(
                                onClick = {
                                    scope.launch {
                                        backupJsonString = onExportBackup()
                                        showBackupResultDialog = true
                                    }
                                },
                                text = "Ekspor",
                                leadingIcon = Icons.Default.Upload,
                                backgroundColor = NeoGreen,
                                modifier = Modifier.weight(1f),
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp,
                                testTag = "export_backup_button"
                            )
                            NeoButton(
                                onClick = { showImportDialog = true },
                                text = "Impor",
                                leadingIcon = Icons.Default.Download,
                                backgroundColor = NeoYellow,
                                modifier = Modifier.weight(1f),
                                borderWidth = 1.5.dp,
                                shadowOffset = 2.dp,
                                testTag = "import_backup_button"
                            )
                        }
                    }
                }

                HorizontalDivider(color = DividerDefaults.color)

                Text(
                    text = "Personal Life Management v1.0.0\nOffline-First • SQLite Lokal • Neo-Brutalism",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            NeoButton(
                onClick = onDismiss,
                text = "Tutup",
                backgroundColor = NeoYellow,
                testTag = "close_settings_button"
            )
        }
    )

    // Sub-dialog: Ubah Saldo Awal
    if (showBalanceDialog) {
        var balanceInput by remember { mutableStateOf(openingBalance.toString()) }
        AlertDialog(
            onDismissRequest = { showBalanceDialog = false },
            title = { Text("Atur Saldo Awal", fontWeight = FontWeight.Black) },
            text = {
                NeoTextField(
                    value = balanceInput,
                    onValueChange = { if (it.all { c -> c.isDigit() }) balanceInput = it },
                    label = "Nominal Saldo Awal (Rp)",
                    placeholder = "0",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    testTag = "opening_balance_input"
                )
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        val amount = balanceInput.toLongOrNull() ?: 0L
                        onSetOpeningBalance(amount)
                        showBalanceDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow
                )
            },
            dismissButton = {
                TextButton(onClick = { showBalanceDialog = false }) { Text("Batal") }
            }
        )
    }

    // Sub-dialog: Atur PIN
    if (showPinDialog) {
        var pinInput by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Atur PIN Aplikasi", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan 4 digit PIN:", style = MaterialTheme.typography.bodyMedium)
                    NeoTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = "PIN (4 Angka)",
                        placeholder = "1234",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        testTag = "new_pin_input"
                    )
                    if (pinError.isNotEmpty()) {
                        Text(pinError, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        if (pinInput.length != 4) {
                            pinError = "PIN harus terdiri dari 4 digit angka!"
                            return@NeoButton
                        }
                        onSetPin(pinInput)
                        showPinDialog = false
                    },
                    text = "Simpan PIN",
                    backgroundColor = NeoYellow
                )
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Batal") }
            }
        )
    }

    // Sub-dialog: Hasil Ekspor
    if (showBackupResultDialog) {
        AlertDialog(
            onDismissRequest = { showBackupResultDialog = false },
            title = { Text("Cadangan Berhasil Diekspor", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Salin data cadangan berikut untuk disimpan dengan aman:")
                    NeoTextField(
                        value = backupJsonString,
                        onValueChange = {},
                        label = "JSON Cadangan",
                        singleLine = false,
                        maxLines = 6
                    )
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(backupJsonString))
                        Toast.makeText(context, "Data cadangan disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        showBackupResultDialog = false
                    },
                    text = "Salin ke Clipboard",
                    backgroundColor = NeoGreen
                )
            },
            dismissButton = {
                TextButton(onClick = { showBackupResultDialog = false }) { Text("Tutup") }
            }
        )
    }

    // Sub-dialog: Impor Cadangan
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Impor Cadangan", fontWeight = FontWeight.Black) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tempelkan data JSON cadangan yang sah:")
                    NeoTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        label = "Data JSON Cadangan",
                        placeholder = "{\"app\": \"Personal Life Management\"...}",
                        singleLine = false,
                        maxLines = 6,
                        testTag = "import_json_input"
                    )
                    if (importStatusMsg.isNotEmpty()) {
                        Text(importStatusMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        scope.launch {
                            val success = onImportBackup(importJsonInput)
                            if (success) {
                                Toast.makeText(context, "Data berhasil dipulihkan!", Toast.LENGTH_SHORT).show()
                                showImportDialog = false
                                importJsonInput = ""
                                importStatusMsg = ""
                            } else {
                                importStatusMsg = "Format file cadangan tidak valid atau rusak!"
                            }
                        }
                    },
                    text = "Pulihkan Sekarang",
                    backgroundColor = NeoRed,
                    testTag = "confirm_import_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Batal") }
            }
        )
    }
}
