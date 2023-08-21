package net.arvandor.talekeeper.clazz

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ConfigLoadException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtClassService(private val plugin: TalekeepersTome) : Service {

    init {
        val classFolder = File(plugin.dataFolder, "classes")
        if (!classFolder.exists()) {
            classFolder.mkdirs()
        }
    }

    private val classes = File(plugin.dataFolder, "classes").listFiles()
        ?.map(::loadClass)
        ?.associateBy(TtClass::id)
        ?: emptyMap()

    override fun getPlugin() = plugin

    fun getAll() = classes.values.toList().sortedBy { it.name }

    fun getClass(id: TtClassId): TtClass? {
        return classes[id]
    }

    fun getClass(name: String): TtClass? {
        return classes.values.firstOrNull { it.name == name }
    }

    private fun loadClass(file: File): TtClass {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getObject("class", TtClass::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load class from ${file.name}", failure.reason)
        }
    }

    private fun saveClass(clazz: TtClass, file: File) {
        val config = YamlConfiguration()
        config.set("class", clazz)
        config.save(file)
    }
}
