package net.arvandor.talekeeper.command.clazz

import com.rpkit.core.bukkit.pagination.PaginatedView
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
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.ChatColor.YELLOW
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtClassWhatsNewCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
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

        val unquotedArgs = args.unquote()

        if (unquotedArgs.size < 3) {
            sender.sendMessage("${RED}Usage: /class whatsnew [class] [subclass|none] [level]")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val clazz = classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0])
            if (clazz == null) {
                sender.sendMessage("${RED}There is no class by that name.")
                return@asyncTask
            }

            val subClass = if (unquotedArgs[1] == "none") {
                null
            } else {
                clazz.getSubClass(TtSubClassId(unquotedArgs[1])) ?: clazz.getSubClass(unquotedArgs[1])
            }

            val level = unquotedArgs[2].toIntOrNull()
            if (level == null) {
                sender.sendMessage("${RED}Level must be an integer.")
                return@asyncTask
            }

            val features = (clazz.features[level] ?: emptyList()) + (subClass?.features?.get(level) ?: emptyList())
            if (features.isEmpty()) {
                sender.sendMessage("${GRAY}There are no features for that class and level.")
                return@asyncTask
            }

            val page = if (unquotedArgs.size > 3) {
                unquotedArgs[3].toIntOrNull() ?: 1
            } else {
                1
            }

            val view = PaginatedView.fromStrings(
                "${YELLOW}Lv$level ${clazz.name}${subClass?.let { " $GRAY(${it.name})" } ?: ""} features",
                features.flatMap { feature ->
                    listOf(
                        "${WHITE}${feature.name}",
                        "${GRAY}${feature.description}",
                    )
                },
                "${ChatColor.GREEN}< Previous",
                "Click here to view the previous page",
                "${ChatColor.GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                4,
                { pageNumber -> "/class whatsnew ${clazz.id.value} ${subClass?.id?.value ?: "none"} $level $pageNumber" },
            )

            if (view.isPageValid(page)) {
                view.sendPage(sender, page)
            } else {
                sender.sendMessage("${RED}Invalid page number.")
            }

            if (sender is Player) {
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
                    sender.sendMessage("${RED}You do not have an active character.")
                    return@asyncTask
                }

                choiceService.displayPendingChoices(sender, character)
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
        val experienceService = Services.INSTANCE[TtExperienceService::class.java]
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
            unquotedArgs.size == 3 -> (1..experienceService.getMaxLevel()).map { it.toString() }.filter { it.startsWith(unquotedArgs[2], ignoreCase = true) }
            unquotedArgs.size == 4 -> {
                val clazz = classService.getClass(TtClassId(unquotedArgs[0])) ?: classService.getClass(unquotedArgs[0]) ?: return emptyList()
                val subClass = if (unquotedArgs[1] == "none") {
                    null
                } else {
                    clazz.getSubClass(TtSubClassId(unquotedArgs[1])) ?: clazz.getSubClass(unquotedArgs[1])
                }
                val level = unquotedArgs[2].toIntOrNull() ?: return emptyList()
                val features = (clazz.features[level] ?: emptyList()) + (subClass?.features?.get(level) ?: emptyList())
                (1..features.size / 2).map(Int::toString).filter { it.startsWith(unquotedArgs[3], ignoreCase = true) }
            }
            else -> emptyList()
        }
    }
}
