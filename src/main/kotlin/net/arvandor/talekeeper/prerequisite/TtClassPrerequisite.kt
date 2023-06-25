package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassService
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("ClassPrerequisite")
class TtClassPrerequisite(val classId: TtClassId, val level: Int) : TtPrerequisite {

    override val name
        get() = "Class: Lv$level ${Services.INSTANCE.get(TtClassService::class.java).getClass(classId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        val classLevel = character.classes[classId]?.level ?: return false
        return classLevel >= level
    }

    override fun serialize() = mapOf(
        "class-id" to classId.value,
        "level" to level,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtClassPrerequisite(
            TtClassId(serialized["class-id"] as String),
            serialized["level"] as Int,
        )
    }
}
