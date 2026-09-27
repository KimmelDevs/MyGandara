package com.pikacheat.mygandara.model

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val date: String,
    val pinned: Boolean = false
)

// Temporary sample data so the UI can be previewed without a backend.
val sampleAnnouncements = listOf(
    Announcement(
        id = "1",
        title = "Water interruption — Barangay Gandara",
        body = "Scheduled maintenance Sept 28, 8am–5pm.",
        date = "Sept 25",
        pinned = true
    ),
    Announcement(
        id = "2",
        title = "Road clearing operation this weekend",
        body = "Please move parked vehicles along Rizal St.",
        date = "Sept 22"
    ),
    Announcement(
        id = "3",
        title = "Free medical mission — Sept 30",
        body = "Barangay covered court, 8am onwards.",
        date = "Sept 19"
    )
)
