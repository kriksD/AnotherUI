package client.stablediffusion

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toAwtImage
import colorConnection
import colorNoConnection
import getImageBitmap
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skiko.toImage
import settings
import java.util.*

object StableDiffusionWebUIClient {
    private val client = HttpClient(CIO) {
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

    fun disconnect() {
        connectionStatus = false
        modelName = null
        onConnectionChange.forEach { it.value.invoke(connectionStatus) }
    }

    fun onConnectionChange(name: String, action: (Boolean) -> Unit) {
        onConnectionChange[name] = action
    }

    fun removeConnectionChangeCheck(name: String) {
        onConnectionChange.remove(name)
    }

    suspend fun checkModelName() {
        try {
            val result = client.get("${createLink()}/options") {
                contentType(ContentType.Application.Json)
            }
            connectionStatus = result.status.value in 200..299

            val map = Json.parseToJsonElement(result.bodyAsText()).jsonObject.toMap()
            modelName = map["sd_model_checkpoint"]?.jsonPrimitive?.content
            if (modelName == null) { connectionStatus = false }

        } catch (e: Exception) {
            connectionStatus = false
            modelName = null
            println("[ERROR] Couldn't receive a stable diffusion model name: ${e.message}\n")
        }
    }

    suspend fun interrogate(image: ImageBitmap): String? {
        return try {
            val result = client.post("${createLink()}/interrogate") {
                contentType(ContentType.Application.Json)
                setBody("{\"image\": \"${image.encodeToBase64()}\", \"model\": \"clip\"}")
                timeout { requestTimeoutMillis = null }
            }

            connectionStatus = result.status.value != 404

            val map = Json.parseToJsonElement(result.bodyAsText()).jsonObject.toMap()
            val description = map["caption"]?.jsonPrimitive?.content?.replace("<error>", "")
            println("===== RESULT =====")
            println("${result.bodyAsText()}\n\n")
            description

        } catch (e: Exception) {
            println("[ERROR] Couldn't interrogate image: ${e.message}\n")
            null
        }
    }

    private fun ImageBitmap.encodeToBase64(): String {
        val bytes = this.toAwtImage().toImage().encodeToData(EncodedImageFormat.PNG)?.bytes
        return Base64.getEncoder().encode(bytes).decodeToString()
    }

    suspend fun generate(prompt: ImagePrompt): ImageBitmap? {
        return try {
            val printJson = Json { prettyPrint = true }

            println("===== PROMPT =====")
            println("${printJson.encodeToString(prompt)}\n\n")

            val imageBytes = generateImage(prompt)
            imageBytes?.let { getImageBitmap(it) }

        } catch (e: Exception) {
            println("[ERROR] Couldn't generate image: ${e.message}\n\n")
            null
        }
    }

    private suspend fun generateImage(prompt: ImagePrompt): ByteArray? {
        return try {
            val result = client.post("${createLink()}/txt2img") {
                contentType(ContentType.Application.Json)
                setBody(Json.encodeToString(prompt))
                timeout { requestTimeoutMillis = null }
            }
            connectionStatus = result.status.value != 404

            val map = Json.parseToJsonElement(result.bodyAsText()).jsonObject.toMap()
            Base64.getDecoder()?.decode(map["images"]?.jsonArray?.firstOrNull()?.jsonPrimitive?.content)

        } catch (e: Exception) { null }
    }

    private fun createLink(): String {
        val link = settings.stableDiffusionLink.removeSuffix("/").removeSuffix("/sdapi")
        return "${if (link.startsWith("http://")) "" else "http://"}$link/sdapi/v1"
    }
}