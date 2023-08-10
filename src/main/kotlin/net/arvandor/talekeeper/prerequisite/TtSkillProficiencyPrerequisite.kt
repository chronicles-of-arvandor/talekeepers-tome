package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.skill.TtSkill
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SkillProficiencyPrerequisite")
data class TtSkillProficiencyPrerequisite(
    private val skills: List<TtSkill>,
) : TtPrerequisite {
    override val name: String
        get() = "Skill Proficiency: ${skills.joinToString(", ") { it.displayName }}"

    override fun isMetBy(character: TtCharacter): Boolean {
        return character.skillProficiencies.containsAll(skills)
    }

    override fun serialize() = mapOf(
        "skills" to skills.map(TtSkill::name),
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSkillProficiencyPrerequisite(
            (serialized["skills"] as List<String>).map(TtSkill::valueOf),
        )
    }
}
