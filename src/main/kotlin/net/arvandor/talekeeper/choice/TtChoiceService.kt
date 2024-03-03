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
import net.arvandor.talekeeper.failure.ConfigLoadException
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.toServiceFailure
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.util.logging.Level.SEVERE

class TtChoiceService(private val plugin: TalekeepersTome, private val optionRepository: TtChoiceOptionRepository) : Service {

    private val choices = File(plugin.dataFolder, "choices").listFiles()
        ?.map(::loadChoice)
        ?.associateBy(TtChoice::id)
        ?: emptyMap()

    override fun getPlugin() = plugin

    fun getChoice(id: TtChoiceId): TtChoice? = choices[id]

    fun getChosenOptions(characterId: TtCharacterId): Result4k<Map<TtChoiceId, TtChoiceOptionId>, ServiceFailure> = resultFrom {
        optionRepository.getChoiceOptions(characterId)
    }.mapFailure { it.toServiceFailure() }

    fun getChosenOption(character: TtCharacter, choiceId: TtChoiceId): Result4k<TtChoiceOption?, ServiceFailure> = resultFrom {
        val choice = choices[choiceId] ?: return@resultFrom null
        val optionId = character.choiceOptions[choiceId]
        return@resultFrom choice.options.singleOrNull { it.id == optionId }
    }.mapFailure { it.toServiceFailure() }

    fun setChosenOption(characterId: TtCharacterId, choiceId: TtChoiceId, optionId: TtChoiceOptionId?): Result4k<Unit, ServiceFailure> = resultFrom {
        if (optionId == null) {
            optionRepository.delete(characterId, choiceId)
        } else {
            optionRepository.upsert(characterId, choiceId, optionId)
        }
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
            getChosenOption(character, choice.id).onFailure {
                plugin.logger.log(SEVERE, "Failed to get chosen option for character ${character.id} and choice ${choice.id}", it.reason.cause)
                return@filter false
            } == null
        }
    }

    fun getCompletedChoices(character: TtCharacter): List<TtChoice> {
        return getApplicableChoices(character).filter { choice ->
            getChosenOption(character, choice.id).onFailure {
                plugin.logger.log(SEVERE, "Failed to get chosen option for character ${character.id} and choice ${choice.id}", it.reason.cause)
                return@filter false
            } != null
        }
    }

    internal fun displayPendingChoices(player: Player, character: TtCharacter) {
        val choices = getPendingChoices(character)
        if (choices.isNotEmpty()) {
            player.spigot().sendMessage(
                TextComponent("[!] ").apply {
                    color = ChatColor.GRAY
                },
                TextComponent("You have ${choices.size} pending choice${if (choices.size == 1) "" else "s"}. Click here to view them.").apply {
                    color = ChatColor.YELLOW
                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view your pending choices."))
                    clickEvent = ClickEvent(RUN_COMMAND, "/choice list")
                },
            )
        }
    }

    private fun loadChoice(file: File): TtChoice {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("choice", TtChoice::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load choice from ${file.name}", failure.reason)
        }
    }
}
