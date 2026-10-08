package com.pikacheat.mygandara.util

import android.content.Context
import androidx.core.content.edit
import com.pikacheat.mygandara.i18n.AppLanguage

/**
 * Small on-device memory (SharedPreferences): which report versions the user has already
 * looked at, and which emergency banners they dismissed. Not synced; fine to lose.
 */
class LocalStore(context: Context) {

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

    fun dismissEmergency(postId: String) = prefs.edit { putString("dismissed_emergency", postId) }

    fun isEmergencyDismissed(postId: String): Boolean = prefs.getString("dismissed_emergency", null) == postId
}
