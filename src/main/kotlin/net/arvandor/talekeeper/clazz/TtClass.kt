package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Class")
data class TtClass(
    val id: TtClassId,
    val name: String,
    val subClasses: List<TtSubClass>,
    val subClassSelectionLevel: Int,
    val skullTexture: String,
    val baseHp: Int,
    val features: Map<Int, List<TtClassFeature>>,
    val levelsPerCasterLevel: Int?,
) : ConfigurationSerializable {

    fun getSubClass(id: TtSubClassId) = subClasses.firstOrNull { it.id == id }

    fun getSubClass(name: String) = subClasses.firstOrNull { it.name == name }

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "sub-classes" to subClasses,
        "sub-class-selection-level" to subClassSelectionLevel,
        "skull-texture" to skullTexture,
        "base-hp" to baseHp,
        "features" to features.mapKeys { (level, _) -> level.toString() },
        "caster-levels-per-level" to levelsPerCasterLevel,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtClass(
            TtClassId(serialized["id"] as String),
            serialized["name"] as String,
            serialized["sub-classes"] as List<TtSubClass>,
            serialized["sub-class-selection-level"] as Int,
            serialized["skull-texture"] as String,
            serialized["base-hp"] as Int,
            (serialized["features"] as? Map<String, List<TtClassFeature>>)
                ?.mapKeys { (level, _) -> level.toInt() }
                ?: emptyMap(),
            serialized["levels-per-caster-level"] as? Int,
        )
    }
}
