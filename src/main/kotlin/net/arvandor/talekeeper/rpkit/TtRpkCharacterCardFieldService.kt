package net.arvandor.talekeeper.rpkit

import com.rpkit.characters.bukkit.character.field.CharacterCardField
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import net.arvandor.talekeeper.TalekeepersTome

class TtRpkCharacterCardFieldService(private val plugin: TalekeepersTome) : RPKCharacterCardFieldService {
    override fun getPlugin() = plugin

    override fun getCharacterCardFields(): List<CharacterCardField> {
        return emptyList()
    }

    override fun addCharacterCardField(field: CharacterCardField) {
    }

    override fun removeCharacterCardField(field: CharacterCardField) {
    }
}
