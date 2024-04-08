package net.arvandor.talekeeper.command.choice.reset

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtChoiceResetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.commands.choice.reset")) {
            sender.sendMessage("${RED}You do not have permission to perform this command.")
            return true
        }

        if (args.size < 2) {
            sender.sendMessage("${RED}Usage: /choice reset [player] [choice id]")
            return true
        }

        val target = plugin.server.getOfflinePlayer(args[0])
        if (!target.isOnline && !target.hasPlayedBefore()) {
            sender.sendMessage("${RED}Could not find a player by that name.")
            return true
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
            return true
        }

        val choiceId = TtChoiceId(args[1])
        val choice = choiceService.getChoice(choiceId)
        if (choice == null) {
            sender.sendMessage("${RED}No choice by that ID was found.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}${if (target == sender) "You do" else "${target.name} does"} not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (target == sender) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}${if (target == sender) "You do" else "${target.name} does"} not have an active character.")
                return@asyncTask
            }

            choiceService.setChosenOption(character.id, choiceId, null).onFailure {
                sender.sendMessage("${RED}An error occurred while resetting the choice.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${GREEN}Choice reset.")
            if (sender is Player) {
                syncTask(plugin) {
                    sender.performCommand("choice view ${choice.id.value} ${target.name}")
                }
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        return when {
            args.isEmpty() -> plugin.server.offlinePlayers.mapNotNull { it.name }
            args.size == 1 -> plugin.server.offlinePlayers.mapNotNull { it.name }
                .filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
