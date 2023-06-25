package net.arvandor.talekeeper.trait

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("CharacterTrait")
data class TtCharacterTrait(val name: String, val description: String) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "name" to name,
        "description" to description,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtCharacterTrait(
            serialized["name"] as String,
            serialized["description"] as String,
        )
    }
}
