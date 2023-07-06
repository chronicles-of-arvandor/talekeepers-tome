package net.arvandor.talekeeper.ancestry.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestry
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text

class TtAncestrySelectionGui(plugin: TalekeepersTome, private val ancestries: List<TtAncestry>) : InventoryGui(
    plugin,
    "Select ancestry",
    page {
        ancestries.forEachIndexed { slot, ancestry ->
            icon(slot) {
                item = skull(
                    ancestry.name,
                    buildList {
                        if (ancestry.subAncestries.isNotEmpty()) {
                            add("${WHITE}Sub-ancestries:")
                            addAll(ancestry.subAncestries.map { subAncestry -> "${GRAY}\u2022 ${subAncestry.name}" })
                        }
                    },
                    ancestry.id.value,
                    ancestry.skullTexture,
                )

                onClick = onSelectAncestry@{ player ->
                    player.closeInventory()

                    player.sendMessage("${GRAY}Traits:")
                    ancestry.traits.forEach {
                        player.sendMessage("${GRAY}\u2022 ${WHITE}${it.name} $GRAY- ${it.description}")
                    }
                    player.spigot().sendMessage(
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
                }
            }
        }
    },
)
