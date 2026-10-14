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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.personallife.data.models.CategoryItem
import com.example.personallife.ui.components.NeoBadge
import com.example.personallife.ui.components.NeoButton
import com.example.personallife.ui.components.NeoCard
import com.example.personallife.ui.components.NeoChip
import com.example.personallife.ui.components.NeoTextField
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow

@Composable
fun CategoriesTab(
    categories: List<CategoryItem>,
    onAddCategory: (CategoryItem) -> Unit,
    onToggleCategoryActive: (id: Long, active: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var typeFilter by remember { mutableStateOf("expense") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredList = categories.filter { it.type == typeFilter }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NeoChip(
                    text = "Kategori Pengeluaran",
                    selected = typeFilter == "expense",
                    onClick = { typeFilter = "expense" },
                    selectedColor = NeoRed,
                    modifier = Modifier.weight(1f)
                )
                NeoChip(
                    text = "Kategori Pemasukan",
                    selected = typeFilter == "income",
                    onClick = { typeFilter = "income" },
                    selectedColor = NeoGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { cat ->
                    NeoCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (cat.isDefault) "Default bawaan" else "Kustom pengguna",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (cat.isActive) {
                                    NeoBadge(text = "Aktif", color = NeoGreen, textColor = LightText)
                                } else {
                                    NeoBadge(text = "Nonaktif", color = NeoRed, textColor = LightText)
                                }
                                Spacer(modifier = Modifier.padding(start = 8.dp))
                                Switch(
                                    checked = cat.isActive,
                                    onCheckedChange = { onToggleCategoryActive(cat.id, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = LightText,
                                        checkedTrackColor = NeoGreen
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_category_fab"),
            containerColor = NeoYellow,
            contentColor = LightText,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tambah Kategori",
                tint = LightText,
                modifier = Modifier.size(28.dp)
            )
        }
    }

    if (showAddDialog) {
        var catName by remember { mutableStateOf("") }
        var catType by remember { mutableStateOf(typeFilter) }
        var errorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            shape = RoundedCornerShape(14.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
            title = {
                Text(
                    text = "Tambah Kategori Baru",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeoChip(
                            text = "Pengeluaran",
                            selected = catType == "expense",
                            onClick = { catType = "expense" },
                            selectedColor = NeoRed,
                            modifier = Modifier.weight(1f)
                        )
                        NeoChip(
                            text = "Pemasukan",
                            selected = catType == "income",
                            onClick = { catType = "income" },
                            selectedColor = NeoGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    NeoTextField(
                        value = catName,
                        onValueChange = { catName = it },
                        label = "Nama Kategori",
                        placeholder = "Contoh: Langganan SaaS, Freelance",
                        testTag = "category_name_input"
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(text = errorMsg, color = NeoRed, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        if (catName.isBlank()) {
                            errorMsg = "Nama kategori tidak boleh kosong!"
                            return@NeoButton
                        }
                        onAddCategory(
                            CategoryItem(
                                name = catName.trim(),
                                type = catType,
                                isDefault = false,
                                isActive = true
                            )
                        )
                        showAddDialog = false
                    },
                    text = "Simpan",
                    backgroundColor = NeoYellow,
                    testTag = "save_category_button"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}
