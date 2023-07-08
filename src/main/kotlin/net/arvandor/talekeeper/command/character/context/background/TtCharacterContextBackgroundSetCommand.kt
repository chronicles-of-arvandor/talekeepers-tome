package net.arvandor.talekeeper.command.character.context.background

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.background.TtBackground
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GRAY
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
import java.util.logging.Level.SEVERE

class TtCharacterContextBackgroundSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
        if (backgroundService == null) {
            sender.sendMessage("${RED}No background service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}Failed to retrieve character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            val background = if (args.isNotEmpty()) {
                backgroundService.getBackground(TtBackgroundId(args[0]))
                    ?: backgroundService.getBackground(args[0])
            } else {
                null
            }

            if (background == null) {
                val page = args.firstOrNull()?.toIntOrNull() ?: 1
                val backgrounds = backgroundService.getAll()
                val view = PaginatedView.fromChatComponents(
                    arrayOf(
                        TextComponent("Select background").apply {
                            color = GRAY
                        },
                    ),
                    backgrounds.flatMap { bg ->
                        listOf(
                            arrayOf(
                                TextComponent(bg.name).apply {
                                    color = GREEN
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to set your background to ${bg.name}"))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/character context background set ${bg.id.value}")
                                },
                            ),
                            arrayOf(
                                TextComponent(bg.description).apply {
                                    color = GRAY
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to set your background to ${bg.name}"))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/character context background set ${bg.id.value}")
                                },
                            ),
                        )
                    },
                    "$GREEN< Previous",
                    "Click here to view the previous page",
                    "${GREEN}Next >",
                    "Click here to view the next page",
                    { pageNumber -> "Page $pageNumber" },
                    4,
                    { pageNumber -> "/character context background set $pageNumber" },
                )
                if (view.isPageValid(page)) {
                    view.sendPage(sender, page)
                } else {
                    sender.sendMessage("${RED}Invalid page number.")
                }
                return@asyncTask
            }

            val updatedCtx = characterService.save(
                ctx.copy(
                    backgroundId = background.id,
                ),
            ).onFailure {
                sender.sendMessage("${RED}Failed to save character creation context. Please contact an admin.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage(
                "$GRAY================================",
                "${GREEN}Background set to ${background.name}.",
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
        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
            ?: return emptyList()
        val backgrounds = backgroundService.getAll()
        return when {
            args.isEmpty() -> backgrounds.map(TtBackground::name)
            args.size == 1 -> backgrounds.map(TtBackground::name).filter { it.startsWith(args[0], ignoreCase = true) } + (1..(backgrounds.size / 2)).map(Int::toString)
            else -> emptyList()
        }
    }
}
