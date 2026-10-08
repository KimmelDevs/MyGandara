package com.pikacheat.mygandara.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.data.model.ReactionType
import com.pikacheat.mygandara.i18n.t
import com.pikacheat.mygandara.ui.viewmodel.ReactionSummary
import com.pikacheat.mygandara.util.Dates

@Composable
fun PostCard(
    post: PostDto,
    imageUrls: List<String>,
    documentUrl: String?,
    reactions: ReactionSummary,
    onOpenDocument: (String) -> Unit,
    onViewImage: (index: Int) -> Unit,
    onReact: (ReactionType) -> Unit,
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
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (post.pinned) PillBadge(text = "Pinned")
                // "New" badge (#13)
                if (isNew) {
                    PillBadge(
                        text = "New",
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
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = t("Delete post"))
                    }
                }
            }

            Text(
                text = post.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
            if (post.body.isNotBlank()) {
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Photos, Facebook-style; tap opens the swipeable viewer.
            PostImageGrid(
                urls = imageUrls,
                onImageClick = onViewImage,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (documentUrl != null) {
                TextButton(onClick = { onOpenDocument(documentUrl) }) {
                    Icon(Icons.Filled.Description, contentDescription = null)
                    Text(t("View attachment"), modifier = Modifier.padding(start = 6.dp))
                }
            }

            ReactionBar(
                summary = reactions,
                onReact = onReact,
                onShare = onShare,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
