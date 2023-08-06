package net.arvandor.talekeeper.ancestry

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.unit.HeightUnit
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.WeightUnit
import net.arvandor.talekeeper.distance.TtDistance
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt

@SerializableAs("Ancestry")
data class TtAncestry(
    val id: TtAncestryId,
    val name: String,
    val namePlural: String,
    val subAncestries: List<TtSubAncestry>,
    val darkVision: TtDistance,
    val minimumAge: Int,
    val maximumAge: Int,
    val minimumHeight: Double,
    val maximumHeight: Double,
    val minimumWeight: Double,
    val maximumWeight: Double,
    val traits: List<TtAncestryTrait>,
    val bonusHpPerLevel: Double?,
    val skullTexture: String,
) : ConfigurationSerializable {

    fun getSubAncestry(id: TtSubAncestryId) = subAncestries.singleOrNull { it.id == id }
    fun getSubAncestry(name: String) = subAncestries.singleOrNull { it.name.equals(name, ignoreCase = true) }
    fun getBonusHp(level: Int): Int = if (bonusHpPerLevel != null) max(1, floor(bonusHpPerLevel * level).roundToInt()) else 0

    override fun serialize(): Map<String, Any?> {
        val unitService = Services.INSTANCE[RPKUnitService::class.java]
        return mapOf(
            "id" to id.value,
            "name" to name,
            "name-plural" to namePlural,
            "sub-ancestries" to subAncestries,
            "dark-vision" to darkVision,
            "minimum-age" to minimumAge,
            "maximum-age" to maximumAge,
            "minimum-height" to unitService.format(minimumHeight * HeightUnit.FEET.scaleFactor, HeightUnit.FEET),
            "maximum-height" to unitService.format(maximumHeight * HeightUnit.FEET.scaleFactor, HeightUnit.FEET),
            "minimum-weight" to unitService.format(minimumWeight * WeightUnit.POUNDS.scaleFactor, WeightUnit.POUNDS),
            "maximum-weight" to unitService.format(maximumWeight * WeightUnit.POUNDS.scaleFactor, WeightUnit.POUNDS),
            "traits" to traits,
            "bonus-hp-per-level" to bonusHpPerLevel,
            "skull-texture" to skullTexture,
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any?>) = TtAncestry(
            (serialized["id"] as String).let(::TtAncestryId),
            serialized["name"] as String,
            serialized["name-plural"] as String,
            serialized["sub-ancestries"] as? List<TtSubAncestry> ?: emptyList(),
            serialized["dark-vision"] as TtDistance,
            serialized["minimum-age"] as Int,
            serialized["maximum-age"] as Int,
            HeightUnit.FEET.parse(serialized["minimum-height"] as String),
            HeightUnit.FEET.parse(serialized["maximum-height"] as String),
            WeightUnit.POUNDS.parse(serialized["minimum-weight"] as String),
            WeightUnit.POUNDS.parse(serialized["maximum-weight"] as String),
            serialized["traits"] as List<TtAncestryTrait>,
            serialized["bonus-hp-per-level"] as? Double,
            serialized["skull-texture"] as String,
        )
    }
}
