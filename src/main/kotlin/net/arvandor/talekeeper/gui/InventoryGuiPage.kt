package net.arvandor.talekeeper.gui

@InventoryGuiDsl
class InventoryGuiPage {
    val icons = mutableMapOf<Int, InventoryGuiIcon>()

    fun icon(slot: Int, init: InventoryGuiIcon.() -> Unit) {
        val icon = InventoryGuiIcon()
        icon.init()
        icons[slot] = icon
    }
}

fun page(init: InventoryGuiPage.() -> Unit): InventoryGuiPage {
    val page = InventoryGuiPage()
    page.init()
    return page
}
