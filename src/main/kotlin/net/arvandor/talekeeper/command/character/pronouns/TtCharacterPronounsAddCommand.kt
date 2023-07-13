package net.arvandor.talekeeper.command.character.pronouns

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCharacterPronounsAddCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val pronounService = Services.INSTANCE[TtPronounService::class.java]
        if (pronounService == null) {
            sender.sendMessage("${RED}No pronoun service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            val pronounSets = pronounService.getAll().onFailure {
                sender.sendMessage("${RED}Failed to retrieve pronouns. Please contact an admin.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val page = if (args.isEmpty()) {
                1
            } else if (args.last().startsWith("p=")) {
                args.last().substring(2).toIntOrNull()
            } else {
                null
            }

            if (page != null) {
                val view = PaginatedView.fromChatComponents(
                    arrayOf(
                        TextComponent("Choose a pronoun set: ").apply {
                            color = WHITE
                        },
                    ),
                    pronounSets.map { pronounSet ->
                        arrayOf(
                            TextComponent("\u2022 ").apply {
                                color = WHITE
                            },
                            TextComponent(pronounSet.name).apply {
                                color = GRAY
                                hoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text("Click here to add the pronoun set ${pronounSet.name}."),
                                )
                                clickEvent = ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/character pronouns add ${pronounSet.id.value}",
                                )
                            },
                        )
                    } /*+ listOf(
                        arrayOf(
                            TextComponent("\u2022").apply {
                                color = WHITE
                            },
                            TextComponent("Custom").apply {
                                color = GRAY
                                hoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    Text("Click here to add a custom pronoun set.")
                                )
                                clickEvent = ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/character pronouns add custom"
                                )
                            }
                        )
                    )*/,
                    "$GREEN< Previous page",
                    "Click here to view the previous page",
                    "${GREEN}Next page >",
                    "Click here to view the next page",
                    { pageNumber -> "Page $pageNumber" },
                    10,
                    { pageNumber -> "/character pronouns add p=$pageNumber" },
                )
                if (view.isPageValid(page)) {
                    view.sendPage(sender, page)
                } else {
                    sender.sendMessage("${RED}Invalid page number.")
                }
                return@asyncTask
            }

//            if (args.first().equals("custom", ignoreCase = true)) {
//                syncTask(plugin) {
//                    sender.performCommand("pronounset create")
//                }
//            }

            val pronounSet = pronounService.get(TtPronounSetId(args.first()))
            if (pronounSet == null) {
                sender.sendMessage("${RED}Invalid pronoun set ID.")
                return@asyncTask
            }

            val updatedCharacter = characterService.save(character.copy(pronouns = character.pronouns + (pronounSet.id to 1))).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage(
                "$GRAY================================",
                "$GREEN${pronounSet.name} pronouns added.",
                "$GRAY================================",
            )
            updatedCharacter.display(sender)
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = emptyList<String>()
}
