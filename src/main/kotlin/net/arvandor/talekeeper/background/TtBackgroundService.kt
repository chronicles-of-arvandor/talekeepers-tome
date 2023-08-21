package net.arvandor.talekeeper.background

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
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
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("background", TtBackground::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load background from ${file.name}", failure.reason)
        }
    }

    private fun saveBackground(background: TtBackground, file: File) {
        val config = YamlConfiguration()
        config.set("background", background)
        config.save(file)
    }
}
