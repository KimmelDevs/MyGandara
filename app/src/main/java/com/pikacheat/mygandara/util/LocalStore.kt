package com.pikacheat.mygandara.util

import android.content.Context
import androidx.core.content.edit
import com.pikacheat.mygandara.data.model.EmergencyContact
import com.pikacheat.mygandara.i18n.AppLanguage
import kotlinx.serialization.json.Json

/**
 * Small on-device memory (SharedPreferences): which report versions the user has already
 * looked at, and which emergency banners they dismissed. Not synced; fine to lose.
 */
class LocalStore(context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private val prefs = context.getSharedPreferences("mygandara_local", Context.MODE_PRIVATE)

    fun markReportSeen(reportId: String, updatedAt: String) =
        prefs.edit { putString("seen_$reportId", updatedAt) }

    /** True when the report changed since the user last opened it. Unopened reports count as changed once staff touch them. */
    fun isReportUpdated(reportId: String, updatedAt: String, createdAt: String): Boolean {
        val seen = prefs.getString("seen_$reportId", null) ?: return updatedAt != createdAt
        return seen != updatedAt
    }

    var language: AppLanguage
        get() = AppLanguage.fromCode(prefs.getString("language", null))
        set(value) = prefs.edit { putString("language", value.code) }

    var themeMode: ThemeMode
        get() = ThemeMode.entries.firstOrNull { it.name == prefs.getString("theme_mode", null) } ?: ThemeMode.SYSTEM
        set(value) = prefs.edit { putString("theme_mode", value.name) }

    /** Last downloaded hotlines, so they still show with no signal. */
    var cachedContacts: List<EmergencyContact>
        get() = prefs.getString("emergency_contacts", null)
            ?.let { runCatching { json.decodeFromString<List<EmergencyContact>>(it) }.getOrNull() }
            .orEmpty()
        set(value) = prefs.edit { putString("emergency_contacts", json.encodeToString(value)) }

    fun dismissEmergency(postId: String) = prefs.edit { putString("dismissed_emergency", postId) }

    fun isEmergencyDismissed(postId: String): Boolean = prefs.getString("dismissed_emergency", null) == postId
}

enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
