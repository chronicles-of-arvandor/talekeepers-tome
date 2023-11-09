package net.arvandor.talekeeper.command.character.shelve

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.util.levenshtein
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.Sound.ITEM_BOTTLE_FILL_DRAGONBREATH
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Duration
import java.time.Instant
import java.util.logging.Level.SEVERE
import kotlin.math.min

class TtCharacterShelveCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to shelve a character.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /character shelve [name|id]")
            return true
        }

        val characterName = args.joinToString(" ")

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

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val activeCharacter = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                sender.sendMessage("${RED}You do not have a profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val cooldown = characterService.getShelveCooldown(profile.id)
            if (cooldown > Duration.ZERO) {
                val hours = cooldown.toHours()
                val minutes = cooldown.toMinutesPart()
                val seconds = cooldown.toSecondsPart()
                val cooldownString = buildString {
                    if (hours > 0) append("${hours}h")
                    if (minutes > 0) append("${minutes}m")
                    if (seconds > 0) append("${seconds}s")
                }
                sender.sendMessage("${RED}You cannot shelve another character for $cooldownString.")
                return@asyncTask
            }

            val characters = characterService.getCharacters(profile.id).onFailure {
                sender.sendMessage("${RED}There was an error getting your characters.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val closestMatch = characters.minByOrNull {
                min(
                    it.id.value.levenshtein(characterName),
                    it.name.levenshtein(characterName),
                )
            }
            if (closestMatch == null) {
                sender.sendMessage("${RED}You do not have a character by that name.")
                return@asyncTask
            }

            if (closestMatch.isShelved) {
                sender.sendMessage("${RED}That character is already shelved.")
                return@asyncTask
            }

            if (closestMatch.id == activeCharacter?.id) {
                sender.sendMessage("${RED}You cannot shelve your active character.")
                return@asyncTask
            }

            characterService.save(closestMatch.copy(isShelved = true)).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            characterService.setShelveCooldown(profile.id, Instant.now())

            sender.sendMessage("${GREEN}Shelved ${closestMatch.name}.")
            sender.playSound(sender.location, ITEM_BOTTLE_FILL_DRAGONBREATH, 1.0f, 1.0f)
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
