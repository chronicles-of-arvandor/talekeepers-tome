package net.arvandor.talekeeper.character

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.bukkit.extension.ItemStacksKt.toByteArray
import com.rpkit.core.bukkit.extension.ItemStacksKt.toItemStackArray
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.clazz.TtSubClassId
import net.arvandor.talekeeper.failure.OptimisticLockingFailureException
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_ABILITY_SCORE
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CLASS
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_PRONOUNS
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_SPELL_SLOTS
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_TEMP_ABILITY_SCORE
import net.arvandor.talekeeper.jooq.tables.records.TtCharacterRecord
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import net.arvandor.talekeeper.speed.TtSpeed
import net.arvandor.talekeeper.speed.TtSpeedUnit.FEET
import org.bukkit.Bukkit
import org.bukkit.Location
import org.jooq.DSLContext
import java.util.*

class TtCharacterRepository(private val plugin: TalekeepersTome, private val dsl: DSLContext) {

    fun upsert(character: TtCharacter, dsl: DSLContext = this.dsl): TtCharacter {
        return dsl.transactionResult { config ->
            val transactionalDsl = config.dsl()
            val newState = upsertCharacter(transactionalDsl, character)
            deletePronouns(transactionalDsl, character.id)
            val newPronouns = character.pronouns.map { (pronounSetId, weight) -> upsertPronoun(transactionalDsl, character.id, pronounSetId, weight) }.toMap()
            deleteClasses(transactionalDsl, character.id)
            val newClasses = character.classes.map { (clazz, info) -> upsertClass(transactionalDsl, character.id, clazz, info) }.toMap()
            deleteAbilityScores(transactionalDsl, character.id)
            val newAbilityScores = character.baseAbilityScores.map { (ability, score) -> upsertAbilityScore(transactionalDsl, character.id, ability, score) }.toMap()
            deleteTempAbilityScores(transactionalDsl, character.id)
            val newTempAbilityScores = character.tempAbilityScores.map { (ability, score) -> upsertTempAbilityScore(transactionalDsl, character.id, ability, score) }.toMap()
            deleteSpellSlots(transactionalDsl, character.id)
            val newUsedSpellSlots = character.usedSpellSlots.map { (spellSlotLevel, usedSpellSlots) -> upsertSpellSlots(transactionalDsl, character.id, spellSlotLevel, usedSpellSlots) }.toMap()
            return@transactionResult newState.copy(
                pronouns = newPronouns,
                classes = newClasses,
                baseAbilityScores = newAbilityScores,
                tempAbilityScores = newTempAbilityScores,
                usedSpellSlots = newUsedSpellSlots,
            )
        }
    }

