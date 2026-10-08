package com.pikacheat.mygandara.ui.screens.profile

import com.pikacheat.mygandara.i18n.I18n
import com.pikacheat.mygandara.i18n.LocalLanguage
import com.pikacheat.mygandara.i18n.t
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pikacheat.mygandara.data.model.Profile
import com.pikacheat.mygandara.ui.components.ConfirmDialog
import com.pikacheat.mygandara.ui.components.LanguagePicker
import com.pikacheat.mygandara.ui.components.ThemeModePicker
import com.pikacheat.mygandara.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: Profile,
    onProfileSaved: () -> Unit,
    onOpenPrivacyNotice: () -> Unit,
    onOpenReactedPosts: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.Factory)
) {
    var fullName by rememberSaveable(profile.id) { mutableStateOf(profile.fullName.orEmpty()) }
    var phone by rememberSaveable(profile.id) { mutableStateOf(profile.phone.orEmpty()) }
    var barangay by rememberSaveable(profile.id) { mutableStateOf(profile.barangay.orEmpty()) }
    val saveState by viewModel.saveState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmSignOut by remember { mutableStateOf(false) }
    val language = LocalLanguage.current

    LaunchedEffect(saveState.message) {
        saveState.message?.let {
            snackbarHostState.showSnackbar(I18n.tr(language, it))
            viewModel.clearMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(t("Profile")) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(profile.email.orEmpty(), style = MaterialTheme.typography.bodyMedium)
            Text(
                t("Role: %s", t(profile.role.label)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(t("Full name")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(t("Mobile number (optional)")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = barangay,
                onValueChange = { barangay = it },
                label = { Text(t("Barangay (optional)")) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { viewModel.save(profile.id, fullName, phone, barangay, onProfileSaved) },
                enabled = !saveState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t(if (saveState.isSaving) "Saving…" else "Save"))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedButton(onClick = onOpenReactedPosts, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.ThumbUp, contentDescription = null)
                Text(t("Posts I reacted to"), modifier = Modifier.padding(start = 8.dp))
            }

            Text(t("Language"), style = MaterialTheme.typography.labelLarge)
            LanguagePicker()

            Text(t("Appearance"), style = MaterialTheme.typography.labelLarge)
            ThemeModePicker()

            TextButton(onClick = onOpenPrivacyNotice) { Text(t("Privacy notice")) }
            OutlinedButton(onClick = { confirmSignOut = true }, modifier = Modifier.fillMaxWidth()) {
                Text(t("Sign out"))
            }
        }
    }

    if (confirmSignOut) {
        ConfirmDialog(
            title = t("Sign out?"),
            message = t("You'll need your email and password to sign in again."),
            confirmLabel = t("Sign out"),
            onConfirm = {
                confirmSignOut = false
                onSignOut()
            },
            onDismiss = { confirmSignOut = false }
        )
    }
}
