package net.arvandor.talekeeper.ancestry.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestry
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor

class TtSubAncestrySelectionGui(plugin: TalekeepersTome, private val ancestry: TtAncestry) : InventoryGui(
    plugin,
    "Select sub-ancestry",
    page {
        ancestry.subAncestries.forEachIndexed { slot, subAncestry ->
            icon(slot) {
                item = skull(
                    "${ChatColor.YELLOW}${subAncestry.name}",
                    emptyList(),
                    subAncestry.id.value,
                    subAncestry.skullTexture,
                )

                onClick = onSelectSubAncestry@{ player, _ ->
                    player.closeInventory()

                    player.performCommand("ancestry traits ${ancestry.id.value} ${subAncestry.id.value} --set")
                }
            }
        }
    },
)
