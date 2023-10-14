package net.arvandor.talekeeper.character

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType
import net.arvandor.magistersmonths.MagistersMonths
import net.arvandor.magistersmonths.datetime.MmDateTime
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbility.CONSTITUTION
import net.arvandor.talekeeper.ability.TtAbilityService
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.ancestry.TtAncestryId
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtSubAncestryId
import net.arvandor.talekeeper.background.TtBackgroundId
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.choice.TtChoiceId
import net.arvandor.talekeeper.choice.option.TtChoiceOptionId
import net.arvandor.talekeeper.clazz.TtClassId
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.feat.TtFeatId
import net.arvandor.talekeeper.item.TtItemId
import net.arvandor.talekeeper.language.TtLanguageId
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSetId
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.scheduler.syncTask
import net.arvandor.talekeeper.skill.TtSkill
import net.arvandor.talekeeper.speed.TtSpeed
import net.arvandor.talekeeper.spell.TtSpellId
import net.arvandor.talekeeper.trait.TtCharacterTrait
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
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat
import java.time.Instant
import java.util.concurrent.CompletableFuture

data class TtCharacter(
    private val plugin: TalekeepersTome,
    // basic identity
    val id: TtCharacterId = TtCharacterId.generate(),
    val version: Int = 0,
    val rpkitId: RPKCharacterId = RPKCharacterId(0),
    val profileId: RPKProfileId,
    val minecraftProfileId: RPKMinecraftProfileId?,
    val name: String,
    val pronouns: Map<TtPronounSetId, Int>,
    val birthdayYear: Int,
    val birthdayDay: Int,
    // d&d
    val ancestryId: TtAncestryId,
    val subAncestryId: TtSubAncestryId?,
    val firstClassId: TtClassId,
    val classes: Map<TtClassId, TtClassInfo>,
    val backgroundId: TtBackgroundId,
    val alignment: TtAlignment,
    val abilityScores: Map<TtAbility, Int>,
    val tempAbilityScores: Map<TtAbility, Int>,
    val hp: Int,
    val tempHp: Int,
    val experience: Int,
    // these are determined from other values, so they're not stored in the repository
    val feats: List<TtFeatId>,
    val spells: List<TtSpellId>,
    val skillProficiencies: List<TtSkill>,
    val skillExpertise: List<TtSkill>,
    val jackOfAllTrades: Boolean,
    val initiativeBonus: Int,
    val itemProficiencies: List<TtItemId>,
    val savingThrowProficiencies: List<TtAbility>,
    val speed: TtSpeed,
    val languages: List<TtLanguageId>,
    val traits: List<TtCharacterTrait>,
    // non-d&d
    val description: String,
    val height: Double,
    val weight: Double,
    val isDead: Boolean,
    val location: Location,
    val inventoryContents: Array<ItemStack?>,
    val health: Double,
    val foodLevel: Int,
    val exhaustion: Float,
    val saturation: Float,
    val isProfileHidden: Boolean,
    val isNameHidden: Boolean,
    val isAgeHidden: Boolean,
    val isAncestryHidden: Boolean,
    val isDescriptionHidden: Boolean,
    val isHeightHidden: Boolean,
    val isWeightHidden: Boolean,
    val choiceOptions: Map<TtChoiceId, TtChoiceOptionId>,
) {

    val age: Int
        get() {
            val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths ?: return 0
            val calendar = magistersMonths.calendar
            val birthday = MmDateTime(calendar.epochInGameTime, birthdayYear, birthdayDay, 0, 0, 0)

            val currentDate = calendar.toMmDateTime(Instant.now())
            return currentDate.year - birthday.year - if (currentDate.dayOfYear < birthday.dayOfYear) 1 else 0
        }

    val maxHp: Int
        get() {
            val classService = Services.INSTANCE[TtClassService::class.java]
            val firstClass = classService.getClass(firstClassId)
            val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
            val ancestry = ancestryService.getAncestry(ancestryId)
            val subAncestry = subAncestryId?.let { ancestry?.getSubAncestry(it) }
            val firstClassHp = firstClass?.baseHp ?: 1
            val experienceService = Services.INSTANCE[TtExperienceService::class.java]
            val level = experienceService.getLevelAtExperience(experience)
            val constitutionHp = getModifier(CONSTITUTION) * level
            val classHp = classes.map { (classId, classInfo) ->
                val clazz = classService.getClass(classId)
                val classLevel = classInfo.level
                val classHp = clazz?.baseHp ?: 1
                return@map if (classId == firstClassId) {
                    (classLevel - 1) * classHp
                } else {
                    classLevel * classHp
                }
            }.sum()
            val ancestryHp = subAncestry?.getBonusHp(level)
                ?: ancestry?.getBonusHp(level)
                ?: 0
            return constitutionHp + firstClassHp + classHp + ancestryHp
        }

    val proficiencyBonus: Int
        get() {
            val experienceService = Services.INSTANCE[TtExperienceService::class.java]
            val level = experienceService.getLevelAtExperience(experience)
            return when (level) {
                1, 2, 3, 4 -> 2
                5, 6, 7, 8 -> 3
                9, 10, 11, 12 -> 4
                13, 14, 15, 16 -> 5
                17, 18, 19, 20 -> 6
                else -> 0
            }
        }

    fun getModifier(ability: TtAbility): Int {
        val abilityService = Services.INSTANCE[TtAbilityService::class.java]
        val abilityScore = abilityScores[ability] ?: 0
        val tempAbilityScore = tempAbilityScores[ability] ?: 0
        return abilityService.getModifier(abilityScore + tempAbilityScore)
    }

    fun display(player: Player) {
        syncTask(plugin) {
            val unitService = Services.INSTANCE.get(RPKUnitService::class.java)
            val minecraftProfileService = Services.INSTANCE.get(RPKMinecraftProfileService::class.java)
            val profileService = Services.INSTANCE.get(RPKProfileService::class.java)

            val viewerMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
            val viewerProfile = viewerMinecraftProfile.profile as? RPKProfile
            val viewerProfileId = viewerProfile?.id

            asyncTask(plugin) {
                val preferredHeightUnitFuture = unitService.getPreferredUnit(viewerProfileId, UnitType.getHEIGHT())
                val preferredWeightUnitFuture = unitService.getPreferredUnit(viewerProfileId, UnitType.getWEIGHT())
                val profileFuture = profileService.getProfile(profileId)
                CompletableFuture.allOf(
                    preferredHeightUnitFuture,
                    preferredWeightUnitFuture,
                    profileFuture,
                ).join()
                val preferredHeightUnit = preferredHeightUnitFuture.join()
                val preferredWeightUnit = preferredWeightUnitFuture.join()
                val profile = profileFuture.join()

                val isOwner = profile.id == viewerProfileId

                syncTask(plugin) {
                    val decimalFormat = DecimalFormat("#.##")
                    player.spigot().sendMessage(
                        *buildList {
                            add(
                                TextComponent("Name: ").apply {
                                    color = WHITE
                                },
                            )
                            if (isNameHidden && !isOwner) {
                                add(
                                    TextComponent("Hidden").apply {
                                        color = RED
                                    },
                                )
                            } else {
                                add(
                                    TextComponent("$name ").apply {
                                        color = GRAY
                                    },
                                )
                            }
                            if (isOwner) {
                                add(
                                    TextComponent("(Edit) ").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to modify your name"))
                                        clickEvent = ClickEvent(RUN_COMMAND, "/character name set")
                                    },
                                )
                                add(
                                    TextComponent(
                                        if (isNameHidden) "(Unhide)" else "(Hide)",
                                    ).apply {
                                        color = YELLOW
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to ${if (isNameHidden) "unhide" else "hide"} your name"),
                                        )
                                        clickEvent =
                                            ClickEvent(RUN_COMMAND, "/character name ${if (isNameHidden) "unhide" else "hide"}")
                                    },
                                )
                            }
                        }.toTypedArray(),
                    )
                    player.spigot().sendMessage(
                        *buildList {
                            add(
                                TextComponent("Profile: ").apply {
                                    color = WHITE
                                },
                            )
                            if (isProfileHidden && !isOwner) {
                                add(
                                    TextComponent("Hidden").apply {
                                        color = RED
                                    },
                                )
                            } else {
                                add(
                                    TextComponent("${profile.name.value}#${profile.discriminator.value} ").apply {
                                        color = GRAY
                                    },
                                )
                            }
                            if (isOwner) {
                                add(
                                    TextComponent(
                                        if (isProfileHidden) "(Unhide)" else "(Hide)",
                                    ).apply {
                                        color = YELLOW
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to ${if (isProfileHidden) "unhide" else "hide"} your profile"),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            "/character profile ${if (isProfileHidden) "unhide" else "hide"}",
                                        )
                                    },
                                )
                            }
                        }.toTypedArray(),
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
                                    *buildList {
                                        add(
                                            TextComponent(pronounSet.name).apply {
                                                color = GRAY
                                            },
                                        )
                                        add(
                                            TextComponent(" - ").apply {
                                                color = WHITE
                                            },
                                        )
                                        add(
                                            TextComponent("$weight/$totalPronounChance (${decimalFormat.format((weight.toDouble() / totalPronounChance.toDouble()) * 100.0)}%) ").apply {
                                                color = GRAY
                                            },
                                        )
                                        if (isOwner) {
                                            add(
                                                TextComponent("(Edit)").apply {
                                                    color = GREEN
                                                    hoverEvent = HoverEvent(
                                                        SHOW_TEXT,
                                                        Text("Click here to modify the chance this pronoun set is used"),
                                                    )
                                                    clickEvent = ClickEvent(
                                                        RUN_COMMAND,
                                                        "/character pronouns setchance ${pronounSet.id.value}",
                                                    )
                                                },
                                            )
                                            add(TextComponent(" "))
                                            add(
                                                TextComponent("(Remove)").apply {
                                                    color = RED
                                                    hoverEvent =
                                                        HoverEvent(
                                                            SHOW_TEXT,
                                                            Text("Click here to remove this pronoun set"),
                                                        )
                                                    clickEvent = ClickEvent(
                                                        RUN_COMMAND,
                                                        "/character pronouns remove ${pronounSet.id.value}",
                                                    )
                                                },
                                            )
                                        }
                                    }.toTypedArray(),
                                )
                            }
                        }
                    if (isOwner) {
                        player.spigot().sendMessage(
                            TextComponent("(Add) ").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to add new pronoun set"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character pronouns add")
                            },
                        )
                    }
                    val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths
                    val calendar = magistersMonths?.calendar
                    if (calendar != null) {
                        player.spigot().sendMessage(
                            *buildList {
                                add(
                                    TextComponent("Age: ").apply {
                                        color = WHITE
                                    },
                                )
                                if (isAgeHidden && !isOwner) {
                                    add(
                                        TextComponent("Hidden").apply {
                                            color = RED
                                        },
                                    )
                                } else {
                                    val birthday = MmDateTime(calendar.epochInGameTime, birthdayYear, birthdayDay, 0, 0, 0)
                                    val month = calendar.getMonthAt(birthday.dayOfYear)
                                    val dayOfMonth =
                                        if (month != null) {
                                            (birthday.dayOfYear - calendar.getMonthAt(birthday.dayOfYear).startDay) + 1
                                        } else {
                                            0
                                        }
                                    add(
                                        TextComponent("$age (Birthday: ${if (month != null) "$dayOfMonth ${month.name}" else birthday.dayOfYear.toString()} ${birthday.year}) ").apply {
                                            color = GRAY
                                        },
                                    )
                                }
                                if (isOwner) {
                                    add(
                                        TextComponent(
                                            if (isAgeHidden) "(Unhide)" else "(Hide)",
                                        ).apply {
                                            color = YELLOW
                                            hoverEvent = HoverEvent(
                                                SHOW_TEXT,
                                                Text("Click here to ${if (isAgeHidden) "unhide" else "hide"} your age"),
                                            )
                                            clickEvent = ClickEvent(
                                                RUN_COMMAND,
                                                "/character age ${if (isAgeHidden) "unhide" else "hide"}",
                                            )
                                        },
                                    )
                                }
                            }.toTypedArray(),
                        )
                    } else {
                        player.sendMessage("${RED}Magister's Months is not installed. Age cannot be displayed.")
                    }
                    val ancestryService = Services.INSTANCE.get(TtAncestryService::class.java)
                    val ancestry = ancestryId.let { ancestryService.getAncestry(it) }
                    player.spigot().sendMessage(
                        *buildList {
                            add(
                                TextComponent("Ancestry: ").apply {
                                    color = WHITE
                                },
                            )
                            if (isAncestryHidden && !isOwner) {
                                add(
                                    TextComponent("Hidden").apply {
                                        color = RED
                                    },
                                )
                            } else {
                                add(
                                    TextComponent("${ancestry?.name ?: "unset"} ").apply {
                                        color = GRAY
                                    },
                                )
                            }
                            if (isOwner) {
                                add(
                                    TextComponent(
                                        if (isAncestryHidden) "(Unhide)" else "(Hide)",
                                    ).apply {
                                        color = YELLOW
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to ${if (isAncestryHidden) "unhide" else "hide"} your ancestry"),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            "/character ancestry ${if (isAncestryHidden) "unhide" else "hide"}",
                                        )
                                    },
                                )
                            }
                        }.toTypedArray(),
                    )
                    if (ancestry?.subAncestries?.isNotEmpty() == true) {
                        val subAncestry = subAncestryId?.let { ancestry.getSubAncestry(it) }
                        player.spigot().sendMessage(
                            *buildList {
                                add(
                                    TextComponent("Sub ancestry: ").apply {
                                        color = WHITE
                                    },
                                )
                                if (isAncestryHidden && !isOwner) {
                                    add(
                                        TextComponent("Hidden").apply {
                                            color = RED
                                        },
                                    )
                                } else {
                                    add(
                                        TextComponent("${subAncestry?.name ?: "unset"} ").apply {
                                            color = GRAY
                                        },
                                    )
                                }
                            }.toTypedArray(),
                        )
                    }
                    if (isOwner) {
                        val classService = Services.INSTANCE.get(TtClassService::class.java)
                        val classes = classes.mapKeys { (id, _) -> classService.getClass(id) }
                            .flatMap { (clazz, info) ->
                                if (clazz == null) {
                                    emptyList()
                                } else {
                                    listOf(clazz to info)
                                }
                            }
                            .toList()
                            .sortedBy { (_, info) -> info.level }
                        if (classes.size == 1) {
                            val (clazz, classInfo) = classes.first()
                            player.spigot().sendMessage(
                                TextComponent("Class: ").apply {
                                    color = WHITE
                                },
                                TextComponent("Lv${classInfo.level} ${clazz.name}").apply {
                                    color = GRAY
                                },
                            )
                        } else {
                            player.spigot().sendMessage(
                                TextComponent("Classes: ").apply {
                                    color = WHITE
                                },
                            )
                            classes.forEach { (clazz, classInfo) ->
                                player.spigot().sendMessage(
                                    TextComponent("Lv${classInfo.level} ${clazz.name}").apply {
                                        color = GRAY
                                    },
                                )
                            }
                        }
                        val backgroundService = Services.INSTANCE.get(TtBackgroundService::class.java)
                        val background = backgroundId.let { backgroundService.getBackground(it) }
                        player.spigot().sendMessage(
                            TextComponent("Background: ").apply {
                                color = WHITE
                            },
                            TextComponent("${background?.name ?: "unset"} ").apply {
                                color = GRAY
                            },
                        )
                        player.spigot().sendMessage(
                            TextComponent("Alignment: ").apply {
                                color = WHITE
                            },
                            TextComponent("${alignment.displayName} ").apply {
                                color = GRAY
                            },
                            TextComponent("(Edit)").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your alignment"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character alignment set")
                            },
                        )
                        player.spigot().sendMessage(
                            TextComponent("Abilities: ").apply {
                                color = WHITE
                            },
                            TextComponent(
                                TtAbility.values()
                                    .joinToString(" / ") { ability -> "${ability.shortName} ${abilityScores[ability] ?: "?"}" } + " ",
                            ).apply {
                                color = GRAY
                            },
                        )
                    }
                    player.spigot().sendMessage(
                        TextComponent("Description:").apply {
                            color = WHITE
                        },
                    )
                    if (isDescriptionHidden && !isOwner) {
                        player.spigot().sendMessage(
                            TextComponent("Hidden").apply {
                                color = RED
                            },
                        )
                    } else {
                        player.spigot().sendMessage(
                            TextComponent(description).apply {
                                color = GRAY
                            },
                        )
                    }
                    if (isOwner) {
                        player.spigot().sendMessage(
                            TextComponent("(Edit) ").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your description"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character description set")
                            },
                            TextComponent("(Extend) ").apply {
                                color = GREEN
                                hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to extend your description"))
                                clickEvent = ClickEvent(RUN_COMMAND, "/character description extend")
                            },
                            TextComponent(
                                if (isDescriptionHidden) "(Unhide)" else "(Hide)",
                            ).apply {
                                color = YELLOW
                                hoverEvent = HoverEvent(
                                    SHOW_TEXT,
                                    Text("Click here to ${if (isDescriptionHidden) "unhide" else "hide"} your description"),
                                )
                                clickEvent = ClickEvent(
                                    RUN_COMMAND,
                                    "/character description ${if (isDescriptionHidden) "unhide" else "hide"}",
                                )
                            },
                        )
                    }
                    player.spigot().sendMessage(
                        *buildList {
                            add(
                                TextComponent("Height: ").apply {
                                    color = WHITE
                                },
                            )
                            if (isHeightHidden && !isOwner) {
                                add(
                                    TextComponent("Hidden").apply {
                                        color = RED
                                    },
                                )
                            } else {
                                add(
                                    TextComponent(
                                        "${
                                            height.let {
                                                unitService.format(
                                                    preferredHeightUnit.scaleFactor * it,
                                                    preferredHeightUnit,
                                                )
                                            } ?: "unset"
                                        } ",
                                    ).apply {
                                        color = GRAY
                                    },
                                )
                            }
                            if (isOwner) {
                                add(
                                    TextComponent("(Edit) ").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your height"))
                                        clickEvent = ClickEvent(RUN_COMMAND, "/character height set")
                                    },
                                )
                                add(
                                    TextComponent(
                                        if (isHeightHidden) "(Unhide)" else "(Hide)",
                                    ).apply {
                                        color = YELLOW
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to ${if (isHeightHidden) "unhide" else "hide"} your height"),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            "/character height ${if (isHeightHidden) "unhide" else "hide"}",
                                        )
                                    },
                                )
                            }
                        }.toTypedArray(),
                    )
                    player.spigot().sendMessage(
                        *buildList {
                            add(
                                TextComponent("Weight: ").apply {
                                    color = WHITE
                                },
                            )
                            add(
                                TextComponent(
                                    "${
                                        weight.let {
                                            unitService.format(
                                                preferredWeightUnit.scaleFactor * it,
                                                preferredWeightUnit,
                                            )
                                        } ?: "unset"
                                    } ",
                                ).apply {
                                    color = GRAY
                                },
                            )
                            if (isOwner) {
                                add(
                                    TextComponent("(Edit) ").apply {
                                        color = GREEN
                                        hoverEvent = HoverEvent(SHOW_TEXT, Text("Click here to edit your weight"))
                                        clickEvent = ClickEvent(RUN_COMMAND, "/character weight set")
                                    },
                                )
                                add(
                                    TextComponent(
                                        if (isWeightHidden) "(Unhide)" else "(Hide)",
                                    ).apply {
                                        color = YELLOW
                                        hoverEvent = HoverEvent(
                                            SHOW_TEXT,
                                            Text("Click here to ${if (isWeightHidden) "unhide" else "hide"} your weight"),
                                        )
                                        clickEvent = ClickEvent(
                                            RUN_COMMAND,
                                            "/character weight ${if (isWeightHidden) "unhide" else "hide"}",
                                        )
                                    },
                                )
                            }
                        }.toTypedArray(),
                    )
                }
            }
        }
    }
}
