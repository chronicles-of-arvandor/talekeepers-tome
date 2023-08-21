package net.arvandor.talekeeper.command.experience

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE
import kotlin.math.roundToInt

class TtExperienceViewCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val target = if (args.isEmpty() && sender is Player) {
            sender
        } else if (sender.hasPermission("talekeeper.commands.experience.view.other")) {
            plugin.server.getOfflinePlayer(args[0])
        } else {
            sender.sendMessage("${RED}You must specify a player when using this command from console.")
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
            sender.sendMessage("No experience service was found. Please contact an admin.")
            return true
        }

        val classService = Services.INSTANCE[TtClassService::class.java]

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}That player does not have a Minecraft profile. Have they logged in before?")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${target.name}'s active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have an active character.")
                return@asyncTask
            }

            val experience = character.experience
            val level = experienceService.getLevelAtExperience(experience)
            val experienceSincePreviousLevel = experience - experienceService.getTotalExperienceForLevel(level)
            val experienceForNextLevel = experienceService.getExperienceForLevel(level + 1)
            val totalExperienceBars = 20
            val filledExperienceBars = ((experienceSincePreviousLevel.toDouble() / experienceForNextLevel.toDouble()) * totalExperienceBars.toDouble()).roundToInt()
            val unfilledExperienceBars = totalExperienceBars - filledExperienceBars
            sender.sendMessage("$GRAY=== ${WHITE}${character.name} $GRAY===")
            sender.sendMessage("Lv$level")
            sender.sendMessage(
                WHITE.toString() + character.classes.mapKeys { (classId, _) -> classService.getClass(classId) }
                    .map { (clazz, classInfo) ->
                        "Lv${classInfo.level} ${clazz?.name} ${if (classInfo.subclassId != null) "(${clazz?.getSubClass(classInfo.subclassId)?.name})" else ""}"
                    }.joinToString(", "),
            )
            if (level > character.classes.map { (_, classInfo) -> classInfo.level }.sum()) {
                sender.spigot().sendMessage(
                    TextComponent("Click here to level up!").apply {
                        color = GREEN
                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to level up"))
                        clickEvent = ClickEvent(RUN_COMMAND, "/character levelup")
                    },
                )
            }

            sender.sendMessage("$WHITE[${YELLOW}${"=".repeat(filledExperienceBars)}${ChatColor.DARK_GRAY}${"=".repeat(unfilledExperienceBars)}$WHITE] ${YELLOW}${experienceSincePreviousLevel}$GRAY/${YELLOW}$experienceForNextLevel")
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
        else -> emptyList()
    }
}
