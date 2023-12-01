package net.arvandor.talekeeper.command.character.switch

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
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE
import kotlin.math.min

class TtCharacterSwitchCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to switch character.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /character swtich [name|id]")
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

            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                sender.sendMessage("${RED}You do not have a profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val characters = characterService.getCharacters(profile.id).onFailure {
                sender.sendMessage("${RED}There was an error getting your characters.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val closestMatch = characters.minByOrNull { min(it.id.value.levenshtein(characterName), it.name.levenshtein(characterName)) }
            if (closestMatch == null) {
                sender.sendMessage("${RED}You do not have a character by that name.")
                return@asyncTask
            }

            if (closestMatch.isShelved) {
                sender.sendMessage("${RED}You cannot switch to a shelved character.")
                return@asyncTask
            }

            if (closestMatch.minecraftProfileId != null) {
                if (closestMatch.minecraftProfileId == minecraftProfile.id) {
                    sender.sendMessage("${RED}You are already playing as ${closestMatch.name}.")
                } else {
                    val otherMinecraftProfile = minecraftProfileService.getMinecraftProfile(closestMatch.minecraftProfileId).join()
                    sender.sendMessage("${RED}That character is already being played on ${otherMinecraftProfile.name}.")
                }
                return@asyncTask
            }

            characterService.setActiveCharacter(minecraftProfile, closestMatch).onFailure {
                sender.sendMessage("${RED}There was an error setting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            sender.playSound(sender.location, Sound.BLOCK_BELL_RESONATE, 1.0f, 1.0f)
            sender.sendMessage("${GREEN}Switched to ${closestMatch.name}.")
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
