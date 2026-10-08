package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.Profile
import com.pikacheat.mygandara.data.remote.SupabaseProvider
import com.pikacheat.mygandara.data.repository.AuthRepository
import com.pikacheat.mygandara.data.repository.ProfileRepository
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object NotConfigured : SessionState
    data object SignedOut : SessionState
    data class SignedIn(val profile: Profile) : SessionState
    data class Error(val message: String) : SessionState
}

/** App-wide: who is signed in and what their role is. Drives which navigation graph is shown. */
class SessionViewModel(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SessionState>(SessionState.Loading)
    val state: StateFlow<SessionState> = _state.asStateFlow()

    init {
        if (!SupabaseProvider.isConfigured) {
            _state.value = SessionState.NotConfigured
        } else {
            viewModelScope.launch {
                authRepository.sessionStatus.collect { status ->
                    when (status) {
                        is SessionStatus.Authenticated -> {
                            val userId = status.session.user?.id
                            val current = _state.value
                            // Token refreshes also emit Authenticated; don't reload for the same user.
                            if (current !is SessionState.SignedIn || current.profile.id != userId) {
                                loadProfile()
                            }
                        }
                        is SessionStatus.NotAuthenticated -> _state.value = SessionState.SignedOut
                        SessionStatus.Initializing -> _state.value = SessionState.Loading
                        is SessionStatus.RefreshFailure -> Unit // keep the cached session; retried automatically
                    }
                }
            }
        }
    }

    /** Re-reads the profile, e.g. after the user edits it or an admin changes their role. */
    fun loadProfile() {
        viewModelScope.launch {
            val userId = authRepository.currentUserId() ?: run {
                _state.value = SessionState.SignedOut
                return@launch
            }
            if (_state.value !is SessionState.SignedIn) _state.value = SessionState.Loading
            _state.value = when (val result = loadState { profileRepository.getProfile(userId) }) {
                is UiState.Success -> result.data?.let { SessionState.SignedIn(it) }
                    ?: SessionState.Error("Your profile could not be found. Please contact the LGU.")
                is UiState.Error -> SessionState.Error(result.message)
                UiState.Loading -> SessionState.Loading
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            runAction { authRepository.signOut() }
            _state.value = SessionState.SignedOut
        }
    }

    companion object {
        val Factory = appViewModelFactory { SessionViewModel(it.authRepository, it.profileRepository) }
    }
}
