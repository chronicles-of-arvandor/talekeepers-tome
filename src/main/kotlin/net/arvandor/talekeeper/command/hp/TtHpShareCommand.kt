package net.arvandor.talekeeper.command.hp

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level

class TtHpShareCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        var target: Player? = null
        if (args.isNotEmpty()) {
            if (sender.hasPermission("talekeeper.commands.hp.share.other")) {
                target = plugin.server.getPlayer(args.first())
            }
        }
        if (target == null) {
            target = sender
        }

        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage("${RED}No Minecraft profile service was found. Please contact an admin.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val nearbyPlayers = sender.world.players.filter { player -> player.location.distanceSquared(sender.location) <= 40 * 40 }

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            nearbyPlayers.forEach { player ->
                player.spigot().sendMessage(
                    *buildList {
                        add(
                            TextComponent(character.name).apply {
                                color = YELLOW
                                hoverEvent = HoverEvent(SHOW_TEXT, Text(sender.name))
                            },
                        )
                        add(
                            TextComponent(" has ").apply {
                                color = GRAY
                            },
                        )
                        add(
                            TextComponent("${character.hp}").apply {
                                color = YELLOW
                            },
                        )
                        if (character.tempHp > 0) {
                            add(
                                TextComponent("(+${character.tempHp})").apply {
                                    color = AQUA
                                },
                            )
                        }
                        add(
                            TextComponent(" / ").apply {
                                color = GRAY
                            },
                        )
                        add(
                            TextComponent("${character.maxHp}").apply {
                                color = YELLOW
                            },
                        )
                        if (character.tempHp > 0) {
                            add(
                                TextComponent("(+${character.tempHp})").apply {
                                    color = AQUA
                                },
                            )
                        }
                        add(
                            TextComponent(" HP").apply {
                                color = GRAY
                            },
                        )
                    }.toTypedArray(),
                )
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = emptyList<String>()
}
