package net.arvandor.talekeeper.mixpanel.event.character

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.unit.HeightUnit.METRES
import com.rpkit.players.bukkit.unit.WeightUnit.KILOGRAMS
import net.arvandor.magistersmonths.MagistersMonths
import net.arvandor.magistersmonths.datetime.MmDateTime
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.feat.TtFeatService
import net.arvandor.talekeeper.language.TtLanguageService
import net.arvandor.talekeeper.mixpanel.TtMixpanelEvent
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.spell.TtSpellService

abstract class TtMixpanelCharacterEvent(private val plugin: TalekeepersTome) : TtMixpanelEvent {

    abstract val character: TtCharacter?
    val characterProps: Map<String, Any?>
        get() = mapOf(
            "Character" to character?.toEventProperties(),
        )

    fun TtCharacter.toEventProperties(): Map<String, Any?> {
        val pronounService = Services.INSTANCE[TtPronounService::class.java]

        // We're being a little lazy here by assuming Magister's Months will always be installed
        // The proper solution would be to pull out these variables into another class that deals with MM interfaces
        // and avoid having any imports referencing MM here
        val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as MagistersMonths
        val calendar = magistersMonths.calendar
        val birthday = MmDateTime(calendar, birthdayYear, birthdayDay, 0, 0, 0)
        val month = calendar.getMonthAt(birthdayDay)
        val dayOfMonth = if (month != null) {
            (birthday.dayOfYear - calendar.getMonthAt(birthday.dayOfYear).startDay) + 1
        } else {
            0
        }

        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        val ancestry = ancestryService.getAncestry(ancestryId)

        val classService = Services.INSTANCE[TtClassService::class.java]
        val clazz = classService.getClass(firstClassId)

        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
        val background = backgroundService.getBackground(backgroundId)

        val experienceService = Services.INSTANCE[TtExperienceService::class.java]
        val level = experienceService.getLevelAtExperience(experience)

        val featService = Services.INSTANCE[TtFeatService::class.java]
        val feats = feats.mapNotNull(featService::getFeat)

        val spellService = Services.INSTANCE[TtSpellService::class.java]
        val spells = spells.mapNotNull(spellService::getSpell)

        val languageService = Services.INSTANCE[TtLanguageService::class.java]
        val languages = languages.mapNotNull(languageService::getLanguage)

        val choiceService = Services.INSTANCE[TtChoiceService::class.java]
        val choices = choiceOptions.mapKeys { (choiceId, _) ->
            choiceService.getChoice(choiceId)
        }.mapValues { (choice, optionId) ->
            choice?.getOption(optionId)
        }

        return mapOf(
            "ID" to id.value,
            "Name" to name,
            "Pronouns" to pronouns.mapKeys { (pronounSetId, _) ->
                pronounService.get(pronounSetId)?.name ?: "Unknown Pronoun Set"
            },
            "Birthday Year" to birthday.year,
            "Birthday Month" to month?.name,
            "Birthday Day" to dayOfMonth,
            "Ancestry" to ancestry?.name,
            "Sub-Ancestry" to subAncestryId?.let { subAncestryId ->
                ancestry?.getSubAncestry(subAncestryId)?.name
            },
            "Class" to clazz?.name,
            "Background" to background?.name,
            "Alignment" to alignment.displayName,
            "Base Ability Scores" to baseAbilityScores.mapKeys { (ability, _) ->
                ability.displayName
            },
            "Temp Ability Scores" to tempAbilityScores.mapKeys { (ability, _) ->
                ability.displayName
            },
            "HP" to maxHp,
            "Experience" to experience,
            "Level" to level,
            "Ability Score Bonuses" to abilityScoreBonuses.mapKeys { (ability, _) ->
                ability.displayName
            },
            "Feats" to feats.map { feat -> feat.name },
            "Spells" to spells.map { spell -> spell.name },
            "Skill Proficiencies" to skillProficiencies.map { skill -> skill.displayName },
            "Skill Expertise" to skillExpertise.map { skill -> skill.displayName },
            "Jack of All Trades" to jackOfAllTrades,
            "Initiative Bonus" to initiativeBonus,
            "Item Proficiencies" to itemProficiencies.map { itemProficiency -> itemProficiency.value },
            "Saving Throw Proficiencies" to savingThrowProficiencies.map { savingThrowProficiency -> savingThrowProficiency.displayName },
            "Speed" to speed.value,
            "Languages" to languages.map { language -> language.name },
            "Description" to description,
            "Height" to METRES.scaleFactor * height,
            "Weight" to KILOGRAMS.scaleFactor * weight,
            "Dead" to isDead,
            "Profile Hidden" to isProfileHidden,
            "Name Hidden" to isNameHidden,
            "Age Hidden" to isAgeHidden,
            "Ancestry Hidden" to isAncestryHidden,
            "Description Hidden" to isDescriptionHidden,
            "Height Hidden" to isHeightHidden,
            "Weight Hidden" to isWeightHidden,
            "Choices" to choices.mapKeys { (choice, _) ->
                choice?.text ?: "Unknown Choice"
            }.mapValues { (_, option) ->
                option?.text ?: "Unknown Option"
            },
            "Shelved" to isShelved,
        )
    }
}
