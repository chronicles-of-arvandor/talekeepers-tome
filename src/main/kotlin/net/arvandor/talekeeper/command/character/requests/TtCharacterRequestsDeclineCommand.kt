package net.arvandor.talekeeper.command.character.requests

import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.*
import java.util.logging.Level.SEVERE

class TtCharacterRequestsDeclineCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.commands.character.requests")) {
            sender.sendMessage("${RED}You do not have permission to use this command.")
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

        val notificationService = Services.INSTANCE[RPKNotificationService::class.java]
        if (notificationService == null) {
            sender.sendMessage("${RED}No notification service was found. Please contact an admin.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /character requests decline [uuid]")
            return true
        }

        val uuid = try {
            UUID.fromString(args[0])
        } catch (exception: IllegalArgumentException) {
            sender.sendMessage("${RED}Invalid UUID.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(uuid).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}No Minecraft profile was found for that UUID.")
                return@asyncTask
            }

            val characterCreationRequest = characterService.getCreationRequest(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting the character creation request.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (characterCreationRequest == null) {
                sender.sendMessage("${RED}No character creation request was found for that player.")
                return@asyncTask
            }

            val characterCreationContext = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting the character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (characterCreationContext != null) {
                sender.sendMessage("${RED}That player is already creating a character.")
                return@asyncTask
            }

            characterService.deleteCreationRequest(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while deleting the character creation request.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val profile = minecraftProfile.profile as? RPKProfile
            if (profile != null) {
                notificationService.createNotification(
                    profile,
                    "${RED}Character creation request declined",
                    "Your character creation request was declined by ${sender.name}.",
                )
            }

            sender.sendMessage("${GREEN}Character creation request declined.")
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
