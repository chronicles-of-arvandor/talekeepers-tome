package net.arvandor.talekeeper.command.cast

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.arvandor.talekeeper.spell.TtSpell
import net.arvandor.talekeeper.spell.TtSpellId
import net.arvandor.talekeeper.spell.TtSpellService
import net.md_5.bungee.api.ChatColor.GRAY
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
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCastCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    // Usage: /cast [spell] [level]
    // Display the spell to players nearby, giving them the ability to click on the spell and display its details.
    // Additionally, consume the spell slots of the given level.
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        if (args.size < 2) {
            sender.sendMessage("${RED}Usage: /cast [spell] [level]")
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

        val spellService = Services.INSTANCE[TtSpellService::class.java]
        if (spellService == null) {
            sender.sendMessage("${RED}No spell service was found. Please contact an admin.")
            return true
        }

        val spellName = args.dropLast(1).joinToString(" ")
        val spell = spellService.getSpell(TtSpellId(spellName)) ?: spellService.getSpell(spellName)
        if (spell == null) {
            sender.sendMessage("${RED}There is no spell by that name.")
            return true
        }

        val level = args.last().toIntOrNull()
        if (level == null) {
            sender.sendMessage("${RED}Level must be an integer.")
            return true
        }

        if (level < spell.level) {
            sender.sendMessage("${RED}${spell.name} requires at least a level ${spell.level} spell slot to cast.")
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
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            val maxSpellSlotCount = character.getMaxSpellSlotCount(level)
            val usedSpellSlots = character.usedSpellSlots[level] ?: 0

            if (maxSpellSlotCount == 0) {
                sender.sendMessage("${RED}You do not have any spell slots of that level.")
                return@asyncTask
            }

            val remainingSpellSlots = maxSpellSlotCount - usedSpellSlots
            if (remainingSpellSlots <= 0) {
                sender.sendMessage("${RED}You do not have any remaining spell slots of that level.")
                return@asyncTask
            }

            val updatedCharacter = characterService.save(
                character.copy(usedSpellSlots = character.usedSpellSlots + (level to usedSpellSlots + 1)),
                player = sender,
            ).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            syncTask(plugin) {
                val spellCastRadius = plugin.config.getInt("spells.casting.radius")
                val spellCastRadiusSquared = spellCastRadius * spellCastRadius
                sender.world.players.filter { player ->
                    player.location.distanceSquared(sender.location) <= spellCastRadiusSquared
                }.forEach { recipient ->
                    recipient.spigot().sendMessage(
                        TextComponent("${updatedCharacter.name} cast ").apply {
                            color = GRAY
                        },
                        TextComponent(spell.name).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(
                                SHOW_TEXT,
                                Text(
                                    "Click here to display information on ${spell.name}",
                                ),
                            )
                            clickEvent = ClickEvent(
                                RUN_COMMAND,
                                "/spell ${spell.id.value}",
                            )
                        },
                        TextComponent(" at level ").apply {
                            color = GRAY
                        },
                        TextComponent(level.toString()).apply {
                            color = YELLOW
                        },
                    )
                }
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ): List<String> {
        val spellService = Services.INSTANCE[TtSpellService::class.java]
        return when {
            args.isEmpty() -> spellService.spells.map(TtSpell::name)
            args.size == 1 -> spellService.spells.map(TtSpell::name).filter { it.startsWith(args[0], ignoreCase = true) }
            args.size == 2 -> (1..9).map(Int::toString).filter { it.startsWith(args[1], ignoreCase = true) }
            else -> emptyList()
        }
    }
}
