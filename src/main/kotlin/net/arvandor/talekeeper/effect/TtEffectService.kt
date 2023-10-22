package net.arvandor.talekeeper.effect

import com.rpkit.core.service.Service
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.failure.ConfigLoadException
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
                if (effect.prerequisites.all { it.isMetBy(updatedCharacter) }) {
                    updatedCharacter = effect(updatedCharacter)
                    effectIterator.remove()
                }
            }
        } while (effects.size < effectsSize)

        return updatedCharacter
    }

    fun getApplicableEffects(character: TtCharacter): List<TtEffect> {
        var updatedCharacter = character
        val effects = effects.toMutableList()
        val appliedEffects = mutableListOf<TtEffect>()
        var effectsSize: Int
        // We need to do multiple passes here because applying an effect may meet the prerequisites of another effect
        do {
            effectsSize = effects.size
            val effectIterator = effects.iterator()
            while (effectIterator.hasNext()) {
                val effect = effectIterator.next()
                if (effect.prerequisites.all { it.isMetBy(updatedCharacter) }) {
                    updatedCharacter = effect(updatedCharacter)
                    appliedEffects.add(effect)
                    effectIterator.remove()
                }
            }
        } while (effects.size < effectsSize)

        return appliedEffects
    }

    private fun loadEffect(file: File): TtEffect {
        return resultFrom {
            val config = YamlConfiguration.loadConfiguration(file)
            config.getSerializable("effect", TtEffect::class.java)!!
        }.onFailure { failure ->
            throw ConfigLoadException("Failed to load ancestry from ${file.name}", failure.reason)
        }
    }
}
