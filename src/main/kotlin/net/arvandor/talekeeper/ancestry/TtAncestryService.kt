package net.arvandor.talekeeper.ancestry

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtAncestryService(private val plugin: TalekeepersTome) : Service {

    override fun getPlugin() = plugin

    private val defaultAncestries = listOf(
        aasimar,
        gnome,
    )

    init {
        val ancestryFolder = File(plugin.dataFolder, "ancestries")
        if (!ancestryFolder.exists()) {
            ancestryFolder.mkdirs()
            defaultAncestries.forEach { ancestry ->
                saveAncestry(ancestry, File(ancestryFolder, "${ancestry.name}.yml"))
            }
        }
    }

    private val ancestries = File(plugin.dataFolder, "ancestries").listFiles()
        ?.map(::loadAncestry)
        ?.associateBy(TtAncestry::id)
        ?: emptyMap()

    fun getAncestry(id: TtAncestryId): TtAncestry? = ancestries[id]
    fun getAncestry(name: String): TtAncestry? = ancestries.values.find { it.name.equals(name, ignoreCase = true) }

    fun getAll() = ancestries.values.toList()

    private fun loadAncestry(file: File): TtAncestry {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("ancestry", TtAncestry::class.java)!!
    }

    private fun saveAncestry(ancestry: TtAncestry, file: File) {
        val config = YamlConfiguration()
        config.set("ancestry", ancestry)
        config.save(file)
    }
}
