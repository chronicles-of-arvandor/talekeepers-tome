package net.arvandor.talekeeper.command.choice.select

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtChoiceSelectCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        if (args.size < 2) {
            sender.sendMessage("${RED}Usage: /choice view [choice id] [option id]")
            return true
        }

        val choiceId = args[0]

        val choice = choiceService.getChoice(TtChoiceId(choiceId))
        if (choice == null) {
            sender.sendMessage("${RED}No choice by that ID was found.")
            return true
        }

        val optionId = args[1]

        val option = choice.getOption(TtChoiceOptionId(optionId))
        if (option == null) {
            sender.sendMessage("${RED}No option by that ID was found.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            val unmetChoicePrerequisites = choice.prerequisites.filter { !it.isMetBy(character) }
            if (unmetChoicePrerequisites.isNotEmpty()) {
                sender.sendMessage("${RED}You do not meet the following prerequisites for this choice:")
                unmetChoicePrerequisites.forEach { prerequisite ->
                    sender.sendMessage("$RED• ${prerequisite.name}")
                }
            }

            val unmetOptionPrerequisites = option.prerequisites.filter { !it.isMetBy(character) }
            if (unmetOptionPrerequisites.isNotEmpty()) {
                sender.sendMessage("${RED}You do not meet the following prerequisites for this option:")
                unmetOptionPrerequisites.forEach { prerequisite ->
                    sender.sendMessage("$RED• ${prerequisite.name}")
                }
            }

            choiceService.setChosenOption(character.id, choice.id, option.id).onFailure {
                sender.sendMessage("${RED}An error occurred while setting your option.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${ChatColor.GREEN}Option selected.")
            val pendingChoices = choiceService.getPendingChoices(character)
            if (pendingChoices.isNotEmpty()) {
                syncTask(plugin) {
                    sender.performCommand("choice list")
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
