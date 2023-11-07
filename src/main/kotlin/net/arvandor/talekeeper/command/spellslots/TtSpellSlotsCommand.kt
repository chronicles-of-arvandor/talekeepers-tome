package net.arvandor.talekeeper.command.spellslots

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.spell.TtSpell
import net.arvandor.talekeeper.spell.TtSpellService
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
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
import java.util.logging.Level.SEVERE

class TtSpellSlotsCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val target = if (sender.hasPermission("taleekeeper.commands.spellslots.other") && args.isNotEmpty()) {
            plugin.server.getPlayer(args[0])
        } else if (sender is Player) {
            sender
        } else {
            sender.sendMessage("${RED}You must specify a player from console.")
            return true
        }
        if (target == null) {
            sender.sendMessage("${RED}There is no player online by that name.")
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

        asyncTask(plugin) {
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(target).join()
            if (minecraftProfile == null) {
                sender.sendMessage("${RED}That player does not have a Minecraft profile.")
                return@asyncTask
            }

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (character == null) {
                sender.sendMessage("${RED}That player does not have a character.")
                return@asyncTask
            }

            val spellSlotCount = generateSequence(1) { it + 1 }
                .takeWhile { character.getMaxSpellSlotCount(it) > 0 }
                .associateWith { character.getMaxSpellSlotCount(it) }

            if (spellSlotCount.isEmpty()) {
                sender.sendMessage("${RED}You do not have any spell slots.")
                return@asyncTask
            }

            spellSlotCount.forEach { (level, count) ->
                val usedSpellSlots = character.usedSpellSlots[level] ?: 0
                val unusedSpellSlots = count - usedSpellSlots
                sender.sendMessage("${YELLOW}Level $level: $unusedSpellSlots/$count")
                val applicableSpells = character.spells.mapNotNull(spellService::getSpell)
                    .filter { it.level <= level }
                    .sortedBy(TtSpell::name)
                sender.spigot().sendMessage(
                    *applicableSpells.fold(mutableListOf<TextComponent>()) { acc, spell ->
                        if (acc.isNotEmpty()) {
                            acc += TextComponent(", ").apply {
                                color = GRAY
                            }
                        }
                        acc += TextComponent(spell.name).apply {
                            if (sender == target) {
                                color = GREEN
                                hoverEvent = HoverEvent(
                                    SHOW_TEXT,
                                    Text("You will soon be able to cast this spell by clicking here!"),
                                )
//                                hoverEvent = HoverEvent(
//                                    SHOW_TEXT,
//                                    Text("Click here to cast ${spell.name} at level $level.")
//                                )
//                                clickEvent = ClickEvent(
//                                    RUN_COMMAND,
//                                    "/cast ${spell.name} $level"
//                                )
                            } else {
                                color = WHITE
                            }
                        }
                        return@fold acc
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
    ) = when {
        args.isEmpty() -> plugin.server.onlinePlayers.map(Player::getName)
        args.size == 1 -> plugin.server.onlinePlayers.map(Player::getName).filter { it.startsWith(args[0], ignoreCase = true) }
        else -> emptyList()
    }
}
