package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Singleton object to configure and expose the Supabase Client.
 */
object SupabaseConfig {
    private const val TAG = "SupabaseConfig"

    private val supabaseUrl: String by lazy {
        val url = BuildConfig.SUPABASE_URL
        if (url.isEmpty() || url.contains("your-project") || url.contains("placeholder")) {
            Log.e(TAG, "SUPABASE_URL is empty or has a placeholder value. Please check your AI Studio Secrets.")
        }
        url
    }

    private val supabaseKey: String by lazy {
        val key = BuildConfig.SUPABASE_ANON_KEY
        if (key.isEmpty() || key.contains("your-anon-key") || key.contains("placeholder")) {
            Log.e(TAG, "SUPABASE_ANON_KEY is empty or has a placeholder value. Please check your AI Studio Secrets.")
        }
        key
    }

    /**
     * The configured Supabase client instance.
     * Uses lazy initialization so that it is only created when accessed.
     */
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseKey
        ) {
            httpEngine = OkHttp.create()
            install(Postgrest)
            install(Auth)
        }
    }

    /**
     * Helper getter for convenient access to the 'users' table database actions.
     */
    val usersTable get() = client.postgrest["users"]

    /**
     * Helper getter for convenient access to the 'palpites' table database actions.
     */
    val palpitesTable get() = client.postgrest["palpites"]
}
