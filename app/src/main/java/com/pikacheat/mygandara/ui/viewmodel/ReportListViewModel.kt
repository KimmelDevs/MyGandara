package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.ReportDto
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import com.pikacheat.mygandara.data.repository.ReportRepository
import com.pikacheat.mygandara.util.Dates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Report lists. Citizens see their own reports (My reports tab);
 * staff see every report (Dashboard tab). RLS enforces the same split on the server.
 */
class ReportListViewModel(
    private val reportRepository: ReportRepository,
    realtimeRepository: RealtimeRepository,
    private val allReports: Boolean
) : ViewModel() {

    val reports = Loadable(viewModelScope) {
        if (allReports) reportRepository.getReports() else reportRepository.getMyReports()
    }

    /** Dashboard filter; null = all statuses. */
    private val _statusFilter = MutableStateFlow<ReportStatus?>(null)
    val statusFilter: StateFlow<ReportStatus?> = _statusFilter.asStateFlow()

    init {
        reports.refresh()
        realtimeRepository.tableChanges(Tables.REPORTS)
            .onEach { reports.refresh(showIndicator = false) }
            .catch { /* best-effort */ }
            .launchIn(viewModelScope)
    }

    fun setStatusFilter(status: ReportStatus?) {
        _statusFilter.value = status
    }

    companion object {
        fun factory(allReports: Boolean) = appViewModelFactory {
            ReportListViewModel(it.reportRepository, it.realtimeRepository, allReports)
        }
    }
}

data class DashboardStats(val pending: Int, val inProgress: Int, val resolvedThisWeek: Int)

fun List<ReportDto>.dashboardStats(nowMillis: Long = System.currentTimeMillis()): DashboardStats {
    val weekAgo = nowMillis - 7L * 24 * 60 * 60 * 1000
    return DashboardStats(
        pending = count { it.status == ReportStatus.PENDING },
        inProgress = count { it.status == ReportStatus.IN_PROGRESS },
        resolvedThisWeek = count {
            it.status == ReportStatus.RESOLVED &&
                (Dates.toMillis(it.updatedAt) ?: 0L) >= weekAgo
        }
    )
}
