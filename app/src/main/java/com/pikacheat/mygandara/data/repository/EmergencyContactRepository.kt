package com.pikacheat.mygandara.data.repository

import com.pikacheat.mygandara.data.model.EmergencyContact
import com.pikacheat.mygandara.data.model.EmergencyContactInput
import com.pikacheat.mygandara.data.model.HotlineCategory
import com.pikacheat.mygandara.data.model.HotlineCategoryInput
import com.pikacheat.mygandara.data.remote.Tables
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/** Hotlines and their categories. Readable by everyone (even signed out); writes are admin-only via RLS. */
class EmergencyContactRepository(private val client: () -> SupabaseClient) {

    suspend fun getContacts(): List<EmergencyContact> =
        client().from(Tables.EMERGENCY_CONTACTS).select {
            order("sort_order", Order.ASCENDING)
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun add(input: EmergencyContactInput) {
        client().from(Tables.EMERGENCY_CONTACTS).insert(input)
    }

    suspend fun update(id: String, input: EmergencyContactInput) {
        client().from(Tables.EMERGENCY_CONTACTS).update(input) { filter { eq("id", id) } }
    }

    suspend fun delete(id: String) {
        client().from(Tables.EMERGENCY_CONTACTS).delete { filter { eq("id", id) } }
    }

    suspend fun getCategories(): List<HotlineCategory> =
        client().from(Tables.HOTLINE_CATEGORIES).select {
            order("sort_order", Order.ASCENDING)
            order("name", Order.ASCENDING)
        }.decodeList()

    suspend fun addCategory(input: HotlineCategoryInput) {
        client().from(Tables.HOTLINE_CATEGORIES).insert(input)
    }

    suspend fun updateCategory(id: String, input: HotlineCategoryInput) {
        client().from(Tables.HOTLINE_CATEGORIES).update(input) { filter { eq("id", id) } }
    }

    /** Hotlines in this category become uncategorised (foreign key ON DELETE SET NULL). */
    suspend fun deleteCategory(id: String) {
        client().from(Tables.HOTLINE_CATEGORIES).delete { filter { eq("id", id) } }
    }
}
