package net.arvandor.talekeeper.spell

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
import net.arvandor.talekeeper.util.levenshtein
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtSpellService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    val spells = File(plugin.dataFolder, "spells").listFiles()
        ?.map(::loadSpell)
        ?: emptyList()

    private val spellsById = spells.associateBy(TtSpell::id)

    fun getSpell(id: TtSpellId): TtSpell? = spellsById[id]
    fun getSpell(name: String): TtSpell? = spells.associateWith { spell -> spell.name.levenshtein(name) }
        .toList()
        .sortedBy { (_, levenshtein) -> levenshtein }
        .takeWhile { (_, levenshtein) -> levenshtein <= 10 }
        .map { (spell, _) -> spell }
        .firstOrNull()

    private fun loadSpell(file: File): TtSpell {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            val spell = config.getObject("spell", TtSpell::class.java)!!
            plugin.logger.info(spell.toString())
            spell
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load spell from ${file.name}", failure.reason)
        }
    }
}
