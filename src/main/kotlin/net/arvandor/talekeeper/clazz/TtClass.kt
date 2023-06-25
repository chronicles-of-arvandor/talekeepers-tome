package net.arvandor.talekeeper.clazz

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Class")
data class TtClass(
    val id: TtClassId,
    val name: String,
    val subClasses: List<TtSubClass>,
) : ConfigurationSerializable {

    fun getSubClass(id: TtSubClassId) = subClasses.single { it.id == id }

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "sub-classes" to subClasses,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtClass(
            TtClassId(serialized["id"] as String),
            serialized["name"] as String,
            serialized["sub-classes"] as List<TtSubClass>,
        )
    }
}
