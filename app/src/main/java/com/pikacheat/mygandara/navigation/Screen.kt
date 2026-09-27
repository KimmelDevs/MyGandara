package com.pikacheat.mygandara.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object CreateReport : Screen("create_report")
    data object AdminDashboard : Screen("admin_dashboard")

    data object ReportDetail : Screen("report_detail/{reportId}") {
        const val ARG_REPORT_ID = "reportId"
        fun createRoute(reportId: String) = "report_detail/$reportId"
    }
}
