package net.arvandor.talekeeper.character

import com.rpkit.core.service.Service
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.BukkitExtensionsKt
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.effect.TtEffectService
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.ServiceFailureType.GENERAL
import net.arvandor.talekeeper.failure.toServiceFailure
import net.arvandor.talekeeper.scheduler.syncTask
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.BLINDNESS
import org.bukkit.potion.PotionEffectType.SLOW
import org.jooq.DSLContext

class TtCharacterService(
    private val plugin: TalekeepersTome,
    private val characterRepo: TtCharacterRepository,
    private val characterCreationContextRepo: TtCharacterCreationContextRepository,
) : Service {

    override fun getPlugin() = plugin

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

    fun getActiveCharacter(minecraftProfileId: RPKMinecraftProfileId): Result4k<TtCharacter?, ServiceFailure> = resultFrom {
        characterRepo.getActive(minecraftProfileId)
    }.mapFailure { it.toServiceFailure() }

    fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: TtCharacter?): Result4k<Unit, ServiceFailure> {
        val oldCharacter = getActiveCharacter(minecraftProfile.id).onFailure {
            return it
        }

        resultFrom {
            characterRepo.setActive(minecraftProfile.id, character?.id)
        }.mapFailure { it.toServiceFailure() }
            .onFailure { return it }

        syncTask(plugin) {
            BukkitExtensionsKt.toBukkitPlayer(minecraftProfile)?.let { player ->
                if (oldCharacter != null) {
                    save(
                        oldCharacter.copy(
                            inventoryContents = player.inventory.contents,
                            location = player.location,
                            health = player.health,
                            foodLevel = player.foodLevel,
                            exhaustion = player.exhaustion,
                            saturation = player.saturation,
                        ),
                    )

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

        return Success(Unit)
    }

    fun save(character: TtCharacter, dsl: DSLContext = plugin.dsl): Result4k<TtCharacter, ServiceFailure> = resultFrom {
        characterRepo.upsert(character, dsl)
    }.mapFailure { it.toServiceFailure() }

    fun delete(id: TtCharacterId): Result4k<Unit, ServiceFailure> = resultFrom {
        characterRepo.delete(id)
    }.mapFailure { it.toServiceFailure() }

    fun getCreationContext(id: TtCharacterCreationContextId): Result4k<TtCharacterCreationContext?, ServiceFailure> = resultFrom<TtCharacterCreationContext?> {
        characterCreationContextRepo.get(id)
    }.mapFailure { it.toServiceFailure() }

    fun getCreationContext(minecraftProfileId: RPKMinecraftProfileId): Result4k<TtCharacterCreationContext?, ServiceFailure> = resultFrom {
        characterCreationContextRepo.get(minecraftProfileId)
    }.mapFailure { it.toServiceFailure() }

    fun save(characterCreationContext: TtCharacterCreationContext): Result4k<TtCharacterCreationContext, ServiceFailure> = resultFrom<TtCharacterCreationContext> {
        characterCreationContextRepo.upsert(characterCreationContext)
    }.mapFailure { it.toServiceFailure() }

    fun delete(id: TtCharacterCreationContextId, dsl: DSLContext = plugin.dsl): Result4k<Unit, ServiceFailure> = resultFrom<Unit> {
        characterCreationContextRepo.delete(id, dsl)
    }.mapFailure { it.toServiceFailure() }
}
