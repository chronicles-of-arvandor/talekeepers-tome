package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.item.TtItemId
import net.arvandor.talekeeper.item.TtItemService

data class TtItemProficiencyPrerequisite(
    val itemIds: List<TtItemId>,
) : TtPrerequisite {
    override val name: String
        get() {
            val itemService = Services.INSTANCE[TtItemService::class.java]
            val items = itemIds.mapNotNull { itemId -> itemService.getItemType(itemId) }
            return "Item Proficiency: ${items.joinToString(", ") { it.name }}"
        }

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.itemProficiencies.containsAll(itemIds)
    }

    override fun serialize() = mapOf(
        "item-ids" to itemIds.map { it.value },
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtItemProficiencyPrerequisite(
            (serialized["item-ids"] as List<String>).map(::TtItemId),
        )
    }
}
