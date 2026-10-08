package com.pikacheat.mygandara.ui.components

import com.pikacheat.mygandara.i18n.t
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.util.Dates

@Composable
fun PostCard(
    post: PostDto,
    attachmentUrl: String?,
    onOpenAttachment: (String) -> Unit,
    onViewImage: (String) -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val isEmergency = post.type == PostType.EMERGENCY
    val isNew = Dates.isWithinHours(post.publishedAt, 24)
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEmergency) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.pinned) PillBadge(text = t("Pinned"))
                // "New" badge (#13)
                if (isNew) {
                    PillBadge(
                        text = t("New"),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
                if (isEmergency) {
                    PillBadge(
                        text = post.type.label,
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                } else {
                    PillBadge(
                        text = post.type.label,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Text(
                    text = Dates.date(post.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )
                // Share (#15)
                IconButton(onClick = onShare) {
                    Icon(Icons.Filled.Share, contentDescription = t("Share post"))
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = t("Delete post"))
                    }
                }
            }

            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if (post.body.isNotBlank()) {
                    Text(
                        text = post.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (attachmentUrl != null) {
                    if (post.attachmentPath.isImagePath()) {
                        // Tap to open full screen (#16)
                        AsyncImage(
                            model = attachmentUrl,
                            contentDescription = t("Attachment for %s. Tap to enlarge.", post.title),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .padding(top = 8.dp, bottom = 8.dp)
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onViewImage(attachmentUrl) }
                        )
                    } else {
                        TextButton(onClick = { onOpenAttachment(attachmentUrl) }) {
                            Icon(Icons.Filled.Description, contentDescription = null)
                            Text(t("View attachment"), modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

private fun String?.isImagePath(): Boolean =
    this != null && substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp")
