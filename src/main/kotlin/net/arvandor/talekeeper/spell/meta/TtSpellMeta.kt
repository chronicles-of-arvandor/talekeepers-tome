package net.arvandor.talekeeper.spell.meta

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpellMeta")
data class TtSpellMeta(
    val ritual: Boolean,
) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "ritual" to ritual,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellMeta(
            serialized["ritual"] as Boolean,
        )
    }
}
