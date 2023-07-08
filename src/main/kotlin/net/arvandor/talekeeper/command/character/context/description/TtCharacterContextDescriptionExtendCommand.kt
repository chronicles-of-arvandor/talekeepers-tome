package net.arvandor.talekeeper.command.character.context.description

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.conversation.ErrorMessagePrompt
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCharacterContextDescriptionExtendCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(DescriptionPrompt())
        .withEscapeSequence("cancel")
        .withLocalEcho(true)
        .thatExcludesNonPlayersWithMessage("${RED}You must be a player to perform this command.")
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage("${RED}Operation cancelled.")
                }
            }
        }

    private inner class DescriptionPrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext) = "Continue writing, or type \"end\" to finish. Cancel setting description with \"cancel\"."

        override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return ErrorMessagePrompt("${RED}You must be a player to perform this command.")
            if (input == null) return ErrorMessagePrompt("${RED}You must enter a description.")

            if (input == "end") {
                val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
                    ?: return ErrorMessagePrompt("${RED}No Minecraft profile service was found. Please contact an admin.")

                val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
                    ?: return ErrorMessagePrompt("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")

                val characterService =
                    context.getSessionData("characterService") as? TtCharacterService ?: return END_OF_CONVERSATION

                asyncTask(plugin) {
                    val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                        plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                        conversable.sendMessage("${RED}An error occurred while getting your character creation context.")
                        return@asyncTask
                    }
                    if (ctx == null) {
                        conversable.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                        return@asyncTask
                    }

                    val updatedCtx = characterService.save(ctx.copy(description = context.getSessionData("description") as? String ?: "")).onFailure {
                        plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                        conversable.sendMessage("${RED}An error occurred while saving your character creation context.")
                        return@asyncTask
                    }

                    conversable.sendMessage(
                        "$GRAY================================",
                        "${GREEN}Description set.",
                        "$GRAY================================",
                    )
                    updatedCtx.display(conversable)
                }

                return END_OF_CONVERSATION
            } else {
                context.setSessionData("description", ((context.getSessionData("description") as? String)?.plus(" ") ?: "") + input)
                return DescriptionPrompt()
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            if (args.isNotEmpty()) {
                val updatedCtx = characterService.save(ctx.copy(description = ctx.description + " " + args.joinToString(" "))).onFailure {
                    sender.sendMessage("${RED}An error occurred while saving your character creation context.")
                    plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                sender.sendMessage(
                    "$GRAY================================",
                    "${GREEN}Description set.",
                    "$GRAY================================",
                )
                updatedCtx.display(sender)
            } else {
                syncTask(plugin) {
                    if (sender.isConversing) {
                        sender.sendRawMessage("${RED}Please finish your current action before attempting to set your description.")
                        return@syncTask
                    }

                    val conversation = conversationFactory.buildConversation(sender)
                    conversation.context.setSessionData("characterService", characterService)
                    conversation.context.setSessionData("description", ctx.description)
                    conversation.begin()
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = emptyList<String>()
}
