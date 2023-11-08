package net.arvandor.talekeeper.command.character.context.create

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.rpkit.players.bukkit.unit.UnitType
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.resultFrom
import net.arvandor.magistersmonths.MagistersMonths
import net.arvandor.magistersmonths.datetime.MmDateTime
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbilityService
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterId
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.scheduler.asyncTask
import net.arvandor.talekeeper.spawn.TtSpawnService
import net.arvandor.talekeeper.speed.TtSpeed
import net.arvandor.talekeeper.speed.TtSpeedUnit
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.time.Instant
import java.util.logging.Level.SEVERE

class TtCharacterContextCreateCommand(private val plugin: TalekeepersTome) : CommandExecutor, TabCompleter {
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

        val abilityService = Services.INSTANCE[TtAbilityService::class.java]
        if (abilityService == null) {
            sender.sendMessage("${RED}No ability service was found. Please contact an admin.")
            return true
        }

        val unitService = Services.INSTANCE[RPKUnitService::class.java]
        if (unitService == null) {
            sender.sendMessage("${RED}No unit service was found. Please contact an admin.")
            return true
        }

        val classService = Services.INSTANCE[TtClassService::class.java]
        if (classService == null) {
            sender.sendMessage("${RED}No class service was found. Please contact an admin.")
            return true
        }

        val spawnService = Services.INSTANCE[TtSpawnService::class.java]
        if (spawnService == null) {
            sender.sendMessage("${RED}No spawn service was found. Please contact an admin.")
            return true
        }

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        if (choiceService == null) {
            sender.sendMessage("${RED}No choice service was found. Please contact an admin.")
            return true
        }

        val newCharacterSpawn = spawnService.newCharacterSpawn
        val defaultInventory = characterService.defaultInventory

