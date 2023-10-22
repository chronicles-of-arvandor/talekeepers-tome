package net.arvandor.talekeeper.command.choice.view

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.scheduler.asyncTask
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
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtChoiceViewCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        val target = if (sender.hasPermission("talekeeper.commands.choice.view.other") && args.size > 1) {
            plugin.server.getPlayer(args[1]) ?: sender
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

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${RED}Usage: /choice view [id]")
            return true
        }

        val choiceId = args[0]

        val choice = choiceService.getChoice(TtChoiceId(choiceId))
        if (choice == null) {
            sender.sendMessage("${RED}No choice by that ID was found.")
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

            if (!choice.isApplicableFor(character)) {
                sender.sendMessage("${RED}This choice is not applicable to ${if (target == sender) "you" else character.name} right now.")
                return@asyncTask
            }

            val chosenOption = choiceService.getChosenOption(character, choice.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${if (target == sender) "your" else "${character.name}'s"} chosen option.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${GRAY}${choice.text}")
            sender.sendMessage("${GRAY}Prerequisites: ${choice.prerequisites.joinToString(", ") { it.name }}")
            if (chosenOption != null) {
                sender.sendMessage("${GRAY}Chosen option: ${WHITE}${chosenOption.text}")
            } else {
                val page = args.lastOrNull()?.toIntOrNull() ?: 1

                val view = PaginatedView.fromChatComponents(
                    arrayOf(
                        TextComponent("Select option").apply {
                            color = GRAY
                        },
                    ),
                    choice.options
                        .filter { option ->
                            option.prerequisites.all { it.isMetBy(character) }
                        }
                        .map { option ->
                            arrayOf(
                                TextComponent(option.text).apply {
                                    color = WHITE
                                    if (target == sender) {
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to select this option."))
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            "/choice select ${choice.id.value} ${option.id.value}",
                                        )
                                    }
                                },
                            )
                        } +
                        choice.options.filter { option ->
                            option.prerequisites.any { !it.isMetBy(character) }
                        }.map { option ->
                            arrayOf(
                                TextComponent(option.text).apply {
                                    color = RED
                                    val unmetOptionPrerequisites = option.prerequisites.filter { !it.isMetBy(character) }
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text(
                                            arrayOf(
                                                TextComponent(
                                                    "${if (target == sender) "You do" else "${character.name} does"} not meet the following prerequisites for this option:\n" +
                                                        unmetOptionPrerequisites.joinToString("\n") { "• ${it.name}" },
                                                ).apply { color = RED },
                                            ),
                                        ),
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
                    { pageNumber -> "/choice view ${choice.id.value} ${if (target != sender) "${target.name} " else ""}$pageNumber" },
                )

                if (!view.isPageValid(page)) {
                    sender.sendMessage("${RED}Invalid page number.")
                    return@asyncTask
                }

                view.sendPage(sender, page)
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> = emptyList()
}
