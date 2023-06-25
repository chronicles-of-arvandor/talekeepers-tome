package net.arvandor.talekeeper.character

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.failure.ServiceFailure
import net.arvandor.talekeeper.failure.toServiceFailure

class TtCharacterService(
    private val plugin: TalekeepersTome,
    private val characterRepo: TtCharacterRepository,
    private val characterCreationContextRepo: TtCharacterCreationContextRepository,
) : Service {

    override fun getPlugin() = plugin

    fun getCharacter(id: TtCharacterId): Result4k<TtCharacter?, ServiceFailure> = resultFrom {
        characterRepo.get(id)
    }.mapFailure { it.toServiceFailure() }

    fun getActiveCharacter(minecraftProfileId: RPKMinecraftProfileId): Result4k<TtCharacter?, ServiceFailure> = resultFrom {
        characterRepo.getActive(minecraftProfileId)
    }.mapFailure { it.toServiceFailure() }

    fun save(character: TtCharacter): Result4k<TtCharacter, ServiceFailure> = resultFrom {
        characterRepo.upsert(character)
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

    fun delete(id: TtCharacterCreationContextId): Result4k<Unit, ServiceFailure> = resultFrom<Unit> {
        characterCreationContextRepo.delete(id)
    }.mapFailure { it.toServiceFailure() }
}
