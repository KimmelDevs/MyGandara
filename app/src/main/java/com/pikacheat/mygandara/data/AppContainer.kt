package com.pikacheat.mygandara.data

import android.content.Context
import com.pikacheat.mygandara.util.LocalStore
import com.pikacheat.mygandara.data.remote.SupabaseProvider
import com.pikacheat.mygandara.data.repository.AuthRepository
import com.pikacheat.mygandara.data.repository.EmergencyContactRepository
import com.pikacheat.mygandara.data.repository.PostRepository
import com.pikacheat.mygandara.data.repository.ProfileRepository
import com.pikacheat.mygandara.data.repository.RealtimeRepository
import com.pikacheat.mygandara.data.repository.ReportRepository

/** Simple manual DI: one instance of each repository for the whole app. */
class AppContainer(context: Context) {
    val localStore by lazy { LocalStore(context) }
    val authRepository by lazy { AuthRepository { SupabaseProvider.client } }
    val profileRepository by lazy { ProfileRepository { SupabaseProvider.client } }
    val reportRepository by lazy { ReportRepository { SupabaseProvider.client } }
    val postRepository by lazy { PostRepository { SupabaseProvider.client } }
    val emergencyContactRepository by lazy { EmergencyContactRepository { SupabaseProvider.client } }
    val realtimeRepository by lazy { RealtimeRepository { SupabaseProvider.client } }
}