    private fun upsertCharacter(dsl: DSLContext, character: TtCharacter): TtCharacter {
        val rowCount = dsl.insertInto(TT_CHARACTER)
            .set(TT_CHARACTER.ID, character.id.value)
            .set(TT_CHARACTER.VERSION, 1)
            .set(TT_CHARACTER.PROFILE_ID, character.profileId.value)
            .set(TT_CHARACTER.MINECRAFT_PROFILE_ID, character.minecraftProfileId?.value)
            .set(TT_CHARACTER.NAME, character.name)
            .set(TT_CHARACTER.ANCESTRY_ID, character.ancestryId.value)
            .set(TT_CHARACTER.SUB_ANCESTRY_ID, character.subAncestryId?.value)
            .set(TT_CHARACTER.FIRST_CLASS_ID, character.firstClassId.value)
            .set(TT_CHARACTER.BACKGROUND_ID, character.backgroundId.value)
            .set(TT_CHARACTER.ALIGNMENT, character.alignment.name)
            .set(TT_CHARACTER.EXPERIENCE, character.experience)
            .set(TT_CHARACTER.HP, character.hp)
            .set(TT_CHARACTER.TEMP_HP, character.tempHp)
            .set(TT_CHARACTER.EXPERIENCE, character.experience)
            .set(TT_CHARACTER.DESCRIPTION, character.description)
            .set(TT_CHARACTER.HEIGHT, character.height)
            .set(TT_CHARACTER.WEIGHT, character.weight)
            .set(TT_CHARACTER.DEAD, character.isDead)
            .set(TT_CHARACTER.WORLD_ID, character.location.world!!.uid.toString())
            .set(TT_CHARACTER.X, character.location.x)
            .set(TT_CHARACTER.Y, character.location.y)
            .set(TT_CHARACTER.Z, character.location.z)
            .set(TT_CHARACTER.YAW, character.location.yaw)
            .set(TT_CHARACTER.PITCH, character.location.pitch)
            .set(TT_CHARACTER.INVENTORY_CONTENTS, toByteArray(character.inventoryContents))
            .set(TT_CHARACTER.HEALTH, character.health)
            .set(TT_CHARACTER.FOOD_LEVEL, character.foodLevel)
            .set(TT_CHARACTER.EXHAUSTION, character.exhaustion)
            .set(TT_CHARACTER.SATURATION, character.saturation)
            .set(TT_CHARACTER.PROFILE_HIDDEN, character.isProfileHidden)
            .set(TT_CHARACTER.NAME_HIDDEN, character.isNameHidden)
            .set(TT_CHARACTER.AGE_HIDDEN, character.isAgeHidden)
            .set(TT_CHARACTER.ANCESTRY_HIDDEN, character.isAncestryHidden)
            .set(TT_CHARACTER.DESCRIPTION_HIDDEN, character.isDescriptionHidden)
            .set(TT_CHARACTER.HEIGHT_HIDDEN, character.isHeightHidden)
            .set(TT_CHARACTER.WEIGHT_HIDDEN, character.isWeightHidden)
            .set(TT_CHARACTER.BIRTHDAY_YEAR, character.birthdayYear)
            .set(TT_CHARACTER.BIRTHDAY_DAY, character.birthdayDay)
            .onConflict(TT_CHARACTER.ID).doUpdate()
            .set(TT_CHARACTER.ID, character.id.value)
            .set(TT_CHARACTER.PROFILE_ID, character.profileId.value)
            .set(TT_CHARACTER.MINECRAFT_PROFILE_ID, character.minecraftProfileId?.value)
            .set(TT_CHARACTER.NAME, character.name)
            .set(TT_CHARACTER.ANCESTRY_ID, character.ancestryId.value)
            .set(TT_CHARACTER.SUB_ANCESTRY_ID, character.subAncestryId?.value)
            .set(TT_CHARACTER.FIRST_CLASS_ID, character.firstClassId.value)
            .set(TT_CHARACTER.BACKGROUND_ID, character.backgroundId.value)
            .set(TT_CHARACTER.ALIGNMENT, character.alignment.name)
            .set(TT_CHARACTER.EXPERIENCE, character.experience)
            .set(TT_CHARACTER.HP, character.hp)
            .set(TT_CHARACTER.TEMP_HP, character.tempHp)
            .set(TT_CHARACTER.EXPERIENCE, character.experience)
            .set(TT_CHARACTER.DESCRIPTION, character.description)
            .set(TT_CHARACTER.HEIGHT, character.height)
            .set(TT_CHARACTER.WEIGHT, character.weight)
            .set(TT_CHARACTER.DEAD, character.isDead)
            .set(TT_CHARACTER.WORLD_ID, character.location.world!!.uid.toString())
            .set(TT_CHARACTER.X, character.location.x)
            .set(TT_CHARACTER.Y, character.location.y)
            .set(TT_CHARACTER.Z, character.location.z)
            .set(TT_CHARACTER.YAW, character.location.yaw)
            .set(TT_CHARACTER.PITCH, character.location.pitch)
            .set(TT_CHARACTER.INVENTORY_CONTENTS, toByteArray(character.inventoryContents))
            .set(TT_CHARACTER.HEALTH, character.health)
            .set(TT_CHARACTER.FOOD_LEVEL, character.foodLevel)
            .set(TT_CHARACTER.EXHAUSTION, character.exhaustion)
            .set(TT_CHARACTER.SATURATION, character.saturation)
            .set(TT_CHARACTER.PROFILE_HIDDEN, character.isProfileHidden)
            .set(TT_CHARACTER.NAME_HIDDEN, character.isNameHidden)
            .set(TT_CHARACTER.AGE_HIDDEN, character.isAgeHidden)
            .set(TT_CHARACTER.ANCESTRY_HIDDEN, character.isAncestryHidden)
            .set(TT_CHARACTER.DESCRIPTION_HIDDEN, character.isDescriptionHidden)
            .set(TT_CHARACTER.HEIGHT_HIDDEN, character.isHeightHidden)
            .set(TT_CHARACTER.WEIGHT_HIDDEN, character.isWeightHidden)
            .set(TT_CHARACTER.BIRTHDAY_YEAR, character.birthdayYear)
            .set(TT_CHARACTER.BIRTHDAY_DAY, character.birthdayDay)
            .set(TT_CHARACTER.VERSION, character.version + 1)
            .where(TT_CHARACTER.ID.eq(character.id.value))
            .and(TT_CHARACTER.VERSION.eq(character.version))
            .execute()
        if (rowCount == 0) throw OptimisticLockingFailureException("Invalid version: ${character.version}")
        return dsl.selectFrom(TT_CHARACTER)
            .where(TT_CHARACTER.ID.eq(character.id.value))
            .fetchOne()
            .let(::requireNotNull)
            .toDomain()
    }

