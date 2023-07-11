package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Class")
data class TtClass(
    val id: TtClassId,
    val name: String,
    val subClasses: List<TtSubClass>,
    val skullTexture: String,
    val baseHp: Int,
) : ConfigurationSerializable {

    fun getSubClass(id: TtSubClassId) = subClasses.single { it.id == id }

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "sub-classes" to subClasses,
        "skull-texture" to skullTexture,
        "base-hp" to baseHp,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtClass(
            TtClassId(serialized["id"] as String),
            serialized["name"] as String,
            serialized["sub-classes"] as List<TtSubClass>,
            serialized["skull-texture"] as String,
            serialized["base-hp"] as Int,
        )
    }
}
