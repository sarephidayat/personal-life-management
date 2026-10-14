package com.example.personallife.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personallife.ui.theme.LightText
import com.example.personallife.ui.theme.NeoGreen
import com.example.personallife.ui.theme.NeoOrange
import com.example.personallife.ui.theme.NeoRed
import com.example.personallife.ui.theme.NeoYellow

@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 10.dp,
    onClick: (() -> Unit)? = null,
    testTag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Box(modifier = baseModifier) {
        // Hard Shadow Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        // Main Surface Layer
        Box(
            modifier = Modifier
                .clip(shape)
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
fun NeoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = LightText,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 10.dp,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(cornerRadius)
    val buttonModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Box(
        modifier = buttonModifier
    ) {
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = shadowOffset, y = shadowOffset)
                    .background(shadowColor, shape)
            )
        }
        Box(
            modifier = Modifier
                .clip(shape)
                .background(if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f), shape)
                .border(borderWidth, borderColor, shape)
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    shadowColor: Color = MaterialTheme.colorScheme.outline,
    size: Dp = 44.dp,
    shadowOffset: Dp = 3.dp,
    cornerRadius: Dp = 10.dp,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(cornerRadius)
    val buttonModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Box(
        modifier = buttonModifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(shadowColor, shape)
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(shape)
                .background(backgroundColor, shape)
                .border(2.dp, borderColor, shape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    testTag: String? = null
) {
    val fieldModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Column(modifier = fieldModifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(
                        placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else null,
            trailingIcon = trailingIcon,
            singleLine = singleLine,
            maxLines = maxLines,
            isError = isError,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = NeoRed,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, if (isError) NeoRed else MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
        )
        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                color = NeoRed,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun NeoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(8.dp)
    val chipModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Box(
        modifier = chipModifier
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 2.dp, y = 2.dp)
                    .background(borderColor, shape)
            )
        }
        Box(
            modifier = Modifier
                .clip(shape)
                .background(if (selected) selectedColor else unselectedColor, shape)
                .border(2.dp, borderColor, shape)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                color = if (selected) LightText else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun NeoBadge(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = LightText,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(color, shape)
            .border(1.5.dp, borderColor, shape)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = textColor
        )
    }
}

@Composable
fun NeoConfirmDialog(
    show: Boolean,
    title: String,
    message: String,
    confirmText: String = "Hapus",
    confirmColor: Color = NeoRed,
    cancelText: String = "Batal",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                NeoButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    text = confirmText,
                    backgroundColor = confirmColor,
                    textColor = LightText,
                    testTag = "confirm_dialog_button"
                )
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(cancelText, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NeoDatePickerDialog(
    initialDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDate) {
        (initialDate ?: LocalDate.now())
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            NeoButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedLocalDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedLocalDate)
                    }
                    onDismiss()
                },
                text = "Pilih",
                backgroundColor = NeoYellow,
                cornerRadius = 8.dp,
                testTag = "date_picker_confirm_button"
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Text("Batal", fontWeight = FontWeight.Bold)
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
            .padding(4.dp)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = "Pilih Tanggal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                )
            },
            headline = {
                val selectedMillis = datePickerState.selectedDateMillis
                val formattedHeadline = if (selectedMillis != null) {
                    val date = Instant.ofEpochMilli(selectedMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", java.util.Locale.forLanguageTag("id-ID")))
                } else {
                    "Pilih Tanggal"
                }
                Text(
                    text = formattedHeadline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurface,
                yearContentColor = MaterialTheme.colorScheme.onSurface,
                currentYearContentColor = NeoYellow,
                selectedYearContentColor = LightText,
                selectedYearContainerColor = NeoYellow,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                selectedDayContentColor = LightText,
                disabledSelectedDayContentColor = LightText.copy(alpha = 0.38f),
                selectedDayContainerColor = NeoYellow,
                disabledSelectedDayContainerColor = NeoYellow.copy(alpha = 0.38f),
                todayDateBorderColor = NeoYellow,
                todayContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun NeoDatePickerField(
    value: String, // stored in YYYY-MM-DD format
    onDateSelected: (String) -> Unit, // returns YYYY-MM-DD
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "Pilih Tanggal (DD/MM/YYYY)",
    isError: Boolean = false,
    errorMessage: String? = null,
    testTag: String? = null,
    optional: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    // Parse the current date for display in DD/MM/YYYY
    val parsedDate = remember(value) {
        try {
            if (value.isNotBlank()) {
                val datePart = if (value.contains("T")) value.substringBefore("T") else value.take(10)
                LocalDate.parse(datePart)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    val displayString = parsedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: ""

    val fieldModifier = if (testTag != null) modifier.testTag(testTag) else modifier

    Column(modifier = fieldModifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 2.dp,
                    color = if (isError) NeoRed else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(10.dp)
                )
                .clickable { showDialog = true }
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Pilih Tanggal",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (displayString.isNotEmpty()) displayString else placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (displayString.isNotEmpty()) FontWeight.Bold else FontWeight.Normal,
                        color = if (displayString.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (optional && displayString.isNotEmpty() && onClear != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Tanggal",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (isError && !errorMessage.isNullOrEmpty()) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = NeoRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }

    if (showDialog) {
        NeoDatePickerDialog(
            initialDate = parsedDate ?: LocalDate.now(),
            onDateSelected = { selectedLocalDate ->
                onDateSelected(selectedLocalDate.toString())
            },
            onDismiss = { showDialog = false }
        )
    }
}
