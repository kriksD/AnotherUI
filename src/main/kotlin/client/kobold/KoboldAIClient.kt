package client.kobold

import androidx.compose.ui.graphics.Color
import character.ACharacter
import client.dropRest
import colorConnection
import colorNoConnection
import gpt2Tokenizer.GlobalTokenizer
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import prompt.Prompt
import prompt.PromptResult
import settings

object KoboldAIClient {
    private val client = HttpClient(OkHttp) {
        install(HttpTimeout)
    }

    private val onConnectionChange = mutableMapOf<String, (Boolean) -> Unit>()
    var connectionStatus = false
        private set(value) {
            field = value
            onConnectionChange.forEach { it.value.invoke(field) }
        }
    var modelName: String? = null
        private set

    val connectionStatusString: String get() = if (connectionStatus) "connection exists" else "no connection"
    val connectionStatusColor: Color get() = if (connectionStatus) colorConnection else colorNoConnection

    fun onConnectionChange(name: String, action: (Boolean) -> Unit) {
        onConnectionChange[name] = action
    }

    fun removeConnectionChangeCheck(name: String) {
        onConnectionChange.remove(name)
    }

    suspend fun checkModelName() {
        try {
            val result = client.get("${createLink()}/model") {
                contentType(ContentType.Application.Json)
            }
            connectionStatus = result.status.value in 200..299

            @Serializable data class Result(val result: String)
            val modelName = Json.decodeFromString<Result>(result.body())
            KoboldAIClient.modelName = modelName.result

            if (KoboldAIClient.modelName == "ReadOnly") {
                connectionStatus = false
            }

        } catch (e: Exception) {
            connectionStatus = false
            modelName = null
            println("[ERROR] Couldn't receive a model name: ${e.message}\n")
        }
    }

    suspend fun countTokens(text: String): Int {
        @Serializable data class Body(val prompt: String)
        @Serializable data class Result(val value: Int)

        try {
            val result = client.post("${createLink().replace("/v1", "/extra")}/tokencount") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(Body(text)))
            }
            connectionStatus = result.status.value in 200..299

            val json = Json { ignoreUnknownKeys = true }
            val count = json.decodeFromString<Result>(result.body())
            return count.value

        } catch (e: Exception) {
            connectionStatus = false
            modelName = null
            println("[ERROR] Couldn't count tokens: ${e.message}\n")
            return -1
        }
    }

    suspend fun generate(prompt: Prompt, character: ACharacter): String? {
        return try {
            val printJson = Json { prettyPrint = true }

            println("===== PROMPT =====")
            println("Tokens: ${GlobalTokenizer.countTokens(prompt.prompt)}")
            println("${printJson.encodeToString(prompt)}\n\n")

            val message = generateAll(prompt)

            println("\n\n===== RESULT =====")
            println("$message\n\n")

            message?.dropRest(character.jsonData.name)

        } catch (e: Exception) {
            println("\n\n===== RESULT =====")
            println("[ERROR] Couldn't generate: ${e.message}\n\n")

            null
        }
    }

    private suspend fun generateAll(prompt: Prompt): String? {
        return try {
            val result = client.post("${createLink()}/generate") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(prompt))
                timeout {
                    requestTimeoutMillis = 600_000
                    socketTimeoutMillis = 600_000
                }
            }
            connectionStatus = result.status.value != 404

            Json.decodeFromString<PromptResult>(result.body()).results.firstOrNull()?.text

        } catch (e: Exception) {
            println(e)
            null
        }
    }

    private fun createLink(): String {
        val link = settings.link.removeSuffix("/").removeSuffix("/api").removeSuffix("/new_ui")
        return "${if (link.startsWith("http")) "" else "http://"}$link/api/v1"
    }
}