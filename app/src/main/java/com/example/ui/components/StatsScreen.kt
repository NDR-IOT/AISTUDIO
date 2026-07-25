package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyCompliance
import com.example.data.model.DoseLog
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun StatsScreen(
    weeklyCompliance: List<DailyCompliance>,
    allLogs: List<DoseLog>,
    modifier: Modifier = Modifier
) {
    var selectedDateString by remember(weeklyCompliance) {
        mutableStateOf(weeklyCompliance.lastOrNull()?.dateString ?: "")
    }

    val selectedDayCompliance = weeklyCompliance.find { it.dateString == selectedDateString }
        ?: weeklyCompliance.lastOrNull()

    val selectedDayLogs = allLogs.filter { it.dateString == selectedDateString }

    // Summary calculations
    val totalWeeklyDoses = weeklyCompliance.sumOf { it.totalDoses }
    val takenWeeklyDoses = weeklyCompliance.sumOf { it.takenDoses }
    val overallPercentage = if (totalWeeklyDoses > 0) {
        (takenWeeklyDoses.toFloat() / totalWeeklyDoses.toFloat() * 100f)
    } else 0f

    val perfectDays = weeklyCompliance.count { it.totalDoses > 0 && it.takenDoses == it.totalDoses }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header Title
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Estadísticas de Cumplimiento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Seguimiento semanal de tomas de medicamentos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Summary KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // KPI 1: Overall Average
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Promedio",
                    value = "${overallPercentage.toInt()}%",
                    icon = Icons.Default.Assessment,
                    color = when {
                        overallPercentage >= 80f -> Color(0xFF2E7D32)
                        overallPercentage >= 50f -> Color(0xFFF57C00)
                        else -> Color(0xFFD32F2F)
                    }
                )

                // KPI 2: Total Doses
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Completadas",
                    value = "$takenWeeklyDoses / $totalWeeklyDoses",
                    icon = Icons.Default.CheckCircle,
                    color = MaterialTheme.colorScheme.primary
                )

                // KPI 3: Perfect Days
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Días 100%",
                    value = "$perfectDays / 7",
                    icon = Icons.Default.EmojiEvents,
                    color = Color(0xFFFFA000)
                )
            }
        }

        // Weekly Bar Chart Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cumplimiento Semanal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Últimos 7 días",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyBarChart(
                        complianceList = weeklyCompliance,
                        selectedDate = selectedDateString,
                        onDateSelected = { selectedDateString = it }
                    )
                }
            }
        }

        // Selected Day Detail Card
        item {
            if (selectedDayCompliance != null) {
                val formattedDate = try {
                    val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outFmt = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES"))
                    val dateObj = inFmt.parse(selectedDayCompliance.dateString)
                    dateObj?.let { outFmt.format(it).replaceFirstChar { char -> char.uppercase() } }
                        ?: selectedDayCompliance.dateString
                } catch (e: Exception) {
                    selectedDayCompliance.dateString
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${selectedDayCompliance.takenDoses} de ${selectedDayCompliance.totalDoses} tomas realizadas (${selectedDayCompliance.percentage.toInt()}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    selectedDayCompliance.percentage >= 100f -> Color(0xFFE8F5E9)
                                    selectedDayCompliance.percentage >= 50f -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFFFEBEE)
                                }
                            ) {
                                Text(
                                    text = "${selectedDayCompliance.percentage.toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        selectedDayCompliance.percentage >= 100f -> Color(0xFF2E7D32)
                                        selectedDayCompliance.percentage >= 50f -> Color(0xFFE65100)
                                        else -> Color(0xFFC62828)
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (selectedDayLogs.isEmpty()) {
                            Text(
                                text = "Sin tomas programadas para este día.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedDayLogs.forEach { log ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Medication,
                                                contentDescription = null,
                                                tint = try {
                                                    Color(android.graphics.Color.parseColor(log.colorHex))
                                                } catch (e: Exception) {
                                                    MaterialTheme.colorScheme.primary
                                                },
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = log.medicationName,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "${log.quantity} pastilla(s) • ${log.scheduledTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (log.isTaken) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFE8F5E9)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Tomada",
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Tomada",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PendingActions,
                                                        contentDescription = "Pendiente",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Pendiente",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Encouragement / Insight Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        overallPercentage >= 80f -> Color(0xFFE8F5E9)
                        overallPercentage >= 50f -> Color(0xFFFFF3E0)
                        else -> Color(0xFFE3F2FD)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = when {
                            overallPercentage >= 80f -> Color(0xFF2E7D32)
                            overallPercentage >= 50f -> Color(0xFFE65100)
                            else -> Color(0xFF1565C0)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = when {
                            overallPercentage >= 80f -> "🎉 ¡Excelente disciplina! Mantener tu constancia asegura la eficacia de tu tratamiento."
                            overallPercentage >= 50f -> "👍 Buen progreso. Mantén tus notificaciones activas para no omitir ninguna toma."
                            else -> "💡 Consejo: Configura alarmas a la misma hora para establecer un hábito saludable de medicación."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            overallPercentage >= 80f -> Color(0xFF1B5E20)
                            overallPercentage >= 50f -> Color(0xFFBF360C)
                            else -> Color(0xFF0D47A1)
                        }
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WeeklyBarChart(
    complianceList: List<DailyCompliance>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val barColorSuccess = Color(0xFF2E7D32)
    val barColorMedium = Color(0xFFFB8C00)
    val barColorLow = Color(0xFFE53935)
    val barColorEmpty = MaterialTheme.colorScheme.surfaceVariant
    val primaryThemeColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        complianceList.forEach { daily ->
            val isSelected = daily.dateString == selectedDate
            val animatedPercentage by animateFloatAsState(
                targetValue = daily.percentage,
                animationSpec = tween(durationMillis = 600),
                label = "barHeight"
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onDateSelected(daily.dateString) }
                    .padding(horizontal = 2.dp)
                    .testTag("bar_chart_item_${daily.dayLabel}"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Percentage text label above bar
                Text(
                    text = if (daily.totalDoses > 0) "${daily.percentage.toInt()}%" else "-",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) primaryThemeColor else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Bar Canvas
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            if (isSelected) primaryThemeColor.copy(alpha = 0.15f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        // Background column track
                        drawRoundRect(
                            color = barColorEmpty,
                            size = Size(canvasWidth, canvasHeight),
                            cornerRadius = CornerRadius(12f, 12f)
                        )

                        // Filled bar
                        if (daily.totalDoses > 0 && animatedPercentage > 0f) {
                            val barHeightPixels = (canvasHeight * (animatedPercentage / 100f))
                                .coerceAtLeast(10f) // Minimum visible bar height

                            val fillColor = when {
                                daily.percentage >= 100f -> barColorSuccess
                                daily.percentage >= 50f -> barColorMedium
                                else -> barColorLow
                            }

                            drawRoundRect(
                                color = fillColor,
                                topLeft = Offset(0f, canvasHeight - barHeightPixels),
                                size = Size(canvasWidth, barHeightPixels),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Day Label and Day Number
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) primaryThemeColor else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = daily.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected || daily.isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = daily.dayNumber,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
