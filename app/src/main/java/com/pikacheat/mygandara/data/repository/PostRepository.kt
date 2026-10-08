package com.pikacheat.mygandara.data.repository

import com.pikacheat.mygandara.data.model.NewPost
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.data.remote.Buckets
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.util.Dates
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.util.UUID

class PostRepository(private val client: () -> SupabaseClient) {

    suspend fun getPosts(): List<PostDto> =
        client().from(Tables.POSTS).select {
            order("pinned", Order.DESCENDING)
            order("published_at", Order.DESCENDING)
        }.decodeList()

    /** Most recent emergency post from the last [withinHours] hours, if any. */
    suspend fun latestEmergency(withinHours: Long = 72): PostDto? {
        val since = Dates.isoHoursAgo(withinHours)
        return client().from(Tables.POSTS).select {
            filter {
                eq("type", PostType.EMERGENCY)
                gte("published_at", since)
            }
            order("published_at", Order.DESCENDING)
            limit(1)
        }.decodeList<PostDto>().firstOrNull()
    }

    /** Admin only (enforced by RLS). */
    suspend fun createPost(post: NewPost): PostDto =
        client().from(Tables.POSTS)
            .insert(post) { select() }
            .decodeSingle()

    /** Admin only (enforced by RLS). */
    suspend fun deletePost(id: String) {
        client().from(Tables.POSTS).delete { filter { eq("id", id) } }
    }

    /** Admin only (storage policy). Returns the object path to store in posts.attachment_path. */
    suspend fun uploadAttachment(bytes: ByteArray, extension: String, contentType: ContentType): String {
        val path = "posts/${UUID.randomUUID()}.$extension"
        client().storage.from(Buckets.POST_ATTACHMENTS).upload(path, bytes) {
            this.contentType = contentType
        }
        return path
    }

    fun attachmentUrl(path: String): String =
        client().storage.from(Buckets.POST_ATTACHMENTS).publicUrl(path)
}
