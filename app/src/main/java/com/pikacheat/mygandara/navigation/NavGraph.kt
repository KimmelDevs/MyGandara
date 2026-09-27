package com.pikacheat.mygandara.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pikacheat.mygandara.model.sampleReports
import com.pikacheat.mygandara.ui.screens.admin.AdminDashboardScreen
import com.pikacheat.mygandara.ui.screens.home.HomeScreen
import com.pikacheat.mygandara.ui.screens.report.CreateReportScreen
import com.pikacheat.mygandara.ui.screens.report.ReportDetailScreen

@Composable
fun MyGandaraNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Home.route) {
            HomeScreen(
                onReportClick = {
                    navController.navigate(Screen.CreateReport.route)
                }
            )
        }

        composable(Screen.CreateReport.route) {
            CreateReportScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitClick = { _, _, _ ->
                    // No backend yet — just return to the feed for now.
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onReportClick = { report ->
                    navController.navigate(Screen.ReportDetail.createRoute(report.id))
                },
                onPostAnnouncementClick = {
                    // Hook up a "create announcement" screen here later.
                }
            )
        }

        composable(Screen.ReportDetail.route) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString(Screen.ReportDetail.ARG_REPORT_ID)
            val report = sampleReports.find { it.id == reportId } ?: sampleReports.first()
            ReportDetailScreen(
                report = report,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
