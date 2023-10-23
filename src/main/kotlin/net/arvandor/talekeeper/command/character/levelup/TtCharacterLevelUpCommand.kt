package net.arvandor.talekeeper.command.character.levelup

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.logging.Level.SEVERE

class TtCharacterLevelUpCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val experienceService = Services.INSTANCE[TtExperienceService::class.java]
        if (experienceService == null) {
            sender.sendMessage("${RED}No experience service was found. Please contact an admin.")
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
                sender.sendMessage("${RED}You do not have an active character.")
                return@asyncTask
            }

            val clazz = classService.getClass(character.firstClassId)
            if (clazz == null) {
                sender.sendMessage("${RED}Your class could not be found. Please contact an admin.")
                return@asyncTask
            }

            val level = experienceService.getLevelAtExperience(character.experience)
            if (level <= character.classes.map { (_, classInfo) -> classInfo.level }.sum()) {
                sender.sendMessage("${RED}You do not have enough experience to level up.")
                return@asyncTask
            }

            val updatedCharacter = characterService.save(
                character.copy(
                    classes = character.classes.map { (classId, classInfo) ->
                        if (classId == character.firstClassId) {
                            classId to classInfo.copy(level = classInfo.level + 1)
                        } else {
                            classId to classInfo
                        }
                    }.toMap(),
                ),
            ).onFailure {
                sender.sendMessage("${RED}An error occurred while saving your character.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            val classInfo = updatedCharacter.classes[updatedCharacter.firstClassId]
            if (classInfo == null) {
                sender.sendMessage("${RED}Your class info is missing. Please contact an admin.")
                return@asyncTask
            }

            val pendingChoices = choiceService.getPendingChoices(updatedCharacter)

            syncTask(plugin) {
                sender.sendMessage("${YELLOW}You are now a ${WHITE}Level ${classInfo.level} ${clazz.name}$YELLOW.")
                sender.spigot().sendMessage(
                    *buildList {
                        add(
                            TextComponent("What's new").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view new class features."))
                                clickEvent = ClickEvent(RUN_COMMAND, "/class whatsnew ${clazz.id.value} ${classInfo.subclassId?.value ?: "none"} ${classInfo.level}")
                            },
                        )
                        if (classInfo.level >= clazz.subClassSelectionLevel && classInfo.subclassId != null) {
                            add(
                                TextComponent(" / ").apply {
                                    color = GRAY
                                },
                            )
                            add(
                                TextComponent("Choose sub-class").apply {
                                    color = GREEN
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to choose a sub-class."))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/character subclass set ${clazz.id.value}")
                                },
                            )
                        }
                        if (pendingChoices.isNotEmpty()) {
                            add(
                                TextComponent(" / ").apply {
                                    color = GRAY
                                },
                            )
                            add(
                                TextComponent("Pending choices").apply {
                                    color = GREEN
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to view pending choices."))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/choice list")
                                },
                            )
                        }
                    }.toTypedArray(),
                )
                sender.playSound(sender, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f)
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
