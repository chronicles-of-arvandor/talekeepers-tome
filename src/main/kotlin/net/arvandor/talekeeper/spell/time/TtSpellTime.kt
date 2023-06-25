package net.arvandor.talekeeper.spell.time

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SpellTime")
data class TtSpellTime(
    val number: Int,
    val unit: TtSpellTimeUnit,
) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "number" to number,
        "unit" to unit.name,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellTime(
            serialized["number"] as Int,
            TtSpellTimeUnit.valueOf(serialized["unit"] as String),
        )
    }
}
