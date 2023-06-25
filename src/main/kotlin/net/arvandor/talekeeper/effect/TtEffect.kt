package net.arvandor.talekeeper.effect

import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.prerequisite.TtPrerequisite
import org.bukkit.configuration.serialization.ConfigurationSerializable

interface TtEffect : ConfigurationSerializable {
    val prerequisites: List<TtPrerequisite>
    operator fun invoke(character: TtCharacter): TtCharacter
}
