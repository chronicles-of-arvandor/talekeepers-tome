package net.arvandor.talekeeper.ancestry.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestry
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.WHITE

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

                    player.performCommand("ancestry traits ${ancestry.id.value} --set")
                }
            }
        }
    },
)
