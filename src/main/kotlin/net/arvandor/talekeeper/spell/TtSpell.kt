package net.arvandor.talekeeper.spell

import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.condition.TtCondition
import net.arvandor.talekeeper.creature.TtCreatureType
import net.arvandor.talekeeper.damage.TtDamageType
import net.arvandor.talekeeper.spell.attack.TtSpellAttack
import net.arvandor.talekeeper.spell.component.TtSpellComponents
import net.arvandor.talekeeper.spell.duration.TtSpellDuration
import net.arvandor.talekeeper.spell.entry.TtSpellEntry
import net.arvandor.talekeeper.spell.meta.TtSpellMeta
import net.arvandor.talekeeper.spell.range.TtSpellRange
import net.arvandor.talekeeper.spell.scaling.TtSpellScalingLevelDice
import net.arvandor.talekeeper.spell.school.TtSpellSchool
import net.arvandor.talekeeper.spell.tag.TtAreaTag
import net.arvandor.talekeeper.spell.tag.TtMiscTag
import net.arvandor.talekeeper.spell.time.TtSpellTime
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Spell")
data class TtSpell(
    val id: TtSpellId,
    val name: String,
    val source: String,
    val page: Int,
    val srd: Boolean,
    val basicRules: Boolean,
    val level: Int,
    val school: TtSpellSchool,
    val time: List<TtSpellTime>,
    val range: TtSpellRange,
    val components: TtSpellComponents,
    val duration: List<TtSpellDuration>,
    val meta: TtSpellMeta?,
    val entries: List<TtSpellEntry>?,
    val entriesHigherLevel: List<TtSpellEntry>?,
    val scalingLevelDice: List<TtSpellScalingLevelDice>?,
    val damageInflict: List<TtDamageType>?,
    val spellAttack: List<TtSpellAttack>?,
    val conditionInflict: List<TtCondition>?,
    val savingThrow: List<TtAbility>?,
    val affectsCreatureType: List<TtCreatureType>?,
    val miscTags: List<TtMiscTag>?,
    val areaTags: List<TtAreaTag>?,
) : ConfigurationSerializable {
    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "source" to source,
        "page" to page,
        "srd" to srd,
        "basic-rules" to basicRules,
        "level" to level,
        "school" to school.name,
        "time" to time,
        "range" to range,
        "components" to components,
        "duration" to duration,
        "meta" to meta,
        "entries" to entries,
        "entries-higher-level" to entriesHigherLevel,
        "scaling-level-dice" to scalingLevelDice,
        "damage-inflict" to damageInflict?.map(TtDamageType::name),
        "spell-attack" to spellAttack?.map(TtSpellAttack::name),
        "condition-inflict" to conditionInflict?.map(TtCondition::name),
        "saving-throw" to savingThrow?.map(TtAbility::name),
        "affects-creature-type" to affectsCreatureType?.map(TtCreatureType::name),
        "misc-tags" to miscTags?.map(TtMiscTag::name),
        "area-tags" to areaTags?.map(TtAreaTag::name),
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpell(
            (serialized["id"] as String).let(::TtSpellId),
            serialized["name"] as String,
            serialized["source"] as String,
            serialized["page"] as Int,
            serialized["srd"] as? Boolean ?: false,
            serialized["basic-rules"] as? Boolean ?: false,
            serialized["level"] as Int,
            (serialized["school"] as String).let(TtSpellSchool::valueOf),
            serialized["time"] as List<TtSpellTime>,
            serialized["range"] as TtSpellRange,
            serialized["components"] as TtSpellComponents,
            serialized["duration"] as List<TtSpellDuration>,
            serialized["meta"] as? TtSpellMeta?,
            serialized["entries"] as? List<TtSpellEntry>?,
            serialized["entries-higher-level"] as? List<TtSpellEntry>?,
            serialized["scaling-level-dice"] as? List<TtSpellScalingLevelDice>?,
            (serialized["damage-inflict"] as? List<String>)?.map(TtDamageType::valueOf),
            (serialized["spell-attack"] as? List<String>)?.map(TtSpellAttack::valueOf),
            (serialized["condition-inflict"] as? List<String>)?.map(TtCondition::valueOf),
            (serialized["saving-throw"] as? List<String>)?.map(TtAbility::valueOf),
            (serialized["affects-creature-type"] as? List<String>)?.map(TtCreatureType::valueOf),
            (serialized["misc-tags"] as? List<String>)?.map(TtMiscTag::valueOf),
            (serialized["area-tags"] as? List<String>)?.map(TtAreaTag::valueOf),
        )
    }
}
