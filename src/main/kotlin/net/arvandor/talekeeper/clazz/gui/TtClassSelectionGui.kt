package net.arvandor.talekeeper.clazz.gui

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.page
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import java.util.logging.Level

class TtClassSelectionGui(plugin: TalekeepersTome, classes: List<TtClass>) : InventoryGui(
    plugin,
    "Select class",
    page {
        classes.forEachIndexed { slot, clazz ->
            icon(slot) {
                item = skull(
                    clazz.name,
                    emptyList(),
                    clazz.id.value,
                    clazz.skullTexture,
                )

                onClick = onSelectClass@{ player, _ ->
                    val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
                    if (minecraftProfileService == null) {
                        player.sendMessage("${ChatColor.RED}No Minecraft profile service was found. Please contact an admin.")
                        return@onSelectClass false
                    }

                    val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
                    if (minecraftProfile == null) {
                        player.sendMessage("${ChatColor.RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                        return@onSelectClass false
                    }

                    val characterService = Services.INSTANCE[TtCharacterService::class.java]
                    if (characterService == null) {
                        player.sendMessage("${ChatColor.RED}No character service was found. Please contact an admin.")
                        return@onSelectClass false
                    }

                    asyncTask(plugin) {
                        val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                            player.sendMessage("${ChatColor.RED}An error occurred while getting your character creation context.")
                            plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                            return@asyncTask
                        }
                        if (ctx == null) {
                            player.sendMessage("${ChatColor.RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                            return@asyncTask
                        }

                        val updatedCtx = characterService.save(
                            ctx.copy(
                                firstClassId = clazz.id,
                                classes = mapOf(
                                    clazz.id to TtClassInfo(
                                        1,
                                        null,
                                    ),
                                ),
                            ),
                        ).onFailure {
                            player.sendMessage("${ChatColor.RED}Failed to save character creation context. Please contact an admin.")
                            plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                            return@asyncTask
                        }

                        player.sendMessage(
                            "${ChatColor.GRAY}================================",
                            "${ChatColor.GREEN}Class set to ${clazz.name}.",
                            "${ChatColor.GRAY}================================",
                        )

                        updatedCtx.display(player)
                    }
                    false
                }
            }
        }
    },
)