        asyncTask(plugin) {
            val ctx = characterService.getCreationContext(minecraftProfile.id).onFailure {
                sender.sendMessage("${RED}An error occurred while getting your character creation context.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }
            if (ctx == null) {
                sender.sendMessage("${RED}You are not currently creating a character. If you have recently made a request to do so, please ensure a staff member has approved it.")
                return@asyncTask
            }

            if (ctx.name.isBlank()) {
                sender.sendMessage("${RED}You must set your name.")
                return@asyncTask
            }

            if (ctx.birthdayDay == null || ctx.birthdayYear == null) {
                sender.sendMessage("${RED}You must set your birthday.")
                return@asyncTask
            }

            if (ctx.ancestryId == null) {
                sender.sendMessage("${RED}You must set your ancestry.")
                return@asyncTask
            }

            val ancestry = ancestryService.getAncestry(ctx.ancestryId)
            if (ancestry == null) {
                sender.sendMessage("${RED}You must set your ancestry.")
                return@asyncTask
            }

            if (ctx.subAncestryId == null && ancestry.subAncestries.isNotEmpty()) {
                sender.sendMessage("${RED}You must set your sub-ancestry.")
                return@asyncTask
            }

            val subAncestry = if (ctx.subAncestryId != null) {
                ancestry.getSubAncestry(ctx.subAncestryId)
            } else {
                null
            }

            if (ctx.firstClassId == null) {
                sender.sendMessage("${RED}You must set your class.")
                return@asyncTask
            }

            if (ctx.backgroundId == null) {
                sender.sendMessage("${RED}You must set your background.")
                return@asyncTask
            }

            if (ctx.alignment == null) {
                sender.sendMessage("${RED}You must set your alignment.")
                return@asyncTask
            }

            if (!TtAbility.values().all { ability ->
                    val score = ctx.abilityScoreChoices[ability]
                    score != null && score >= 8 && score <= 15
                }
            ) {
                sender.sendMessage("${RED}Some of your ability scores are not within bounds. Please ensure all of your ability scores are between 8 and 15.")
                return@asyncTask
            }

            val scoreCost = TtAbility.values().sumOf { ability ->
                val score = ctx.abilityScoreChoices[ability] ?: 0
                abilityService.getAbilityScoreCost(score) ?: 0
            }
            if (scoreCost > abilityService.maxTotalAbilityCost) {
                sender.sendMessage("${RED}Your overall ability score cost is too high. Please reduce some of your ability scores.")
                return@asyncTask
            }

            val preferredHeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getHEIGHT()).join()
            val preferredWeightUnit = unitService.getPreferredUnit(profile.id, UnitType.getWEIGHT()).join()

            if (ctx.height == null) {
                sender.sendMessage("${RED}You must set your height.")
                return@asyncTask
            }

            if (ctx.weight == null) {
                sender.sendMessage("${RED}You must set your weight.")
                return@asyncTask
            }

            if (subAncestry == null) {
                if (ctx.height > ancestry.maximumHeight) {
                    sender.sendMessage(
                        "${RED}${ancestry.namePlural} are at most ${unitService.format(preferredHeightUnit.scaleFactor * ancestry.maximumHeight, preferredHeightUnit)} tall.",
                    )
                    return@asyncTask
                }

                if (ctx.height < ancestry.minimumHeight) {
                    sender.sendMessage(
                        "${RED}${ancestry.namePlural} are at least ${unitService.format(preferredHeightUnit.scaleFactor * ancestry.minimumHeight, preferredHeightUnit)} tall.",
                    )
                    return@asyncTask
                }

                if (ctx.weight > ancestry.maximumWeight) {
                    sender.sendMessage(
                        "${RED}${ancestry.namePlural} weigh at most ${unitService.format(preferredWeightUnit.scaleFactor * ancestry.maximumWeight, preferredWeightUnit)}.",
                    )
                    return@asyncTask
                }

                if (ctx.weight < ancestry.minimumWeight) {
                    sender.sendMessage(
                        "${RED}${ancestry.namePlural} weigh at least ${unitService.format(preferredWeightUnit.scaleFactor * ancestry.minimumWeight, preferredWeightUnit)}.",
                    )
                    return@asyncTask
                }

                val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths
                val calendar = magistersMonths?.calendar
                if (calendar != null) {
                    val birthday = MmDateTime(calendar, ctx.birthdayYear, ctx.birthdayDay, 0, 0, 0)
                    val currentDate = calendar.toMmDateTime(Instant.now())
                    val age = currentDate.year - birthday.year - if (currentDate.dayOfYear < birthday.dayOfYear) 1 else 0
                    if (age < ancestry.minimumAge) {
                        sender.sendMessage(
                            "${RED}${ancestry.namePlural} must be at least ${ancestry.minimumAge} years old.",
                        )
                        return@asyncTask
                    }

                    if (age > ancestry.maximumAge) {
                        sender.sendMessage(
                            "${RED}${ancestry.namePlural} must be at most ${ancestry.maximumAge} years old.",
                        )
                        return@asyncTask
                    }
                }
            } else {
                if (ctx.height > subAncestry.maximumHeight) {
                    sender.sendMessage(
                        "${RED}${subAncestry.name} ${ancestry.namePlural} are at most ${unitService.format(subAncestry.maximumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                    )
                    return@asyncTask
                }

                if (ctx.height < subAncestry.minimumHeight) {
                    sender.sendMessage(
                        "${RED}${subAncestry.name} ${ancestry.namePlural} are at least ${unitService.format(subAncestry.minimumHeight * preferredHeightUnit.scaleFactor, preferredHeightUnit)} tall.",
                    )
                    return@asyncTask
                }

                if (ctx.weight > subAncestry.maximumWeight) {
                    sender.sendMessage(
                        "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at most ${unitService.format(subAncestry.maximumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                    )
                    return@asyncTask
                }

                if (ctx.weight < subAncestry.minimumWeight) {
                    sender.sendMessage(
                        "${RED}${subAncestry.name} ${ancestry.namePlural} weigh at least ${unitService.format(subAncestry.minimumWeight * preferredWeightUnit.scaleFactor, preferredWeightUnit)}.",
                    )
                    return@asyncTask
                }

                val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as? MagistersMonths
                val calendar = magistersMonths?.calendar
                if (calendar != null) {
                    val birthday = MmDateTime(calendar, ctx.birthdayYear, ctx.birthdayDay, 0, 0, 0)
                    val currentDate = calendar.toMmDateTime(Instant.now())
                    val age = currentDate.year - birthday.year - if (currentDate.dayOfYear < birthday.dayOfYear) 1 else 0
                    if (age < subAncestry.minimumAge) {
                        sender.sendMessage(
                            "${RED}${ancestry.namePlural} must be at least ${ancestry.minimumAge} years old.",
                        )
                        return@asyncTask
                    }

                    if (age > subAncestry.maximumAge) {
                        sender.sendMessage(
                            "${RED}${ancestry.namePlural} must be at most ${ancestry.maximumAge} years old.",
                        )
                        return@asyncTask
                    }
                }
            }

            val character = resultFrom {
                plugin.dsl.transactionResult { config ->
                    val transactionalDsl = config.dsl()

                    val character = characterService.save(
                        TtCharacter(
                            plugin,
                            id = TtCharacterId.generate(),
                            profileId = ctx.profileId,
                            minecraftProfileId = null,
                            name = ctx.name,
                            pronouns = ctx.pronouns,
                            birthdayYear = ctx.birthdayYear,
                            birthdayDay = ctx.birthdayDay,
                            ancestryId = ctx.ancestryId,
                            subAncestryId = ctx.subAncestryId,
                            firstClassId = ctx.firstClassId,
                            classes = ctx.classes,
                            backgroundId = ctx.backgroundId,
                            alignment = ctx.alignment,
                            baseAbilityScores = ctx.abilityScoreChoices,
                            abilityScoreBonuses = emptyMap(),
                            tempAbilityScores = emptyMap(),
                            hp = 1,
                            tempHp = 0,
                            experience = 0,
                            usedSpellSlots = emptyMap(),
                            feats = emptyList(),
                            spells = emptyList(),
                            skillProficiencies = emptyList(),
                            skillExpertise = emptyList(),
                            jackOfAllTrades = false,
                            initiativeBonus = 0,
                            itemProficiencies = emptyList(),
                            savingThrowProficiencies = emptyList(),
                            speed = TtSpeed(0, TtSpeedUnit.FEET),
                            languages = emptyList(),
                            traits = emptyList(),
                            description = ctx.description,
                            height = ctx.height,
                            weight = ctx.weight,
                            isDead = false,
                            location = newCharacterSpawn,
                            inventoryContents = defaultInventory,
                            health = 20.0,
                            foodLevel = 20,
                            exhaustion = 0f,
                            saturation = 5f,
                            isProfileHidden = ctx.isProfileHidden,
                            isNameHidden = ctx.isNameHidden,
                            isAgeHidden = ctx.isAgeHidden,
                            isAncestryHidden = ctx.isAncestryHidden,
                            isDescriptionHidden = ctx.isDescriptionHidden,
                            isHeightHidden = ctx.isHeightHidden,
                            isWeightHidden = ctx.isWeightHidden,
                            choiceOptions = emptyMap(),
                            isShelved = false,
                        ),
                        transactionalDsl,
                    ).onFailure {
                        throw it.reason.cause
                    }

                    characterService.delete(ctx.id, transactionalDsl).onFailure {
                        throw it.reason.cause
                    }

                    return@transactionResult character
                }
            }.onFailure {
                sender.sendMessage("${RED}Failed to save character. Please contact an admin.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            characterService.setActiveCharacter(minecraftProfile, character).onFailure {
                sender.sendMessage("${RED}Failed to set active character. Please contact an admin.")
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                return@asyncTask
            }

            sender.sendMessage("${GREEN}Welcome to Talgalen, ${character.name}!")
            sender.sendMessage("${GRAY}If you wish to view or change your character, use \"/character card\".")

            choiceService.displayPendingChoices(sender, character)
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
