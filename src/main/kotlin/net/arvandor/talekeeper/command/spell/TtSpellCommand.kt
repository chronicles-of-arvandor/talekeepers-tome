package net.arvandor.talekeeper.command.spell

import com.rpkit.core.bukkit.pagination.PaginatedView
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.args.unquote
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.arvandor.talekeeper.spell.TtSpell
import net.arvandor.talekeeper.spell.TtSpellId
import net.arvandor.talekeeper.spell.TtSpellService
import net.arvandor.talekeeper.spell.duration.TtInstantSpellDuration
import net.arvandor.talekeeper.spell.duration.TtPermanentSpellDuration
import net.arvandor.talekeeper.spell.duration.TtSpecialSpellDuration
import net.arvandor.talekeeper.spell.duration.TtSpellDuration
import net.arvandor.talekeeper.spell.duration.TtTimedSpellDuration
import net.arvandor.talekeeper.spell.entry.TtEntriesSpellEntry
import net.arvandor.talekeeper.spell.entry.TtInsetSpellEntry
import net.arvandor.talekeeper.spell.entry.TtListSpellEntry
import net.arvandor.talekeeper.spell.entry.TtSpellEntry
import net.arvandor.talekeeper.spell.entry.TtStringSpellEntry
import net.arvandor.talekeeper.spell.entry.TtTableSpellEntry
import net.arvandor.talekeeper.spell.range.TtConeSpellRange
import net.arvandor.talekeeper.spell.range.TtCubeSpellRange
import net.arvandor.talekeeper.spell.range.TtHemisphereSpellRange
import net.arvandor.talekeeper.spell.range.TtLineSpellRange
import net.arvandor.talekeeper.spell.range.TtPointSpellRange
import net.arvandor.talekeeper.spell.range.TtRadiusSpellRange
import net.arvandor.talekeeper.spell.range.TtSpecialSpellRange
import net.arvandor.talekeeper.spell.range.TtSpellRange
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistance
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceFeet
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceMile
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceSelf
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceSight
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceTouch
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceUnlimited
import net.arvandor.talekeeper.spell.range.TtSphereSpellRange
import net.arvandor.talekeeper.spell.scaling.TtSpellScalingLevelDice
import net.arvandor.talekeeper.spell.tag.TtMiscTag.CONCENTRATION
import net.md_5.bungee.api.ChatColor.DARK_GRAY
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.ChatColor.WHITE
import net.md_5.bungee.api.ChatColor.YELLOW
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL
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

class TtSpellCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("${RED}You must be a player to use this command.")
            return true
        }

        val unquotedArgs = args.unquote()

        if (unquotedArgs.isEmpty()) {
            sender.sendMessage("${RED}You must specify a spell.")
            return true
        }

        val spellName = unquotedArgs[0]
        val spellService = Services.INSTANCE[TtSpellService::class.java]
        if (spellService == null) {
            sender.sendMessage("${RED}No spell service was found. Please contact an admin.")
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

        val spell = spellService.getSpell(TtSpellId(spellName)) ?: spellService.getSpell(spellName)
        if (spell == null) {
            sender.sendMessage("${RED}There is no spell by that name.")
            return true
        }

        val page = if (unquotedArgs.size < 2) {
            1
        } else {
            unquotedArgs[1].toIntOrNull() ?: 1
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

            val view = PaginatedView.fromChatComponents(
                arrayOf(
                    TextComponent("=== ").apply { color = GRAY },
                    TextComponent(spell.name).apply { color = WHITE },
                    TextComponent(" ===").apply { color = GRAY },
                ),
                buildList {
                    add(
                        arrayOf(
                            TextComponent("Click here to view on the web").apply {
                                color = YELLOW
                                val link = "https://arvandor.net/spells/${
                                    spell.name.replace(Regex("[^a-zA-Z0-9._-]"), "-").lowercase()
                                }"
                                hoverEvent = HoverEvent(SHOW_TEXT, Text(link))
                                clickEvent = ClickEvent(OPEN_URL, link)
                            },
                        ),
                    )
                    add(
                        arrayOf(
                            TextComponent("Level: ").apply { color = GRAY },
                            TextComponent(spell.level.toString()).apply { color = WHITE },
                        ),
                    )
                    add(
                        arrayOf(
                            TextComponent("School: ").apply { color = GRAY },
                            TextComponent(spell.school.displayName).apply { color = WHITE },
                        ),
                    )
                    add(
                        arrayOf(
                            TextComponent("Time: ").apply { color = GRAY },
                            TextComponent(
                                spell.time.joinToString(", ") { time ->
                                    "${time.number} ${time.unit.displayName}"
                                },
                            ).apply { color = WHITE },
                        ),
                    )
                    add(
                        arrayOf(
                            TextComponent("Range: ").apply { color = GRAY },
                            TextComponent(toDisplayText(spell.range)).apply { color = WHITE },
                        ),
                    )
                    add(
                        arrayOf(
                            TextComponent("Duration: ").apply { color = GRAY },
                            TextComponent(
                                spell.duration.joinToString(", ") { duration ->
                                    toDisplayText(duration)
                                },
                            ).apply { color = WHITE },
                        ),
                    )
                    if (spell.scalingLevelDice != null) {
                        addAll(
                            spell.scalingLevelDice.flatMap { scalingLevelDice ->
                                toDisplay(scalingLevelDice, character)
                            },
                        )
                    }
                    if (spell.damageInflict != null) {
                        add(
                            arrayOf(
                                TextComponent("Damage type(s): ").apply { color = GRAY },
                                TextComponent(
                                    spell.damageInflict.joinToString(", ") { damageType ->
                                        damageType.name.lowercase().replaceFirstChar { it.uppercase() }
                                    },
                                ).apply { color = WHITE },
                            ),
                        )
                    }
                    if (spell.spellAttack != null) {
                        add(
                            arrayOf(
                                TextComponent("Spell attack: ").apply { color = GRAY },
                                TextComponent(
                                    spell.spellAttack.joinToString(", ") {
                                        it.name.lowercase().replaceFirstChar { char -> char.uppercase() }
                                    },
                                ).apply { color = WHITE },
                            ),
                        )
                    }
                    if (spell.conditionInflict != null) {
                        add(
                            arrayOf(
                                TextComponent("Conditions inflicted: ").apply { color = GRAY },
                                TextComponent(
                                    spell.conditionInflict.joinToString(", ") { condition ->
                                        condition.name.lowercase().replaceFirstChar { it.uppercase() }
                                    },
                                ).apply { color = WHITE },
                            ),
                        )
                    }
                    if (spell.savingThrow != null) {
                        add(
                            arrayOf(
                                TextComponent("Saving throw: ").apply { color = GRAY },
                                TextComponent(
                                    spell.savingThrow.joinToString(", ") { ability ->
                                        ability.name.lowercase().replaceFirstChar { it.uppercase() }
                                    },
                                ).apply { color = WHITE },
                            ),
                        )
                    }
                    if (spell.miscTags != null) {
                        if (spell.miscTags.contains(CONCENTRATION)) {
                            add(
                                arrayOf(
                                    TextComponent("Requires concentration: ").apply { color = GRAY },
                                    TextComponent("yes").apply { color = WHITE },
                                ),
                            )
                        } else {
                            add(
                                arrayOf(
                                    TextComponent("Requires concentration: ").apply { color = GRAY },
                                    TextComponent("no").apply { color = WHITE },
                                ),
                            )
                        }
                    }
                    if (spell.entries != null) {
                        addAll(
                            spell.entries.flatMap { entry: TtSpellEntry ->
                                toDisplay(entry)
                            },
                        )
                        add(
                            arrayOf(
                                TextComponent("---").apply { color = DARK_GRAY },
                            ),
                        )
                    }
                    if (spell.entriesHigherLevel != null) {
                        add(
                            arrayOf(
                                TextComponent("At Higher Levels: ").apply { color = YELLOW },
                            ),
                        )
                        addAll(
                            spell.entriesHigherLevel.flatMap { entry: TtSpellEntry ->
                                toDisplay(entry)
                            },
                        )
                        add(
                            arrayOf(
                                TextComponent("---").apply { color = DARK_GRAY },
                            ),
                        )
                    }
                },
                "$GREEN< Previous",
                "Click here to view the previous page",
                "${GREEN}Next >",
                "Click here to view the next page",
                { pageNumber -> "Page $pageNumber" },
                10,
                { pageNumber -> "/spell \"$spellName\" $pageNumber" },
            )
            syncTask(plugin) {
                if (view.isPageValid(page)) {
                    view.sendPage(sender, page)
                } else {
                    sender.sendMessage("${RED}Invalid page number.")
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
        val spellService = Services.INSTANCE[TtSpellService::class.java] ?: return emptyList()
        val unquotedArgs = args.unquote()
        return when {
            unquotedArgs.isEmpty() -> spellService.spells.map(TtSpell::name)
            unquotedArgs.size == 1 -> spellService.spells.map(TtSpell::name)
                .filter { name -> name.startsWith(unquotedArgs[0], ignoreCase = true) }
                .map { name -> if (name.contains(Regex("\\s+"))) "\"$name\"" else name }
            else -> emptyList()
        }
    }

    private fun toDisplayText(range: TtSpellRange) = when (range) {
        is TtPointSpellRange -> "Point: ${toDisplayText(range.distance)}"
        is TtRadiusSpellRange -> "Radius: ${toDisplayText(range.distance)}"
        is TtSphereSpellRange -> "Sphere: ${toDisplayText(range.distance)}"
        is TtConeSpellRange -> "Cone: ${toDisplayText(range.distance)}"
        is TtSpecialSpellRange -> "Special"
        is TtLineSpellRange -> "Line: ${toDisplayText(range.distance)}"
        is TtHemisphereSpellRange -> "Hemisphere: ${toDisplayText(range.distance)}"
        is TtCubeSpellRange -> "Cube: ${toDisplayText(range.distance)}"
    }

    private fun toDisplayText(distance: TtSpellRangeDistance) = when (distance) {
        is TtSpellRangeDistanceFeet -> "${distance.amount} feet"
        is TtSpellRangeDistanceMile -> "${distance.amount} miles"
        is TtSpellRangeDistanceSelf -> "self"
        is TtSpellRangeDistanceTouch -> "touch"
        is TtSpellRangeDistanceSight -> "sight"
        is TtSpellRangeDistanceUnlimited -> "unlimited range"
    }

    private fun toDisplayText(duration: TtSpellDuration) = when (duration) {
        is TtInstantSpellDuration -> "instantaneous"
        is TtTimedSpellDuration -> "${duration.amount} ${duration.type.name.lowercase()}" +
            if (duration.concentration) " (requires concentration)" else ""
        is TtPermanentSpellDuration -> "permanent"
        is TtSpecialSpellDuration -> "special"
    }

    private fun toDisplay(entry: TtSpellEntry): List<Array<out BaseComponent>> = when (entry) {
        is TtEntriesSpellEntry -> buildList {
            add(
                arrayOf(
                    TextComponent("---").apply { color = DARK_GRAY },
                ),
            )
            add(
                arrayOf(
                    TextComponent(entry.name).apply { color = WHITE },
                ),
            )
            addAll(
                entry.entries.map { arrayOf(TextComponent(it).apply { color = GRAY }) },
            )
        }
        is TtInsetSpellEntry -> buildList {
            add(
                arrayOf(
                    TextComponent("---").apply { color = DARK_GRAY },
                ),
            )
            add(
                arrayOf(
                    TextComponent(entry.name).apply { color = WHITE },
                ),
            )
            add(
                arrayOf(
                    TextComponent("Source: ").apply { color = GRAY },
                    TextComponent("${entry.source} page ${entry.page}").apply { color = WHITE },
                ),
            )
            addAll(
                entry.entries.map { arrayOf(TextComponent(it).apply { color = GRAY }) },
            )
        }
        is TtListSpellEntry -> buildList {
            add(
                arrayOf(
                    TextComponent("---").apply { color = DARK_GRAY },
                ),
            )
            addAll(
                entry.items.map { arrayOf(TextComponent(it).apply { color = GRAY }) },
            )
        }
        is TtStringSpellEntry -> buildList {
            add(
                arrayOf(
                    TextComponent("---").apply { color = DARK_GRAY },
                ),
            )
            add(
                arrayOf(
                    TextComponent(entry.value).apply { color = GRAY },
                ),
            )
        }
        is TtTableSpellEntry -> buildList {
            add(
                arrayOf(
                    TextComponent("---").apply { color = DARK_GRAY },
                ),
            )
            add(
                arrayOf(
                    TextComponent("See table on website").apply { color = GRAY },
                ),
            )
        }
    }

    private fun toDisplay(scalingLevelDice: TtSpellScalingLevelDice, character: TtCharacter): List<Array<out BaseComponent>> {
        val experienceService = Services.INSTANCE[TtExperienceService::class.java] ?: return emptyList()
        val characterLevel = experienceService.getLevelAtExperience(character.experience)
        val scalingLevel = scalingLevelDice.scaling.filter { (level, _) -> level <= characterLevel }
            .maxByOrNull { (level, _) -> level }
        return buildList {
            add(
                arrayOf(
                    TextComponent(scalingLevelDice.label).apply { color = WHITE },
                ),
            )
            if (scalingLevel != null) {
                add(
                    arrayOf(
                        TextComponent("Level $characterLevel: ").apply { color = GRAY },
                        TextComponent(scalingLevel.value).apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to roll ${scalingLevel.value}"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/roll ${scalingLevel.value}")
                        },
                    ),
                )
            } else {
                add(
                    arrayOf(
                        TextComponent("Level $characterLevel: ").apply { color = GRAY },
                        TextComponent("No scaling level dice found for your character level").apply { color = RED },
                    ),
                )
            }
        }
    }
}
