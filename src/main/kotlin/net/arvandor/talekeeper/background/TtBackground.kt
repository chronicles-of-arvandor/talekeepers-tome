package net.arvandor.talekeeper.background

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Background")
data class TtBackground(
    val id: TtBackgroundId,
    val name: String,
) : ConfigurationSerializable {

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtBackground(
            (serialized["id"] as String).let(::TtBackgroundId),
            serialized["name"] as String,
        )
    }
}
