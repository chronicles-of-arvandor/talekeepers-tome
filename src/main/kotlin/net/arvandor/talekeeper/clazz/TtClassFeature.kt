package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("ClassFeature")
data class TtClassFeature(
    val name: String,
    val description: String,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "name" to name,
        "description" to description,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any?>) = TtClassFeature(
            serialized["name"] as String,
            serialized["description"] as String,
        )
    }
}
