package net.arvandor.talekeeper.ancestry

import net.arvandor.talekeeper.distance.TtDistance
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Ancestry")
data class TtAncestry(
    val id: TtAncestryId,
    val name: String,
    val subAncestries: List<TtSubAncestry>,
    val darkVision: TtDistance,
    val minimumAge: Int,
    val maximumAge: Int,
    val minimumHeight: Double,
    val maximumHeight: Double,
    val minimumWeight: Double,
    val maximumWeight: Double,
    val traits: List<TtAncestryTrait>,
    val skullTexture: String,
) : ConfigurationSerializable {

    fun getSubAncestry(id: TtSubAncestryId) = subAncestries.singleOrNull { it.id == id }

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "sub-ancestries" to subAncestries,
        "dark-vision" to darkVision,
        "minimum-age" to minimumAge,
        "maximum-age" to maximumAge,
        "minimum-height" to minimumHeight,
        "maximum-height" to maximumHeight,
        "minimum-weight" to minimumWeight,
        "maximum-weight" to maximumWeight,
        "traits" to traits,
        "skull-texture" to skullTexture,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtAncestry(
            (serialized["id"] as String).let(::TtAncestryId),
            serialized["name"] as String,
            serialized["sub-ancestries"] as? List<TtSubAncestry> ?: emptyList(),
            serialized["dark-vision"] as TtDistance,
            serialized["minimum-age"] as Int,
            serialized["maximum-age"] as Int,
            serialized["minimum-height"] as Double,
            serialized["maximum-height"] as Double,
            serialized["minimum-weight"] as Double,
            serialized["maximum-weight"] as Double,
            serialized["traits"] as List<TtAncestryTrait>,
            serialized["skull-texture"] as String,
        )
    }
}
