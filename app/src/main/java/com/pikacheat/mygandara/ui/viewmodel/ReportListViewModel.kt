package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.ReportCategory
import com.pikacheat.mygandara.data.model.ReportDto
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import com.pikacheat.mygandara.data.repository.ReportRepository
import com.pikacheat.mygandara.util.Dates
import com.pikacheat.mygandara.util.LocalStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

enum class DateRange(val label: String, val days: Int?) {
    WEEK("Last 7 days", 7),
    MONTH("Last 30 days", 30),
    ALL("All time", null)
}

enum class ReportSort(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    UPDATED("Recently updated")
}

data class ReportFilters(
    val query: String = "",
    val dateRange: DateRange = DateRange.ALL,
    val category: ReportCategory? = null,
    val status: ReportStatus? = null,
    val sort: ReportSort = ReportSort.NEWEST,
    val assignedToMe: Boolean = false
)

data class ReportListData(
    val reports: List<ReportDto>,
    /** photo_path -> signed URL, for card thumbnails. */
    val thumbnails: Map<String, String>
)

/**
 * Report lists. Citizens see their own reports (My reports tab);
 * staff see every report (Dashboard tab). RLS enforces the same split on the server.
 */
class ReportListViewModel(
    private val reportRepository: ReportRepository,
    realtimeRepository: RealtimeRepository,
    private val localStore: LocalStore,
    private val allReports: Boolean,
    private val currentUserId: String
) : ViewModel() {

    val reports = Loadable(viewModelScope) {
        val list = if (allReports) reportRepository.getReports() else reportRepository.getMyReports()
        val thumbs = runCatching { reportRepository.photoUrls(list.mapNotNull { it.photoPath }) }
            .getOrDefault(emptyMap())
        ReportListData(list, thumbs)
    }

    private val _filters = MutableStateFlow(ReportFilters())
    val filters: StateFlow<ReportFilters> = _filters.asStateFlow()

    init {
        reports.refresh()
        realtimeRepository.tableChanges(Tables.REPORTS)
            .onEach { reports.refresh(showIndicator = false) }
            .catch { /* best-effort */ }
            .launchIn(viewModelScope)
    }

    fun updateFilters(change: (ReportFilters) -> ReportFilters) = _filters.update(change)

    fun isUpdated(report: ReportDto): Boolean =
        !allReports && localStore.isReportUpdated(report.id, report.updatedAt, report.createdAt)

    fun apply(list: List<ReportDto>, f: ReportFilters): List<ReportDto> {
        val q = f.query.trim()
        return list
            .filter { r ->
                (q.isEmpty() || r.title.contains(q, true) || r.description.contains(q, true) ||
                    r.address.orEmpty().contains(q, true) || r.referenceNumber.orEmpty().contains(q, true)) &&
                    (f.dateRange.days == null || Dates.isWithinDays(r.createdAt, f.dateRange.days)) &&
                    (f.category == null || r.category == f.category) &&
                    (f.status == null || r.status == f.status) &&
                    (!f.assignedToMe || r.assignedTo == currentUserId)
            }
            .let { filtered ->
                when (f.sort) {
                    ReportSort.NEWEST -> filtered.sortedByDescending { Dates.toMillis(it.createdAt) }
                    ReportSort.OLDEST -> filtered.sortedBy { Dates.toMillis(it.createdAt) }
                    ReportSort.UPDATED -> filtered.sortedByDescending { Dates.toMillis(it.updatedAt) }
                }
            }
    }

    /** Timestamp used for the Today / Yesterday / ... headers. */
    fun groupKey(report: ReportDto, sort: ReportSort): String =
        if (sort == ReportSort.UPDATED) report.updatedAt else report.createdAt

    companion object {
        fun factory(allReports: Boolean, currentUserId: String) = appViewModelFactory {
            ReportListViewModel(it.reportRepository, it.realtimeRepository, it.localStore, allReports, currentUserId)
        }
    }
}

data class DashboardStats(val pending: Int, val inProgress: Int, val resolvedThisWeek: Int)

fun List<ReportDto>.dashboardStats(): DashboardStats = DashboardStats(
    pending = count { it.status == ReportStatus.PENDING },
    inProgress = count { it.status == ReportStatus.IN_PROGRESS },
    resolvedThisWeek = count { it.status == ReportStatus.RESOLVED && Dates.isWithinDays(it.updatedAt, 7) }
)

fun Dates.Bucket.label(): String = when (this) {
    Dates.Bucket.TODAY -> "Today"
    Dates.Bucket.YESTERDAY -> "Yesterday"
    Dates.Bucket.THIS_WEEK -> "This week"
    Dates.Bucket.EARLIER -> "Earlier"
}