    private fun upsertPronoun(dsl: DSLContext, characterId: TtCharacterId, pronounSetId: TtPronounSetId, weight: Int): Pair<TtPronounSetId, Int> {
        dsl.insertInto(TT_CHARACTER_PRONOUNS)
            .set(TT_CHARACTER_PRONOUNS.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_PRONOUNS.PRONOUN_SET_ID, pronounSetId.value)
            .set(TT_CHARACTER_PRONOUNS.WEIGHT, weight)
            .onConflict(TT_CHARACTER_PRONOUNS.CHARACTER_ID, TT_CHARACTER_PRONOUNS.PRONOUN_SET_ID).doUpdate()
            .set(TT_CHARACTER_PRONOUNS.WEIGHT, weight)
            .where(TT_CHARACTER_PRONOUNS.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_PRONOUNS.PRONOUN_SET_ID.eq(pronounSetId.value))
            .execute()
        return pronounSetId to weight
    }

    private fun upsertClass(dsl: DSLContext, characterId: TtCharacterId, classId: TtClassId, classInfo: TtClassInfo): Pair<TtClassId, TtClassInfo> {
        dsl.insertInto(TT_CHARACTER_CLASS)
            .set(TT_CHARACTER_CLASS.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_CLASS.CLASS_ID, classId.value)
            .set(TT_CHARACTER_CLASS.LEVEL, classInfo.level)
            .set(TT_CHARACTER_CLASS.SUBCLASS_ID, classInfo.subclassId?.value)
            .onConflict(TT_CHARACTER_CLASS.CHARACTER_ID, TT_CHARACTER_CLASS.CLASS_ID).doUpdate()
            .set(TT_CHARACTER_CLASS.LEVEL, classInfo.level)
            .set(TT_CHARACTER_CLASS.SUBCLASS_ID, classInfo.subclassId?.value)
            .where(TT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_CLASS.CLASS_ID.eq(classId.value))
            .execute()
        return classId to classInfo
    }

    private fun upsertAbilityScore(dsl: DSLContext, characterId: TtCharacterId, ability: TtAbility, value: Int): Pair<TtAbility, Int> {
        dsl.insertInto(TT_CHARACTER_ABILITY_SCORE)
            .set(TT_CHARACTER_ABILITY_SCORE.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_ABILITY_SCORE.ABILITY, ability.name)
            .set(TT_CHARACTER_ABILITY_SCORE.SCORE, value)
            .onConflict(TT_CHARACTER_ABILITY_SCORE.CHARACTER_ID, TT_CHARACTER_ABILITY_SCORE.ABILITY).doUpdate()
            .set(TT_CHARACTER_ABILITY_SCORE.SCORE, value)
            .where(TT_CHARACTER_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_ABILITY_SCORE.ABILITY.eq(ability.name))
            .execute()
        return ability to value
    }

    private fun upsertTempAbilityScore(dsl: DSLContext, characterId: TtCharacterId, ability: TtAbility, value: Int): Pair<TtAbility, Int> {
        dsl.insertInto(TT_CHARACTER_TEMP_ABILITY_SCORE)
            .set(TT_CHARACTER_TEMP_ABILITY_SCORE.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_TEMP_ABILITY_SCORE.ABILITY, ability.name)
            .set(TT_CHARACTER_TEMP_ABILITY_SCORE.SCORE, value)
            .onConflict(TT_CHARACTER_TEMP_ABILITY_SCORE.CHARACTER_ID, TT_CHARACTER_TEMP_ABILITY_SCORE.ABILITY).doUpdate()
            .set(TT_CHARACTER_TEMP_ABILITY_SCORE.SCORE, value)
            .where(TT_CHARACTER_TEMP_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_TEMP_ABILITY_SCORE.ABILITY.eq(ability.name))
            .execute()
        return ability to value
    }

