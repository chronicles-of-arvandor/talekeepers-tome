package net.arvandor.talekeeper.ancestry

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SubAncestry")
data class TtSubAncestry(
    val id: TtSubAncestryId,
    val name: String,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubAncestry(
            (serialized["id"] as String).let(::TtSubAncestryId),
            serialized["name"] as String,
        )
    }
}
