package net.arvandor.talekeeper.command.character.create

import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterCreationRequest
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.staff.TtStaffService
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.logging.Level.SEVERE

class TtCharacterCreateCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val profile = minecraftProfile.profile as? RPKProfile
        if (profile == null) {
            sender.sendMessage("${RED}You do not have a profile. Please try relogging, or contact an admin if the error persists.")
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

        val staffService = Services.INSTANCE[TtStaffService::class.java]
        if (staffService == null) {
            sender.sendMessage("${RED}No staff service was found. Please contact an admin.")
            return true
        }

        val staff = staffService.getStaff()
        val onlineStaff = staff.mapNotNull { it.player }
        val offlineStaff = staff.filter { !it.isOnline }
        val offlineStaffProfiles = offlineStaff.mapNotNull { staffPlayer ->
            val staffMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(staffPlayer)
            staffMinecraftProfile?.profile as? RPKProfile
        }

        val unshelvedCharacterLimit = characterService.getUnshelvedCharacterLimit(minecraftProfile)

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx != null) {
                sender.sendMessage("${RED}You are already creating a character.")
                return@asyncTask
            }

            val characters = characterService.getCharacters(profile.id).onFailure {
                sender.sendMessage("${RED}There was an error getting your characters.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (characters.count { !it.isShelved } >= unshelvedCharacterLimit) {
                sender.sendMessage("${RED}You have the maximum amount of active characters. Please shelve one and try again.")
                // TODO monetisation opportunity! link people to the store page!
                return@asyncTask
            }

            val request = characterService.getCreationRequest(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation request.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (request != null) {
                sender.sendMessage("${RED}You already have a pending request to create a character.")
                return@asyncTask
            }

            val newRequest = characterService.save(
                TtCharacterCreationRequest(
                    minecraftProfile.id,
                    Instant.now(),
                ),
            ).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character creation request.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val formattedDate = DateTimeFormatter.ISO_INSTANT.format(newRequest.requestTime)
            onlineStaff.forEach { staffPlayer ->
                staffPlayer.spigot().sendMessage(
                    TextComponent("${minecraftProfile.name} requested to create a new character: "),
                    TextComponent("Accept").apply {
                        color = GREEN
                        hoverEvent = HoverEvent(
                            SHOW_TEXT,
                            Text("Click here to accept this request."),
                        )
                        clickEvent = ClickEvent(
                            RUN_COMMAND,
                            "/character requests accept ${minecraftProfile.minecraftUUID}",
                        )
                    },
                    TextComponent(" "),
                    TextComponent("Decline").apply {
                        color = RED
                        hoverEvent = HoverEvent(
                            SHOW_TEXT,
                            Text("Click here to decline this request."),
                        )
                        clickEvent = ClickEvent(
                            RUN_COMMAND,
                            "/character requests decline ${minecraftProfile.minecraftUUID}",
                        )
                    },
                )
            }

            offlineStaffProfiles.forEach { staffProfile ->
                notificationService.createNotification(
                    staffProfile,
                    "Character creation request",
                    "${minecraftProfile.name} requested to create a new character at $formattedDate. Use /character requests to view pending character creation requests.",
                )
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
