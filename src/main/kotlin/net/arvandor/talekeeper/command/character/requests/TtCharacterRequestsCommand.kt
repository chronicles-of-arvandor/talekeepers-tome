package net.arvandor.talekeeper.command.character.requests

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
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
import java.time.format.DateTimeFormatter

class TtCharacterRequestsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val acceptCommand = TtCharacterRequestsAcceptCommand(plugin)
    private val declineCommand = TtCharacterRequestsDeclineCommand(plugin)

    private val acceptAliases = listOf("accept", "approve", "a")
    private val declineAliases = listOf("decline", "deny", "reject", "d")

    private val subcommands = acceptAliases + declineAliases

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.command.character.requests")) {
            sender.sendMessage("${RED}You do not have permission to use this command.")
            return true
        }
        return when (args.firstOrNull()?.lowercase()) {
            in acceptAliases -> acceptCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            in declineAliases -> declineCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                displayRequestList(sender, args)
                true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> subcommands
        args.size == 1 -> subcommands.filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }

    private fun displayRequestList(sender: CommandSender, args: Array<out String>) {
        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return
        }

        val page = args.firstOrNull()?.toIntOrNull() ?: 1

        asyncTask(plugin) {
            val requests = characterService.getCreationRequests().onFailure {
                sender.sendMessage("${RED}An error occurred while retrieving the list of pending requests.")
                return@asyncTask
            }

            if (requests.isEmpty()) {
                sender.sendMessage("${GREEN}There are currently no pending character creation requests.")
                return@asyncTask
            }

            val view = PaginatedView.fromChatComponents(
                arrayOf(
                    TextComponent("Pending character creation requests").apply {
                        color = GRAY
                    },
                ),
                requests.mapNotNull { request ->
                    val minecraftProfile = minecraftProfileService.getMinecraftProfile(request.minecraftProfileId).join() ?: return@mapNotNull null
                    return@mapNotNull arrayOf(
                        TextComponent(
                            DateTimeFormatter.ISO_INSTANT.format(request.requestTime),
                        ).apply {
                            color = GRAY
                        },
                        TextComponent(" - ").apply {
                            color = GRAY
                        },
                        TextComponent(minecraftProfile.name).apply {
                            color = WHITE
                        },
                        TextComponent(" "),
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
                            clickEvent = ClickEvent(
                                RUN_COMMAND,
                                "/character requests decline ${minecraftProfile.minecraftUUID}",
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
                { pageNumber -> "/character requests $pageNumber" },
            )
            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
            } else {
                sender.sendMessage("${RED}Invalid page number.")
            }
        }
        return
    }
}
