package prompt

import character.ACharacter
import chat.Chat
import chat.Message
import client.kobold.KoboldAIClient
import client.format
import settings
import user

class PromptBuilder {
    private var type: PromptType? = null
    private var complete: Boolean = false
    private var regenerate: Boolean = false
    private var forUser: Boolean = false
    private var character: ACharacter? = null
    private var chat: Chat? = null
    private var systemPrompt: String? = null
    private var additionalMessages: MutableList<Message> = mutableListOf()
    private var pattern: String = """
        {{systemPrompt}}
        {{persona}}
        {{personality}}
        {{scenario}}
        {{userDescription}}
        {{messageExample}}
        {{chat}}
    """.trimIndent()
    private var firstMessageIndex: Int = 0

    var meta: PromptBuilderMeta? = null

    fun type(type: PromptType): PromptBuilder {
        this.type = type
        return this
    }

    fun complete(complete: Boolean = true): PromptBuilder {
        this.complete = complete
        return this
    }

    fun regenerate(regenerate: Boolean = true): PromptBuilder {
        this.regenerate = regenerate
        return this
    }

    fun forUser(forUser: Boolean = true): PromptBuilder {
        this.forUser = forUser
        return this
    }

    fun character(character: ACharacter): PromptBuilder {
        this.character = character
        return this
    }

    fun chat(chat: Chat): PromptBuilder {
        this.chat = chat
        return this
    }

    fun systemPrompt(systemPrompt: String): PromptBuilder {
        this.systemPrompt = systemPrompt
        return this
    }

    fun addAdditionalMessage(message: Message): PromptBuilder {
        this.additionalMessages.add(message)
        return this
    }

    fun pattern(pattern: String): PromptBuilder {
        this.pattern = pattern
        return this
    }

    fun firstMessageIndex(index: Int): PromptBuilder {
        this.firstMessageIndex = index
        return this
    }

    suspend fun build(): Prompt? {
        val prompt = when (type) {
            PromptType.Instruct -> makeInstructPrompt(character, chat)
            PromptType.Chat -> makeChatPrompt(character, chat)
            null -> null
        } ?: return null

        return Prompt(
            prompt = prompt,
            stop_sequence = settings.promptSettings.splitStopSequence,
            use_story = false,
            use_memory = false,
            use_authors_note = false,
            use_world_info = false,
            max_context_length = settings.generating.maxContextLength,
            max_length = settings.generating.maxLength,
            rep_pen = settings.generating.repPen,
            rep_pen_range = settings.generating.repPenRange,
            rep_pen_slope = settings.generating.repPenSlope,
            temperature = settings.generating.temperature,
            tfs = settings.generating.tfs,
            top_a = settings.generating.topA,
            top_k = settings.generating.topK,
            top_p = settings.generating.topP,
            min_p = settings.generating.minP,
            typical = settings.generating.typical,
            smoothing_factor = settings.generating.smoothingFactor,
            dynatemp_range = settings.generating.dynatempRange,
            sampler_order = settings.generating.samplerOrder,
            use_default_badwordsids = false,
            seed = settings.generating.seed,
        )
    }

    private suspend fun makeInstructPrompt(character: ACharacter?, chat: Chat?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern

        systemPrompt?.let { spr ->
            val systemInstruct = settings.promptSettings.systemInstruct.replace("\\n", "\n")

            prompt = prompt.replace(
                "{{systemPrompt}}",
                if (spr.isNotBlank())
                    if (systemInstruct.contains("{{prompt}}"))
                        systemInstruct.replace("{{prompt}}", spr)
                    else
                        "$systemInstruct$spr"
                else ""
            )
        } ?: run { prompt.replace("{{systemPrompt}}", "") }

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotBlank()) "${character.jsonData.name}'s Persona: $des" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotBlank()) "Personality: $per" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotBlank()) "Scenario: $sce" else ""
            )
        }

        prompt = prompt.replace(
            "{{userDescription}}",
            if (user.description.isNotBlank()) "${user.name}'s description: ${user.description}" else ""
        )

        character.jsonData.messageExample.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                mex.ifBlank { "" }
            )
        }

        val mergedMessages = chat.messages + additionalMessages
        val maxContextLength = settings.generating.maxContextLength
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        var messagesPrompt = ""
        val lastIndex = if (regenerate) mergedMessages.lastIndex - 1 else mergedMessages.lastIndex

        for (messageIndex in lastIndex downTo firstMessageIndex) {
            val message = mergedMessages[messageIndex]

            val messageTokenCount = KoboldAIClient.countTokens(message.content)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) {
                meta = PromptBuilderMeta(messageIndex + 1)
                break
            }

            val instructMessage = if (messageIndex == lastIndex && complete) {
                message.lastStringInstruct
            } else {
                message.stringInstruct
            }
            messagesPrompt = "$instructMessage\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                if (mpr.isNotEmpty()) mpr.dropLast(1) else ""
            )
        }

        if (!complete) {
            val instruct = (if (forUser) settings.promptSettings.userInstruct else settings.promptSettings.modelInstruct)
                .replace("\\n", "\n")
                .substringBefore("{{prompt}}")
                .trimEnd { it == ' ' }

            prompt += "\n$instruct"
        }

        return prompt.format(character)
    }

    private suspend fun makeChatPrompt(character: ACharacter?, chat: Chat?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern.replace("{{systemPrompt}}", "")

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotBlank()) "${character.jsonData.name}'s Persona: $des" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotBlank()) "Personality: $per" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotBlank()) "Scenario: $sce" else ""
            )
        }

        prompt = prompt.replace(
            "{{userDescription}}",
            if (user.description.isNotBlank()) "${user.name}'s description: ${user.description}" else ""
        )

        character.jsonData.messageExample.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                mex.ifBlank { "" }
            )
        }

        val mergedMessages = chat.messages + additionalMessages
        val maxContextLength = settings.generating.maxContextLength
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        var messagesPrompt = ""
        val lastIndex = if (regenerate) mergedMessages.lastIndex - 1 else mergedMessages.lastIndex

        for (messageIndex in lastIndex downTo firstMessageIndex) {
            val message = mergedMessages[messageIndex]

            val messageTokenCount = KoboldAIClient.countTokens(message.content)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) {
                meta = PromptBuilderMeta(messageIndex + 1)
                break
            }

            messagesPrompt = "${message.string}\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                mpr.ifEmpty { "" }
            )
        }

        if (!complete) {
            prompt += "\n${if (forUser) user.name else character.jsonData.name}:"
        }

        return prompt.format(character)
    }
}