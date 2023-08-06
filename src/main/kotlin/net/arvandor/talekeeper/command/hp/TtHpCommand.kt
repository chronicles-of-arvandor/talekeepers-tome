package net.arvandor.talekeeper.command.hp

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.logging.Level

class TtHpCommand(private val plugin: TalekeepersTome) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
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

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender).join()
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

            sender.sendMessage("${GREEN}HP")
            sender.spigot().sendMessage(
                *buildList {
                    add(
                        TextComponent("[ - ]").apply {
                            color = RED
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to decrease your HP by 1."))
                            clickEvent = ClickEvent(RUN_COMMAND, "/hp reduce")
                        },
                    )
                    add(TextComponent(" "))
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
                    add(TextComponent(" "))
                    add(
                        TextComponent("[ + ]").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to increase your HP by 1."))
                            clickEvent = ClickEvent(RUN_COMMAND, "/hp increase")
                        },
                    )
                }.toTypedArray(),
            )
        }
        return true
    }
}
