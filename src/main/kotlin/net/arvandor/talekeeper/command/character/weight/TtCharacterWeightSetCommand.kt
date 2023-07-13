package net.arvandor.talekeeper.command.character.weight

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType
import com.rpkit.players.bukkit.unit.WeightUnit
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

class TtCharacterWeightSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(WeightPrompt())
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

    private inner class WeightPrompt : ValidatingPrompt() {
        override fun getPromptText(context: ConversationContext) = "What is your weight? (Type \"cancel\" to cancel)"

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val weight = WeightUnit.POUNDS.parse(input)
                ?: WeightUnit.KILOGRAMS.parse(input)
            return weight != null
        }

        override fun getFailedValidationText(context: ConversationContext, invalidInput: String): String? {
            val weight = WeightUnit.POUNDS.parse(invalidInput)
                ?: WeightUnit.KILOGRAMS.parse(invalidInput)
            if (weight != null) return null
            return "${RED}Failed to parse weight - weight must be in stone & pounds or kilograms & grams. Example valid inputs: 80kg, 80kg 450g, 9st 3lb, 160lb"
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
                val preferredWeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getWEIGHT()).join()

                val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    conversable.sendMessage("${RED}An error occurred while getting your active character.")
                    return@asyncTask
                }
                if (character == null) {
                    conversable.sendMessage("${RED}You do not currently have an active character.")
                    return@asyncTask
                }

                val weight = WeightUnit.POUNDS.parse(input)
                    ?: WeightUnit.KILOGRAMS.parse(input)

                val ancestry = ancestryService.getAncestry(character.ancestryId)
                if (ancestry != null) {
                    val subAncestry = character.subAncestryId?.let(ancestry::getSubAncestry)
                    if (subAncestry == null) {
                        if (weight > ancestry.maximumWeight) {
                            conversable.sendMessage(
                                "${RED}${ancestry.namePlural} weigh at most ${unitService.format(ancestry.maximumWeight, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }

                        if (weight < ancestry.minimumWeight) {
                            conversable.sendMessage(
                                "${RED}${ancestry.namePlural} weigh at least ${unitService.format(ancestry.minimumWeight, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }
                    } else {
                        if (weight > subAncestry.maximumWeight) {
                            conversable.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at most ${unitService.format(subAncestry.maximumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }

                        if (weight < subAncestry.minimumWeight) {
                            conversable.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at least ${unitService.format(subAncestry.minimumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }
                    }
                }

                val updatedCharacter = characterService.save(character.copy(weight = weight)).onFailure {
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    conversable.sendMessage("${RED}An error occurred while saving your character.")
                    return@asyncTask
                }

                conversable.sendMessage(
                    "${ChatColor.GRAY}================================",
                    "${ChatColor.GREEN}Weight set.",
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
            val preferredWeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getWEIGHT()).join()

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
                val weight = WeightUnit.POUNDS.parse(input)
                    ?: WeightUnit.KILOGRAMS.parse(input)
                if (weight == null) {
                    sender.sendMessage("${RED}Failed to parse weight - weight must be in stone & pounds or kilograms & grams. Example valid inputs: 80kg, 80kg 450g, 9st 3lb, 160lb")
                    return@asyncTask
                }

                val ancestry = ancestryService.getAncestry(character.ancestryId)
                if (ancestry != null) {
                    val subAncestry = character.subAncestryId?.let(ancestry::getSubAncestry)
                    if (subAncestry == null) {
                        if (weight > ancestry.maximumWeight) {
                            sender.sendMessage(
                                "${RED}${ancestry.namePlural} weigh at most ${unitService.format(ancestry.maximumWeight, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }

                        if (weight < ancestry.minimumWeight) {
                            sender.sendMessage(
                                "${RED}${ancestry.namePlural} weigh at least ${unitService.format(ancestry.minimumWeight, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }
                    } else {
                        if (weight > subAncestry.maximumWeight) {
                            sender.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at most ${unitService.format(subAncestry.maximumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }

                        if (weight < subAncestry.minimumWeight) {
                            sender.sendMessage(
                                "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at least ${unitService.format(subAncestry.minimumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                            )
                            return@asyncTask
                        }
                    }
                }

                val updatedCharacter = characterService.save(character.copy(weight = weight)).onFailure {
                    sender.sendMessage("${RED}An error occurred while saving your character.")
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                sender.sendMessage(
                    "${ChatColor.GRAY}================================",
                    "${ChatColor.GREEN}Weight set.",
                    "${ChatColor.GRAY}================================",
                )
                updatedCharacter.display(sender)
            } else {
                syncTask(plugin) {
                    if (sender.isConversing) {
                        sender.sendRawMessage("${RED}Please finish your current action before attempting to set your weight.")
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
