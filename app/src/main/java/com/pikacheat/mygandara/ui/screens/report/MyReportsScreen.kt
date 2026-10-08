package com.pikacheat.mygandara.ui.screens.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import com.pikacheat.mygandara.ui.components.MessageBox
import com.pikacheat.mygandara.ui.components.ReportCard
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.viewmodel.ReportListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReportsScreen(
    onNewReportClick: () -> Unit,
    onReportClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportListViewModel = viewModel(factory = ReportListViewModel.factory(allReports = false))
) {
    val state by viewModel.reports.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.reports.isRefreshing.collectAsStateWithLifecycle()

    RefreshOnResume { viewModel.reports.refreshIfLoaded() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("My reports") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewReportClick,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Report a problem") }
            )
        }
    ) { innerPadding ->
        UiStateContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.reports.refresh() },
            modifier = Modifier.padding(innerPadding)
        ) { reports ->
            if (reports.isEmpty()) {
                MessageBox("You haven't reported anything yet.\nTap \"Report a problem\" to send one to the LGU.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp)
                ) {
                    items(reports, key = { it.id }) { report ->
                        ReportCard(report = report, onClick = { onReportClick(report.id) })
                    }
                }
            }
        }
    }
}
