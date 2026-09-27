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
        description = "Large pothole causing traffic, needs patching. Located right in front of the covered court entrance.",
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
    ),
    Report(
        id = "3",
        title = "Broken streetlight, corner Mabini",
        description = "Streetlight has been out for over a week, area is dark at night.",
        category = ReportCategory.STREETLIGHT,
        status = "Resolved",
        date = "Sept 15"
    )
)

data class TimelineEvent(
    val title: String,
    val subtitle: String,
    val completed: Boolean
)

val sampleTimeline = listOf(
    TimelineEvent("Report received", "Sept 24, 9:12 AM", completed = true),
    TimelineEvent("Assigned to engineering office", "Sept 24, 2:40 PM", completed = true),
    TimelineEvent("In progress", "Awaiting materials", completed = false)
)

// Dashboard summary numbers (UI-only placeholders)
const val samplePendingCount = 14
const val sampleResolvedThisWeek = 27

