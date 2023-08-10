package net.arvandor.talekeeper.prerequisite

import com.rpkit.core.service.Services
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("ChoicePrerequisite")
data class TtChoicePrerequisite(val choiceId: TtChoiceId, val optionId: TtChoiceOptionId) : TtPrerequisite {
    override val name: String
        get() {
            val choiceService = Services.INSTANCE.get(TtChoiceService::class.java)
            val choice = choiceService.getChoice(choiceId)
            val option = choice?.getOption(optionId)
            return "Choice made: ${choice?.text ?: "Unknown"}: ${option?.text ?: "Unknown"}"
        }

    override fun isMetBy(character: TtCharacter): Boolean {
        val choiceService = Services.INSTANCE.get(TtChoiceService::class.java)
        val option = choiceService.getChosenOption(character.id, choiceId).onFailure {
            return false
        }
        return option?.id == optionId
    }

    override fun serialize() = mapOf(
        "choice-id" to choiceId.value,
        "option-id" to optionId.value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtChoicePrerequisite(
            TtChoiceId(serialized["choice-id"] as String),
            TtChoiceOptionId(serialized["option-id"] as String),
        )
    }
}
