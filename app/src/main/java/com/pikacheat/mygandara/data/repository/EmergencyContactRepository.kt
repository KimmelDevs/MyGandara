package com.pikacheat.mygandara.data.repository

import com.pikacheat.mygandara.data.model.EmergencyContact
import com.pikacheat.mygandara.data.model.EmergencyContactInput
import com.pikacheat.mygandara.data.remote.Tables
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class EmergencyContactRepository(private val client: () -> SupabaseClient) {

    /** Readable by everyone, including signed-out users. */
    suspend fun getContacts(): List<EmergencyContact> =
        client().from(Tables.EMERGENCY_CONTACTS).select {
            order("sort_order", Order.ASCENDING)
            order("name", Order.ASCENDING)
        }.decodeList()

    /** Admin only (RLS). */
    suspend fun add(input: EmergencyContactInput) {
        client().from(Tables.EMERGENCY_CONTACTS).insert(input)
    }

    /** Admin only (RLS). */
    suspend fun update(id: String, input: EmergencyContactInput) {
        client().from(Tables.EMERGENCY_CONTACTS).update(input) { filter { eq("id", id) } }
    }

    /** Admin only (RLS). */
    suspend fun delete(id: String) {
        client().from(Tables.EMERGENCY_CONTACTS).delete { filter { eq("id", id) } }
    }
}
