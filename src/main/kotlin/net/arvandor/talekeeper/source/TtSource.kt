package net.arvandor.talekeeper.source

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Source")
data class TtSource(
    val source: String,
    val page: Int,
) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "source" to source,
        "page" to page,
    )

    companion object {
        @JvmStatic
        fun deserialize(map: Map<String, Any>) = TtSource(
            source = map["source"] as String,
            page = map["page"] as Int,
        )
    }
}
