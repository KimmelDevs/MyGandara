package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.PostDto
import com.pikacheat.mygandara.data.remote.Tables
import com.pikacheat.mygandara.data.repository.PostRepository
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import com.pikacheat.mygandara.util.LocalStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** The latest emergency post (last 72h), shown as a banner on every screen until dismissed (#14). */
class EmergencyViewModel(
    private val postRepository: PostRepository,
    realtimeRepository: RealtimeRepository,
    private val localStore: LocalStore
) : ViewModel() {

    private val _emergency = MutableStateFlow<PostDto?>(null)
    val emergency: StateFlow<PostDto?> = _emergency.asStateFlow()

    init {
        load()
        realtimeRepository.tableChanges(Tables.POSTS)
            .onEach { load() }
            .catch { }
            .launchIn(viewModelScope)
    }

    fun load() {
        viewModelScope.launch {
            val latest = runCatching { postRepository.latestEmergency() }.getOrNull()
            _emergency.value = latest?.takeUnless { localStore.isEmergencyDismissed(it.id) }
        }
    }

    fun dismiss() {
        _emergency.value?.let { localStore.dismissEmergency(it.id) }
        _emergency.value = null
    }

    companion object {
        val Factory = appViewModelFactory { EmergencyViewModel(it.postRepository, it.realtimeRepository, it.localStore) }
    }
}