    private fun upsertSpellSlots(dsl: DSLContext, characterId: TtCharacterId, spellSlotLevel: Int, usedSpellSlots: Int): Pair<Int, Int> {
        dsl.insertInto(TT_CHARACTER_SPELL_SLOTS)
            .set(TT_CHARACTER_SPELL_SLOTS.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_SPELL_SLOTS.SPELL_SLOT_LEVEL, spellSlotLevel)
            .set(TT_CHARACTER_SPELL_SLOTS.USED_SPELL_SLOTS, usedSpellSlots)
            .onConflict(TT_CHARACTER_SPELL_SLOTS.CHARACTER_ID, TT_CHARACTER_SPELL_SLOTS.SPELL_SLOT_LEVEL).doUpdate()
            .set(TT_CHARACTER_SPELL_SLOTS.USED_SPELL_SLOTS, usedSpellSlots)
            .where(TT_CHARACTER_SPELL_SLOTS.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_SPELL_SLOTS.SPELL_SLOT_LEVEL.eq(spellSlotLevel))
            .execute()
        return spellSlotLevel to usedSpellSlots
    }

    fun delete(id: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER)
            .where(TT_CHARACTER.ID.eq(id.value))
            .execute()
    }

    private fun deletePronouns(dsl: DSLContext, characterId: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER_PRONOUNS)
            .where(TT_CHARACTER_PRONOUNS.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    private fun deleteClasses(dsl: DSLContext, characterId: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER_CLASS)
            .where(TT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    private fun deleteAbilityScores(dsl: DSLContext, characterId: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER_ABILITY_SCORE)
            .where(TT_CHARACTER_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    private fun deleteTempAbilityScores(dsl: DSLContext, characterId: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER_TEMP_ABILITY_SCORE)
            .where(TT_CHARACTER_TEMP_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    private fun deleteSpellSlots(dsl: DSLContext, characterId: TtCharacterId) {
        dsl.deleteFrom(TT_CHARACTER_SPELL_SLOTS)
            .where(TT_CHARACTER_SPELL_SLOTS.CHARACTER_ID.eq(characterId.value))
            .execute()
    }

    fun get(id: TtCharacterId): TtCharacter? = dsl.selectFrom(TT_CHARACTER)
        .where(TT_CHARACTER.ID.eq(id.value))
        .fetchOne()
        ?.toDomain(
            pronouns = getPronouns(id),
            classes = getClasses(id),
            abilityScores = getAbilityScores(id),
            tempAbilityScores = getTempAbilityScores(id),
            choiceOptions = getChoiceOptions(id),
            usedSpellSlots = getUsedSpellSlots(id),
        )

    fun get(rpkitId: RPKCharacterId): TtCharacter? = dsl.selectFrom(TT_CHARACTER)
        .where(TT_CHARACTER.RPKIT_ID.eq(rpkitId.value))
        .fetchOne()
        ?.let {
            val id = TtCharacterId(it.id)
            it.toDomain(
                pronouns = getPronouns(id),
                classes = getClasses(id),
                abilityScores = getAbilityScores(id),
                tempAbilityScores = getTempAbilityScores(id),
                choiceOptions = getChoiceOptions(id),
                usedSpellSlots = getUsedSpellSlots(id),
            )
        }

    fun getActive(minecraftProfileId: RPKMinecraftProfileId): TtCharacter? {
        val character = dsl.selectFrom(TT_CHARACTER)
            .where(TT_CHARACTER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .fetchOne()
            ?.toDomain()
        return character?.copy(
            pronouns = getPronouns(character.id),
            classes = getClasses(character.id),
            baseAbilityScores = getAbilityScores(character.id),
            tempAbilityScores = getTempAbilityScores(character.id),
            choiceOptions = getChoiceOptions(character.id),
            usedSpellSlots = getUsedSpellSlots(character.id),
        )
    }

    fun getAll(profileId: RPKProfileId): List<TtCharacter> {
        return dsl.selectFrom(TT_CHARACTER)
            .where(TT_CHARACTER.PROFILE_ID.eq(profileId.value))
            .fetch()
            .map {
                val id = TtCharacterId(it.id)
                it.toDomain().copy(
                    pronouns = getPronouns(id),
                    classes = getClasses(id),
                    baseAbilityScores = getAbilityScores(id),
                    tempAbilityScores = getTempAbilityScores(id),
                    choiceOptions = getChoiceOptions(id),
                    usedSpellSlots = getUsedSpellSlots(id),
                )
            }
    }

    private fun getPronouns(characterId: TtCharacterId): Map<TtPronounSetId, Int> =
        dsl.selectFrom(TT_CHARACTER_PRONOUNS)
            .where(TT_CHARACTER_PRONOUNS.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result -> result.pronounSetId.let(::TtPronounSetId) to result.weight }
            .toMap()

    private fun getClasses(characterId: TtCharacterId): Map<TtClassId, TtClassInfo> =
        dsl.selectFrom(TT_CHARACTER_CLASS)
            .where(TT_CHARACTER_CLASS.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result ->
                result.classId.let(::TtClassId) to TtClassInfo(
                    level = result.level,
                    subclassId = result.subclassId?.let(::TtSubClassId),
                )
            }.toMap()

    private fun getAbilityScores(characterId: TtCharacterId): Map<TtAbility, Int> =
        dsl.selectFrom(TT_CHARACTER_ABILITY_SCORE)
            .where(TT_CHARACTER_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result ->
                result.ability.let(TtAbility::valueOf) to result.score
            }.toMap()

    private fun getTempAbilityScores(characterId: TtCharacterId): Map<TtAbility, Int> =
        dsl.selectFrom(TT_CHARACTER_TEMP_ABILITY_SCORE)
            .where(TT_CHARACTER_TEMP_ABILITY_SCORE.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result ->
                result.ability.let(TtAbility::valueOf) to result.score
            }.toMap()

    private fun getChoiceOptions(characterId: TtCharacterId): Map<TtChoiceId, TtChoiceOptionId> =
        Services.INSTANCE[TtChoiceService::class.java].getChosenOptions(characterId).onFailure {
            throw it.reason.cause
        }

    private fun getUsedSpellSlots(characterId: TtCharacterId): Map<Int, Int> =
        dsl.selectFrom(TT_CHARACTER_SPELL_SLOTS)
            .where(TT_CHARACTER_SPELL_SLOTS.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result ->
                result.spellSlotLevel to result.usedSpellSlots
            }.toMap()

    private fun TtCharacterRecord.toDomain(
        pronouns: Map<TtPronounSetId, Int> = emptyMap(),
        classes: Map<TtClassId, TtClassInfo> = emptyMap(),
        abilityScores: Map<TtAbility, Int> = emptyMap(),
        tempAbilityScores: Map<TtAbility, Int> = emptyMap(),
        choiceOptions: Map<TtChoiceId, TtChoiceOptionId> = emptyMap(),
        usedSpellSlots: Map<Int, Int> = emptyMap(),
    ) = TtCharacter(
        plugin,
        id = id.let(::TtCharacterId),
        version = version,
        rpkitId = rpkitId.let(::RPKCharacterId),
        profileId = profileId.let(::RPKProfileId),
        minecraftProfileId = minecraftProfileId?.let(::RPKMinecraftProfileId),
        name = name,
        pronouns = pronouns,
        ancestryId = ancestryId.let(::TtAncestryId),
        subAncestryId = subAncestryId?.let(::TtSubAncestryId),
        firstClassId = firstClassId.let(::TtClassId),
        classes = classes,
        backgroundId = backgroundId.let(::TtBackgroundId),
        alignment = alignment.let(TtAlignment::valueOf),
        baseAbilityScores = abilityScores,
        abilityScoreBonuses = emptyMap(),
        tempAbilityScores = tempAbilityScores,
        hp = hp,
        tempHp = tempHp,
        experience = experience,
        usedSpellSlots = usedSpellSlots,
        feats = emptyList(),
        spells = emptyList(),
        skillProficiencies = emptyList(),
        skillExpertise = emptyList(),
        jackOfAllTrades = false,
        initiativeBonus = 0,
        itemProficiencies = emptyList(),
        savingThrowProficiencies = emptyList(),
        speed = TtSpeed(0, FEET),
        languages = emptyList(),
        traits = emptyList(),
        description = description,
        height = height,
        weight = weight,
        isDead = dead,
        location = Location(
            Bukkit.getWorld(worldId.let(UUID::fromString)),
            x,
            y,
            z,
            yaw,
            pitch,
        ),
        inventoryContents = toItemStackArray(inventoryContents),
        health = health,
        foodLevel = foodLevel,
        exhaustion = exhaustion,
        saturation = saturation,
        isProfileHidden = profileHidden,
        isNameHidden = nameHidden,
        isAgeHidden = ageHidden,
        isAncestryHidden = ancestryHidden,
        isDescriptionHidden = descriptionHidden,
        isHeightHidden = heightHidden,
        isWeightHidden = weightHidden,
        birthdayYear = birthdayYear,
        birthdayDay = birthdayDay,
        choiceOptions = choiceOptions,
    )
}
