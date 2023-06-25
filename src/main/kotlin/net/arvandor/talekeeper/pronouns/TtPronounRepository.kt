package net.arvandor.talekeeper.pronouns

import net.arvandor.talekeeper.jooq.Tables.TT_PRONOUN_SET
import net.arvandor.talekeeper.jooq.tables.records.TtPronounSetRecord
import net.arvandor.talekeeper.player.TtPlayerId
import org.jooq.DSLContext

class TtPronounRepository(private val dsl: DSLContext) {

    fun upsert(pronounSet: TtPronounSet): TtPronounSet {
        dsl.insertInto(TT_PRONOUN_SET)
            .set(TT_PRONOUN_SET.ID, pronounSet.id.value)
            .set(TT_PRONOUN_SET.NAME, pronounSet.name)
            .set(TT_PRONOUN_SET.CREATED_BY, pronounSet.createdBy?.value)
            .set(TT_PRONOUN_SET.SUBJECT, pronounSet.subject)
            .set(TT_PRONOUN_SET.OBJECT, pronounSet.`object`)
            .set(TT_PRONOUN_SET.DEPENDENT_POSSESSIVE, pronounSet.dependentPossessive)
            .set(TT_PRONOUN_SET.INDEPENDENT_POSSESSIVE, pronounSet.independentPossessive)
            .set(TT_PRONOUN_SET.REFLEXIVE, pronounSet.reflexive)
            .onConflict(TT_PRONOUN_SET.ID).doUpdate()
            .set(TT_PRONOUN_SET.ID, pronounSet.id.value)
            .set(TT_PRONOUN_SET.NAME, pronounSet.name)
            .set(TT_PRONOUN_SET.CREATED_BY, pronounSet.createdBy?.value)
            .set(TT_PRONOUN_SET.SUBJECT, pronounSet.subject)
            .set(TT_PRONOUN_SET.OBJECT, pronounSet.`object`)
            .set(TT_PRONOUN_SET.DEPENDENT_POSSESSIVE, pronounSet.dependentPossessive)
            .set(TT_PRONOUN_SET.INDEPENDENT_POSSESSIVE, pronounSet.independentPossessive)
            .set(TT_PRONOUN_SET.REFLEXIVE, pronounSet.reflexive)
            .where(TT_PRONOUN_SET.ID.eq(pronounSet.id.value))
            .execute()
        return dsl.selectFrom(TT_PRONOUN_SET)
            .where(TT_PRONOUN_SET.ID.eq(pronounSet.id.value))
            .fetchOne()
            .let(::requireNotNull)
            .toDomain()
    }

    fun getAll(): List<TtPronounSet> = dsl.selectFrom(TT_PRONOUN_SET)
        .fetch()
        .map { it.toDomain() }

    private fun TtPronounSetRecord.toDomain() = TtPronounSet(
        id.let(::TtPronounSetId),
        name,
        createdBy.let(::TtPlayerId),
        subject,
        `object`,
        dependentPossessive,
        independentPossessive,
        reflexive,
    )
}
