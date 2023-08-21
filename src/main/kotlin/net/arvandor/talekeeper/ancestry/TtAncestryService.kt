package net.arvandor.talekeeper.ancestry

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtAncestryService(private val plugin: TalekeepersTome) : Service {

    override fun getPlugin() = plugin

    init {
        val ancestryFolder = File(plugin.dataFolder, "ancestries")
        if (!ancestryFolder.exists()) {
            ancestryFolder.mkdirs()
        }
    }

    private val ancestries = File(plugin.dataFolder, "ancestries").listFiles()
        ?.map(::loadAncestry)
        ?.associateBy(TtAncestry::id)
        ?: emptyMap()

    fun getAncestry(id: TtAncestryId): TtAncestry? = ancestries[id]
    fun getAncestry(name: String): TtAncestry? = ancestries.values.find { it.name.equals(name, ignoreCase = true) }

    fun getAll() = ancestries.values.toList().sortedBy { it.name }

    private fun loadAncestry(file: File): TtAncestry {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("ancestry", TtAncestry::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load ancestry from ${file.name}", failure.reason)
        }
    }

    private fun saveAncestry(ancestry: TtAncestry, file: File) {
        val config = YamlConfiguration()
        config.set("ancestry", ancestry)
        config.save(file)
    }
}
