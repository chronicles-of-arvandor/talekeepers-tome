package net.arvandor.talekeeper.character

import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.clazz.TtSubClassId
import net.arvandor.talekeeper.failure.OptimisticLockingFailureException
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CREATION_CONTEXT
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CREATION_CONTEXT_CLASS
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET
import net.arvandor.talekeeper.jooq.tables.records.TtCharacterCreationContextRecord
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import org.jooq.DSLContext

class TtCharacterCreationContextRepository(private val plugin: TalekeepersTome, private val dsl: DSLContext) {

    fun upsert(ctx: TtCharacterCreationContext): TtCharacterCreationContext {
        return dsl.transactionResult { config ->
            val transactionalDsl = config.dsl()
            val upsertedContext = upsertContext(transactionalDsl, ctx)
            deletePronouns(transactionalDsl, ctx.id)
            val newPronouns = ctx.pronouns.map { (pronounSetId, weight) -> upsertPronoun(transactionalDsl, ctx.id, pronounSetId, weight) }.toMap()
            deleteClasses(transactionalDsl, ctx.id)
            val newClasses = ctx.classes.map { (clazz, info) -> upsertClass(transactionalDsl, ctx.id, clazz, info) }.toMap()
            deleteAbilityScoreChoices(transactionalDsl, ctx.id)
            val newAbilityScores = ctx.abilityScoreChoices.map { (ability, score) -> upsertAbilityScoreChoice(transactionalDsl, ctx.id, ability, score) }.toMap()
            return@transactionResult upsertedContext.copy(
                pronouns = newPronouns,
                classes = newClasses,
                abilityScoreChoices = newAbilityScores,
            )
        }
    }

    fun upsertContext(dsl: DSLContext, ctx: TtCharacterCreationContext): TtCharacterCreationContext {
        val rowCount = dsl.insertInto(TT_CHARACTER_CREATION_CONTEXT)
            .set(TT_CHARACTER_CREATION_CONTEXT.ID, ctx.id.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.VERSION, 1)
            .set(TT_CHARACTER_CREATION_CONTEXT.PROFILE_ID, ctx.profileId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.MINECRAFT_PROFILE_ID, ctx.minecraftProfileId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.NAME, ctx.name)
            .set(TT_CHARACTER_CREATION_CONTEXT.ANCESTRY_ID, ctx.ancestryId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.SUB_ANCESTRY_ID, ctx.subAncestryId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.FIRST_CLASS_ID, ctx.firstClassId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.BACKGROUND_ID, ctx.backgroundId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.ALIGNMENT, ctx.alignment?.name)
            .set(TT_CHARACTER_CREATION_CONTEXT.EXPERIENCE, ctx.experience)
            .set(TT_CHARACTER_CREATION_CONTEXT.DESCRIPTION, ctx.description)
            .set(TT_CHARACTER_CREATION_CONTEXT.HEIGHT, ctx.height)
            .set(TT_CHARACTER_CREATION_CONTEXT.WEIGHT, ctx.weight)
            .set(TT_CHARACTER_CREATION_CONTEXT.PROFILE_HIDDEN, ctx.isProfileHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.NAME_HIDDEN, ctx.isNameHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.PRONOUNS_HIDDEN, ctx.isPronounsHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.AGE_HIDDEN, ctx.isAgeHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.ANCESTRY_HIDDEN, ctx.isAncestryHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.DESCRIPTION_HIDDEN, ctx.isDescriptionHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.HEIGHT_HIDDEN, ctx.isHeightHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.WEIGHT_HIDDEN, ctx.isWeightHidden)
            .onConflict(TT_CHARACTER_CREATION_CONTEXT.ID).doUpdate()
            .set(TT_CHARACTER_CREATION_CONTEXT.ID, ctx.id.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.PROFILE_ID, ctx.profileId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.MINECRAFT_PROFILE_ID, ctx.minecraftProfileId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.NAME, ctx.name)
            .set(TT_CHARACTER_CREATION_CONTEXT.ANCESTRY_ID, ctx.ancestryId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.SUB_ANCESTRY_ID, ctx.subAncestryId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.FIRST_CLASS_ID, ctx.firstClassId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.BACKGROUND_ID, ctx.backgroundId?.value)
            .set(TT_CHARACTER_CREATION_CONTEXT.ALIGNMENT, ctx.alignment?.name)
            .set(TT_CHARACTER_CREATION_CONTEXT.EXPERIENCE, ctx.experience)
            .set(TT_CHARACTER_CREATION_CONTEXT.DESCRIPTION, ctx.description)
            .set(TT_CHARACTER_CREATION_CONTEXT.HEIGHT, ctx.height)
            .set(TT_CHARACTER_CREATION_CONTEXT.WEIGHT, ctx.weight)
            .set(TT_CHARACTER_CREATION_CONTEXT.PROFILE_HIDDEN, ctx.isProfileHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.NAME_HIDDEN, ctx.isNameHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.PRONOUNS_HIDDEN, ctx.isPronounsHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.AGE_HIDDEN, ctx.isAgeHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.ANCESTRY_HIDDEN, ctx.isAncestryHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.DESCRIPTION_HIDDEN, ctx.isDescriptionHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.HEIGHT_HIDDEN, ctx.isHeightHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.WEIGHT_HIDDEN, ctx.isWeightHidden)
            .set(TT_CHARACTER_CREATION_CONTEXT.VERSION, ctx.version + 1)
            .where(TT_CHARACTER_CREATION_CONTEXT.ID.eq(ctx.id.value))
            .and(TT_CHARACTER_CREATION_CONTEXT.VERSION.eq(ctx.version))
            .execute()
        if (rowCount == 0) throw OptimisticLockingFailureException("Invalid version: ${ctx.version}")
        return dsl.selectFrom(TT_CHARACTER_CREATION_CONTEXT)
            .where(TT_CHARACTER_CREATION_CONTEXT.ID.eq(ctx.id.value))
            .fetchOne()
            .let(::requireNotNull)
            .toDomain()
    }

