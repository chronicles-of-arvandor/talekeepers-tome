package net.arvandor.talekeeper.command.spells

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.spell.TtSpell
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
import org.bukkit.entity.Player

class TtSpellsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val spellService = Services.INSTANCE[TtSpellService::class.java]
        if (spellService == null) {
            sender.sendMessage("${RED}No spell service was found. Please contact an admin.")
            return true
        }

        val page = if (args.isEmpty()) {
            1
        } else {
            args.last().toIntOrNull() ?: 1
        }

        val filter = if (args.isEmpty()) {
            "self"
        } else {
            when (args.first().lowercase()) {
                "all" -> "all"
                else -> "self"
            }
        }

        if (filter == "self" && sender is Player) {
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
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender).join()
                if (minecraftProfile == null) {
                    sender.sendMessage("${RED}You do not have a Minecraft profile.")
                    return@asyncTask
                }

                val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                    sender.sendMessage("${RED}An error occurred while getting your active character.")
                    return@asyncTask
                }

                if (character == null) {
                    sender.sendMessage("${RED}You do not currently have an active character.")
                    return@asyncTask
                }

                displaySpells(
                    spellService.spells
                        .filter { spell -> character.spells.contains(spell.id) },
                    page,
                    sender,
                    "self",
                )
            }
            return true
        }

        displaySpells(spellService.spells, page, sender, "all")
        return true
    }

    private fun displaySpells(
        spells: List<TtSpell>,
        page: Int,
        sender: CommandSender,
        filter: String,
    ) {
        val view = PaginatedView.fromChatComponents(
            arrayOf(
                TextComponent("=== ").apply { color = GRAY },
                TextComponent("Spells").apply { color = WHITE },
                TextComponent(" ===").apply { color = GRAY },
            ),
            spells
                .sortedBy { spell -> spell.name }
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
            { pageNumber -> "/spells $filter $pageNumber" },
        )

        if (view.isPageValid(page)) {
            view.sendPage(sender, page)
        } else {
            sender.sendMessage("${RED}Invalid page number.")
        }
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
        return when {
            args.isEmpty() -> (1..pages).map { it.toString() } + listOf("all", "self")
            args.size == 1 -> ((1..pages).map { it.toString() } + listOf("all", "self"))
                .filter { it.startsWith(args[0], ignoreCase = true) }.toList()
            else -> (1..pages).map { it.toString() }.filter { it.startsWith(args[1], ignoreCase = true) }.toList()
        }
    }
}
