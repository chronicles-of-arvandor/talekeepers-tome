package net.arvandor.talekeeper.ancestry

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtAncestryService(private val plugin: TalekeepersTome) : Service {

    override fun getPlugin() = plugin

    private val ancestries = File(plugin.dataFolder, "ancestries").listFiles()
        ?.map(::loadAncestry)
        ?.associateBy(TtAncestry::id)
        ?: emptyMap()

    fun getAncestry(id: TtAncestryId): TtAncestry? = ancestries[id]

    private fun loadAncestry(file: File): TtAncestry {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("ancestry", TtAncestry::class.java)!!
    }
}
