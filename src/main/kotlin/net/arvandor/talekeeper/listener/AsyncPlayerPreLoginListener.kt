package net.arvandor.talekeeper.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.talekeeper.character.TtCharacterService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent

class AsyncPlayerPreLoginListener : Listener {

    @EventHandler
    fun onAsyncPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
            ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.uniqueId).join()
            ?: return
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
            ?: return
        characterService.loadActiveCharacter(minecraftProfile.id)
    }
}
