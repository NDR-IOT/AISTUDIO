package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DoseSchedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScheduleDialog(
    initialSchedule: DoseSchedule? = null,
    onDismiss: () -> Unit,
    onSave: (DoseSchedule) -> Unit,
    onDelete: ((Int) -> Unit)? = null
) {
    var medName by remember { mutableStateOf(initialSchedule?.medicationName ?: "") }
    var quantity by remember { mutableIntStateOf(initialSchedule?.quantity ?: 1) }
    var hour by remember { mutableIntStateOf(initialSchedule?.timeHour ?: 8) }
    var minute by remember { mutableIntStateOf(initialSchedule?.timeMinute ?: 0) }
    var offsetMins by remember { mutableIntStateOf(initialSchedule?.reminderOffsetMinutes ?: 15) }
    var colorHex by remember { mutableStateOf(initialSchedule?.colorHex ?: "#E53935") }
    var instructions by remember { mutableStateOf(initialSchedule?.instructions ?: "Tomar con agua") }

    var showTimePickerModal by remember { mutableStateOf(false) }

    val colorOptions = listOf("#E53935", "#00897B", "#FB8C00", "#1E88E5", "#8E24AA")

    // Format time for 12-hour display
    val period = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val timeFormatted12h = String.format("%02d:%02d %s", displayHour, minute, period)
    val timeFormatted24h = String.format("%02d:%02d hs", hour, minute)

    if (showTimePickerModal) {
        val timePickerState = rememberTimePickerState(
            initialHour = hour,
            initialMinute = minute,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showTimePickerModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Seleccionar Hora", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hour = timePickerState.hour
                        minute = timePickerState.minute
                        showTimePickerModal = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerModal = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialSchedule == null) "Agregar Toma de Pastilla" else "Editar Horario de Pastilla",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("Nombre del medicamento") },
                    placeholder = { Text("Ej: Metimazol, Propranolol") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input")
                )

                // Quantity selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Cantidad de pastillas:", fontWeight = FontWeight.Medium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1,
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Menos", modifier = Modifier.size(18.dp))
                        }

                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        OutlinedButton(
                            onClick = { if (quantity < 10) quantity++ },
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Más", modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Enhanced Time Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Hora programada",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        // Big Time display & Open Clock button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = timeFormatted12h,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = timeFormatted24h,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { showTimePickerModal = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("open_clock_picker")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reloj")
                            }
                        }

                        // Steppers for quick adjustment
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Hour controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Hora: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { hour = if (hour == 0) 23 else hour - 1 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Hora anterior", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = String.format("%02d", hour),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { hour = (hour + 1) % 24 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Hora siguiente", modifier = Modifier.size(16.dp))
                                }
                            }

                            // Minute controls
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Min: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                IconButton(
                                    onClick = { minute = (minute - 5 + 60) % 60 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Restar 5 min", modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    text = String.format("%02d", minute),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                IconButton(
                                    onClick = { minute = (minute + 5) % 60 },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Sumar 5 min", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        // Preset shortcuts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val presets = listOf(
                                "08:00" to (8 to 0),
                                "12:00" to (12 to 0),
                                "16:00" to (16 to 0),
                                "20:00" to (20 to 0)
                            )
                            presets.forEach { (label, hM) ->
                                Surface(
                                    onClick = {
                                        hour = hM.first
                                        minute = hM.second
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (hour == hM.first && minute == hM.second) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (hour == hM.first && minute == hM.second) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Aviso previo: $offsetMins minutos antes",
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = offsetMins.toFloat(),
                        onValueChange = { offsetMins = it.toInt() },
                        valueRange = 5f..60f,
                        steps = 10
                    )
                }

                // Color selector
                Column {
                    Text("Color identificador:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        colorOptions.forEach { hex ->
                            val color = try {
                                Color(android.graphics.Color.parseColor(hex))
                            } catch (e: Exception) {
                                Color.Red
                            }
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { colorHex = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                if (colorHex == hex) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Instrucciones") },
                    placeholder = { Text("Ej: Tomar con alimentos y agua") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (medName.isNotBlank()) {
                        val schedule = DoseSchedule(
                            id = initialSchedule?.id ?: 0,
                            medicationName = medName,
                            quantity = quantity,
                            timeHour = hour,
                            timeMinute = minute,
                            reminderOffsetMinutes = offsetMins,
                            colorHex = colorHex,
                            instructions = instructions,
                            isActive = true
                        )
                        onSave(schedule)
                        onDismiss()
                    }
                },
                enabled = medName.isNotBlank(),
                modifier = Modifier.testTag("save_schedule_button")
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (initialSchedule != null && onDelete != null) {
                    TextButton(
                        onClick = {
                            onDelete(initialSchedule.id)
                            onDismiss()
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}

