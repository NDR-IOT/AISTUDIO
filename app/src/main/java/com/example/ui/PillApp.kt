package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import com.example.ui.components.StatsScreen
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DoseSchedule
import com.example.ui.components.AddEditScheduleDialog
import com.example.ui.components.DateSelectorRow
import com.example.ui.components.DoseCard
import com.example.ui.components.HeaderCard
import com.example.ui.components.NextDoseHighlightCard
import com.example.ui.components.NotificationPermissionBanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PillApp(
    viewModel: PillViewModel
) {
    val selectedDate by viewModel.selectedDateString.collectAsStateWithLifecycle()
    val logs by viewModel.logsForSelectedDate.collectAsStateWithLifecycle()
    val nextDose by viewModel.nextUpcomingDose.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
    val currentThemeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val weeklyCompliance by viewModel.weeklyCompliance.collectAsStateWithLifecycle()
    val allWeeklyLogs by viewModel.logsForWeeklyRange.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<DoseSchedule?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isToday = selectedDate == viewModel.todayDateString

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recordatorio de Medicamentos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.sendTestNotification("Metimazol", 2)
                            scope.launch {
                                snackbarHostState.showSnackbar("🔔 Alarma de prueba enviada (sonarás en 3 seg)")
                            }
                        },
                        modifier = Modifier.testTag("test_notification_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Probar Notificación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Tomas del Día") },
                    label = { Text("Tomas de Hoy") },
                    modifier = Modifier.testTag("nav_tab_today")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.Schedule, contentDescription = "Horarios") },
                    label = { Text("Horarios (${schedules.size})") },
                    modifier = Modifier.testTag("nav_tab_schedules")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                    label = { Text("Estadísticas") },
                    modifier = Modifier.testTag("nav_tab_stats")
                )
            }
        },
        floatingActionButton = {
            if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = {
                        scheduleToEdit = null
                        showAddDialog = true
                    },
                    modifier = Modifier.testTag("fab_add_schedule")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Horario")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    // MAIN TAB: Daily Log & Timeline
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            NotificationPermissionBanner()
                        }

                        item {
                            HeaderCard(
                                logs = logs,
                                streakDays = streakDays,
                                isToday = isToday
                            )
                        }

                        if (isToday && nextDose != null) {
                            item {
                                NextDoseHighlightCard(
                                    nextDose = nextDose!!,
                                    onMarkTaken = { logId ->
                                        viewModel.toggleDoseTaken(logId, false)
                                        scope.launch {
                                            snackbarHostState.showSnackbar("¡Dosis registrada correctamente! 💊")
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            DateSelectorRow(
                                selectedDateStr = selectedDate,
                                todayDateStr = viewModel.todayDateString,
                                onSelectDate = { viewModel.selectDate(it) }
                            )
                        }

                        item {
                            Text(
                                text = if (isToday) "Secuencia de Tomas (Hoy)" else "Secuencia de Tomas ($selectedDate)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (logs.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("No hay dosis programadas para esta fecha.")
                                    }
                                }
                            }
                        } else {
                            items(
                                items = logs,
                                key = { it.id }
                            ) { log ->
                                DoseCard(
                                    doseLog = log,
                                    onToggleTaken = { logId, currentStatus ->
                                        viewModel.toggleDoseTaken(logId, currentStatus)
                                        scope.launch {
                                            val msg = if (!currentStatus) "¡Tomada registrada!" else "Estado actualizado"
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }

                1 -> {
                    // SECOND TAB: Schedules & Configuration
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Configuración de Horarios Diarios",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Cada toma genera una alarma automática 15 minutos antes de la hora fijada.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.sendTestNotification("Metimazol", 2)
                                            scope.launch {
                                                snackbarHostState.showSnackbar("🔔 Alarma de prueba activada. Sonará en 3 segundos.")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Alarm, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Probar Alarma de Muestra")
                                    }
                                }
                            }
                        }

                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Apariencia y Tema",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Selecciona el modo de tema para la aplicación:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val options = listOf(
                                            "CLARO" to "☀️ Claro",
                                            "OSCURO" to "🌙 Oscuro"
                                        )

                                        options.forEach { (mode, label) ->
                                            val isSelected = currentThemeMode.equals(mode, ignoreCase = true)
                                            Surface(
                                                onClick = { viewModel.setThemeMode(mode) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("theme_option_$mode"),
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ) {
                                                Box(
                                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "Tiempos Configurados (${schedules.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(
                            items = schedules,
                            key = { it.id }
                        ) { schedule ->
                            val hexColor = try {
                                Color(android.graphics.Color.parseColor(schedule.colorHex))
                            } catch (e: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = hexColor,
                                            contentColor = Color.White
                                        ) {
                                            Text(
                                                text = schedule.formattedTime,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 16.sp
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = "${schedule.quantity} pastilla/s de ${schedule.medicationName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Aviso: ${schedule.reminderTimeFormatted} hrs (${schedule.reminderOffsetMinutes} min antes)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            scheduleToEdit = schedule
                                            showAddDialog = true
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    StatsScreen(
                        weeklyCompliance = weeklyCompliance,
                        allLogs = allWeeklyLogs,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddEditScheduleDialog(
            initialSchedule = scheduleToEdit,
            onDismiss = { showAddDialog = false },
            onSave = { schedule ->
                viewModel.saveSchedule(schedule)
                showAddDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Horario guardado correctamente")
                }
            },
            onDelete = { id ->
                viewModel.deleteSchedule(id)
                showAddDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Horario eliminado")
                }
            }
        )
    }
}
