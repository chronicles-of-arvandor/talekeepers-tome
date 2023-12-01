package net.arvandor.talekeeper.command.character.subclass

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.args.unquote
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.clazz.TtSubClassId
import net.arvandor.talekeeper.clazz.gui.TtSubClassSelectionGui
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCharacterSubClassSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val unquotedArgs = args.unquote()

        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to perform this command.")
            return true
        }

        if (unquotedArgs.size < 1) {
            sender.sendMessage("${RED}Usage: /character subclass set [class] (subclass)")
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

        val classService = Services.INSTANCE[TtClassService::class.java]
        if (classService == null) {
            sender.sendMessage("${RED}No class service was found. Please contact an admin.")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
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

            val clazz = classService.getClass(TtClassId(unquotedArgs[0]))
                ?: classService.getClass(unquotedArgs[0])
            if (clazz == null) {
                sender.sendMessage("${RED}There is no class by that name.")
                return@asyncTask
            }

            val classInfo = character.classes[clazz.id]
            if (classInfo == null) {
                sender.sendMessage("${RED}You do not have that class.")
                return@asyncTask
            }

            if (classInfo.level < clazz.subClassSelectionLevel) {
                sender.sendMessage("${RED}You must be at least a level ${clazz.subClassSelectionLevel} ${clazz.name} to select a sub-class.")
                return@asyncTask
            }

            if (classInfo.subclassId != null) {
                val subClass = clazz.getSubClass(classInfo.subclassId)
                sender.sendMessage("${RED}You have already selected ${subClass?.name} as your ${clazz.name} sub-class.")
                return@asyncTask
            }

            if (unquotedArgs.size > 1) {
                val subClass = clazz.getSubClass(TtSubClassId(unquotedArgs[1]))
                    ?: clazz.getSubClass(unquotedArgs[1])
                if (subClass == null) {
                    sender.sendMessage("${RED}There is no sub-class by that name.")
                    return@asyncTask
                }

                val updatedCharacter = characterService.save(
                    character.copy(
                        classes = character.classes + (clazz.id to classInfo.copy(subclassId = subClass.id)),
                    ),
                    player = sender,
                ).onFailure {
                    sender.sendMessage("${RED}Failed to save character. Please contact an admin.")
                    plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                sender.sendMessage(
                    "$GRAY================================",
                    "${GREEN}${clazz.name} sub-class set to ${subClass.name}.",
                    "$GRAY================================",
                )

                choiceService.displayPendingChoices(sender, updatedCharacter)

                return@asyncTask
            }

            syncTask(plugin) {
                val subClasses = clazz.subClasses
                if (subClasses.isEmpty()) {
                    sender.sendMessage("${RED}There are no sub-classes for ${clazz.name}.")
                    return@syncTask
                }

                val subClassSelectionGui = TtSubClassSelectionGui(plugin, clazz)
                sender.openInventory(subClassSelectionGui.inventory)
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
        val classService = Services.INSTANCE[TtClassService::class.java]
        val unquotedArgs = args.unquote()
        return when {
            unquotedArgs.isEmpty() -> classService.getAll().map(TtClass::name)
            unquotedArgs.size == 1 -> classService.getAll().map(TtClass::name)
                .filter { it.startsWith(unquotedArgs[0], ignoreCase = true) }
                .map { if (it.contains(" ")) "\"$it\"" else it }
            unquotedArgs.size == 2 -> (classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0]))
                ?.subClasses
                ?.map { it.name }
                ?.filter { it.startsWith(unquotedArgs[1], ignoreCase = true) }
                ?.map { if (it.contains(" ")) "\"$it\"" else it }
                ?: emptyList()
            else -> emptyList()
        }
    }
}
