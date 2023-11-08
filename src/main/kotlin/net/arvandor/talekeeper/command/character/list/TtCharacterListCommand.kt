package net.arvandor.talekeeper.command.character.list

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterService
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

class TtCharacterListCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val target = if (sender.hasPermission("talekeeper.commands.character.list.other") && args.size > 2) {
            plugin.server.getPlayer(args[2])
        } else if (sender is Player) {
            sender
        } else {
            sender.sendMessage("${RED}You must specify a player from console.")
            return true
        }

        val page = if (args.size > 1) {
            args[1].toIntOrNull() ?: 1
        } else {
            1
        }

        val filter = if (args.isNotEmpty()) {
            when (args[0].lowercase()) {
                "active" -> { character: TtCharacter -> !character.isShelved }
                "shelved" -> { character: TtCharacter -> character.isShelved }
                "all" -> { _: TtCharacter -> true }
                else -> {
                    sender.sendMessage("${RED}Usage: /character list [active|shelved|all] (page) (player)")
                    return true
                }
            }
        } else {
            { _: TtCharacter -> true }
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

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}${if (sender == target) "You do" else "That player does"} not have a Minecraft profile.")
                return@asyncTask
            }

            val activeCharacter = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val profile = minecraftProfile.profile
            if (profile !is RPKProfile) {
                sender.sendMessage("${RED}${if (sender == target) "You do" else "That player does"} not have a profile.")
                return@asyncTask
            }

            val characters = characterService.getCharacters(profile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting characters.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (characters.isEmpty()) {
                sender.sendMessage("${RED}${if (sender == target) "You do" else "That player does"} not have any characters.")
                return@asyncTask
            }

            val compareActive = Comparator<TtCharacter> { a, b -> if (a.id == activeCharacter?.id) -1 else if (b.id == activeCharacter?.id) 1 else 0 }
            val compareShelved = Comparator.comparing(TtCharacter::isShelved)
            val compareName = Comparator.comparing(TtCharacter::name)

            val view = PaginatedView.fromChatComponents(
                arrayOf(
                    TextComponent("=== ").apply {
                        color = GRAY
                    },
                    TextComponent("Characters").apply {
                        color = WHITE
                    },
                    TextComponent(" ===").apply {
                        color = GRAY
                    },
                ),
                characters.filter(filter)
                    .sortedWith { a, b ->
                        val activeComparison = compareActive.compare(a, b)
                        if (activeComparison != 0) {
                            return@sortedWith activeComparison
                        }
                        val shelvedComparison = compareShelved.compare(a, b)
                        if (shelvedComparison != 0) {
                            return@sortedWith shelvedComparison
                        }
                        return@sortedWith compareName.compare(a, b)
                    }
                    .map { character ->
                        arrayOf(
                            TextComponent(character.name).apply {
                                if (character.id == activeCharacter?.id) {
                                    color = WHITE
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text("This is your active character."),
                                    )
                                } else if (!character.isShelved) {
                                    color = GREEN
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text("Click here to switch to ${character.name}"),
                                    )
                                    clickEvent = ClickEvent(
                                        RUN_COMMAND,
                                        "/character switch ${character.id.value}",
                                    )
                                } else {
                                    color = YELLOW
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT,
                                        Text("This character is currently shelved."),
                                    )
                                }
                            },
                        )
                    },
                "$GREEN< Previous",
                "Click here to view the previous page",
                "${GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                10,
                { pageNumber -> "/character list $pageNumber" },
            )

            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
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
    ) = when {
        args.isEmpty() -> listOf("active", "shelved", "all")
        args.size == 1 -> listOf("active", "shelved", "all").filter { it.startsWith(args[0], ignoreCase = true) }
        args.size == 2 -> (0..100).map(Int::toString).filter { it.startsWith(args[1], ignoreCase = true) }
        args.size == 3 -> plugin.server.onlinePlayers.map { it.name }.filter { it.startsWith(args[2], ignoreCase = true) }
        else -> emptyList()
    }
}
