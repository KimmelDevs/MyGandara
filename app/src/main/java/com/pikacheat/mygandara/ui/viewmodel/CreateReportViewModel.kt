package com.pikacheat.mygandara.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.NewReport
import com.pikacheat.mygandara.data.model.ReportCategory
import com.pikacheat.mygandara.data.repository.ReportRepository
import com.pikacheat.mygandara.util.ImageCompressor
import com.pikacheat.mygandara.util.LatLng
import com.pikacheat.mygandara.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateReportState(
    val photoUri: Uri? = null,
    val location: LatLng? = null,
    val isLocating: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    /** Set once the report is saved; the screen navigates to it. */
    val submittedId: String? = null
)

class CreateReportViewModel(
    private val app: Application,
    private val reportRepository: ReportRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreateReportState())
    val state: StateFlow<CreateReportState> = _state.asStateFlow()

    fun setPhoto(uri: Uri?) = _state.update { it.copy(photoUri = uri) }

    fun clearLocation() = _state.update { it.copy(location = null) }

    fun clearError() = _state.update { it.copy(error = null) }

    fun fetchLocation() {
        if (_state.value.isLocating) return
        _state.update { it.copy(isLocating = true, error = null) }
        viewModelScope.launch {
            val location = runCatching { LocationHelper.currentLocation(app) }.getOrNull()
            _state.update {
                it.copy(
                    isLocating = false,
                    location = location ?: it.location,
                    error = if (location == null) "Couldn't get your location. Make sure location is turned on." else null
                )
            }
        }
    }

    /** Checks required fields before showing the confirmation summary; sets [CreateReportState.error] if invalid. */
    fun validate(title: String, category: ReportCategory?): Boolean {
        val error = when {
            category == null -> "Choose a category."
            title.trim().length < 3 -> "Give the report a short title."
            else -> null
        }
        _state.update { it.copy(error = error) }
        return error == null
    }

    fun submit(title: String, description: String, category: ReportCategory?, address: String) {
        val current = _state.value
        if (current.isSubmitting) return
        when {
            category == null -> return _state.update { it.copy(error = "Choose a category.") }
            title.trim().length < 3 -> return _state.update { it.copy(error = "Give the report a short title.") }
        }
        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val result = loadState {
                val photoPath = current.photoUri?.let { uri ->
                    reportRepository.uploadPhoto(ImageCompressor.compressToJpeg(app, uri))
                }
                reportRepository.submitReport(
                    NewReport(
                        title = title.trim(),
                        description = description.trim(),
                        category = category!!,
                        photoPath = photoPath,
                        latitude = current.location?.latitude,
                        longitude = current.location?.longitude,
                        address = address.trim().ifBlank { null }
                    )
                )
            }
            _state.update {
                when (result) {
                    is UiState.Success -> it.copy(isSubmitting = false, submittedId = result.data.id)
                    is UiState.Error -> it.copy(isSubmitting = false, error = result.message)
                    UiState.Loading -> it
                }
            }
        }
    }

    companion object {
        val Factory = appViewModelFactory {
            CreateReportViewModel(this[APPLICATION_KEY] as Application, it.reportRepository)
        }
    }
}
