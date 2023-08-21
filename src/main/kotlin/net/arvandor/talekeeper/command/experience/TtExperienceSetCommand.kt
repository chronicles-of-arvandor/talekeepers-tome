package net.arvandor.talekeeper.command.experience

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.logging.Level.SEVERE

class TtExperienceSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.commands.experience.set")) {
            sender.sendMessage("${RED}You do not have permission to use this command.")
            return true
        }
        val target = plugin.server.getOfflinePlayer(args[0])

        val experience = args[1].toIntOrNull()
        if (experience == null) {
            sender.sendMessage("Experience must be a number.")
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

        val experienceService = Services.INSTANCE[TtExperienceService::class.java]
        if (experienceService == null) {
            sender.sendMessage("${RED}No experience service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}That player does not have a Minecraft profile. Have they logged in before?")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have an active character.")
                return@asyncTask
            }

            val maxExperience = experienceService.getTotalExperienceForLevel(experienceService.getMaxLevel())

            val updatedCharacter = characterService.save(
                character.copy(
                    experience = experience.coerceIn(0, maxExperience),
                ),
            ).onFailure {
                sender.sendMessage("${RED}An error occurred while saving the character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${GREEN}Set ${updatedCharacter.name}'s experience to ${updatedCharacter.experience}.")

            val onlineTarget = target.player
            if (onlineTarget != null) {
                experienceService.handleExperienceChange(onlineTarget, character.experience, updatedCharacter.experience)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> plugin.server.offlinePlayers.mapNotNull(OfflinePlayer::getName)
        args.size == 1 -> plugin.server.offlinePlayers.mapNotNull(OfflinePlayer::getName).filter { it.startsWith(args[0], ignoreCase = true) }
        args.size == 2 -> {
            val experienceService = Services.INSTANCE[TtExperienceService::class.java]
            val maxLevel = experienceService.getMaxLevel()
            val maxExp = experienceService.getTotalExperienceForLevel(maxLevel)
            (1..maxExp).map(Int::toString).filter { it.startsWith(args[1], ignoreCase = true) }
        }
        else -> emptyList()
    }
}
