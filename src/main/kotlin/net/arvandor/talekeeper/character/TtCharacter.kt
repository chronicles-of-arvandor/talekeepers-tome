package net.arvandor.talekeeper.character

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.feat.TtFeatId
import net.arvandor.talekeeper.item.TtItemId
import net.arvandor.talekeeper.language.TtLanguageId
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import net.arvandor.talekeeper.skill.TtSkill
import net.arvandor.talekeeper.speed.TtSpeed
import net.arvandor.talekeeper.spell.TtSpellId
import net.arvandor.talekeeper.trait.TtCharacterTrait
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

data class TtCharacter(
    // basic identity
    val id: TtCharacterId = TtCharacterId.generate(),
    val version: Int = 0,
    val rpkitId: RPKCharacterId = RPKCharacterId(0),
    val profileId: RPKProfileId,
    val minecraftProfileId: RPKMinecraftProfileId?,
    val name: String,
    val pronouns: Map<TtPronounSetId, Int>,
    // d&d
    val ancestryId: TtAncestryId,
    val subAncestryId: TtSubAncestryId?,
    val firstClassId: TtClassId,
    val classes: Map<TtClassId, TtClassInfo>,
    val backgroundId: TtBackgroundId,
    val alignment: TtAlignment,
    val abilityScores: Map<TtAbility, Int>,
    val tempAbilityScores: Map<TtAbility, Int>,
    val hp: Int,
    val tempHp: Int,
    val experience: Int,
    // these are determined from other values, so they're not stored in the repository
    val feats: List<TtFeatId>,
    val spells: List<TtSpellId>,
    val skillProficiencies: List<TtSkill>,
    val itemProficiencies: List<TtItemId>,
    val speed: TtSpeed,
    val languages: List<TtLanguageId>,
    val traits: List<TtCharacterTrait>,
    // non-d&d
    val description: String,
    val height: Double,
    val weight: Double,
    val isDead: Boolean,
    val location: Location,
    val inventoryContents: Array<ItemStack?>,
    val health: Double,
    val foodLevel: Int,
    val exhaustion: Float,
    val saturation: Float,
    val isProfileHidden: Boolean,
    val isNameHidden: Boolean,
    val isPronounsHidden: Boolean,
    val isAgeHidden: Boolean,
    val isAncestryHidden: Boolean,
    val isDescriptionHidden: Boolean,
    val isHeightHidden: Boolean,
    val isWeightHidden: Boolean,
)
