package net.arvandor.talekeeper.conversation

import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.MessagePrompt
import org.bukkit.conversations.Prompt

class ErrorMessagePrompt(val message: String) : MessagePrompt() {
    override fun getPromptText(context: ConversationContext) = message
    override fun getNextPrompt(context: ConversationContext): Prompt? = END_OF_CONVERSATION
}
