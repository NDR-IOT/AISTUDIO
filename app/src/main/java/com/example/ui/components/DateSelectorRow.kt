package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DateSelectorRow(
    selectedDateStr: String,
    todayDateStr: String,
    onSelectDate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Calculate Yesterday and Tomorrow strings
    val cal = Calendar.getInstance()
    val yesterdayStr = dateFormat.format(Date(cal.timeInMillis - 24 * 60 * 60 * 1000))
    val tomorrowStr = dateFormat.format(Date(cal.timeInMillis + 24 * 60 * 60 * 1000))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedDateStr == yesterdayStr,
            onClick = { onSelectDate(yesterdayStr) },
            label = { Text("Ayer") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("date_yesterday_chip")
        )

        FilterChip(
            selected = selectedDateStr == todayDateStr,
            onClick = { onSelectDate(todayDateStr) },
            label = { Text("Hoy", fontWeight = FontWeight.Bold) },
            shape = RoundedCornerShape(12.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("date_today_chip")
        )

        FilterChip(
            selected = selectedDateStr == tomorrowStr,
            onClick = { onSelectDate(tomorrowStr) },
            label = { Text("Mañana") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.testTag("date_tomorrow_chip")
        )
    }
}
