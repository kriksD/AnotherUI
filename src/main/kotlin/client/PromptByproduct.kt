package client

/**
 * @param messagesTokenCounts the key is index of message and the value is token count.
 */
data class PromptByproduct(
    val lastMessageIndex: Int,
    val messagesTokenCounts: Map<Int, Int>,
) {
    fun lastIndexWithRemovedBuffer(buffer: Int): Int {
        var tokens = 0
        messagesTokenCounts.keys.sorted().forEach { key ->
            tokens += messagesTokenCounts[key] ?: 0

            if (tokens >= buffer) {
                return key
            }
        }

        return 0
    }
}