    private fun deletePronouns(dsl: DSLContext, ctxId: TtCharacterCreationContextId) {
        dsl.deleteFrom(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET)
            .where(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .execute()
    }

    private fun upsertPronoun(dsl: DSLContext, ctxId: TtCharacterCreationContextId, pronounSetId: TtPronounSetId, weight: Int): Pair<TtPronounSetId, Int> {
        dsl.insertInto(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET)
            .set(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.CHARACTER_CREATION_CONTEXT_ID, ctxId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.PRONOUN_SET_ID, pronounSetId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.WEIGHT, weight)
            .onConflict(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.CHARACTER_CREATION_CONTEXT_ID, TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.PRONOUN_SET_ID)
            .doUpdate()
            .set(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.WEIGHT, weight)
            .where(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .and(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.PRONOUN_SET_ID.eq(pronounSetId.value))
            .execute()
        return pronounSetId to weight
    }

    private fun deleteClasses(dsl: DSLContext, ctxId: TtCharacterCreationContextId) {
        dsl.deleteFrom(TT_CHARACTER_CREATION_CONTEXT_CLASS)
            .where(TT_CHARACTER_CREATION_CONTEXT_CLASS.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .execute()
    }

    private fun upsertClass(dsl: DSLContext, ctxId: TtCharacterCreationContextId, classId: TtClassId, classInfo: TtClassInfo): Pair<TtClassId, TtClassInfo> {
        dsl.insertInto(TT_CHARACTER_CREATION_CONTEXT_CLASS)
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.CHARACTER_CREATION_CONTEXT_ID, ctxId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.CLASS_ID, classId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.LEVEL, classInfo.level)
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.SUBCLASS_ID, classInfo.subclassId?.value)
            .onConflict(TT_CHARACTER_CREATION_CONTEXT_CLASS.CHARACTER_CREATION_CONTEXT_ID, TT_CHARACTER_CREATION_CONTEXT_CLASS.CLASS_ID)
            .doUpdate()
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.LEVEL, classInfo.level)
            .set(TT_CHARACTER_CREATION_CONTEXT_CLASS.SUBCLASS_ID, classInfo.subclassId?.value)
            .where(TT_CHARACTER_CREATION_CONTEXT_CLASS.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .and(TT_CHARACTER_CREATION_CONTEXT_CLASS.CLASS_ID.eq(classId.value))
            .execute()
        return classId to classInfo
    }

    private fun deleteAbilityScoreChoices(dsl: DSLContext, ctxId: TtCharacterCreationContextId) {
        dsl.deleteFrom(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE)
            .where(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .execute()
    }

    private fun upsertAbilityScoreChoice(dsl: DSLContext, ctxId: TtCharacterCreationContextId, ability: TtAbility, choice: Int): Pair<TtAbility, Int> {
        dsl.insertInto(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE)
            .set(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHARACTER_CREATION_CONTEXT_ID, ctxId.value)
            .set(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.ABILITY, ability.name)
            .set(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHOICE, choice)
            .onConflict(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHARACTER_CREATION_CONTEXT_ID, TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.ABILITY)
            .doUpdate()
            .set(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHOICE, choice)
            .where(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHARACTER_CREATION_CONTEXT_ID.eq(ctxId.value))
            .and(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.ABILITY.eq(ability.name))
            .execute()
        return ability to choice
    }

    fun get(id: TtCharacterCreationContextId) = dsl.selectFrom(TT_CHARACTER_CREATION_CONTEXT)
        .where(TT_CHARACTER_CREATION_CONTEXT.ID.eq(id.value))
        .fetchOne()
        ?.toDomain(
            pronouns = getPronouns(id),
            classes = getClasses(id),
            abilityScoreChoices = getAbilityScoreChoices(id),
        )

    fun get(minecraftProfileId: RPKMinecraftProfileId): TtCharacterCreationContext? {
        val result = dsl.selectFrom(TT_CHARACTER_CREATION_CONTEXT)
            .where(TT_CHARACTER_CREATION_CONTEXT.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .fetchOne() ?: return null
        val ctxId = result.id.let(::TtCharacterCreationContextId)
        return result.toDomain(
            pronouns = getPronouns(ctxId),
            classes = getClasses(ctxId),
            abilityScoreChoices = getAbilityScoreChoices(ctxId),
        )
    }

    private fun getPronouns(id: TtCharacterCreationContextId) = dsl.selectFrom(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET)
        .where(TT_CHARACTER_CREATION_CONTEXT_PRONOUN_SET.CHARACTER_CREATION_CONTEXT_ID.eq(id.value))
        .fetch()
        .map { result -> result.pronounSetId.let(::TtPronounSetId) to result.weight }
        .toMap()

    private fun getClasses(id: TtCharacterCreationContextId) = dsl.selectFrom(TT_CHARACTER_CREATION_CONTEXT_CLASS)
        .where(TT_CHARACTER_CREATION_CONTEXT_CLASS.CHARACTER_CREATION_CONTEXT_ID.eq(id.value))
        .fetch()
        .map { result ->
            result.classId.let(::TtClassId) to TtClassInfo(
                level = result.level,
                subclassId = result.subclassId?.let(::TtSubClassId),
            )
        }.toMap()

    private fun getAbilityScoreChoices(id: TtCharacterCreationContextId) = dsl.selectFrom(
        TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE,
    ).where(TT_CHARACTER_CREATION_CONTEXT_ABILITY_SCORE_CHOICE.CHARACTER_CREATION_CONTEXT_ID.eq(id.value))
        .fetch()
        .map { result ->
            result.ability.let(TtAbility::valueOf) to result.choice
        }.toMap()

    fun delete(id: TtCharacterCreationContextId, dsl: DSLContext = this.dsl) {
        dsl.deleteFrom(TT_CHARACTER_CREATION_CONTEXT)
            .where(TT_CHARACTER_CREATION_CONTEXT.ID.eq(id.value))
            .execute()
    }

    private fun TtCharacterCreationContextRecord.toDomain(
        pronouns: Map<TtPronounSetId, Int> = emptyMap(),
        classes: Map<TtClassId, TtClassInfo> = emptyMap(),
        abilityScoreChoices: Map<TtAbility, Int> = emptyMap(),
    ) = TtCharacterCreationContext(
        plugin,
        id.let(::TtCharacterCreationContextId),
        version,
        profileId.let(::RPKProfileId),
        minecraftProfileId.let(::RPKMinecraftProfileId),
        name,
        pronouns,
        ancestryId?.let(::TtAncestryId),
        subAncestryId?.let(::TtSubAncestryId),
        firstClassId?.let(::TtClassId),
        classes,
        backgroundId?.let(::TtBackgroundId),
        alignment?.let(TtAlignment::valueOf),
        abilityScoreChoices,
        experience,
        description,
        height,
        weight,
        profileHidden,
        nameHidden,
        pronounsHidden,
        ageHidden,
        ancestryHidden,
        descriptionHidden,
        heightHidden,
        weightHidden,
    )
}
