package net.arvandor.talekeeper.command.subclass

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.args.unquote
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.clazz.TtSubClassId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.BOLD
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

class TtSubClassFeaturesCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        var unquotedArgs = args.unquote()
        if (unquotedArgs.size < 2) {
            sender.sendMessage("${RED}Usage: /subclass features [class] [sub-class]")
            return true
        }

        val displayButtons = unquotedArgs.contains("--set")
        unquotedArgs = unquotedArgs.filter { !it.startsWith("--") }.toTypedArray()

        val classService = Services.INSTANCE[TtClassService::class.java]

        asyncTask(plugin) {
            val clazz = classService.getClass(TtClassId(unquotedArgs[0]))
                ?: classService.getClass(unquotedArgs[0])
            if (clazz == null) {
                sender.sendMessage("${RED}There is no class by that name.")
                return@asyncTask
            }

            val subClass = clazz.getSubClass(TtSubClassId(unquotedArgs[1]))
                ?: clazz.getSubClass(unquotedArgs[1])
            if (subClass == null) {
                sender.sendMessage("${RED}There is no sub-class by that name.")
                return@asyncTask
            }

            val buttons = arrayOf(
                TextComponent("Back").apply {
                    color = AQUA
                    hoverEvent =
                        HoverEvent(SHOW_TEXT, Text("Click to go back to the sub-class selection menu."))
                    clickEvent = ClickEvent(RUN_COMMAND, "/character subclass set ${clazz.id.value}")
                },
                TextComponent(" "),
                TextComponent("Confirm").apply {
                    color = GREEN
                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to confirm your sub-class selection."))
                    clickEvent = ClickEvent(
                        RUN_COMMAND,
                        "/character subclass set ${clazz.id.value} ${subClass.id.value}",
                    )
                },
            )

            if (subClass.features.isEmpty()) {
                sender.sendMessage("${RED}That sub-class has no features.")
                if (displayButtons) {
                    sender.spigot().sendMessage(*buttons)
                }
                return@asyncTask
            }

            val page = if (unquotedArgs.size > 2) {
                unquotedArgs[2].toIntOrNull() ?: 1
            } else {
                1
            }

            val view = PaginatedView.fromStrings(
                "${GRAY}${clazz.name}: ${subClass.name} features",
                subClass.features.flatMap { (level, features) ->
                    buildList {
                        add("${WHITE}${BOLD}Lv$level")
                        addAll(
                            features.flatMap { feature ->
                                listOf(
                                    "${WHITE}${feature.name}",
                                    "${GRAY}${feature.description}",
                                )
                            },
                        )
                    }
                },
                "$GREEN< Previous",
                "Click here to view the previous page",
                "${GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                5,
                { pageNumber -> "/subclass features ${clazz.id.value} ${subClass.id.value} $pageNumber" },
            )

            if (view.isPageValid(page)) {
                view.sendPage(sender, page)

                if (displayButtons) {
                    sender.spigot().sendMessage(*buttons)
                }
            } else {
                sender.sendMessage("${RED}Invalid page number.")
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val classService = Services.INSTANCE[TtClassService::class.java]
        return when {
            args.isEmpty() -> classService.getAll().map(TtClass::name)
            args.size == 1 -> classService.getAll().map(TtClass::name).filter { it.startsWith(args[0], ignoreCase = true) }
            args.size == 2 -> {
                val clazz = classService.getClass(TtClassId(args[0]))
                    ?: classService.getClass(args[0])
                clazz?.subClasses
                    ?.map { it.name }
                    ?.filter { it.startsWith(args[1], ignoreCase = true) }
                    ?: emptyList()
            }
            args.size == 3 -> {
                val clazz = classService.getClass(TtClassId(args[0]))
                    ?: classService.getClass(args[0])
                val subClass = clazz?.getSubClass(TtSubClassId(args[1]))
                    ?: clazz?.getSubClass(args[1])
                val features = subClass?.features ?: return emptyList()
                val pages = (features.size + (features.flatMap { (_, features) -> features }.size * 2)) / 5
                (1..pages).map { it.toString() }.filter { it.startsWith(args[2], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }
}
