package com.pikacheat.mygandara.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pikacheat.mygandara.model.Report
import com.pikacheat.mygandara.model.sampleReports
import com.pikacheat.mygandara.model.samplePendingCount
import com.pikacheat.mygandara.model.sampleResolvedThisWeek
import com.pikacheat.mygandara.ui.components.StatusBadge
import com.pikacheat.mygandara.ui.theme.MyGandaraTheme

@Composable
fun AdminDashboardScreen(
    onReportClick: (Report) -> Unit = {},
    onPostAnnouncementClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("LGU dashboard") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onPostAnnouncementClick,
                icon = { Icon(Icons.Filled.Campaign, contentDescription = null) },
                text = { Text("Post announcement") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCard(label = "Pending", value = samplePendingCount.toString(), modifier = Modifier.weight(1f))
                    StatCard(label = "Resolved this week", value = sampleResolvedThisWeek.toString(), modifier = Modifier.weight(1f))
                }
            }

            item {
                Text(
                    text = "Recent reports",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(sampleReports) { report ->
                ReportRow(report = report, onClick = { onReportClick(report) })
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ReportRow(
    report: Report,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = report.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = report.category.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            StatusBadge(status = report.status)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    MyGandaraTheme {
        AdminDashboardScreen()
    }
}
