package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.ProfileUpdate
import com.pikacheat.mygandara.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SaveState(val isSaving: Boolean = false, val message: String? = null)

class ProfileViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    private val _saveState = MutableStateFlow(SaveState())
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    fun save(userId: String, fullName: String, phone: String, barangay: String, onSaved: () -> Unit) {
        if (fullName.isBlank()) {
            _saveState.value = SaveState(message = "Name can't be empty.")
            return
        }
        _saveState.value = SaveState(isSaving = true)
        viewModelScope.launch {
            val error = runAction {
                profileRepository.updateProfile(
                    userId,
                    ProfileUpdate(
                        fullName = fullName.trim(),
                        phone = phone.trim().ifBlank { null },
                        barangay = barangay.trim().ifBlank { null }
                    )
                )
            }
            _saveState.value = SaveState(message = error ?: "Profile saved")
            if (error == null) onSaved()
        }
    }

    fun clearMessage() {
        _saveState.value = _saveState.value.copy(message = null)
    }

    companion object {
        val Factory = appViewModelFactory { ProfileViewModel(it.profileRepository) }
    }
}
