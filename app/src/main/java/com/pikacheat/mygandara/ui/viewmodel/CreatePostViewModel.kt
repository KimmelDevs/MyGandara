package com.pikacheat.mygandara.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.NewPost
import com.pikacheat.mygandara.data.model.PostType
import com.pikacheat.mygandara.data.repository.PostRepository
import com.pikacheat.mygandara.util.ImageCompressor
import io.ktor.http.ContentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CreatePostState(
    val attachmentUri: Uri? = null,
    val attachmentName: String? = null,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val posted: Boolean = false
)

private const val MAX_PDF_BYTES = 10 * 1024 * 1024

class CreatePostViewModel(
    private val app: Application,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    fun setAttachment(uri: Uri?) {
        val name = uri?.let { displayName(it) }
        _state.update { it.copy(attachmentUri = uri, attachmentName = name) }
    }

    fun clearError() = _state.update { it.copy(error = null) }

    fun submit(type: PostType, title: String, body: String, pinned: Boolean) {
        val current = _state.value
        if (current.isSubmitting) return
        if (title.trim().length < 3) return _state.update { it.copy(error = "Enter a title.") }

        _state.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            val error = runAction {
                val attachmentPath = current.attachmentUri?.let { uploadAttachment(it) }
                postRepository.createPost(
                    NewPost(
                        type = type,
                        title = title.trim(),
                        body = body.trim(),
                        attachmentPath = attachmentPath,
                        pinned = pinned
                    )
                )
            }
            _state.update { it.copy(isSubmitting = false, error = error, posted = error == null) }
        }
    }

    /** Images are compressed to JPEG; PDFs are uploaded as-is (max 10 MB). */
    private suspend fun uploadAttachment(uri: Uri): String {
        val mime = app.contentResolver.getType(uri).orEmpty()
        return if (mime == "application/pdf") {
            val bytes = withContext(Dispatchers.IO) {
                app.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            } ?: error("Couldn't read the selected file.")
            require(bytes.size <= MAX_PDF_BYTES) { "PDF is too large (max 10 MB)." }
            postRepository.uploadAttachment(bytes, "pdf", ContentType.Application.Pdf)
        } else {
            val jpeg = ImageCompressor.compressToJpeg(app, uri)
            postRepository.uploadAttachment(jpeg, "jpg", ContentType.Image.JPEG)
        }
    }

    private fun displayName(uri: Uri): String? =
        app.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        }

    companion object {
        val Factory = appViewModelFactory {
            CreatePostViewModel(this[APPLICATION_KEY] as Application, it.postRepository)
        }
    }
}
