package net.arvandor.talekeeper.pronouns

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.recover
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.toServiceFailure
import net.arvandor.talekeeper.pronouns.TtPronounSet.Companion.HE_HIM_HIS
import net.arvandor.talekeeper.pronouns.TtPronounSet.Companion.IT_IT_ITS
import net.arvandor.talekeeper.pronouns.TtPronounSet.Companion.SHE_HER_HERS
import net.arvandor.talekeeper.pronouns.TtPronounSet.Companion.THEY_THEM_THEIRS
import kotlin.random.Random

class TtPronounService(private val plugin: TalekeepersTome, private val repo: TtPronounRepository) : Service {

    override fun getPlugin() = plugin

    private val pronounSets = mutableMapOf<TtPronounSetId, TtPronounSet>()

    init {
        save(SHE_HER_HERS)
        save(HE_HIM_HIS)
        save(THEY_THEM_THEIRS)
        save(IT_IT_ITS)
        pronounSets.putAll(getAll().recover { emptyList() }.associateBy(TtPronounSet::id))
    }

    fun save(pronounSet: TtPronounSet): Result4k<TtPronounSet, ServiceFailure> = resultFrom {
        val upsertedPronounSet = repo.upsert(pronounSet)
        pronounSets[upsertedPronounSet.id] = upsertedPronounSet
        return@resultFrom upsertedPronounSet
    }.mapFailure { it.toServiceFailure() }

    fun getAll() = resultFrom {
        repo.getAll()
    }.mapFailure { it.toServiceFailure() }

    fun get(id: TtPronounSetId) = pronounSets[id]

    fun pronoun(character: TtCharacter, pronoun: (TtPronounSet) -> String): String {
        if (character.pronouns.isEmpty()) return character.name
        val weightSum = character.pronouns.values.sum()
        val choice = Random.nextInt(weightSum)
        var sum = 0
        for ((pronounSetId, weight) in character.pronouns) {
            sum += weight
            val pronounSet = pronounSets[pronounSetId] ?: return character.name
            if (sum > choice) return pronoun(pronounSet)
        }
        return character.name
    }
}
