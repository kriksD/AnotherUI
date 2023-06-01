package client

import character.ACharacter
import character.chat.Chat
import character.chat.Message
import character.chat.legacyChat.LegacyChat
import gpt2Tokenizer.GlobalTokenizer
import kotlinx.serialization.Serializable
import settings
import user

@Serializable
data class PromptResult(
    val results: List<LineResult>,
)

@Serializable
data class LineResult(
    val text: String,
)

@Serializable
data class Prompt(
    val prompt: String,
    val use_story: Boolean,
    val use_memory: Boolean,
    val use_authors_note: Boolean,
    val use_world_info: Boolean,
    val max_context_length: Int,
    val max_length: Int,
    val rep_pen: Float,
    val rep_pen_range: Int,
    val rep_pen_slope: Float,
    val temperature: Float,
    val tfs: Float,
    val top_a: Float,
    val top_k: Int,
    val top_p: Float,
    val typical: Float,
    val sampler_order: List<Int>,
) {
    companion object {
        fun createPrompt(character: ACharacter, chat: Chat, message: String): Prompt {
            var prompt = ""

            if (character.jsonData.description.isNotEmpty()) {
                prompt += "${character.jsonData.name}'s Persona: ${character.jsonData.description.formatAnything(character)}\n"
            }

            if (character.jsonData.personality.isNotEmpty()) {
                prompt += "Personality: ${character.jsonData.personality.formatAnything(character)}\n"
            }

            if (character.jsonData.scenario.isNotEmpty()) {
                prompt += "Scenario: ${character.jsonData.scenario.formatAnything(character)}\n"
            }

            if (character.jsonData.mes_example.isNotEmpty()) {
                prompt += character.jsonData.mes_example.formatExamples(character) + "\n"
            }

            val maxContextLength = settings.generating.max_context_length
            var tokensLeft = maxContextLength - GlobalTokenizer.countTokens(prompt)
            var messageIndex = chat.messages.lastIndex
            var messagesPrompt = ""

            while (messageIndex >= 0) {
                val mes = chat.messages.elementAt(messageIndex)
                val messageTokenCount = GlobalTokenizer.countTokens(mes.mes)

                if (tokensLeft - messageTokenCount < 0) break

                messagesPrompt = mes.string().formatAnything(character) + "\n" + messagesPrompt
                tokensLeft = maxContextLength - GlobalTokenizer.countTokens(prompt + messagesPrompt)
                messageIndex--
            }

            if (messagesPrompt.isNotEmpty()) {
                prompt += messagesPrompt
            }

            if (message.isNotEmpty()) prompt += "${if (settings.use_username) user.name else "You"}: $message\n"
            prompt += "${character.jsonData.name}:"

            return Prompt(
                prompt,
                use_story = false,
                use_memory = false,
                use_authors_note = false,
                use_world_info = false,
                max_context_length = settings.generating.max_context_length,
                max_length = settings.generating.max_length,
                rep_pen = settings.generating.rep_pen,
                rep_pen_range = settings.generating.rep_pen_range,
                rep_pen_slope = settings.generating.rep_pen_slope,
                temperature = settings.generating.temperature,
                tfs = settings.generating.tfs,
                top_a = settings.generating.top_a,
                top_k = settings.generating.top_k,
                top_p = settings.generating.top_p,
                typical = settings.generating.typical,
                sampler_order = listOf(6, 0, 1, 2, 3, 4, 5),
            )
        }

        private fun Message.string(): String = "${if (settings.use_username && is_user) user.name else name}: $mes"
    }
}