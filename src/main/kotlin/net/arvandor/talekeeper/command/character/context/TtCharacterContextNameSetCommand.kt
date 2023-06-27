package net.arvandor.talekeeper.command.character.context

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.conversation.ErrorMessagePrompt
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
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

class TtCharacterContextNameSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(NamePrompt())
        .withEscapeSequence("cancel")
        .withLocalEcho(false)
        .thatExcludesNonPlayersWithMessage("${RED}You must be a player to perform this command.")
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage("${RED}Operation cancelled.")
                }
            }
        }

    private inner class NamePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext) = "What is your name? (Type \"cancel\" to cancel)"

        override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return ErrorMessagePrompt("${RED}You must be a player to perform this command.")
            if (input == null) return ErrorMessagePrompt("${RED}You must enter a name.")

            val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
                ?: return ErrorMessagePrompt("${RED}No Minecraft profile service was found. Please contact an admin.")

            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
                ?: return ErrorMessagePrompt("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")

            val characterService = context.getSessionData("characterService") as? TtCharacterService ?: return END_OF_CONVERSATION

            asyncTask(plugin) {
                val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                    plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                    syncTask(plugin) {
                        conversable.sendMessage("${RED}An error occurred while getting your character creation context.")
                    }
                    return@asyncTask
                }
                if (ctx == null) {
                    syncTask(plugin) {
                        conversable.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                    }
                    return@asyncTask
                }

                val updatedCtx = characterService.save(ctx.copy(name = input)).onFailure {
                    plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                    syncTask(plugin) {
                        conversable.sendMessage("${RED}An error occurred while saving your character creation context.")
                    }
                    return@asyncTask
                }

                syncTask(plugin) {
                    updatedCtx.display(conversable)
                }
            }

            return END_OF_CONVERSATION
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

        val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
            sender.sendMessage("${RED}An error occurred while getting your character creation context.")
            plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
            return true
        }

        if (ctx == null) {
            sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
            return true
        }

        if (args.isNotEmpty()) {
            val updatedCtx = characterService.save(ctx.copy(name = args.joinToString(" "))).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return true
            }

            updatedCtx.display(sender)
        } else {
            val conversation = conversationFactory.buildConversation(sender)
            conversation.context.setSessionData("characterService", characterService)
            conversation.begin()
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
