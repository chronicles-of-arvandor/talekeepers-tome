package net.arvandor.talekeeper.command.experience

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.recover
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.ServiceFailureType
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.logging.Level.SEVERE

class TtExperienceAddCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.commands.experience.add")) {
            sender.sendMessage("${RED}You do not have permission to use this command.")
            return true
        }

        if (args.size < 2) {
            sender.sendMessage("${RED}Usage: /experience add [player] [experience]")
            return true
        }

        val target = plugin.server.getOfflinePlayer(args[0])

        val experience = args[1].toIntOrNull()
        if (experience == null) {
            sender.sendMessage("${RED}Experience must be a number.")
            return true
        }

        if (experience <= 0) {
            sender.sendMessage("${RED}Experience must be 1 or greater.")
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
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have an active character.")
                return@asyncTask
            }

            val (originalCharacter, updatedCharacter) = addExperience(sender, target, characterService, experienceService, character, experience, 0).onFailure {
                sender.sendMessage("${RED}An error occurred while adding experience to ${if (sender == target) "your" else "${target.name}'s"} character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${ChatColor.GREEN}Granted ${updatedCharacter.name} ${updatedCharacter.experience - originalCharacter.experience} experience.")

            val onlineTarget = target.player
            if (onlineTarget != null) {
                experienceService.handleExperienceChange(onlineTarget, originalCharacter.experience, updatedCharacter.experience)
            }
        }
        return true
    }

    private fun addExperience(
        sender: CommandSender,
        target: OfflinePlayer,
        characterService: TtCharacterService,
        experienceService: TtExperienceService,
        character: TtCharacter,
        experience: Int,
        failures: Int,
    ): Result4k<Pair<TtCharacter, TtCharacter>, ServiceFailure> {
        val maxExperience = experienceService.getTotalExperienceForLevel(experienceService.getMaxLevel())
        val result = characterService.save(
            character.copy(
                experience = (character.experience + experience).coerceAtMost(maxExperience),
            ),
        )
        if (result is Failure && result.reason.type == ServiceFailureType.CONFLICT && failures < 10) {
            val updatedCharacter = characterService.getCharacter(character.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return it
            }
            if (updatedCharacter == null) {
                sender.sendMessage("${RED}An error occurred while getting ${if (sender == target) "your" else "${target.name}'s"} active character.")
                return result
            }
            return result.recover {
                addExperience(sender, target, characterService, experienceService, updatedCharacter, experience, failures + 1)
            }
        } else {
            return result.map { character to it }
        }
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
