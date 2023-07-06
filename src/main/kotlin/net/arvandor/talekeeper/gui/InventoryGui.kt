package net.arvandor.talekeeper.gui

import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.inventory.InventoryHolder

abstract class InventoryGui(plugin: TalekeepersTome, val title: String, val initialPage: InventoryGuiPage) : InventoryHolder {

    private val inventory = plugin.server.createInventory(this, 27, title)

    override fun getInventory() = inventory

    var page: InventoryGuiPage = initialPage
        set(value) {
            renderPage(value)
            field = value
        }

    init {
        renderPage(page)
    }

    private fun renderPage(page: InventoryGuiPage) {
        inventory.clear()
        page.icons.forEach { (slot, icon) ->
            inventory.setItem(slot, icon.item)
        }
    }
}
