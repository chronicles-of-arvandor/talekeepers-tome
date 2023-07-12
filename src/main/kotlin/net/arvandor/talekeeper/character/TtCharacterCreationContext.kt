package net.arvandor.talekeeper.character

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSetId
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
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.concurrent.CompletableFuture

data class TtCharacterCreationContext(
    private val plugin: TalekeepersTome,
    val id: TtCharacterCreationContextId = TtCharacterCreationContextId.generate(),
    val version: Int = 0,
    val profileId: RPKProfileId,
    val minecraftProfileId: RPKMinecraftProfileId,
    val name: String,
    val pronouns: Map<TtPronounSetId, Int>,
    val ancestryId: TtAncestryId?,
    val subAncestryId: TtSubAncestryId?,
    val firstClassId: TtClassId?,
    val classes: Map<TtClassId, TtClassInfo>,
    val backgroundId: TtBackgroundId?,
    val alignment: TtAlignment?,
    val abilityScoreChoices: Map<TtAbility, Int>,
    val experience: Int,
    val description: String,
    val height: Double?,
    val weight: Double?,
    val isProfileHidden: Boolean,
    val isNameHidden: Boolean,
    val isPronounsHidden: Boolean,
    val isAgeHidden: Boolean,
    val isAncestryHidden: Boolean,
    val isDescriptionHidden: Boolean,
    val isHeightHidden: Boolean,
    val isWeightHidden: Boolean,
) {
    fun display(player: Player) {
        syncTask(plugin) {
            val unitService = Services.INSTANCE.get(RPKUnitService::class.java)
            val profileService = Services.INSTANCE.get(RPKProfileService::class.java)
            asyncTask(plugin) {
                val preferredHeightUnitFuture = unitService.getPreferredUnit(profileId, UnitType.getHEIGHT())
                val preferredWeightUnitFuture = unitService.getPreferredUnit(profileId, UnitType.getWEIGHT())
                val profileFuture = profileService.getProfile(profileId)
                CompletableFuture.allOf(
                    preferredHeightUnitFuture,
                    preferredWeightUnitFuture,
                    profileFuture,
                ).join()
                val preferredHeightUnit = preferredHeightUnitFuture.join()
                val preferredWeightUnit = preferredWeightUnitFuture.join()
                val profile = profileFuture.join()
                syncTask(plugin) {
                    val decimalFormat = DecimalFormat("#.##")
                    player.spigot().sendMessage(
                        TextComponent("Name: ").apply {
                            color = WHITE
                        },
                        TextComponent("$name ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to modify your name"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context name set")
                        },
                        TextComponent(
                            if (isNameHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isNameHidden) "unhide" else "hide"} your name"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context name ${if (isNameHidden) "unhide" else "hide"}")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Profile: ").apply {
                            color = WHITE
                        },
                        TextComponent("${profile.name.value}#${profile.discriminator.value} "),
                        TextComponent(
                            if (isProfileHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isProfileHidden) "unhide" else "hide"} your profile"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context profile ${if (isProfileHidden) "unhide" else "hide"}")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Pronouns:").apply {
                            color = WHITE
                        },
                    )
                    val pronounService = Services.INSTANCE.get(TtPronounService::class.java)
                    val totalPronounChance = pronouns.values.sum()
                    pronouns.mapKeys { (id, _) -> pronounService.get(id) }
                        .forEach { (pronounSet, weight) ->
                            if (pronounSet != null) {
                                player.spigot().sendMessage(
                                    TextComponent(pronounSet.name).apply {
                                        color = GRAY
                                    },
                                    TextComponent(" - ").apply {
                                        color = WHITE
                                    },
                                    TextComponent("$weight/$totalPronounChance (${decimalFormat.format((weight.toDouble() / totalPronounChance.toDouble()) * 100.0)}%) ").apply {
                                        color = GRAY
                                    },
                                    TextComponent("(Edit)").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to modify the chance this pronoun set is used"))
                                        clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns setchance ${pronounSet.id.value}")
                                    },
                                    TextComponent(" "),
                                    TextComponent("(Remove)").apply {
                                        color = RED
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to remove this pronoun set"))
                                        clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns remove ${pronounSet.id.value}")
                                    },
                                )
                            }
                        }
                    player.spigot().sendMessage(
                        TextComponent("(Add) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add new pronoun set"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns add")
                        },
                        TextComponent(
                            if (isPronounsHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isPronounsHidden) "unhide" else "hide"} your pronouns"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context pronouns ${if (isProfileHidden) "unhide" else "hide"}")
                        },
                    )
                    val ancestryService = Services.INSTANCE.get(TtAncestryService::class.java)
                    val ancestry = ancestryId?.let { ancestryService.getAncestry(it) }
                    player.spigot().sendMessage(
                        TextComponent("Ancestry: ").apply {
                            color = WHITE
                        },
                        TextComponent("${ancestry?.name ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your ancestry"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context ancestry set")
                        },
                        TextComponent(
                            if (isAncestryHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isAncestryHidden) "unhide" else "hide"} your ancestry"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context ancestry ${if (isAncestryHidden) "unhide" else "hide"}")
                        },
                    )
                    if (ancestry?.subAncestries?.isNotEmpty() == true) {
                        val subAncestry = subAncestryId?.let { ancestry.getSubAncestry(it) }
                        player.spigot().sendMessage(
                            TextComponent("Sub ancestry: ").apply {
                                color = WHITE
                            },
                            TextComponent("${subAncestry?.name ?: "unset"} ").apply {
                                color = GRAY
                            },
                            TextComponent("(Edit)").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your subancestry"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character context subancestry set")
                            },
                        )
                    }
                    val classService = Services.INSTANCE.get(TtClassService::class.java)
                    val clazz = firstClassId?.let { classService.getClass(it) }
                    player.spigot().sendMessage(
                        TextComponent("Class: ").apply {
                            color = WHITE
                        },
                        TextComponent("${clazz?.name ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit)").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your class"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context class set")
                        },
                    )
                    val backgroundService = Services.INSTANCE.get(TtBackgroundService::class.java)
                    val background = backgroundId?.let { backgroundService.getBackground(it) }
                    player.spigot().sendMessage(
                        TextComponent("Background: ").apply {
                            color = WHITE
                        },
                        TextComponent("${background?.name ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit)").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your background"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context background set")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Alignment: ").apply {
                            color = WHITE
                        },
                        TextComponent("${alignment?.displayName ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit)").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your alignment"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context alignment set")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Abilities: ").apply {
                            color = WHITE
                        },
                        TextComponent(TtAbility.values().joinToString(" / ") { ability -> "${ability.shortName} ${abilityScoreChoices[ability] ?: "?"}" } + " ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit)").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your ability scores"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context abilities set")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Description:").apply {
                            color = WHITE
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent(description).apply {
                            color = GRAY
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("(Edit) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your description"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context description set")
                        },
                        TextComponent("(Extend) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to extend your description"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context description extend")
                        },
                        TextComponent(
                            if (isDescriptionHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isDescriptionHidden) "unhide" else "hide"} your description"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context description ${if (isDescriptionHidden) "unhide" else "hide"}")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Height: ").apply {
                            color = WHITE
                        },
                        TextComponent("${height?.let { unitService.format(preferredHeightUnit.scaleFactor * it, preferredHeightUnit) } ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your height"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context height set")
                        },
                        TextComponent(
                            if (isHeightHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isHeightHidden) "unhide" else "hide"} your height"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context height ${if (isHeightHidden) "unhide" else "hide"}")
                        },
                    )
                    player.spigot().sendMessage(
                        TextComponent("Weight: ").apply {
                            color = WHITE
                        },
                        TextComponent("${weight?.let { unitService.format(preferredWeightUnit.scaleFactor * it, preferredWeightUnit) } ?: "unset"} ").apply {
                            color = GRAY
                        },
                        TextComponent("(Edit) ").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your weight"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context weight set")
                        },
                        TextComponent(
                            if (isWeightHidden) "(Unhide)" else "(Hide)",
                        ).apply {
                            color = YELLOW
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to ${if (isWeightHidden) "unhide" else "hide"} your weight"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context weight ${if (isWeightHidden) "unhide" else "hide"}")
                        },
                    )

                    player.spigot().sendMessage(
                        TextComponent("(Create character)").apply {
                            color = GREEN
                            hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to create your character!"))
                            clickEvent = ClickEvent(RUN_COMMAND, "/character context create")
                        },
                    )
                }
            }
        }
    }
}
