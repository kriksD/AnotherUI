package client

import character.ACharacter
import character.chat.Chat
import character.chat.Message
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
        fun createPrompt(character: ACharacter, chat: Chat, lastMessageIndex: Int = 0): Prompt {
            return createPromptWithByproduct(character, chat, lastMessageIndex).first
        }

        fun createPromptWithByproduct(character: ACharacter, chat: Chat, lastMessageIndex: Int = 0): Pair<Prompt, PromptByproduct> {
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
            var tokensLeft = maxContextLength - GlobalTokenizer.countTokens(prompt) - 32
            println("tokensLeft: $tokensLeft")
            var messagesPrompt = ""
            val messageTokens = mutableMapOf<Int, Int>()

            for (messageIndex in chat.messages.lastIndex downTo lastMessageIndex) {
                val mes = chat.messages[messageIndex]
                if (!mes.is_user && mes.swipes != null && mes.swipe_id != null) continue
                val messageTokenCount = GlobalTokenizer.countTokens(mes.mes)

                tokensLeft -= messageTokenCount
                if (tokensLeft < 0) break

                messageTokens[messageIndex] = messageTokenCount

                messagesPrompt = mes.string().formatAnything(character) + "\n" + messagesPrompt
            }

            if (messagesPrompt.isNotEmpty()) {
                prompt += messagesPrompt
            }

            prompt += "${character.jsonData.name}:"

            return Pair(
                Prompt(
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
                ),
                PromptByproduct(
                    messageTokens.keys.minOrNull() ?: 0,
                    messageTokens,
                ),
            )
        }

        private fun Message.string(): String = "${if (settings.use_username && is_user) user.name else name}: $mes"
    }
}