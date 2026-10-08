package com.pikacheat.mygandara.ui.screens.report

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.pikacheat.mygandara.data.model.ReportStatus
import com.pikacheat.mygandara.data.model.ReportUpdateDto
import com.pikacheat.mygandara.ui.components.StatusBadge
import com.pikacheat.mygandara.ui.components.UiStateContent
import com.pikacheat.mygandara.ui.viewmodel.ReportDetail
import com.pikacheat.mygandara.ui.viewmodel.ReportDetailViewModel
import com.pikacheat.mygandara.ui.viewmodel.StatusUpdateState
import com.pikacheat.mygandara.util.Dates
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    isStaff: Boolean,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportDetailViewModel = viewModel(factory = ReportDetailViewModel.factory(isStaff))
) {
    val state by viewModel.detail.state.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.detail.isRefreshing.collectAsStateWithLifecycle()
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateState.message) {
        updateState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Report details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        UiStateContent(
            state = state,
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.detail.refresh() },
            modifier = Modifier.padding(innerPadding)
        ) { detail ->
            ReportDetailContent(
                detail = detail,
                isStaff = isStaff,
                updateState = updateState,
                onAddUpdate = viewModel::addUpdate
            )
        }
    }
}

@Composable
private fun ReportDetailContent(
    detail: ReportDetail,
    isStaff: Boolean,
    updateState: StatusUpdateState,
    onAddUpdate: (ReportStatus?, String, onDone: () -> Unit) -> Unit
) {
    val report = detail.report
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (detail.photoUrl != null) {
            AsyncImage(
                model = detail.photoUrl,
                contentDescription = "Photo of the reported problem",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            StatusBadge(status = report.status)
            Text(
                text = "Submitted ${Dates.dateTime(report.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Text(
            text = report.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (report.description.isNotBlank()) {
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(modifier = Modifier.padding(vertical = 12.dp)) {
            DetailRow("Category", report.category.label)
            report.address?.takeIf { it.isNotBlank() }?.let { DetailRow("Location", it) }
            detail.reporter?.let { reporter ->
                DetailRow("Reported by", reporter.displayName)
                reporter.phone?.let { DetailRow("Mobile", it) }
                reporter.email?.let { DetailRow("Email", it) }
            }
        }

        if (report.latitude != null && report.longitude != null) {
            val lat = report.latitude
            val lng = report.longitude
            TextButton(onClick = {
                val coords = String.format(Locale.US, "%.6f,%.6f", lat, lng)
                val geo = Intent(Intent.ACTION_VIEW, "geo:$coords?q=$coords".toUri())
                try {
                    context.startActivity(geo)
                } catch (_: ActivityNotFoundException) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://www.google.com/maps/search/?api=1&query=$coords".toUri())
                    )
                }
            }) {
                Icon(Icons.Filled.Map, contentDescription = null)
                Text("Open location in maps", modifier = Modifier.padding(start = 6.dp))
            }
        }

        if (isStaff) {
            StaffUpdatePanel(current = report.status, updateState = updateState, onAddUpdate = onAddUpdate)
        }

        Text(
            text = "Status timeline",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            detail.timeline.forEachIndexed { index, update ->
                TimelineRow(update = update, isLatest = index == detail.timeline.lastIndex)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StaffUpdatePanel(
    current: ReportStatus,
    updateState: StatusUpdateState,
    onAddUpdate: (ReportStatus?, String, onDone: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var newStatus by rememberSaveable { mutableStateOf<ReportStatus?>(null) }
    var note by rememberSaveable { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Post an update", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = newStatus?.label ?: "Keep as ${current.label}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("New status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Keep as ${current.label}") },
                        onClick = { newStatus = null; expanded = false }
                    )
                    ReportStatus.entries.filter { it != current }.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.label) },
                            onClick = { newStatus = status; expanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note to the reporter (optional)") },
                placeholder = { Text("e.g. Assigned to engineering office") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onAddUpdate(newStatus, note) { newStatus = null; note = "" } },
                enabled = !updateState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (updateState.isSaving) "Posting…" else "Post update")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun TimelineRow(update: ReportUpdateDto, isLatest: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val done = !isLatest || update.status == ReportStatus.RESOLVED
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.Schedule,
            contentDescription = null,
            tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Column {
            Text(
                text = update.status?.label ?: "Note",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            update.note?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = Dates.dateTime(update.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
