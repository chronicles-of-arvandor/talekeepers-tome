package net.arvandor.talekeeper.clazz.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.md_5.bungee.api.ChatColor.WHITE
import org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES
import org.bukkit.inventory.ItemFlag.HIDE_DESTROYS
import org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS
import org.bukkit.inventory.ItemFlag.HIDE_PLACED_ON
import org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS
import org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE

class TtSubClassSelectionGui(plugin: TalekeepersTome, clazz: TtClass) : InventoryGui(
    plugin,
    "Select sub-class",
    page {
        clazz.subClasses.forEachIndexed { slot, subClass ->
            icon(slot) {
                item = subClass.icon.apply {
                    itemMeta = itemMeta?.apply {
                        setDisplayName("${WHITE}${subClass.name}")
                        addItemFlags(
                            HIDE_ENCHANTS,
                            HIDE_ATTRIBUTES,
                            HIDE_UNBREAKABLE,
                            HIDE_DESTROYS,
                            HIDE_PLACED_ON,
                            HIDE_POTION_EFFECTS,
                        )
                    }
                }

                onClick = onSelectSubClass@{ player, _ ->
                    player.performCommand("subclass features ${clazz.id.value} ${subClass.id.value} --set")
                    false
                }
            }
        }
    },
)
