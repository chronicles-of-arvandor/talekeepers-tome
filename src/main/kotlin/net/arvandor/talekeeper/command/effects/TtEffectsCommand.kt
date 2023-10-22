package net.arvandor.talekeeper.command.effects

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.effect.TtEffectService
import net.arvandor.talekeeper.prerequisite.TtAndPrerequisite
import net.arvandor.talekeeper.prerequisite.TtNotPrerequisite
import net.arvandor.talekeeper.prerequisite.TtOrPrerequisite
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.logging.Level

class TtEffectsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("talekeeper.commands.effects")) {
            sender.sendMessage("${RED}You do not have permission to use this command.")
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage("${RED}You must specify a player to view effects for.")
            return true
        }

        val target = plugin.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage("${RED}There is no player by that name online.")
            return true
        }

        val page = if (args.size > 1) {
            args[1].toIntOrNull() ?: 1
        } else {
            1
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

        val effectService = Services.INSTANCE[TtEffectService::class.java]
        if (effectService == null) {
            sender.sendMessage("${RED}No effect service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}That player does not have a Minecraft profile. Have they logged in before?")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting ${minecraftProfile.name}'s active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have an active character.")
                return@asyncTask
            }

            val effects = effectService.getApplicableEffects(character)
            if (effects.isEmpty()) {
                sender.sendMessage("${RED}There are no applicable effects for ${character.name}.")
                return@asyncTask
            }

            val view = PaginatedView.fromChatComponents(
                arrayOf(
                    TextComponent("=== ").apply {
                        color = GRAY
                    },
                    TextComponent("Applicable effects for ${character.name}").apply {
                        color = WHITE
                    },
                    TextComponent(" ===").apply {
                        color = GRAY
                    },
                ),
                effects.map { effect ->
                    arrayOf(
                        TextComponent("\u2022 ${effect.name}").apply {
                            color = GRAY
                            hoverEvent = HoverEvent(
                                SHOW_TEXT,
                                Text(
                                    arrayOf(
                                        TextComponent("Prerequisites:\n").apply {
                                            color = WHITE
                                        },
                                        *effect.prerequisites.flatMap { prerequisite ->
                                            renderPrerequisite(prerequisite, character, 0)
                                        }.toTypedArray(),
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
                5,
                { pageNumber -> "/effects ${target.name} $pageNumber" },
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
        args.isEmpty() -> plugin.server.onlinePlayers.map { it.name }
        args.size == 1 -> plugin.server.onlinePlayers.map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
        args.size == 2 -> (1..1000).map(Int::toString).filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }

    private fun renderPrerequisite(prerequisite: TtPrerequisite, character: TtCharacter, tabLevel: Int): List<BaseComponent> {
        return when (prerequisite) {
            is TtAndPrerequisite -> listOf(
                TextComponent("  ".repeat(tabLevel) + "\u2022 AND\n").apply {
                    color = when {
                        prerequisite.prerequisites.all { it.isMetBy(character) } -> GREEN
                        prerequisite.prerequisites.any { it.isMetBy(character) } -> YELLOW
                        else -> RED
                    }
                },
                *prerequisite.prerequisites.flatMap { renderPrerequisite(it, character, tabLevel + 1).toList() }
                    .toTypedArray(),
            )
            is TtOrPrerequisite -> listOf(
                TextComponent("  ".repeat(tabLevel) + "\u2022 OR\n").apply {
                    color = when {
                        prerequisite.prerequisites.any { it.isMetBy(character) } -> GREEN
                        else -> RED
                    }
                },
                *prerequisite.prerequisites.flatMap { renderPrerequisite(it, character, tabLevel + 1).toList() }
                    .toTypedArray(),
            )
            is TtNotPrerequisite -> listOf(
                TextComponent("  ".repeat(tabLevel) + "\u2022 NOT\n").apply {
                    color = when {
                        prerequisite.prerequisite.isMetBy(character) -> RED
                        else -> GREEN
                    }
                },
                *renderPrerequisite(prerequisite.prerequisite, character, tabLevel + 1).toTypedArray(),
            )
            else -> listOf(
                TextComponent("  ".repeat(tabLevel) + "\u2022 ${prerequisite.name}\n").apply {
                    color = when {
                        prerequisite.isMetBy(character) -> GREEN
                        else -> RED
                    }
                },
            )
        }
    }
}
