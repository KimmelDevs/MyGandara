package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.EmergencyContact
import com.pikacheat.mygandara.data.model.EmergencyContactInput
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.data.repository.EmergencyContactRepository
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import com.pikacheat.mygandara.util.LocalStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class HotlinesState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** Set when the latest download failed; cached contacts are still shown. */
    val loadError: String? = null,
    val isSaving: Boolean = false,
    val message: String? = null
)

/**
 * Emergency hotlines. Shows the copy saved on the phone immediately (works with no signal),
 * then refreshes from Supabase. Admins can add, edit, and delete (enforced by RLS).
 */
class HotlinesViewModel(
    private val repository: EmergencyContactRepository,
    realtimeRepository: RealtimeRepository,
    private val localStore: LocalStore
) : ViewModel() {

    private val _state = MutableStateFlow(
        localStore.cachedContacts.let { cached -> HotlinesState(contacts = cached, isLoading = cached.isEmpty()) }
    )
    val state: StateFlow<HotlinesState> = _state.asStateFlow()

    init {
        refresh(showIndicator = false)
        realtimeRepository.tableChanges(Tables.EMERGENCY_CONTACTS)
            .onEach { refresh(showIndicator = false) }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun refresh(showIndicator: Boolean = true) {
        viewModelScope.launch {
            if (showIndicator) _state.value = _state.value.copy(isRefreshing = true)
            when (val result = loadState { repository.getContacts() }) {
                is UiState.Success -> {
                    localStore.cachedContacts = result.data
                    _state.value = _state.value.copy(
                        contacts = result.data, isLoading = false, isRefreshing = false, loadError = null
                    )
                }
                is UiState.Error -> _state.value = _state.value.copy(
                    isLoading = false, isRefreshing = false, loadError = result.message
                )
                UiState.Loading -> Unit
            }
        }
    }

    /** Add when [existing] is null, otherwise update it. */
    fun save(existing: EmergencyContact?, input: EmergencyContactInput, onDone: () -> Unit) {
        val name = input.name.trim()
        val phone = input.phone.trim()
        when {
            name.length < 2 -> return showMessage("Enter a name.")
            phone.length < 3 -> return showMessage("Enter a phone number.")
        }
        val clean = input.copy(name = name, phone = phone, note = input.note?.trim()?.ifBlank { null })
        mutate(if (existing == null) "Hotline added" else "Hotline updated", onDone) {
            if (existing == null) repository.add(clean) else repository.update(existing.id, clean)
        }
    }

    fun delete(contact: EmergencyContact) = mutate("Hotline deleted") { repository.delete(contact.id) }

    private fun mutate(success: String, onDone: () -> Unit = {}, block: suspend () -> Unit) {
        if (_state.value.isSaving) return
        _state.value = _state.value.copy(isSaving = true)
        viewModelScope.launch {
            val error = runAction(block)
            _state.value = _state.value.copy(isSaving = false, message = error ?: success)
            if (error == null) {
                onDone()
                refresh(showIndicator = false)
            }
        }
    }

    private fun showMessage(message: String) {
        _state.value = _state.value.copy(message = message)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    companion object {
        val Factory = appViewModelFactory {
            HotlinesViewModel(it.emergencyContactRepository, it.realtimeRepository, it.localStore)
        }
    }
}
