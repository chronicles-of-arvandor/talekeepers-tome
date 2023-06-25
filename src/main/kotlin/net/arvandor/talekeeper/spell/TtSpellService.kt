package net.arvandor.talekeeper.spell

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
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
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("spell", TtSpell::class.java)!!
    }
}
