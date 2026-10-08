package com.pikacheat.mygandara.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Formats the ISO-8601 timestamps Supabase returns, in the device's time zone. */
object Dates {

    @OptIn(ExperimentalTime::class)
    fun toMillis(iso: String): Long? =
        runCatching { Instant.parse(iso).toEpochMilliseconds() }.getOrNull()

    fun date(iso: String): String = format(iso, "MMM d, yyyy")

    fun dateTime(iso: String): String = format(iso, "MMM d, yyyy h:mm a")

    private fun format(iso: String, pattern: String): String {
        val millis = toMillis(iso) ?: return iso
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }
}
