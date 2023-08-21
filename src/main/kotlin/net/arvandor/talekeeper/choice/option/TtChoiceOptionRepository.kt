package net.arvandor.talekeeper.choice.option

import net.arvandor.talekeeper.character.TtCharacterId
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CHOICE_OPTION
import org.jooq.DSLContext

class TtChoiceOptionRepository(private val dsl: DSLContext) {

    fun getChoiceOptions(characterId: TtCharacterId): Map<TtChoiceId, TtChoiceOptionId> =
        dsl.selectFrom(TT_CHARACTER_CHOICE_OPTION)
            .where(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID.eq(characterId.value))
            .fetch()
            .map { result ->
                TtChoiceId(result.choiceId) to TtChoiceOptionId(result.optionId)
            }.toMap()

    fun getChoiceOption(characterId: TtCharacterId, choiceId: TtChoiceId): TtChoiceOptionId? =
        dsl.selectFrom(TT_CHARACTER_CHOICE_OPTION)
            .where(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_CHOICE_OPTION.CHOICE_ID.eq(choiceId.value))
            .fetchOne()
            ?.optionId
            ?.let(::TtChoiceOptionId)

    fun upsert(characterId: TtCharacterId, choiceId: TtChoiceId, optionId: TtChoiceOptionId) {
        dsl.insertInto(TT_CHARACTER_CHOICE_OPTION)
            .set(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID, characterId.value)
            .set(TT_CHARACTER_CHOICE_OPTION.CHOICE_ID, choiceId.value)
            .set(TT_CHARACTER_CHOICE_OPTION.OPTION_ID, optionId.value)
            .onConflict(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID, TT_CHARACTER_CHOICE_OPTION.CHOICE_ID).doUpdate()
            .set(TT_CHARACTER_CHOICE_OPTION.OPTION_ID, optionId.value)
            .where(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_CHOICE_OPTION.CHOICE_ID.eq(choiceId.value))
            .execute()
    }

    fun delete(characterId: TtCharacterId, choiceId: TtChoiceId) {
        dsl.deleteFrom(TT_CHARACTER_CHOICE_OPTION)
            .where(TT_CHARACTER_CHOICE_OPTION.CHARACTER_ID.eq(characterId.value))
            .and(TT_CHARACTER_CHOICE_OPTION.CHOICE_ID.eq(choiceId.value))
            .execute()
    }
}
