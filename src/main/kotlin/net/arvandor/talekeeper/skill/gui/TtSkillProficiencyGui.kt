package net.arvandor.talekeeper.skill.gui

import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility
import net.arvandor.talekeeper.ability.TtAbility.CHARISMA
import net.arvandor.talekeeper.ability.TtAbility.CONSTITUTION
import net.arvandor.talekeeper.ability.TtAbility.DEXTERITY
import net.arvandor.talekeeper.ability.TtAbility.INTELLIGENCE
import net.arvandor.talekeeper.ability.TtAbility.STRENGTH
import net.arvandor.talekeeper.ability.TtAbility.WISDOM
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.gui.InventoryGui
import net.arvandor.talekeeper.gui.InventoryGuiIcon
import net.arvandor.talekeeper.gui.page
import net.arvandor.talekeeper.skill.TtSkill
import net.arvandor.talekeeper.skill.TtSkill.ACROBATICS
import net.arvandor.talekeeper.skill.TtSkill.ANIMAL_HANDLING
import net.arvandor.talekeeper.skill.TtSkill.ARCANA
import net.arvandor.talekeeper.skill.TtSkill.ATHLETICS
import net.arvandor.talekeeper.skill.TtSkill.DECEPTION
import net.arvandor.talekeeper.skill.TtSkill.HISTORY
import net.arvandor.talekeeper.skill.TtSkill.INSIGHT
import net.arvandor.talekeeper.skill.TtSkill.INTIMIDATION
import net.arvandor.talekeeper.skill.TtSkill.INVESTIGATION
import net.arvandor.talekeeper.skill.TtSkill.MEDICINE
import net.arvandor.talekeeper.skill.TtSkill.NATURE
import net.arvandor.talekeeper.skill.TtSkill.PERCEPTION
import net.arvandor.talekeeper.skill.TtSkill.PERFORMANCE
import net.arvandor.talekeeper.skill.TtSkill.PERSUASION
import net.arvandor.talekeeper.skill.TtSkill.RELIGION
import net.arvandor.talekeeper.skill.TtSkill.SLEIGHT_OF_HAND
import net.arvandor.talekeeper.skill.TtSkill.STEALTH
import net.arvandor.talekeeper.skill.TtSkill.SURVIVAL
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material.AXOLOTL_BUCKET
import org.bukkit.Material.BEACON
import org.bukkit.Material.CAMPFIRE
import org.bukkit.Material.CARVED_PUMPKIN
import org.bukkit.Material.CLOCK
import org.bukkit.Material.COMPASS
import org.bukkit.Material.DIAMOND_SWORD
import org.bukkit.Material.ENDER_EYE
import org.bukkit.Material.GOLD_INGOT
import org.bukkit.Material.JACK_O_LANTERN
import org.bukkit.Material.LADDER
import org.bukkit.Material.LEATHER_BOOTS
import org.bukkit.Material.MAP
import org.bukkit.Material.MILK_BUCKET
import org.bukkit.Material.MUSIC_DISC_CAT
import org.bukkit.Material.PHANTOM_MEMBRANE
import org.bukkit.Material.POTION
import org.bukkit.Material.PUMPKIN
import org.bukkit.Material.RABBIT_FOOT
import org.bukkit.Material.ROSE_BUSH
import org.bukkit.Material.SPYGLASS
import org.bukkit.Material.SUNFLOWER
import org.bukkit.Material.TOTEM_OF_UNDYING
import org.bukkit.Material.WHITE_CANDLE
import org.bukkit.Material.WRITABLE_BOOK
import org.bukkit.event.inventory.ClickType.LEFT
import org.bukkit.event.inventory.ClickType.RIGHT
import org.bukkit.event.inventory.ClickType.SHIFT_LEFT
import org.bukkit.inventory.ItemFlag.HIDE_POTION_EFFECTS
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType.INSTANT_HEAL
import kotlin.math.floor
import kotlin.math.roundToInt

