package com.pikacheat.mygandara.ui.screens.hotlines

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.ContactCategory
import com.pikacheat.mygandara.data.model.EmergencyContact
import com.pikacheat.mygandara.data.model.EmergencyContactInput
import com.pikacheat.mygandara.i18n.I18n
import com.pikacheat.mygandara.i18n.LocalLanguage
import com.pikacheat.mygandara.i18n.t
import com.pikacheat.mygandara.ui.components.ConfirmDialog
import com.pikacheat.mygandara.ui.components.EmptyListText
import com.pikacheat.mygandara.ui.components.ListSectionHeader
import com.pikacheat.mygandara.ui.components.RefreshOnResume
import com.pikacheat.mygandara.ui.components.SkeletonList
import com.pikacheat.mygandara.ui.viewmodel.HotlinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotlinesScreen(
    canEdit: Boolean,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    viewModel: HotlinesViewModel = viewModel(factory = HotlinesViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val language = LocalLanguage.current
    val context = LocalContext.current
    var editing by remember { mutableStateOf<EditTarget?>(null) }
    var pendingDelete by remember { mutableStateOf<EmergencyContact?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(I18n.tr(language, it))
            viewModel.clearMessage()
        }
    }

    RefreshOnResume { viewModel.refresh(showIndicator = false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(t("Emergency hotlines")) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Back"))
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (canEdit) {
                ExtendedFloatingActionButton(
                    onClick = { editing = EditTarget(null) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(t("Add hotline")) }
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isLoading) {
                SkeletonList()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp)
                ) {
                    item {
                        Text(
                            t("Tap a number to call. These numbers are saved on your phone, so they still show without internet."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (state.loadError != null && state.contacts.isNotEmpty()) {
                        item {
                            Text(
                                t("Couldn't refresh. Showing the numbers saved on this phone."),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (state.contacts.isEmpty()) {
                        item {
                            EmptyListText(
                                state.loadError ?: if (canEdit) {
                                    "No hotlines yet. Tap \"Add hotline\" to add the local numbers."
                                } else {
                                    "No hotlines yet."
                                }
                            )
                        }
                    }
                    state.contacts
                        .groupBy { it.category }
                        .toSortedMap(compareBy { it.ordinal })
                        .forEach { (category, contacts) ->
                            item(key = "header_${category.name}") { ListSectionHeader(category.label) }
                            items(contacts, key = { it.id }) { contact ->
                                HotlineCard(
                                    contact = contact,
                                    canEdit = canEdit,
                                    onCall = { dial(context, contact.phone) },
                                    onEdit = { editing = EditTarget(contact) },
                                    onDelete = { pendingDelete = contact }
                                )
                            }
                        }
                }
            }
        }
    }

    editing?.let { target ->
        HotlineEditDialog(
            existing = target.contact,
            isSaving = state.isSaving,
            onSave = { input -> viewModel.save(target.contact, input) { editing = null } },
            onDismiss = { editing = null }
        )
    }
    pendingDelete?.let { contact ->
        ConfirmDialog(
            title = "Delete hotline?",
            message = t("\"%s\" will be removed for everyone.", contact.name),
            confirmLabel = "Delete",
            destructive = true,
            onConfirm = {
                viewModel.delete(contact)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null }
        )
    }
}

/** Wrapper so "add new" (null contact) is distinguishable from "no dialog". */
private data class EditTarget(val contact: EmergencyContact?)

@Composable
private fun HotlineCard(
    contact: EmergencyContact,
    canEdit: Boolean,
    onCall: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val urgent = contact.category == ContactCategory.EMERGENCY
    Card(
        onClick = onCall,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (urgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                contact.category.icon(),
                contentDescription = null,
                tint = if (urgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(contact.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(
                    contact.phone,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                contact.note?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (canEdit) {
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = t("Edit")) }
                IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = t("Delete")) }
            }
            FilledIconButton(
                onClick = onCall,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (urgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Filled.Call, contentDescription = t("Call %s", contact.name))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HotlineEditDialog(
    existing: EmergencyContact?,
    isSaving: Boolean,
    onSave: (EmergencyContactInput) -> Unit,
    onDismiss: () -> Unit
) {
    var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
    var phone by rememberSaveable { mutableStateOf(existing?.phone.orEmpty()) }
    var note by rememberSaveable { mutableStateOf(existing?.note.orEmpty()) }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: ContactCategory.FIRE) }
    var order by rememberSaveable { mutableStateOf((existing?.sortOrder ?: 100).toString()) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t(if (existing == null) "Add hotline" else "Edit hotline")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = t(category.label),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(t("Type")) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ContactCategory.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(t(option.label)) },
                                leadingIcon = { Icon(option.icon(), contentDescription = null) },
                                onClick = { category = option; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(120) },
                    label = { Text(t("Name")) },
                    placeholder = { Text(t("e.g. BFP Gandara Fire Station")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it.take(40) },
                    label = { Text(t("Phone number")) },
                    placeholder = { Text(t("e.g. 0917 123 4567")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    label = { Text(t("Note (optional)")) },
                    placeholder = { Text(t("e.g. 24/7, near the municipal hall")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = order,
                    onValueChange = { v -> order = v.filter(Char::isDigit).take(4) },
                    label = { Text(t("Order in list (lower shows first)")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    onSave(EmergencyContactInput(name, category, phone, note, order.toIntOrNull() ?: 100))
                }
            ) { Text(t(if (isSaving) "Saving…" else "Save")) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(t("Cancel")) } }
    )
}

private fun ContactCategory.icon(): ImageVector = when (this) {
    ContactCategory.EMERGENCY -> Icons.Filled.Warning
    ContactCategory.FIRE -> Icons.Filled.LocalFireDepartment
    ContactCategory.POLICE -> Icons.Filled.LocalPolice
    ContactCategory.MEDICAL -> Icons.Filled.LocalHospital
    ContactCategory.DISASTER -> Icons.Filled.Flood
    ContactCategory.UTILITY -> Icons.Filled.Power
    ContactCategory.OTHER -> Icons.Filled.Phone
}

/** Opens the dialer with the number filled in (no CALL_PHONE permission needed). */
private fun dial(context: Context, phone: String) {
    val number = phone.filter { it.isDigit() || it == '+' }
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$number".toUri()))
    } catch (_: ActivityNotFoundException) {
        // No dialer (e.g. some tablets); nothing to do.
    }
}
