package net.arvandor.talekeeper.ancestry

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("AncestryTrait")
data class TtAncestryTrait(val name: String, val description: String) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "name" to name,
        "description" to description,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAncestryTrait(
            serialized["name"] as String,
            serialized["description"] as String,
        )
    }
}
