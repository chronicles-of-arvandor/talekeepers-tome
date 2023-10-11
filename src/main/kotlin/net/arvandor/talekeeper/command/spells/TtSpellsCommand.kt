package net.arvandor.talekeeper.command.spells

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import net.arvandor.talekeeper.spell.TtSpellService
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
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class TtSpellsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val spellService = Services.INSTANCE[TtSpellService::class.java]
        if (spellService == null) {
            sender.sendMessage("${RED}No spell service was found. Please contact an admin.")
            return true
        }

        val page = if (args.isEmpty()) {
            1
        } else {
            args[0].toIntOrNull() ?: 1
        }

        val view = PaginatedView.fromChatComponents(
            arrayOf(
                TextComponent("=== ").apply { color = GRAY },
                TextComponent("Spells").apply { color = WHITE },
                TextComponent(" ===").apply { color = GRAY },
            ),
            spellService.spells.sortedBy { spell -> spell.name }
                .map { spell ->
                    arrayOf(
                        TextComponent(spell.name).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view the details of ${spell.name}"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/spell \"${spell.name}\"")
                        },
                    )
                },
            "$GREEN< Previous",
            "Click here to view the previous page",
            "${GREEN}Next >",
            "Click here to view the next page",
            { pageNumber -> "Page $pageNumber" },
            10,
            { pageNumber -> "/spells $pageNumber" },
        )

        if (view.isPageValid(page)) {
            view.sendPage(sender, page)
        } else {
            sender.sendMessage("${RED}Invalid page number.")
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val spellService = Services.INSTANCE[TtSpellService::class.java] ?: return emptyList()
        val spells = spellService.spells
        val pages = spells.size / 10
        return if (args.isEmpty()) {
            (1..pages).map { it.toString() }
        } else {
            (1..pages).map { it.toString() }.filter { it.startsWith(args[0], ignoreCase = true) }.toList()
        }
    }
}
