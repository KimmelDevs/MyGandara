package com.pikacheat.mygandara.data.repository

import com.pikacheat.mygandara.data.model.NewReport
import com.pikacheat.mygandara.data.model.NewReportUpdate
import com.pikacheat.mygandara.data.model.ReportDto
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.data.model.ReportUpdateDto
import com.pikacheat.mygandara.data.remote.Buckets
import com.pikacheat.mygandara.data.remote.Tables
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class ReportRepository(private val client: () -> SupabaseClient) {

    /** RLS limits citizens to their own reports; staff get all of them. */
    suspend fun getReports(status: ReportStatus? = null): List<ReportDto> =
        client().from(Tables.REPORTS).select {
            if (status != null) filter { eq("status", status) }
            order("created_at", Order.DESCENDING)
        }.decodeList()

    suspend fun getMyReports(): List<ReportDto> {
        val userId = client().auth.currentUserOrNull()?.id ?: return emptyList()
        return client().from(Tables.REPORTS).select {
            filter { eq("reporter_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList()
    }

    suspend fun getReport(id: String): ReportDto? =
        client().from(Tables.REPORTS)
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull()

    suspend fun getTimeline(reportId: String): List<ReportUpdateDto> =
        client().from(Tables.REPORT_UPDATES).select {
            filter { eq("report_id", reportId) }
            order("created_at", Order.ASCENDING)
        }.decodeList()

    suspend fun submitReport(report: NewReport): ReportDto =
        client().from(Tables.REPORTS)
            .insert(report) { select() }
            .decodeSingle()

    /** Staff only (enforced by RLS). A status here also updates reports.status via trigger. */
    suspend fun addUpdate(update: NewReportUpdate) {
        client().from(Tables.REPORT_UPDATES).insert(update)
    }

    /** Uploads a JPEG to "<user id>/<uuid>.jpg" as the storage policy requires; returns the object path. */
    suspend fun uploadPhoto(jpegBytes: ByteArray): String {
        val userId = requireNotNull(client().auth.currentUserOrNull()?.id) { "Not signed in" }
        val path = "$userId/${UUID.randomUUID()}.jpg"
        client().storage.from(Buckets.REPORT_PHOTOS).upload(path, jpegBytes) {
            contentType = ContentType.Image.JPEG
        }
        return path
    }

    suspend fun photoUrl(path: String): String =
        client().storage.from(Buckets.REPORT_PHOTOS).createSignedUrl(path, expiresIn = 1.hours)
}
