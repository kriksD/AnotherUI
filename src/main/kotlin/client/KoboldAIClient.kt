package client

import androidx.compose.ui.graphics.Color
import character.ACharacter
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
            this.modelName = modelName.result

            if (this.modelName == "ReadOnly") {
                connectionStatus = false
            }

        } catch (e: Exception) {
            connectionStatus = false
            modelName = null
            println("[ERROR] Couldn't receive a model name: ${e.message}\n")
        }
    }

    suspend fun generate(prompt: Prompt, character: ACharacter): String? {
        return try {
            val printJson = Json { prettyPrint = true }

            println("===== PROMPT =====")
            println("Tokens: ${GlobalTokenizer.countTokens(prompt.prompt)}")
            println("${printJson.encodeToString(prompt)}\n\n")

            val message = if (settings.multi_gen.enabled) {
                generateNext(prompt, GeneratedData("", prompt.max_length), character)?.message
            } else {
                generateAll(prompt)
            }

            println("\n\n===== RESULT =====")
            println("$message\n\n")

            message?.dropRest(character.jsonData.name)

        } catch (e: Exception) {
            println("\n\n===== RESULT =====")
            println("[ERROR] Couldn't generate: ${e.message}\n\n")

            null
        }
    }

    private suspend fun generateNext(prompt: Prompt, data: GeneratedData, character: ACharacter): GeneratedData? {
        try {
            val tokensToGenerate = data.tokensToGenerate()
            println("Generating next $tokensToGenerate tokens... (${data.tokensLeft} left)")

            val message = generateAll(prompt.copy(prompt = prompt.prompt + data.message, max_length = tokensToGenerate))
            val newData = message?.let { data.nextData(it, character.jsonData.name, tokensToGenerate) } ?: return data
            printData(data, message)

            if (newData.end) return newData
            return generateNext(prompt, newData, character) ?: return newData

        } catch (e: Exception) {
            println("[ERROR] Couldn't generate: ${e.message}\n\n")
            return null
        }
    }

    private fun printData(data: GeneratedData, message: String) {
        println("Message generated (${GlobalTokenizer.countTokens(message)} tokens, step ${data.tries}): "
                + message.replace("\n", "\\n").replace("\r", "\\r")
        )
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