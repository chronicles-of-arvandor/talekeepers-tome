package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import net.arvandor.talekeeper.skill.TtSkill
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SkillProficiencyEffect")
data class TtSkillProficiencyEffect(
    private val skills: List<TtSkill>,
    override val prerequisites: List<TtPrerequisite>,
) : TtEffect {
    override val name: String
        get() = "Skill proficiency: ${skills.joinToString(", ") { it.name }}"

    override fun invoke(character: TtCharacter): TtCharacter {
        return character.copy(skillProficiencies = character.skillProficiencies + skills)
    }

    override fun serialize() = mapOf(
        "skills" to skills.map(TtSkill::name),
        "prerequisites" to prerequisites,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSkillProficiencyEffect(
            (serialized["skills"] as List<String>).map(TtSkill::valueOf),
            serialized["prerequisites"] as List<TtPrerequisite>,
        )
    }
}
