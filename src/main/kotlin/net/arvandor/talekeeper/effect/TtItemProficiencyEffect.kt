package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.item.TtItemId
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("ItemProficiencyEffect")
data class TtItemProficiencyEffect(
    val items: List<TtItemId>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Item proficiency: ${items.joinToString(", ") { it.value }}"

    override fun invoke(character: TtCharacter) = character.copy(
        itemProficiencies = character.itemProficiencies + items,
    )

    override fun serialize() = mapOf(
        "items" to items.map(TtItemId::value),
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtItemProficiencyEffect(
            (serialized["items"] as List<String>).map(::TtItemId),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
