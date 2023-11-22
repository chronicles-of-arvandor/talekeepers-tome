package net.arvandor.talekeeper.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.mixpanel.TtMixpanelService
import net.arvandor.talekeeper.mixpanel.event.player.TtMixpanelPlayerLeftEvent
import net.arvandor.talekeeper.scheduler.asyncTask
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class PlayerQuitListener(private val plugin: TalekeepersTome) : Listener {

    @EventHandler(priority = MONITOR)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        trackPlayerLeave(event.player.uniqueId)

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services.INSTANCE[TtCharacterService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept { minecraftProfile ->
            if (minecraftProfile == null) return@thenAccept
            // If a player relogs quickly, then by the time the data has been retrieved, the player is sometimes back
            // online. We only want to unload data if the player is offline.
            if (!minecraftProfile.isOnline) {
                characterService.unloadActiveCharacter(minecraftProfile.id)
            }
        }
    }

    private fun trackPlayerLeave(minecraftUuid: UUID) {
        asyncTask(plugin) {
            val mixpanelService = Services.INSTANCE[TtMixpanelService::class.java] ?: return@asyncTask
            mixpanelService.trackEvent(TtMixpanelPlayerLeftEvent(minecraftUuid))
        }
    }
}
