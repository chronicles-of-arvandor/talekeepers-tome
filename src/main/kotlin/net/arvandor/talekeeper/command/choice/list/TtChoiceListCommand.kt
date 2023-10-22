package net.arvandor.talekeeper.command.choice.list

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.scheduler.asyncTask
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
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtChoiceListCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        val target = if (sender.hasPermission("talekeeper.commands.choice.list.other") && args.isNotEmpty()) {
            plugin.server.getPlayer(args[0]) ?: sender
        } else {
            sender
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

        val classService = Services.INSTANCE[TtClassService::class.java]
        if (classService == null) {
            sender.sendMessage("${RED}No class service was found. Please contact an admin.")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}${if (target == sender) "You do" else "${target.name} does"} not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (target == sender) "your" else "${minecraftProfile.name}'s"} active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}${if (target == sender) "You do" else "${minecraftProfile.name} does"} not currently have an active character.")
                return@asyncTask
            }

            val classes = character.classes.mapKeys { (classId, _) -> classService.getClass(classId) }
            val classesPendingSubclassSelection = classes.filter { (clazz, classInfo) ->
                if (clazz == null) return@filter false
                classInfo.subclassId == null && classInfo.level >= clazz.subClassSelectionLevel
            }.keys.filterNotNull()

            val pendingChoices = choiceService.getPendingChoices(character)
                .sortedBy { choice -> choice.text }
            val completedChoices = choiceService.getCompletedChoices(character)
                .sortedBy { choice -> choice.text }
            if (pendingChoices.isEmpty() && classesPendingSubclassSelection.isEmpty() && completedChoices.isEmpty()) {
                sender.sendMessage("${GREEN}You have no applicable choices.")
                return@asyncTask
            }

            val page = if (target == sender) {
                if (args.isNotEmpty()) {
                    args[0].toIntOrNull()
                } else {
                    1
                }
            } else {
                if (args.size > 1) {
                    args[1].toIntOrNull()
                } else {
                    1
                }
            } ?: 1

            val view = PaginatedView.fromChatComponents(
                arrayOf(
                    TextComponent("Choices:").apply {
                        color = GRAY
                    },
                ),
                buildList {
                    addAll(
                        classesPendingSubclassSelection.map { clazz ->
                            arrayOf(
                                TextComponent("${clazz.name}: Sub-class").apply {
                                    color = YELLOW
                                    if (target == sender) {
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to view the sub-class selection for ${clazz.name}."),
                                        )
                                        clickEvent =
                                            ClickEvent(RUN_COMMAND, "/character subclass set ${clazz.id.value}")
                                    }
                                },
                            )
                        },
                    )
                    addAll(
                        pendingChoices.map { choice ->
                            arrayOf(
                                TextComponent(choice.text).apply {
                                    color = GREEN
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view this choice."))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/choice view ${choice.id.value}${if (target != sender) " ${target.name}" else ""}")
                                },
                            )
                        },
                    )
                    addAll(
                        completedChoices.mapNotNull { choice ->
                            val optionId = character.choiceOptions[choice.id] ?: return@mapNotNull null
                            val chosenOption = choice.options.singleOrNull { it.id == optionId } ?: return@mapNotNull null
                            arrayOf(
                                TextComponent(choice.text).apply {
                                    color = WHITE
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view this choice."))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/choice view ${choice.id.value}${if (target != sender) " ${target.name}" else ""}")
                                },
                                TextComponent(" (${chosenOption.text})").apply {
                                    color = GRAY
                                },
                            )
                        },
                    )
                },
                "$GREEN< Previous",
                "Click here to view the previous page",
                "${GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                10,
                { pageNumber -> "/choice list ${if (target != sender) "${target.name} " else ""}$pageNumber" },
            )

            if (!view.isPageValid(page)) {
                sender.sendMessage("${RED}Invalid page number.")
                return@asyncTask
            }

            view.sendPage(sender, page)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> plugin.server.onlinePlayers.map { it.name } + (1..100).map(Int::toString)
        args.size == 1 -> (plugin.server.onlinePlayers.map { it.name } + (1..100).map(Int::toString))
            .filter { it.startsWith(args[0], true) }
        args.size == 2 -> (1..100).map(Int::toString).filter { it.startsWith(args[1], true) }
        else -> emptyList()
    }
}
