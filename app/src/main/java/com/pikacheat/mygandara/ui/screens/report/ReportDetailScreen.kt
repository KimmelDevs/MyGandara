package com.pikacheat.mygandara.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pikacheat.mygandara.model.Report
import com.pikacheat.mygandara.model.TimelineEvent
import com.pikacheat.mygandara.model.sampleReports
import com.pikacheat.mygandara.model.sampleTimeline
import com.pikacheat.mygandara.ui.components.StatusBadge
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

@Composable
fun ReportDetailScreen(
    report: Report,
    timeline: List<TimelineEvent> = sampleTimeline,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Report details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Photo,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                StatusBadge(status = report.status)
                Text(
                    text = "Submitted ${report.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            DetailRow(label = "Category", value = report.category.label)
            DetailRow(label = "Location", value = "Barangay Gandara")

            Text(
                text = "Status timeline",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                timeline.forEach { event ->
                    TimelineRow(event = event)
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TimelineRow(
    event: TimelineEvent,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (event.completed) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
            contentDescription = null,
            tint = if (event.completed) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(text = event.title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = event.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportDetailScreenPreview() {
    MyGandaraTheme {
        ReportDetailScreen(report = sampleReports.first())
    }
}
