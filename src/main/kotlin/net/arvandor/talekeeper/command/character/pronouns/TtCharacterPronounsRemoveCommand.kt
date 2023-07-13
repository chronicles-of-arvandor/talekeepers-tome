package net.arvandor.talekeeper.command.character.pronouns

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCharacterPronounsRemoveCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

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

        val pronounService = Services.INSTANCE[TtPronounService::class.java]
        if (pronounService == null) {
            sender.sendMessage("${RED}No pronoun service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            val pronounSetId = TtPronounSetId(args[0])
            if (!character.pronouns.containsKey(pronounSetId)) {
                sender.sendMessage("${RED}Invalid pronoun set ID.")
                return@asyncTask
            }

            val pronounSet = pronounService.get(pronounSetId)
            if (pronounSet == null) {
                sender.sendMessage("${RED}Invalid pronoun set ID.")
                return@asyncTask
            }

            val updatedCharacter = characterService.save(
                character.copy(
                    pronouns = character.pronouns - pronounSetId,
                ),
            ).onFailure {
                sender.sendMessage("${RED}Failed to save character. Please contact an admin.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage(
                "${ChatColor.GRAY}================================",
                "${ChatColor.GREEN}${pronounSet.name} pronouns removed.",
                "${ChatColor.GRAY}================================",
            )
            updatedCharacter.display(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val pronounService = Services.INSTANCE[TtPronounService::class.java] ?: return emptyList()
        return when {
            args.isEmpty() -> pronounService.getAll()
                .onFailure { return emptyList() }
                .map { it.id.value }
            args.size == 1 -> pronounService.getAll()
                .onFailure { return emptyList() }
                .map { it.id.value }
                .filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
