package net.arvandor.talekeeper.clazz

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtClassService(private val plugin: TalekeepersTome) : Service {

    private val defaultClasses = listOf(
        fighter,
        wizard,
    )

    init {
        val classFolder = File(plugin.dataFolder, "classes")
        if (!classFolder.exists()) {
            classFolder.mkdirs()
            defaultClasses.forEach { clazz ->
                saveClass(clazz, File(classFolder, "${clazz.name}.yml"))
            }
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

    private fun loadClass(file: File): TtClass {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("class", TtClass::class.java)!!
    }

    private fun saveClass(clazz: TtClass, file: File) {
        val config = YamlConfiguration()
        config.set("class", clazz)
        config.save(file)
    }
}
