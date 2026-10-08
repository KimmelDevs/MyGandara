package com.pikacheat.mygandara.ui.screens.report

import com.pikacheat.mygandara.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.ui.components.MessageBox
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.SkeletonList
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.viewmodel.ReportListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    currentUserId: String,
    onNewReportClick: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportListViewModel = viewModel(
        factory = ReportListViewModel.factory(allReports = false, currentUserId = currentUserId)
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
                title = { Text(t("My reports")) },
                actions = { SortMenuButton(filters.sort) { s -> viewModel.updateFilters { it.copy(sort = s) } } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewReportClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(t("Report a problem")) }
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
            if (data.reports.isEmpty()) {
                MessageBox(t("You haven't reported anything yet.\nTap \"Report a problem\" to send one to the LGU."))
            } else {
                ReportListContent(
                    data = data,
                    filters = filters,
                    viewModel = viewModel,
                    onReportClick = onReportClick,
                    emptyText = "No reports match these filters."
                ) { all ->
                    // Status counts that double as a status filter (#10)
                    item(key = "statusCounts") {
                        val counts = all.groupingBy { it.status }.eachCount()
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ReportStatus.entries.filter { (counts[it] ?: 0) > 0 }) { status ->
                                FilterChip(
                                    selected = filters.status == status,
                                    onClick = {
                                        viewModel.updateFilters {
                                            it.copy(status = if (it.status == status) null else status)
                                        }
                                    },
                                    label = { Text("${counts[status]} ${t(status.label)}") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
