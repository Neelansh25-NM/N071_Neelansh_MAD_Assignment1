package com.fahim.geminiApiComposeStarter.data

/** Abstraction over the Gemini text generation call so the ViewModel can be unit tested. */
interface GeminiRepository {

    suspend fun initialize()

    suspend fun sendMessage(
        prompt: String
    ): String
}
