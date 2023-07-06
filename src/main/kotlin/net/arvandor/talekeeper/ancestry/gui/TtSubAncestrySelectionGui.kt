package net.arvandor.talekeeper.ancestry.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtSubAncestry
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text

class TtSubAncestrySelectionGui(plugin: TalekeepersTome, private val subAncestries: List<TtSubAncestry>) : InventoryGui(
    plugin,
    "Select sub-ancestry",
    page {
        subAncestries.forEachIndexed { slot, subAncestry ->
            icon(slot) {
                item = skull(
                    "${ChatColor.YELLOW}${subAncestry.name}",
                    emptyList(),
                    subAncestry.id.value,
                    subAncestry.skullTexture,
                )

                onClick = onSelectSubAncestry@{ player ->
                    player.closeInventory()

                    player.sendMessage("${ChatColor.GRAY}Traits:")
                    subAncestry.traits.forEach {
                        player.sendMessage("${ChatColor.GRAY}\u2022 ${ChatColor.WHITE}${it.name} ${ChatColor.GRAY}- ${it.description}")
                    }
                    player.spigot().sendMessage(
                        TextComponent("Back").apply {
                            color = ChatColor.AQUA
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to go back to the sub-ancestry selection menu."))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context subancestry set")
                        },
                        TextComponent(" "),
                        TextComponent("Confirm").apply {
                            color = ChatColor.GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click to confirm your sub-ancestry selection."))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context subancestry set ${subAncestry.id.value}")
                        },
                    )
                }
            }
        }
    },
)
