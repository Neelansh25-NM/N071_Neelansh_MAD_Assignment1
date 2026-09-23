package com.fahim.geminiApiComposeStarter.data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CancellationException

private const val TAG = "GeminiRepository"
private const val DEFAULT_MODEL = "gemini-3.6-flash"


class GeminiRepositoryImpl(
    private val secureApiKeyStore: SecureApiKeyStore
) : GeminiRepository {

    override suspend fun initialize() {
        secureApiKeyStore.initializeEncryptedKey()
    }

    override suspend fun sendMessage(
        prompt: String
    ): String = withContext(Dispatchers.IO) {

        println("GEMINI: sendMessage() started")

        println("GEMINI: attempting to decrypt API key")

        println("GEMINI: calling getDecryptedApiKey()")

        val apiKey =
            secureApiKeyStore.getDecryptedApiKey()

        println("GEMINI: getDecryptedApiKey() returned")

        println(
            "GEMINI: API key loaded = ${apiKey.isNotBlank()}"
        )

        println(
            "GEMINI: API key loaded = ${apiKey.isNotBlank()}"
        )

        require(apiKey.isNotBlank()) {
            "API key is empty."
        }

        println("GEMINI: creating model")

        val model =
            GenerativeModel(
                modelName = "gemini-3.6-flash",
                apiKey = apiKey
            )

        println("GEMINI: model created")

        println("GEMINI: calling generateContent()")

        val response =
            model.generateContent(prompt)

        println("GEMINI: generateContent() returned")

        response.text
            ?: "Gemini returned no text."
    }
}
