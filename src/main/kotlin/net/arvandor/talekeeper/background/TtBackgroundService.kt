package net.arvandor.talekeeper.background

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtBackgroundService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val defaultBackgrounds = listOf(
        acolyte,
        charlatan,
        criminal,
        entertainer,
        folkHero,
    )

    init {
        val backgroundFolder = File(plugin.dataFolder, "backgrounds")
        if (!backgroundFolder.exists()) {
            backgroundFolder.mkdirs()
            defaultBackgrounds.forEach { background ->
                saveBackground(background, File(backgroundFolder, "${background.name}.yml"))
            }
        }
    }

    private val backgrounds = File(plugin.dataFolder, "backgrounds").listFiles()
        ?.map(::loadBackground)
        ?.associateBy(TtBackground::id)
        ?: emptyMap()

    fun getAll() = backgrounds.values.toList().sortedBy { it.name }

    fun getBackground(id: TtBackgroundId) = backgrounds[id]
    fun getBackground(name: String) = backgrounds.values.singleOrNull { it.name.equals(name, ignoreCase = true) }

    private fun loadBackground(file: File): TtBackground {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("background", TtBackground::class.java)!!
    }

    private fun saveBackground(background: TtBackground, file: File) {
        val config = YamlConfiguration()
        config.set("background", background)
        config.save(file)
    }
}
