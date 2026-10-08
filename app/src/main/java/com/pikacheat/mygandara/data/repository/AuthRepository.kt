package com.pikacheat.mygandara.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(private val client: () -> SupabaseClient) {

    val sessionStatus: Flow<SessionStatus> get() = client().auth.sessionStatus

    fun currentUserId(): String? = client().auth.currentUserOrNull()?.id

    /**
     * Role is never sent here; the database trigger creates the profile as citizen.
     * Returns true when Supabase requires email confirmation before the user can sign in.
     */
    suspend fun signUp(email: String, password: String, fullName: String): Boolean {
        client().auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject { put("full_name", fullName) }
        }
        return client().auth.currentSessionOrNull() == null
    }

    suspend fun signIn(email: String, password: String) {
        client().auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = client().auth.signOut()
}
