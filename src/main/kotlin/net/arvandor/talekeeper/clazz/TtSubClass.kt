package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import org.bukkit.inventory.ItemStack

@SerializableAs("SubClass")
data class TtSubClass(
    val id: TtSubClassId,
    val name: String,
    val features: Map<Int, List<TtClassFeature>>,
    val icon: ItemStack,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "features" to features.mapKeys { (level, _) -> level.toString() },
        "icon" to icon,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubClass(
            TtSubClassId(serialized["id"] as String),
            serialized["name"] as String,
            (serialized["features"] as? Map<String, List<TtClassFeature>>)
                ?.mapKeys { (level, _) -> level.toInt() }
                ?: emptyMap(),
            serialized["icon"] as ItemStack,
        )
    }
}
