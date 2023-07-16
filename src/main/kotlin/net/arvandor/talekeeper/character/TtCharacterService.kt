package net.arvandor.talekeeper.character

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.minecraft.BukkitExtensionsKt
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.peek
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.effect.TtEffectService
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.ServiceFailureType.GENERAL
import net.arvandor.talekeeper.failure.toServiceFailure
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS
import org.bukkit.potion.PotionEffectType.SLOW
import org.jooq.DSLContext
import java.util.concurrent.ConcurrentHashMap

class TtCharacterService(
    private val plugin: TalekeepersTome,
    private val dsl: DSLContext,
    private val characterRepo: TtCharacterRepository,
    private val characterCreationContextRepo: TtCharacterCreationContextRepository,
    private val characterCreationRequestRepo: TtCharacterCreationRequestRepository,
) : Service {

    override fun getPlugin() = plugin

    private val characters = ConcurrentHashMap<TtCharacterId, TtCharacter>()
    private val charactersByRpkitId = ConcurrentHashMap<RPKCharacterId, TtCharacterId>()
    private val activeCharacters = ConcurrentHashMap<RPKMinecraftProfileId, TtCharacterId>()

    var defaultInventory: Array<ItemStack?>
        get() = (plugin.config.getList("characters.defaults.inventory") as? List<ItemStack?>)?.toTypedArray()
            ?: emptyArray<ItemStack?>()
        set(value) {
            plugin.config.set("characters.defaults.inventory", value)
            plugin.saveConfig()
        }

    fun getCharacter(id: TtCharacterId): Result4k<TtCharacter?, ServiceFailure> {
        val character = resultFrom {
            characterRepo.get(id)
        }.mapFailure { it.toServiceFailure() }
            .onFailure { return it }
            ?: return Success(null)

        val effectService = Services.INSTANCE[TtEffectService::class.java]
            ?: return Failure(ServiceFailure(GENERAL, "Effect service not found", RuntimeException("Effect service not found")))

        return Success(effectService.applyEffects(character))
    }

    fun getCharacter(rpkitId: RPKCharacterId): Result4k<TtCharacter?, ServiceFailure> {
        val character = resultFrom {
            characterRepo.get(rpkitId)
        }.mapFailure { it.toServiceFailure() }
            .onFailure { return it }
            ?: return Success(null)

        val effectService = Services.INSTANCE[TtEffectService::class.java]
            ?: return Failure(ServiceFailure(GENERAL, "Effect service not found", RuntimeException("Effect service not found")))

        return Success(effectService.applyEffects(character))
    }

    fun getActiveCharacter(minecraftProfileId: RPKMinecraftProfileId): Result4k<TtCharacter?, ServiceFailure> = resultFrom {
        characterRepo.getActive(minecraftProfileId)
    }.mapFailure { it.toServiceFailure() }

    fun getCharacters(profileId: RPKProfileId): Result4k<List<TtCharacter>, ServiceFailure> = resultFrom {
        characterRepo.getAll(profileId)
    }.mapFailure { it.toServiceFailure() }

    fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: TtCharacter?): Result4k<Unit, ServiceFailure> {
        val oldCharacter = getActiveCharacter(minecraftProfile.id).onFailure {
            return it
        }

        syncTask(plugin) {
            BukkitExtensionsKt.toBukkitPlayer(minecraftProfile)?.let { player ->
                asyncTask(plugin) {
                    resultFrom {
                        dsl.transaction { config ->
                            val transactionalDsl = config.dsl()

                            if (oldCharacter != null) {
                                unloadCharacter(oldCharacter.rpkitId)
                                resultFrom {
                                    characterRepo.upsert(
                                        oldCharacter.copy(
                                            minecraftProfileId = null,
                                            inventoryContents = player.inventory.contents,
                                            location = player.location,
                                            health = player.health,
                                            foodLevel = player.foodLevel,
                                            exhaustion = player.exhaustion,
                                            saturation = player.saturation,
                                        ),
                                        transactionalDsl,
                                    )
                                }.mapFailure { it.toServiceFailure() }
                                    .onFailure {
                                        throw it.reason.cause
                                    }
                            }

                            if (character != null) {
                                val updatedNewCharacter = resultFrom {
                                    characterRepo.upsert(
                                        character.copy(
                                            minecraftProfileId = minecraftProfile.id,
                                        ),
                                    )
                                }.mapFailure { it.toServiceFailure() }
                                    .onFailure {
                                        throw it.reason.cause
                                    }

                                characters[updatedNewCharacter.id] = updatedNewCharacter
                                charactersByRpkitId[updatedNewCharacter.rpkitId] = updatedNewCharacter.id
                                if (updatedNewCharacter.minecraftProfileId != null) {
                                    activeCharacters[updatedNewCharacter.minecraftProfileId] = updatedNewCharacter.id
                                }
                            }
                        }
                    }.peek {
                        syncTask(plugin) {
                            if (oldCharacter != null) {
                                if (oldCharacter.isDead) {
                                    player.removePotionEffect(BLINDNESS)
                                    player.removePotionEffect(SLOW)
                                }
                            }

                            if (character != null) {
                                player.inventory.contents = character.inventoryContents
                                player.teleport(character.location)
                                player.foodLevel = character.foodLevel
                                player.exhaustion = character.exhaustion
                                player.saturation = character.saturation

                                if (character.isDead) {
                                    player.addPotionEffect(PotionEffect(BLINDNESS, 1000000, 0))
                                    player.addPotionEffect(PotionEffect(SLOW, 1000000, 255))
                                }
                            }
                        }
                    }
                }
            }
        }

        return Success(Unit)
    }

    fun save(character: TtCharacter, dsl: DSLContext = plugin.dsl): Result4k<TtCharacter, ServiceFailure> = resultFrom {
        characterRepo.upsert(character, dsl)
    }.mapFailure { it.toServiceFailure() }
        .peek { upsertedCharacter ->
            characters[upsertedCharacter.id] = upsertedCharacter
            charactersByRpkitId[upsertedCharacter.rpkitId] = upsertedCharacter.id
            if (upsertedCharacter.minecraftProfileId != null) {
                activeCharacters[upsertedCharacter.minecraftProfileId] = upsertedCharacter.id
            }
        }

    fun delete(id: TtCharacterId): Result4k<Unit, ServiceFailure> = resultFrom {
        characterRepo.delete(id)
    }.mapFailure { it.toServiceFailure() }
        .peek {
            characters.remove(id)?.let { character ->
                charactersByRpkitId.remove(character.rpkitId)
                if (character.minecraftProfileId != null) {
                    activeCharacters.remove(character.minecraftProfileId)
                }
            }
        }

    fun getCreationContext(id: TtCharacterCreationContextId): Result4k<TtCharacterCreationContext?, ServiceFailure> = resultFrom {
        characterCreationContextRepo.get(id)
    }.mapFailure { it.toServiceFailure() }

    fun getCreationContext(minecraftProfileId: RPKMinecraftProfileId): Result4k<TtCharacterCreationContext?, ServiceFailure> = resultFrom {
        characterCreationContextRepo.get(minecraftProfileId)
    }.mapFailure { it.toServiceFailure() }

    fun save(characterCreationContext: TtCharacterCreationContext): Result4k<TtCharacterCreationContext, ServiceFailure> = resultFrom {
        characterCreationContextRepo.upsert(characterCreationContext)
    }.mapFailure { it.toServiceFailure() }

    fun delete(id: TtCharacterCreationContextId, dsl: DSLContext = plugin.dsl): Result4k<Unit, ServiceFailure> = resultFrom {
        characterCreationContextRepo.delete(id, dsl)
    }.mapFailure { it.toServiceFailure() }

    fun getCreationRequest(id: RPKMinecraftProfileId): Result4k<TtCharacterCreationRequest?, ServiceFailure> = resultFrom {
        characterCreationRequestRepo.getCharacterCreationRequest(id)
    }.mapFailure { it.toServiceFailure() }

    fun getCreationRequests() = resultFrom {
        characterCreationRequestRepo.getAll()
    }.mapFailure { it.toServiceFailure() }

    fun save(characterCreationRequest: TtCharacterCreationRequest): Result4k<TtCharacterCreationRequest, ServiceFailure> = resultFrom {
        characterCreationRequestRepo.upsert(characterCreationRequest)
    }.mapFailure { it.toServiceFailure() }

    fun deleteCreationRequest(id: RPKMinecraftProfileId): Result4k<Unit, ServiceFailure> = resultFrom {
        characterCreationRequestRepo.delete(id)
    }.mapFailure { it.toServiceFailure() }

    // This stuff is mostly for RPKit, so we don't expose it outside the plugin, aside from through the RPKit character service

    internal fun getPreloadedCharacter(rpkitId: RPKCharacterId): TtCharacter? {
        val id = charactersByRpkitId[rpkitId] ?: return null
        return characters[id]
    }

    internal fun getPreloadedActiveCharacter(minecraftProfileId: RPKMinecraftProfileId): TtCharacter? {
        return activeCharacters[minecraftProfileId]?.let { characters[it] }
    }

    internal fun loadCharacter(id: RPKCharacterId): Result4k<TtCharacter?, ServiceFailure> {
        val character = resultFrom {
            characterRepo.get(id)
        }.mapFailure { it.toServiceFailure() }
            .onFailure { return it }
            ?: return Success(null)

        characters[character.id] = character
        charactersByRpkitId[character.rpkitId] = character.id
        if (character.minecraftProfileId != null) {
            activeCharacters[character.minecraftProfileId] = character.id
        }

        return Success(character)
    }

    internal fun loadActiveCharacter(minecraftProfileId: RPKMinecraftProfileId): TtCharacter? {
        val character = characterRepo.getActive(minecraftProfileId)
        if (character != null) {
            characters[character.id] = character
            charactersByRpkitId[character.rpkitId] = character.id
            if (character.minecraftProfileId != null) {
                activeCharacters[character.minecraftProfileId] = character.id
            }
        }
        return character
    }

    internal fun unloadCharacter(rpkitId: RPKCharacterId) {
        val id = charactersByRpkitId.remove(rpkitId)
        if (id != null) {
            val character = characters.remove(id)
            if (character?.minecraftProfileId != null) {
                activeCharacters.remove(character.minecraftProfileId)
            }
        }
    }

    internal fun unloadActiveCharacter(minecraftProfileId: RPKMinecraftProfileId) {
        val id = activeCharacters.remove(minecraftProfileId)
        if (id != null) {
            val character = characters.remove(id)
            if (character?.rpkitId != null) {
                charactersByRpkitId.remove(character.rpkitId)
            }
        }
    }
}
