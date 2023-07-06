package net.arvandor.talekeeper.listener

import net.arvandor.talekeeper.gui.InventoryGui
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val topInventoryHolder = event.view.topInventory.holder
        if (topInventoryHolder is InventoryGui) {
            event.isCancelled = true
        }

        val holder = event.inventory.holder
        if (holder !is InventoryGui) return

        val whoClicked = event.whoClicked
        if (whoClicked !is Player) return

        val slot = event.slot
        val icon = holder.page.icons[slot] ?: return
        icon.onClick(holder, whoClicked)
    }
}
