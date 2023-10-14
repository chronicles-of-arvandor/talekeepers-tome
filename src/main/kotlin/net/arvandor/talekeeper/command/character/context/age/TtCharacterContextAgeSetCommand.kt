package net.arvandor.talekeeper.command.character.context.age

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.magistersmonths.MagistersMonths
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.args.unquote
import net.arvandor.talekeeper.birthday.gui.TtBirthdaySelectionGui
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Instant
import java.util.logging.Level

class TtCharacterContextAgeSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val unquotedArgs = args.unquote()

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

        val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths
        if (magistersMonths == null) {
            sender.sendMessage("${RED}Magister's Months plugin was not found. Please contact an admin.")
            return true
        }

        val calendar = magistersMonths.calendar
        val currentDate = calendar.toMmDateTime(Instant.now())

        val year = if (unquotedArgs.isNotEmpty()) {
            unquotedArgs[0].toIntOrNull()
        } else {
            null
        }
        if (year != null) {
            if (year > currentDate.year) {
                sender.sendMessage("${RED}You cannot be born in the future.")
                return true
            }
        }

        val months = calendar.months

        val month = if (unquotedArgs.size > 1) {
            months.singleOrNull { it.name.equals(unquotedArgs[1], ignoreCase = true) }
        } else {
            null
        }
        if (month != null) {
            if (year == currentDate.year && months.indexOf(month) > months.indexOf(calendar.getMonthAt(currentDate.dayOfYear))) {
                sender.sendMessage("${RED}You cannot be born in the future.")
                return true
            }
        }

        val monthDay = if (unquotedArgs.size > 2) {
            unquotedArgs[2].toIntOrNull()
        } else {
            null
        }

        if (year == null || month == null || monthDay == null) {
            asyncTask(plugin) {
                val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                    sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                if (ctx == null) {
                    sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                    return@asyncTask
                }

                val birthdayMonth = ctx.birthdayDay?.let(calendar::getMonthAt)

                syncTask(plugin) {
                    val birthdaySelectionGui = TtBirthdaySelectionGui(plugin, calendar, year ?: ctx.birthdayYear, month ?: birthdayMonth)
                    sender.openInventory(birthdaySelectionGui.inventory)
                }
            }
            return true
        }

        if (monthDay < 1 || monthDay > (month.endDay - month.startDay) + 1) {
            sender.sendMessage("${RED}Invalid day of month.")
            return true
        }

        val yearDay = month.startDay + monthDay - 1
        if (year == currentDate.year && month == calendar.getMonthAt(currentDate.dayOfYear) && yearDay > currentDate.dayOfYear) {
            sender.sendMessage("${RED}You cannot be born in the future.")
            return true
        }

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            val updatedCtx = characterService.save(
                ctx.copy(
                    birthdayYear = year,
                    birthdayDay = yearDay,
                ),
            ).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character creation context.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val month = calendar.getMonthAt(yearDay)
            sender.sendMessage(
                "$GRAY================================",
                "${GREEN}Birthday set to ${if (month != null) "$monthDay ${month.name}" else yearDay.toString()} $year.",
                "$GRAY================================",
            )

            updatedCtx.display(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val unquotedArgs = args.unquote()
        val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths ?: return emptyList()
        val calendar = magistersMonths.calendar
        val currentDate = calendar.toMmDateTime(Instant.now())
        return when {
            unquotedArgs.isEmpty() -> ((currentDate.year - 1000)..currentDate.year).map { it.toString() }
            unquotedArgs.size == 1 -> ((currentDate.year - 1000)..currentDate.year).map { it.toString() }
                .filter { it.startsWith(unquotedArgs[0], ignoreCase = true) }
            unquotedArgs.size == 2 -> calendar.months.filter { month -> month.name.startsWith(unquotedArgs[1], ignoreCase = true) }
                .map { month -> if (month.name.contains(" ")) "\"${month.name}\"" else month.name }
            unquotedArgs.size == 3 -> {
                val month = calendar.months.singleOrNull { month -> month.name.equals(unquotedArgs[1], ignoreCase = true) }
                if (month != null) {
                    (1..(month.endDay - month.startDay)).map { it.toString() }
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
