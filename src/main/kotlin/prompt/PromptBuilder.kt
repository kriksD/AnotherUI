package prompt

import character.ACharacter
import character.chat.Chat
import client.KoboldAIClient
import client.Prompt
import client.formatAnything
import client.formatExamples
import settings
import user

class PromptBuilder {
    private var type: PromptType? = null
    private var complete: Boolean = false
    private var forUser: Boolean = false
    private var character: ACharacter? = null
    private var chat: Chat? = null
    private var systemPrompt: String? = null
    private var pattern: String = """
        {{systemPrompt}}
        {{persona}}
        {{personality}}
        {{scenario}}
        {{messageExample}}
        {{chat}}
    """.trimIndent()

    fun type(type: PromptType): PromptBuilder {
        this.type = type
        return this
    }

    fun complete(complete: Boolean = false): PromptBuilder {
        this.complete = complete
        return this
    }

    fun forUser(forUser: Boolean = false): PromptBuilder {
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

    fun pattern(pattern: String): PromptBuilder {
        this.pattern = pattern
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

    private suspend fun makeInstructPrompt(character: ACharacter?, chat: Chat?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern

        systemPrompt?.let { spr ->
            prompt = prompt.replace(
                "{{systemPrompt}}",
                if (spr.isNotEmpty()) "<|system|>${spr.formatAnything(character)}" else ""
            )
        } ?: run { prompt.replace("{{systemPrompt}}", "") }

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotEmpty()) "${character.jsonData.name}'s Persona: ${des.formatAnything(character)}" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotEmpty()) "Personality: ${per.formatAnything(character)}" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotEmpty()) "Scenario: ${sce.formatAnything(character)}" else ""
            )
        }

        character.jsonData.mes_example.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                if (mex.isNotEmpty()) mex.formatExamples(character) else ""
            )
        }

        val maxContextLength = settings.generating.max_context_length
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        println("tokensLeft: $tokensLeft")
        var messagesPrompt = ""

        for (messageIndex in chat.messages.lastIndex downTo 0) {
            val message = chat.messages[messageIndex]
            //if (!message.is_user && message.swipes != null && message.swipe_id != null) continue
            val messageTokenCount = 100//KoboldAIClient.countTokens(message.mes)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) break

            messagesPrompt = "${message.stringInstruct.formatAnything(character)}\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                if (mpr.isNotEmpty()) mpr.dropLast(1).formatExamples(character) else ""
            )
        }

        if (!complete) {
            prompt += "\n${if (forUser) "<|user|>" else "<|model|>"}"
        }

        return prompt
    }

    private suspend fun makeChatPrompt(character: ACharacter?, chat: Chat?): String? {
        character ?: return null
        chat ?: return null

        var prompt = pattern.replace("{{systemPrompt}}", "")

        character.jsonData.description.let { des ->
            prompt = prompt.replace(
                "{{persona}}",
                if (des.isNotEmpty()) "${character.jsonData.name}'s Persona: ${des.formatAnything(character)}" else ""
            )
        }

        character.jsonData.personality.let { per ->
            prompt = prompt.replace(
                "{{personality}}",
                if (per.isNotEmpty()) "Personality: ${per.formatAnything(character)}" else ""
            )
        }

        character.jsonData.scenario.let { sce ->
            prompt = prompt.replace(
                "{{scenario}}",
                if (sce.isNotEmpty()) "Scenario: ${sce.formatAnything(character)}" else ""
            )
        }

        character.jsonData.mes_example.let { mex ->
            prompt = prompt.replace(
                "{{messageExample}}",
                if (mex.isNotEmpty()) mex.formatExamples(character) else ""
            )
        }

        val maxContextLength = settings.generating.max_context_length
        val currentTokenCount = KoboldAIClient.countTokens(prompt)
        if (currentTokenCount == -1) return null
        var tokensLeft = maxContextLength - currentTokenCount - 32
        println("tokensLeft: $tokensLeft")
        var messagesPrompt = ""

        for (messageIndex in chat.messages.lastIndex downTo 0) {
            val message = chat.messages[messageIndex]
            //if (!message.is_user && message.swipes != null && message.swipe_id != null) continue
            val messageTokenCount = KoboldAIClient.countTokens(message.mes)
            if (messageTokenCount == -1) return null

            tokensLeft -= messageTokenCount
            if (tokensLeft < 0) break

            messagesPrompt = "${message.string.formatAnything(character)}\n$messagesPrompt"
        }

        messagesPrompt.let { mpr ->
            prompt = prompt.replace(
                "{{chat}}",
                if (mpr.isNotEmpty()) mpr.formatExamples(character) else ""
            )
        }

        if (!complete) {
            prompt += "\n${if (forUser) { if (settings.use_username) user.name else "You" } else character.jsonData.name}:"
        }

        return prompt
    }
}