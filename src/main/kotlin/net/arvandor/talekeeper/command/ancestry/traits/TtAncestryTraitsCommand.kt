package net.arvandor.talekeeper.command.ancestry.traits

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
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

class TtAncestryTraitsCommand : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /ancestry traits [ancestry] (subancestry) (page)")
            return true
        }

        val ancestryNameOrId = args[0]
        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        if (ancestryService == null) {
            sender.sendMessage("${RED}No ancestry service was found. Please contact an admin.")
            return true
        }
        val ancestry = ancestryService.getAncestry(TtAncestryId(ancestryNameOrId))
            ?: ancestryService.getAncestry(ancestryNameOrId)
        if (ancestry == null) {
            sender.sendMessage("${RED}There is no ancestry by that name or ID.")
            return true
        }

        val subAncestry = if (args.size > 2) {
            ancestry.getSubAncestry(TtSubAncestryId(args[1]))
                ?: ancestry.getSubAncestry(args[1])
        } else {
            null
        }

        val page = if (args.size < 2) {
            1
        } else {
            if (subAncestry != null) {
                args[2].toIntOrNull() ?: 1
            } else {
                args[1].toIntOrNull() ?: 1
            }
        }
        val displayConfirmButton = args.contains("--set")
        val traits = subAncestry?.traits ?: ancestry.traits
        val view = PaginatedView.fromChatComponents(
            arrayOf(
                TextComponent("${subAncestry?.name ?: ancestry.name} traits").apply {
                    color = GRAY
                },
            ),
            traits.flatMap { trait ->
                buildList {
                    add(
                        arrayOf(
                            TextComponent("\u2022 ").apply {
                                color = GRAY
                            },
                            TextComponent(trait.name).apply {
                                color = WHITE
                            },
                            TextComponent(" - ").apply {
                                color = GRAY
                            },
                            TextComponent(trait.description).apply {
                                color = GRAY
                            },
                        ),
                    )
                }
            }.let {
                if (displayConfirmButton) {
                    val buttons = if (subAncestry == null) {
                        arrayOf(
                            TextComponent("Back").apply {
                                color = AQUA
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to go back to the ancestry selection menu."))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context ancestry set")
                            },
                            TextComponent(" "),
                            TextComponent("Confirm").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to confirm your ancestry selection."))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context ancestry set ${ancestry.id.value}")
                            },
                        )
                    } else {
                        arrayOf(
                            TextComponent("Back").apply {
                                color = AQUA
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to go back to the sub-ancestry selection menu."))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context subancestry set")
                            },
                            TextComponent(" "),
                            TextComponent("Confirm").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to confirm your sub-ancestry selection."))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context subancestry set ${subAncestry.id.value}")
                            },
                        )
                    }

                    it.chunked(3)
                        .map { pageContents -> pageContents + listOf(buttons) }
                        .flatten()
                } else {
                    it
                }
            },
            "$GREEN< Previous",
            "Click here to view the previous page",
            "${GREEN}Next >",
            "Click here to view the next page",
            { pageNumber -> "Page $pageNumber" },
            if (displayConfirmButton) 4 else 3,
            { pageNumber -> "/ancestry traits ${ancestry.id.value} ${subAncestry?.id?.value?.plus(" ") ?: ""}$pageNumber" + (if (displayConfirmButton) " --set" else "") },
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
        val ancestryService = Services.INSTANCE[TtAncestryService::class.java] ?: return emptyList()
        val ancestries = ancestryService.getAll()
        return when {
            args.isEmpty() -> ancestries.flatMap { listOf(it.id.value, it.name) }
            args.size == 1 -> ancestries.flatMap { listOf(it.id.value, it.name) }.filter { it.startsWith(args[0], ignoreCase = true) }
            args.size == 2 -> {
                val ancestry = ancestryService.getAncestry(TtAncestryId(args[0])) ?: ancestryService.getAncestry(args[0])
                    ?: return emptyList()
                return (1..ancestry.traits.size).map { it.toString() }
            }
            else -> return emptyList()
        }
    }
}
