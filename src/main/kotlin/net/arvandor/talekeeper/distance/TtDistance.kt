package net.arvandor.talekeeper.distance

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Distance")
data class TtDistance(val value: Double, val unit: TtDistanceUnit) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "value" to value,
        "unit" to unit.name,
    )

    override fun toString() = unit.format(value)

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtDistance(
            serialized["value"] as Double,
            TtDistanceUnit.valueOf(serialized["unit"] as String),
        )
    }
}
