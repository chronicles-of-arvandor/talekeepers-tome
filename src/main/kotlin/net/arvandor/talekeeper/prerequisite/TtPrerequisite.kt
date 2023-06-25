package net.arvandor.talekeeper.prerequisite

import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.serialization.ConfigurationSerializable

sealed interface TtPrerequisite : ConfigurationSerializable {

    val name: String
    fun isMetBy(character: TtCharacter): Boolean
}
