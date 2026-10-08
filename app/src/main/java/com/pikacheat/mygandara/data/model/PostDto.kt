package com.pikacheat.mygandara.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PostType(val label: String) {
    @SerialName("announcement") ANNOUNCEMENT("Announcement"),
    @SerialName("ordinance") ORDINANCE("Ordinance"),
    @SerialName("event") EVENT("Event"),
    @SerialName("emergency") EMERGENCY("Emergency")
}

/** A row of the `posts` table (the LGU bulletin board). */
@Serializable
data class PostDto(
    val id: String,
    @SerialName("author_id") val authorId: String? = null,
    val type: PostType = PostType.ANNOUNCEMENT,
    val title: String,
    val body: String = "",
    @SerialName("attachment_path") val attachmentPath: String? = null,
    val pinned: Boolean = false,
    @SerialName("published_at") val publishedAt: String
)

@Serializable
data class NewPost(
    val type: PostType,
    val title: String,
    val body: String,
    @SerialName("attachment_path") val attachmentPath: String? = null,
    val pinned: Boolean = false
)
