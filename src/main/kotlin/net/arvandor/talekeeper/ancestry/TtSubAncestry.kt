package net.arvandor.talekeeper.ancestry

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.unit.HeightUnit
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.WeightUnit
import net.arvandor.talekeeper.distance.TtDistance
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SubAncestry")
data class TtSubAncestry(
    val id: TtSubAncestryId,
    val name: String,
    val darkVision: TtDistance?,
    val minimumAge: Int,
    val maximumAge: Int,
    val minimumHeight: Double,
    val maximumHeight: Double,
    val minimumWeight: Double,
    val maximumWeight: Double,
    val traits: List<TtAncestryTrait>,
    val skullTexture: String,
) : ConfigurationSerializable {

    override fun serialize(): Map<String, Any?> {
        val unitService = Services.INSTANCE[RPKUnitService::class.java]
        return mapOf(
            "id" to id.value,
            "name" to name,
            "dark-vision" to darkVision,
            "minimum-age" to minimumAge,
            "maximum-age" to maximumAge,
            "minimum-height" to unitService.format(minimumHeight * HeightUnit.FEET.scaleFactor, HeightUnit.FEET),
            "maximum-height" to unitService.format(maximumHeight * HeightUnit.FEET.scaleFactor, HeightUnit.FEET),
            "minimum-weight" to unitService.format(minimumWeight * WeightUnit.POUNDS.scaleFactor, WeightUnit.POUNDS),
            "maximum-weight" to unitService.format(maximumWeight * WeightUnit.POUNDS.scaleFactor, WeightUnit.POUNDS),
            "traits" to traits,
            "skull-texture" to skullTexture,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubAncestry(
            (serialized["id"] as String).let(::TtSubAncestryId),
            serialized["name"] as String,
            serialized["dark-vision"] as? TtDistance,
            serialized["minimum-age"] as Int,
            serialized["maximum-age"] as Int,
            HeightUnit.FEET.parse(serialized["minimum-height"] as String),
            HeightUnit.FEET.parse(serialized["maximum-height"] as String),
            WeightUnit.POUNDS.parse(serialized["minimum-weight"] as String),
            WeightUnit.POUNDS.parse(serialized["maximum-weight"] as String),
            serialized["traits"] as List<TtAncestryTrait>,
            serialized["skull-texture"] as String,
        )
    }
}
