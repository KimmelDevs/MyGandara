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
    @SerialName("image_paths") val imagePaths: List<String> = emptyList(),
    val pinned: Boolean = false,
    @SerialName("published_at") val publishedAt: String
) {
    /** Photos to show in the grid. Older posts kept a single image in attachment_path. */
    val allImagePaths: List<String>
        get() = imagePaths + listOfNotNull(attachmentPath?.takeIf { it.isImagePath() })

    /** A non-image attachment (PDF), if any. */
    val documentPath: String? get() = attachmentPath?.takeUnless { it.isImagePath() }
}

private fun String.isImagePath(): Boolean =
    substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp")

@Serializable
data class NewPost(
    val type: PostType,
    val title: String,
    val body: String,
    @SerialName("attachment_path") val attachmentPath: String? = null,
    @SerialName("image_paths") val imagePaths: List<String> = emptyList(),
    val pinned: Boolean = false
)

@Serializable
enum class ReactionType(val emoji: String, val label: String) {
    @SerialName("like") LIKE("👍", "Like"),
    @SerialName("love") LOVE("❤️", "Love"),
    @SerialName("care") CARE("🥰", "Care"),
    @SerialName("haha") HAHA("😆", "Haha"),
    @SerialName("wow") WOW("😮", "Wow"),
    @SerialName("sad") SAD("😢", "Sad"),
    @SerialName("angry") ANGRY("😠", "Angry")
}

/** A row of the `post_reactions` table. */
@Serializable
data class PostReaction(
    @SerialName("post_id") val postId: String,
    @SerialName("user_id") val userId: String,
    val reaction: ReactionType
)

@Serializable
data class NewReaction(
    @SerialName("post_id") val postId: String,
    val reaction: ReactionType
)
