package net.arvandor.talekeeper.choice

import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterId
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.option.TtChoiceOption
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import net.arvandor.talekeeper.choice.option.TtChoiceOptionRepository
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.toServiceFailure
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Level.SEVERE

class TtChoiceService(private val plugin: TalekeepersTome, private val optionRepository: TtChoiceOptionRepository) : Service {

    private val choices = File(plugin.dataFolder, "choices").listFiles()
        ?.map(::loadChoice)
        ?.associateBy(TtChoice::id)
        ?: emptyMap()

    override fun getPlugin() = plugin

    fun getChoice(id: TtChoiceId): TtChoice? = choices[id]

    fun getChosenOption(characterId: TtCharacterId, choiceId: TtChoiceId): Result4k<TtChoiceOption?, ServiceFailure> = resultFrom {
        val choice = choices[choiceId] ?: return@resultFrom null
        val optionId = optionRepository.getChoiceOption(characterId, choiceId)
        return@resultFrom choice.options.singleOrNull { it.id == optionId }
    }.mapFailure { it.toServiceFailure() }

    fun setChosenOption(characterId: TtCharacterId, choiceId: TtChoiceId, optionId: TtChoiceOptionId): Result4k<Unit, ServiceFailure> = resultFrom {
        optionRepository.upsert(characterId, choiceId, optionId)
    }.mapFailure { it.toServiceFailure() }

    fun getApplicableChoices(characterId: TtCharacterId): Result4k<List<TtChoice>, ServiceFailure> {
        val characterService = Services.INSTANCE.get(TtCharacterService::class.java)
        val character = characterService.getCharacter(characterId).onFailure { return it }
            ?: return Success(emptyList())
        return Success(getApplicableChoices(character))
    }

    fun getApplicableChoices(character: TtCharacter): List<TtChoice> {
        return choices.values.filter { it.isApplicableFor(character) }
    }

    fun getPendingChoices(character: TtCharacter): List<TtChoice> {
        return getApplicableChoices(character).filter { choice ->
            getChosenOption(character.id, choice.id).onFailure {
                plugin.logger.log(SEVERE, "Failed to get chosen option for character ${character.id} and choice ${choice.id}", it.reason.cause)
                return@filter false
            } == null
        }
    }

    private fun loadChoice(file: File): TtChoice {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("choice", TtChoice::class.java)!!
    }
}
