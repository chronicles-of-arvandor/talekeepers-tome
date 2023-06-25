package net.arvandor.talekeeper.clazz

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtClassService(private val plugin: TalekeepersTome) : Service {

    private val classes = File(plugin.dataFolder, "classes").listFiles()
        ?.map(::loadClass)
        ?.associateBy(TtClass::id)
        ?: emptyMap()

    override fun getPlugin() = plugin

    fun getClass(id: TtClassId): TtClass? {
        return classes[id]
    }

    private fun loadClass(file: File): TtClass {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getObject("class", TtClass::class.java)!!
    }
}