class TtSkillProficiencyGui(plugin: TalekeepersTome, private val character: TtCharacter) : InventoryGui(
    plugin,
    "Skills",
    page {
        TtSkill.values().forEachIndexed { slot, skill ->
            icon(slot) {
                item = iconFor(skill, character)

                onClick = { player, clickType ->
                    player.closeInventory()

                    val rollRadius = plugin.config.getInt("rolls.radius")
                    val radiusSquared = rollRadius * rollRadius
                    player.world.players.filter { it.location.distanceSquared(player.location) <= radiusSquared }.forEach {
                        it.sendMessage("${ChatColor.of("#b7c495")}Rolling skill: ${skill.displayName}")
                    }
                    if (clickType == LEFT || clickType == SHIFT_LEFT) {
                        player.performCommand("roll d20+${calculateModifier(character, skill)}")
                    } else if (clickType == RIGHT) {
                        repeat(2) {
                            player.performCommand("roll d20+${calculateModifier(character, skill)}")
                        }
                    }
                }
            }
        }
        TtAbility.values().forEachIndexed { slot, ability ->
            icon(slot + 18) {
                item = iconFor(ability, character)

                onClick = { player, clickType ->
                    player.closeInventory()

                    val rollRadius = plugin.config.getInt("rolls.radius")
                    val radiusSquared = rollRadius * rollRadius
                    player.world.players.filter { it.location.distanceSquared(player.location) <= radiusSquared }.forEach {
                        it.sendMessage("${ChatColor.of("#5a8c3e")}Rolling saving throw: ${ability.displayName}")
                    }
                    if (clickType == LEFT || clickType == SHIFT_LEFT) {
                        player.performCommand("roll d20+${calculateModifier(character, ability)}")
                    } else if (clickType == RIGHT) {
                        repeat(2) {
                            player.performCommand("roll d20+${calculateModifier(character, ability)}")
                        }
                    }
                }
            }
        }

        icon(26) {
            val dexterityModifier = character.getModifier(DEXTERITY)
            val jackOfAllTradesModifier = if (character.jackOfAllTrades) {
                floor(character.proficiencyBonus.toDouble() / 2.0).roundToInt()
            } else {
                0
            }
            val initiativeModifier = dexterityModifier + jackOfAllTradesModifier + character.initiativeBonus
            item = item(
                CLOCK,
                "${ChatColor.of("#7d6fb1")}Initiative",
                listOf(
                    "${ChatColor.of("#b7c495")}Roll: ${ChatColor.of("#5a8c3e")}d20+$initiativeModifier",
                    "${ChatColor.of("#b7c495")}Left click to roll, right click to roll twice.",
                ),
            )

            onClick = { player, clickType ->
                player.closeInventory()

                val rollRadius = plugin.config.getInt("rolls.radius")
                val radiusSquared = rollRadius * rollRadius
                player.world.players.filter { it.location.distanceSquared(player.location) <= radiusSquared }.forEach {
                    it.sendMessage("${ChatColor.of("#7d6fb1")}Rolling initiative")
                }
                if (clickType == LEFT || clickType == SHIFT_LEFT) {
                    player.performCommand("roll d20+$initiativeModifier")
                } else if (clickType == RIGHT) {
                    repeat(2) {
                        player.performCommand("roll d20+$initiativeModifier")
                    }
                }
            }
        }
    },
    27,
)

private fun InventoryGuiIcon.iconFor(skill: TtSkill, character: TtCharacter): ItemStack {
    val displayName = "${ChatColor.of("#7d6fb1")}${skill.displayName}"

    val modifier = calculateModifier(character, skill)
    val lore = listOf(
        "${ChatColor.of("#b7c495")}Ability: ${ChatColor.of("#5a8c3e")}${skill.ability.displayName}",
        "${ChatColor.of("#b7c495")}Proficiency: ${ChatColor.of("#5a8c3e")}${character.skillProficiencies.contains(skill)}",
        "${ChatColor.of("#b7c495")}Expertise: ${ChatColor.of("#5a8c3e")}${character.skillExpertise.contains(skill)}",
        "${ChatColor.of("#b7c495")}Roll: ${ChatColor.of("#5a8c3e")}d20+$modifier",
        "${ChatColor.of("#b7c495")}Left click to roll, right click to roll twice.",
    )
    return when (skill) {
        ACROBATICS -> item(
            LADDER,
            displayName,
            lore,
        )

        ANIMAL_HANDLING -> item(
            AXOLOTL_BUCKET,
            displayName,
            lore,
        )

        ARCANA -> item(
            ENDER_EYE,
            displayName,
            lore,
        )

        ATHLETICS -> item(
            LEATHER_BOOTS,
            displayName,
            lore,
        )

        DECEPTION -> item(
            CARVED_PUMPKIN,
            displayName,
            lore,
        )

        HISTORY -> item(
            WRITABLE_BOOK,
            displayName,
            lore,
        )

        INSIGHT -> item(
            WHITE_CANDLE,
            displayName,
            lore,
        )

        INTIMIDATION -> item(
            PUMPKIN,
            displayName,
            lore,
        )

        INVESTIGATION -> item(
            COMPASS,
            displayName,
            lore,
        )

        MEDICINE -> item(
            POTION,
            displayName,
            lore,
        ) {
            itemMeta = (itemMeta as? PotionMeta)?.apply {
                basePotionData = PotionData(INSTANT_HEAL)
                addItemFlags(HIDE_POTION_EFFECTS)
            }
        }

        NATURE -> item(
            SUNFLOWER,
            displayName,
            lore,
        )

        PERCEPTION -> item(
            SPYGLASS,
            displayName,
            lore,
        )

        PERFORMANCE -> item(
            MUSIC_DISC_CAT,
            displayName,
            lore,
        )

        PERSUASION -> item(
            JACK_O_LANTERN,
            displayName,
            lore,
        )

        RELIGION -> item(
            TOTEM_OF_UNDYING,
            displayName,
            lore,
        )

        SLEIGHT_OF_HAND -> item(
            GOLD_INGOT,
            displayName,
            lore,
        )

        STEALTH -> item(
            PHANTOM_MEMBRANE,
            displayName,
            lore,
        )

        SURVIVAL -> item(
            CAMPFIRE,
            displayName,
            lore,
        )
    }
}

private fun calculateModifier(
    character: TtCharacter,
    skill: TtSkill,
): Int {
    // d20 + ability modifier
    // + proficiency (if proficient)
    // + double proficiency (if expertise NOT TRIPLE, add one more time)
    // + jack of all trades class feature (if not proficiency or expertise)
    val abilityModifier = character.getModifier(skill.ability)
    val proficiencyBonus = if (character.skillProficiencies.contains(skill)) character.proficiencyBonus else 0
    val expertiseBonus = if (character.skillExpertise.contains(skill)) character.proficiencyBonus else 0
    val jackOfAllTradesBonus = if (character.jackOfAllTrades && !character.skillProficiencies.contains(skill)) {
        floor(character.proficiencyBonus.toDouble() / 2.0).roundToInt()
    } else {
        0
    }
    return abilityModifier + proficiencyBonus + expertiseBonus + jackOfAllTradesBonus
}

private fun InventoryGuiIcon.iconFor(ability: TtAbility, character: TtCharacter): ItemStack {
    val displayName = "${ChatColor.of("#7d6fb1")}${ability.displayName} saving throw"

    val modifier = calculateModifier(character, ability)
    val lore = listOf(
        "${ChatColor.of("#b7c495")}Proficiency: ${ChatColor.of("#5a8c3e")}${character.savingThrowProficiencies.contains(ability)}",
        "${ChatColor.of("#b7c495")}Roll: ${ChatColor.of("#5a8c3e")}d20+$modifier",
        "${ChatColor.of("#b7c495")}Left click to roll, right click to roll twice.",
    )
    return when (ability) {
        STRENGTH -> item(
            DIAMOND_SWORD,
            displayName,
            lore,
        )

        DEXTERITY -> item(
            RABBIT_FOOT,
            displayName,
            lore,
        )

        CONSTITUTION -> item(
            MILK_BUCKET,
            displayName,
            lore,
        )

        INTELLIGENCE -> item(
            MAP,
            displayName,
            lore,
        )

        WISDOM -> item(
            BEACON,
            displayName,
            lore,
        )

        CHARISMA -> item(
            ROSE_BUSH,
            displayName,
            lore,
        )
    }
}

private fun calculateModifier(
    character: TtCharacter,
    ability: TtAbility,
): Int {
    val abilityModifier = character.getModifier(ability)
    val proficiencyModifier =
        if (character.savingThrowProficiencies.contains(ability)) character.proficiencyBonus else 0
    return abilityModifier + proficiencyModifier
}
