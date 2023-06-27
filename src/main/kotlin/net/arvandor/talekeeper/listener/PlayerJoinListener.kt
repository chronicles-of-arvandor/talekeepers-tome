package net.arvandor.talekeeper.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterCreationContext
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.util.logging.Level.SEVERE

class PlayerJoinListener(private val plugin: TalekeepersTome) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService::class.java) ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        val profile = minecraftProfile.profile as? RPKProfile ?: return
        val characterService = Services.INSTANCE.get(TtCharacterService::class.java) ?: return
        asyncTask(plugin) {
            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure { failure ->
                plugin.logger.log(SEVERE, "Failed to retrieve character", failure.reason.cause)
                event.player.sendMessage("${RED}Failed to retrieve character")
                return@asyncTask
            }
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure { failure ->
                plugin.logger.log(SEVERE, "Failed to get character creation context", failure.reason.cause)
                event.player.sendMessage("${RED}Failed to get character creation context")
                return@asyncTask
            }
            if (character == null && ctx == null) {
                characterService.save(
                    TtCharacterCreationContext(
                        plugin,
                        profileId = profile.id,
                        minecraftProfileId = minecraftProfile.id,
                        name = "",
                        pronouns = emptyMap(),
                        ancestryId = null,
                        subAncestryId = null,
                        firstClassId = null,
                        classes = emptyMap(),
                        backgroundId = null,
                        alignment = null,
                        abilityScoreChoices = emptyMap(),
                        experience = 0,
                        description = "",
                        height = null,
                        weight = null,
                        isProfileHidden = false,
                        isNameHidden = false,
                        isPronounsHidden = false,
                        isAgeHidden = false,
                        isAncestryHidden = false,
                        isDescriptionHidden = false,
                        isHeightHidden = false,
                        isWeightHidden = false,
                    ),
                ).onFailure { failure ->
                    plugin.logger.log(SEVERE, "Failed to save character creation context", failure.reason.cause)
                    event.player.sendMessage("${RED}Failed to save character creation context")
                    return@asyncTask
                }.display(event.player)
            }
        }
    }
}
