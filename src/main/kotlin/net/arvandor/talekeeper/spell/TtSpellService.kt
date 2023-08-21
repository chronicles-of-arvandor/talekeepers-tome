package net.arvandor.talekeeper.spell

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtSpellService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val spells = File(plugin.dataFolder, "spells").listFiles()
        ?.map(::loadSpell)
        ?.associateBy(TtSpell::id)
        ?: emptyMap()

    fun getSpell(id: TtSpellId): TtSpell? = spells[id]

    private fun loadSpell(file: File): TtSpell {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("spell", TtSpell::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load spell from ${file.name}", failure.reason)
        }
    }
}
