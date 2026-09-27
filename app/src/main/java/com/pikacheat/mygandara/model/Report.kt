package com.pikacheat.mygandara.model

enum class ReportCategory(val label: String) {
    ROAD("Road and infrastructure"),
    GARBAGE("Garbage collection"),
    STREETLIGHT("Streetlight"),
    PEACE_ORDER("Peace and order")
}

data class Report(
    val id: String,
    val title: String,
    val description: String,
    val category: ReportCategory,
    val status: String, // "Pending" | "In progress" | "Resolved"
    val date: String
)

val sampleReports = listOf(
    Report(
        id = "1",
        title = "Pothole near barangay hall",
        description = "Large pothole causing traffic, needs patching.",
        category = ReportCategory.ROAD,
        status = "In progress",
        date = "Sept 24"
    ),
    Report(
        id = "2",
        title = "Uncollected garbage on Rizal St.",
        description = "Garbage has not been collected for 3 days.",
        category = ReportCategory.GARBAGE,
        status = "Pending",
        date = "Sept 20"
    )
)
