package net.arvandor.talekeeper.effect

import com.rpkit.core.service.Service
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class TtEffectService(private val plugin: TalekeepersTome) : Service {
    override fun getPlugin() = plugin

    private val effects = File(plugin.dataFolder, "effects")
        .listFiles { file -> file.isFile && file.name.endsWith(".yml") }
        ?.map(::loadEffect)
        ?: emptyList()

    fun applyEffects(character: TtCharacter): TtCharacter {
        var updatedCharacter = character
        val effects = effects.toMutableList()
        var effectsSize: Int
        // We need to do multiple passes here because applying an effect may meet the prerequisites of another effect
        do {
            effectsSize = effects.size
            val effectIterator = effects.iterator()
            while (effectIterator.hasNext()) {
                val effect = effectIterator.next()
                if (effect.prerequisites.all { it.isMetBy(character) }) {
                    updatedCharacter = effect(character)
                    effectIterator.remove()
                }
            }
        } while (effects.size < effectsSize)

        return updatedCharacter
    }

    private fun loadEffect(file: File): TtEffect {
        val config = YamlConfiguration.loadConfiguration(file)
        return config.getSerializable("effect", TtEffect::class.java)!!
    }
}
