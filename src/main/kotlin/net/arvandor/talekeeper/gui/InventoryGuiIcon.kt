package net.arvandor.talekeeper.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Material.PLAYER_HEAD
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

@InventoryGuiDsl
class InventoryGuiIcon {
    var item: ItemStack? = null
    var onClick: InventoryGui.(Player, ClickType) -> Unit = { _, _ -> }

    fun skull(name: String, lore: List<String>, player: OfflinePlayer, init: (ItemStack.() -> Unit)? = null) =
        item(PLAYER_HEAD, name, lore) {
            init?.invoke(this)
            itemMeta = itemMeta?.apply {
                if (this is SkullMeta) {
                    owningPlayer = player
                }
            }
        }

    fun skull(name: String, lore: List<String>, id: String, texture: String, init: (ItemStack.() -> Unit)? = null): ItemStack {
        val idParts = id.replace("-", "").chunked(8)
            .map { part -> part.toLong(16).toInt() }

        return item(PLAYER_HEAD, name, lore) {
            init?.invoke(this)
            Bukkit.getUnsafe().modifyItemStack(
                this,
                "{SkullOwner:{Id:[I;${idParts.joinToString(",")}],Properties:{textures:[{Value:\"$texture\"}]}}}",
            )
        }
    }

    fun item(material: Material, name: String, lore: List<String> = emptyList(), init: (ItemStack.() -> Unit)? = null) =
        item(material) {
            init?.invoke(this)
            itemMeta = itemMeta?.apply {
                setDisplayName(name)
                setLore(lore.toList())
            }
        }

    fun item(material: Material, init: (ItemStack.() -> Unit)? = null): ItemStack {
        val item = ItemStack(material)
        init?.invoke(item)
        item.itemMeta = item.itemMeta?.apply {
            addItemFlags(
                HIDE_ATTRIBUTES,
            )
        }
        return item
    }
}
