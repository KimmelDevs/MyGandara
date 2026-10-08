package com.pikacheat.mygandara.ui.screens.staff

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.i18n.t
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.SearchTopBar
import com.pikacheat.mygandara.ui.components.SkeletonList
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.screens.report.FilterButton
import com.pikacheat.mygandara.ui.screens.report.ReportFilterSheet
import com.pikacheat.mygandara.ui.screens.report.ReportListContent
import com.pikacheat.mygandara.ui.viewmodel.ReportListViewModel

/**
 * Opens with just three stat tiles and the list. The tiles double as the status filter;
 * search is behind the top-bar icon and everything else is in the Filters sheet.
 */
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
    var showFilters by remember { mutableStateOf(false) }

    RefreshOnResume { viewModel.reports.refreshIfLoaded() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchTopBar(
                title = "LGU dashboard",
                query = filters.query,
                onQueryChange = { q -> viewModel.updateFilters { it.copy(query = q) } },
                searchPlaceholder = "Search title, place, or reference no."
            ) {
                FilterButton(filters) { showFilters = true }
            }
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
                    val counts = all.groupingBy { it.status }.eachCount()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        listOf(ReportStatus.PENDING, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED).forEach { status ->
                            StatTile(
                                label = status.label,
                                value = counts[status] ?: 0,
                                selected = filters.status == status,
                                onClick = {
                                    viewModel.updateFilters { it.copy(status = if (it.status == status) null else status) }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showFilters) {
        ReportFilterSheet(
            filters = filters,
            onChange = viewModel::updateFilters,
            showStaffOptions = true,
            onDismiss = { showFilters = false }
        )
    }
}

/** Count tile that toggles the status filter; outlined when selected. */
@Composable
private fun StatTile(
    label: String,
    value: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.semantics {
            this.selected = selected
            role = Role.Tab
        },
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = t(label),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
