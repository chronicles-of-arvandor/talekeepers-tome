package net.arvandor.talekeeper.command.background

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.background.TtBackgroundService
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
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

class TtBackgroundViewCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /background view [id]")
            return true
        }

        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
        if (backgroundService == null) {
            sender.sendMessage("${RED}No background service was found. Please contact an admin.")
            return true
        }

        val background = backgroundService.getBackground(TtBackgroundId(args[0]))
            ?: backgroundService.getBackground(args[0])
        if (background == null) {
            sender.sendMessage("${RED}No background was found with that ID or name.")
            return true
        }

        val returnPage = args.firstOrNull { it.startsWith("--return-page=") }?.substringAfter("--return-page=")?.toIntOrNull()

        sender.sendMessage("$WHITE${background.name}")
        sender.sendMessage("$GRAY${background.description}")
        if (returnPage != null) {
            sender.spigot().sendMessage(
                TextComponent("Back").apply {
                    color = AQUA
                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to return to the background list"))
                    clickEvent = ClickEvent(RUN_COMMAND, "/character context background set $returnPage")
                },
                TextComponent(" "),
                TextComponent("Confirm").apply {
                    color = GREEN
                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to confirm your background selection."))
                    clickEvent = ClickEvent(RUN_COMMAND, "/character context background set ${background.id.value}")
                },
            )
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java] ?: return emptyList()
        return when {
            args.isEmpty() -> backgroundService.getAll().flatMap { listOf(it.id.value, it.name) }
            args.size == 1 -> backgroundService.getAll().flatMap { listOf(it.id.value, it.name) }.filter { it.startsWith(args[0], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
