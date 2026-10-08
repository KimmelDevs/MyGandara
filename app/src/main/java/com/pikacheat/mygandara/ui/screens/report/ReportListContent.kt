package com.pikacheat.mygandara.ui.screens.report

import com.pikacheat.mygandara.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pikacheat.mygandara.data.model.ReportCategory
import com.pikacheat.mygandara.data.model.ReportDto
import com.pikacheat.mygandara.ui.components.ChoiceChipRow
import com.pikacheat.mygandara.ui.components.EmptyListText
import com.pikacheat.mygandara.ui.components.ListSectionHeader
import com.pikacheat.mygandara.ui.components.ReportCard
import com.pikacheat.mygandara.ui.components.SearchField
import com.pikacheat.mygandara.ui.viewmodel.DateRange
import com.pikacheat.mygandara.ui.viewmodel.ReportFilters
import com.pikacheat.mygandara.ui.viewmodel.ReportListData
import com.pikacheat.mygandara.ui.viewmodel.ReportListViewModel
import com.pikacheat.mygandara.ui.viewmodel.ReportSort
import com.pikacheat.mygandara.ui.viewmodel.label
import com.pikacheat.mygandara.util.Dates

/** Top-bar sort button (#7). */
@Composable
fun SortMenuButton(sort: ReportSort, onSortChange: (ReportSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = t("Sort"))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ReportSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { androidx.compose.material3.Text(t(option.label)) },
                    leadingIcon = {
                        if (option == sort) Icon(Icons.Filled.Check, contentDescription = null)
                    },
                    onClick = {
                        onSortChange(option)
                        open = false
                    }
                )
            }
        }
    }
}

/**
 * Search, date-range and category filters, then the reports grouped under Today / Yesterday / ... headers.
 * [header] adds screen-specific rows (status counts, stat cards, staff chips) above the filters.
 */
@Composable
fun ReportListContent(
    data: ReportListData,
    filters: ReportFilters,
    viewModel: ReportListViewModel,
    onReportClick: (String) -> Unit,
    emptyText: String,
    modifier: Modifier = Modifier,
    header: LazyListScope.(all: List<ReportDto>) -> Unit = {}
) {
    val visible = viewModel.apply(data.reports, filters)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp)
    ) {
        header(data.reports)

        item(key = "search") {
            SearchField(
                query = filters.query,
                onQueryChange = { q -> viewModel.updateFilters { it.copy(query = q) } },
                placeholder = t("Search title, place, or reference no.")
            )
        }
        item(key = "dateRange") {
            ChoiceChipRow(
                options = DateRange.entries,
                selected = filters.dateRange,
                label = { it.label },
                onSelect = { range -> viewModel.updateFilters { it.copy(dateRange = range ?: DateRange.ALL) } },
                allLabel = null
            )
        }
        item(key = "category") {
            ChoiceChipRow(
                options = ReportCategory.entries,
                selected = filters.category,
                label = { it.label },
                onSelect = { cat -> viewModel.updateFilters { it.copy(category = cat) } },
                allLabel = t("All categories")
            )
        }

        if (visible.isEmpty()) {
            item(key = "empty") { EmptyListText(emptyText) }
        }

        // Grouped by day (#8)
        visible
            .groupBy { Dates.bucket(viewModel.groupKey(it, filters.sort)) }
            .forEach { (bucket, reports) ->
                item(key = "header_${bucket.name}") { ListSectionHeader(bucket.label()) }
                items(reports, key = { it.id }) { report ->
                    ReportCard(
                        report = report,
                        onClick = { onReportClick(report.id) },
                        thumbnailUrl = report.photoPath?.let { data.thumbnails[it] },
                        isUpdated = viewModel.isUpdated(report)
                    )
                }
            }
    }
}
