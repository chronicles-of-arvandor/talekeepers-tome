package net.arvandor.talekeeper.command.character.context.abilities

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbilityService
import net.arvandor.talekeeper.character.TtCharacterCreationContext
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatColor.AQUA
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.ComponentBuilder
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

class TtCharacterContextAbilitiesSetCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

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

        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage("${RED}No character service was found. Please contact an admin.")
            return true
        }

        val abilityService = Services.INSTANCE[TtAbilityService::class.java]
        if (abilityService == null) {
            sender.sendMessage("${RED}No ability service was found. Please contact an admin.")
            return true
        }

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            if (args.size >= 2) {
                val ability = try {
                    TtAbility.valueOf(args[0].uppercase())
                } catch (exception: IllegalArgumentException) {
                    null
                } ?: TtAbility.ofShortName(args[0])
                if (ability == null) {
                    sender.sendMessage("${RED}There is no ability by that name.")
                    return@asyncTask
                }

                val score = args[1].toIntOrNull()
                if (score == null) {
                    sender.sendMessage("${RED}Ability score must be an integer.")
                    return@asyncTask
                }
                if (score < 8) {
                    sender.sendMessage("${RED}Ability score may not be less than 8.")
                    return@asyncTask
                }
                if (score > 15) {
                    sender.sendMessage("${RED}Ability score may not be greater than 15.")
                    return@asyncTask
                }

                val cost = abilityService.getAbilityScoreCost(score) ?: 0
                val otherScoreCost = TtAbility.values()
                    .filter { otherAbility -> otherAbility != ability }
                    .sumOf { otherAbility ->
                        val otherScoreChoice = ctx.abilityScoreChoices[otherAbility] ?: 0
                        val otherScoreCost = abilityService.getAbilityScoreCost(otherScoreChoice) ?: 0
                        otherScoreCost
                    }
                if (otherScoreCost + cost > abilityService.maxTotalAbilityCost) {
                    sender.sendMessage("${RED}Decrease your other stats if you wish to choose this option.")
                    return@asyncTask
                }

                val updatedCtx = characterService.save(
                    ctx.copy(
                        abilityScoreChoices = ctx.abilityScoreChoices + (ability to score),
                    ),
                ).onFailure {
                    sender.sendMessage("${RED}Failed to save character creation context. Please contact an admin.")
                    plugin.logger.log(Level.SEVERE, it.reason.message, it.reason.cause)
                    return@asyncTask
                }

                sender.sendMessage(
                    "$GRAY================================",
                    "${GREEN}${ability.displayName} set to $score.",
                    "$GRAY================================",
                )

                sendAbilityChoices(sender, updatedCtx, abilityService)
                return@asyncTask
            }

            sendAbilityChoices(sender, ctx, abilityService)
        }
        return true
    }

    private fun sendAbilityChoices(sender: CommandSender, ctx: TtCharacterCreationContext, abilityService: TtAbilityService) {
        sender.sendMessage("${GRAY}Select ability scores:")
        for (ability in TtAbility.values()) {
            val abilityLineBuilder: ComponentBuilder = ComponentBuilder(ability.shortName)
                .color(ChatColor.WHITE)
            val scores = intArrayOf(8, 9, 10, 11, 12, 13, 14, 15)
            scores.forEach { score: Int ->
                var color = GRAY
                if (ctx.abilityScoreChoices[ability] == score) {
                    color = ChatColor.WHITE
                } else {
                    val cost: Int = abilityService.getAbilityScoreCost(score) ?: 0
                    val otherScoreCost: Int = TtAbility.values()
                        .filter { otherAbility -> otherAbility !== ability }
                        .sumOf { otherAbility ->
                            val otherAbilityScore = ctx.abilityScoreChoices[otherAbility] ?: 0
                            abilityService.getAbilityScoreCost(otherAbilityScore) ?: 0
                        }
                    if (otherScoreCost + cost > abilityService.maxTotalAbilityCost) {
                        color = ChatColor.DARK_GRAY
                    }
                }
                val scoreComponent = TextComponent(Integer.toString(score))
                if (color === GRAY) {
                    scoreComponent.clickEvent = ClickEvent(
                        RUN_COMMAND,
                        "/character context abilities set " + ability.shortName + " " + score,
                    )
                    scoreComponent.hoverEvent = HoverEvent(
                        SHOW_TEXT,
                        Text("Click here to set your " + ability.displayName + " score to " + score + "."),
                    )
                } else if (color === ChatColor.DARK_GRAY) {
                    scoreComponent.hoverEvent = HoverEvent(
                        SHOW_TEXT,
                        Text("Decrease your other stats if you wish to choose this option."),
                    )
                }
                abilityLineBuilder.append(" - ")
                    .color(ChatColor.WHITE)
                    .append(scoreComponent)
                    .color(color)
            }
            sender.spigot().sendMessage(*abilityLineBuilder.create())
        }

        val backButton = TextComponent("Back").apply {
            color = AQUA
            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to return to character creation"))
            clickEvent = ClickEvent(RUN_COMMAND, "/character context")
        }

        sender.spigot().sendMessage(
            *buildList {
                add(backButton)
                if (TtAbility.values().all { ability ->
                        (
                            (ctx.abilityScoreChoices[ability] ?: 0) >= 8 &&
                                (ctx.abilityScoreChoices[ability] ?: 0) <= 15
                            )
                    }
                ) {
                    val confirmButton = TextComponent("Confirm").apply {
                        color = GREEN
                        hoverEvent = HoverEvent(
                            SHOW_TEXT,
                            Text("Click here to confirm your ability score choices."),
                        )
                        clickEvent = ClickEvent(RUN_COMMAND, "/character context abilities confirm")
                    }

                    add(TextComponent(" "))
                    add(confirmButton)
                }
            }.toTypedArray(),
        )
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>,
    ) = when {
        args.isEmpty() -> TtAbility.values()
            .flatMap { ability -> listOf(ability.shortName, ability.displayName) }
        args.size == 1 -> TtAbility.values()
            .flatMap { ability -> listOf(ability.shortName, ability.displayName) }
            .filter { it.startsWith(args[0], ignoreCase = true) }
        args.size == 2 -> (8..15)
            .filter { score -> score.toString().startsWith(args[1], ignoreCase = true) }
            .map { score -> score.toString() }
        else -> emptyList()
    }
}
