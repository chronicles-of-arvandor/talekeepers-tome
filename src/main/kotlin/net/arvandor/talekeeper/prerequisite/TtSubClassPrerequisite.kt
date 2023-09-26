package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.clazz.TtSubClassId
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SubClassPrerequisite")
class TtSubClassPrerequisite(val classId: TtClassId, val subclassId: TtSubClassId, val level: Int) : TtPrerequisite {

    override val name
        get() = "Subclass: Lv$level ${Services.INSTANCE.get(TtClassService::class.java).getClass(classId)?.getSubClass(subclassId)?.name}"

    override fun isMetBy(character: TtCharacter): Boolean {
        val classInfo = character.classes[classId] ?: return false
        val subclassId = classInfo.subclassId
        if (subclassId != this.subclassId) return false
        val classLevel = classInfo.level
        return classLevel >= level
    }

    override fun serialize() = mapOf(
        "class-id" to classId.value,
        "sub-class-id" to subclassId.value,
        "level" to level,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSubClassPrerequisite(
            TtClassId(serialized["class-id"] as String),
            TtSubClassId(serialized["sub-class-id"] as String),
            serialized["level"] as Int,
        )
    }
}
