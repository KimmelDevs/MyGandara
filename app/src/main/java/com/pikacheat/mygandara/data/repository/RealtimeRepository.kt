package com.pikacheat.mygandara.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class RealtimeRepository(private val client: () -> SupabaseClient) {

    /**
     * Emits whenever a row in [table] is inserted, updated or deleted (RLS still applies,
     * so users only hear about rows they can read). The channel is removed when collection stops.
     */
    fun tableChanges(table: String): Flow<Unit> = channelFlow {
        val supabase = client()
        val channel = supabase.channel("$table-${UUID.randomUUID()}")
        val changes = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            this.table = table
        }
        launch { changes.collect { send(Unit) } }
        channel.subscribe()
        try {
            awaitCancellation()
        } finally {
            withContext(NonCancellable) { supabase.realtime.removeChannel(channel) }
        }
    }
}
