package net.arvandor.talekeeper.gui

import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.inventory.InventoryHolder

abstract class InventoryGui(plugin: TalekeepersTome, title: String, initialPage: InventoryGuiPage, size: Int = 27) : InventoryHolder {

    private val inventory = plugin.server.createInventory(this, size, title)

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
