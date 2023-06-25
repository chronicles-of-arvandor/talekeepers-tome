package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SubClass")
data class TtSubClass(
    val id: TtSubClassId,
    val name: String,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubClass(
            TtSubClassId(serialized["id"] as String),
            serialized["name"] as String,
        )
    }
}
