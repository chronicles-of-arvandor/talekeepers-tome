package net.arvandor.talekeeper.command.character.height

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.unit.HeightUnit
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.conversation.ErrorMessagePrompt
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.ConversationFactory
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.entity.Player
import java.util.logging.Level

class TtCharacterHeightSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(HeightPrompt())
        .withEscapeSequence("cancel")
        .withLocalEcho(false)
        .thatExcludesNonPlayersWithMessage("${RED}You must be a player to perform this command.")
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage("${RED}Operation cancelled.")
                }
            }
        }

    private inner class HeightPrompt : ValidatingPrompt() {
        override fun getPromptText(context: ConversationContext) = "What is your height? (Type \"cancel\" to cancel)"

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val height = HeightUnit.FEET.parse(input)
                ?: HeightUnit.METRES.parse(input)
            return height != null
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String? {
            val height = HeightUnit.FEET.parse(invalidInput)
                ?: HeightUnit.METRES.parse(invalidInput)
            if (height != null) return null
            return "${RED}Failed to parse height - height must be in feet & inches or metres & centimetres. Example valid inputs: 1m 81cm, 181cm, 5'11\", 6', 71\""
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
            val conversable = context.forWhom
            if (conversable !is Player) return ErrorMessagePrompt("${RED}You must be a player to perform this command.")

            val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
                ?: return ErrorMessagePrompt("${RED}No Minecraft profile service was found. Please contact an admin.")

            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(conversable)
                ?: return ErrorMessagePrompt("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")

            val profile = minecraftProfile.profile as? RPKProfile
                ?: return ErrorMessagePrompt("${RED}You do not have a profile. Please try relogging, or contact an admin if the error persists.")

            val characterService = context.getSessionData("characterService") as? TtCharacterService ?: return END_OF_CONVERSATION
            val ancestryService = context.getSessionData("ancestryService") as? TtAncestryService ?: return END_OF_CONVERSATION
            val unitService = context.getSessionData("unitService") as? RPKUnitService ?: return END_OF_CONVERSATION

            asyncTask(plugin) {
                val preferredHeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getHEIGHT()).join()

                val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    conversable.sendMessage("${RED}An error occurred while getting your active character.")
                    return@asyncTask
                }
                if (character == null) {
                    conversable.sendMessage("${RED}You do not currently have an active character.")
                    return@asyncTask
                }

                val height = HeightUnit.FEET.parse(input)
                    ?: HeightUnit.METRES.parse(input)

                val ancestry = ancestryService.getAncestry(character.ancestryId)
                if (ancestry != null) {
                    val subAncestry = character.subAncestryId?.let(ancestry::getSubAncestry)
                    if (subAncestry == null) {
                        if (height > ancestry.maximumHeight) {
                            conversable.sendMessage(
                                "${RED}${ancestry.namePlural} are at most ${unitService.format(ancestry.maximumHeight, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }

                        if (height < ancestry.minimumHeight) {
                            conversable.sendMessage(
                                "${RED}${ancestry.namePlural} are at least ${unitService.format(ancestry.minimumHeight, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }
                    } else {
                        if (height > subAncestry.maximumHeight) {
                            conversable.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} are at most ${unitService.format(subAncestry.maximumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }

                        if (height < subAncestry.minimumHeight) {
                            conversable.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} are at least ${unitService.format(subAncestry.minimumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }
                    }
                }

                val updatedCharacter = characterService.save(character.copy(height = height)).onFailure {
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    conversable.sendMessage("${RED}An error occurred while saving your character.")
                    return@asyncTask
                }

                conversable.sendMessage(
                    "${ChatColor.GRAY}================================",
                    "${ChatColor.GREEN}Height set.",
                    "${ChatColor.GRAY}================================",
                )
                updatedCharacter.display(conversable)
            }

            return END_OF_CONVERSATION
        }
    }

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

        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage("${RED}You do not have a Minecraft profile. Please try relogging, or contact an admin if the error persists.")
            return true
        }

        val profile = minecraftProfile.profile as? RPKProfile
        if (profile == null) {
            sender.sendMessage("${RED}You do not have a profile. Please try relogging, or contact an admin if the error persists.")
            return true
        }

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        if (ancestryService == null) {
            sender.sendMessage("${RED}No ancestry service was found. Please contact an admin.")
            return true
        }

        val unitService = Services.INSTANCE[RPKUnitService::class.java]
        if (unitService == null) {
            sender.sendMessage("${RED}No unit service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val preferredHeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getHEIGHT()).join()

            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your active character.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            if (character == null) {
                sender.sendMessage("${RED}You do not currently have an active character.")
                return@asyncTask
            }

            if (args.isNotEmpty()) {
                val input = args.joinToString(" ")
                val height = HeightUnit.FEET.parse(input)
                    ?: HeightUnit.METRES.parse(input)
                if (height == null) {
                    sender.sendMessage("${RED}Failed to parse height - height must be in feet & inches or metres & centimetres. Example valid inputs: 1m 81cm, 181cm, 5'11\", 6', 71\"")
                    return@asyncTask
                }

                val ancestry = ancestryService.getAncestry(character.ancestryId)
                if (ancestry != null) {
                    val subAncestry = character.subAncestryId?.let(ancestry::getSubAncestry)
                    if (subAncestry == null) {
                        if (height > ancestry.maximumHeight) {
                            sender.sendMessage(
                                "${RED}${ancestry.namePlural} are at most ${unitService.format(ancestry.maximumHeight, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }

                        if (height < ancestry.minimumHeight) {
                            sender.sendMessage(
                                "${RED}${ancestry.namePlural} are at least ${unitService.format(ancestry.minimumHeight, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }
                    } else {
                        if (height > subAncestry.maximumHeight) {
                            sender.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} are at most ${unitService.format(subAncestry.maximumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }

                        if (height < subAncestry.minimumHeight) {
                            sender.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} are at least ${unitService.format(subAncestry.minimumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                            )
                            return@asyncTask
                        }
                    }
                }

                val updatedCharacter = characterService.save(character.copy(height = height)).onFailure {
                    sender.sendMessage("${RED}An error occurred while saving your character.")
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                sender.sendMessage(
                    "${ChatColor.GRAY}================================",
                    "${ChatColor.GREEN}Height set.",
                    "${ChatColor.GRAY}================================",
                )
                updatedCharacter.display(sender)
            } else {
                syncTask(plugin) {
                    if (sender.isConversing) {
                        sender.sendRawMessage("${RED}Please finish your current action before attempting to set your height.")
                        return@syncTask
                    }

                    val conversation = conversationFactory.buildConversation(sender)
                    conversation.context.setSessionData("characterService", characterService)
                    conversation.context.setSessionData("ancestryService", ancestryService)
                    conversation.context.setSessionData("unitService", unitService)
                    conversation.begin()
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
    ) = emptyList<String>()
}
