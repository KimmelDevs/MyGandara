package com.pikacheat.mygandara.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pikacheat.mygandara.data.model.Profile
import com.pikacheat.mygandara.data.model.UserRole
import com.pikacheat.mygandara.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Admin user management: list everyone and change roles via the set_user_role RPC. */
class UsersViewModel(private val profileRepository: ProfileRepository) : ViewModel() {

    val users = Loadable(viewModelScope) { profileRepository.getAllProfiles() }

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        users.refresh()
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setRole(user: Profile, role: UserRole) {
        viewModelScope.launch {
            val error = runAction { profileRepository.setRole(user.id, role) }
            _message.value = error ?: "${user.displayName} is now ${role.label.lowercase()}"
            users.refresh(showIndicator = false)
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        val Factory = appViewModelFactory { UsersViewModel(it.profileRepository) }
    }
}

fun List<Profile>.matching(query: String): List<Profile> {
    val q = query.trim()
    if (q.isEmpty()) return this
    return filter {
        it.fullName.orEmpty().contains(q, ignoreCase = true) ||
            it.email.orEmpty().contains(q, ignoreCase = true) ||
            it.barangay.orEmpty().contains(q, ignoreCase = true)
    }
}
