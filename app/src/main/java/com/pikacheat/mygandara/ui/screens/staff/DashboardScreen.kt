package com.pikacheat.mygandara.ui.screens.staff

import com.pikacheat.mygandara.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.ui.components.ChoiceChipRow
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.SkeletonList
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.screens.report.ReportListContent
import com.pikacheat.mygandara.ui.screens.report.SortMenuButton
import com.pikacheat.mygandara.ui.viewmodel.ReportListViewModel
import com.pikacheat.mygandara.ui.viewmodel.dashboardStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    currentUserId: String,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportListViewModel = viewModel(
        factory = ReportListViewModel.factory(allReports = true, currentUserId = currentUserId)
    )
) {
    val state by viewModel.reports.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.reports.isRefreshing.collectAsStateWithLifecycle()
    val filters by viewModel.filters.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.reports.refreshIfLoaded() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(t("LGU dashboard")) },
                actions = { SortMenuButton(filters.sort) { s -> viewModel.updateFilters { it.copy(sort = s) } } }
            )
        }
    ) { innerPadding ->
        UiStateContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.reports.refresh() },
            modifier = Modifier.padding(innerPadding),
            loading = { SkeletonList() }
        ) { data ->
            ReportListContent(
                data = data,
                filters = filters,
                viewModel = viewModel,
                onReportClick = onReportClick,
                emptyText = "No reports here."
            ) { all ->
                item(key = "stats") {
                    val stats = all.dashboardStats()
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCard(t("Pending"), stats.pending, Modifier.weight(1f))
                        StatCard(t("In progress"), stats.inProgress, Modifier.weight(1f))
                        StatCard(t("Resolved (7 days)"), stats.resolvedThisWeek, Modifier.weight(1f))
                    }
                }
                item(key = "status") {
                    ChoiceChipRow(
                        options = ReportStatus.entries,
                        selected = filters.status,
                        label = { it.label },
                        onSelect = { s -> viewModel.updateFilters { it.copy(status = s) } },
                        allLabel = t("All statuses")
                    )
                }
                // "My assigned" filter (#21)
                item(key = "assigned") {
                    FilterChip(
                        selected = filters.assignedToMe,
                        onClick = { viewModel.updateFilters { it.copy(assignedToMe = !it.assignedToMe) } },
                        label = { Text(t("Assigned to me")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
