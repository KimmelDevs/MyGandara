package com.pikacheat.mygandara.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class StatusColors(val container: Color, val content: Color)

@Composable
private fun colorsForStatus(status: String): StatusColors = when (status) {
    "Pending" -> StatusColors(
        container = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer
    )
    "In progress" -> StatusColors(
        container = MaterialTheme.colorScheme.tertiaryContainer,
        content = MaterialTheme.colorScheme.onTertiaryContainer
    )
    "Resolved" -> StatusColors(
        container = MaterialTheme.colorScheme.primaryContainer,
        content = MaterialTheme.colorScheme.onPrimaryContainer
    )
    else -> StatusColors(
        container = MaterialTheme.colorScheme.surfaceVariant,
        content = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val colors = colorsForStatus(status)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = colors.container)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = colors.content,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}
