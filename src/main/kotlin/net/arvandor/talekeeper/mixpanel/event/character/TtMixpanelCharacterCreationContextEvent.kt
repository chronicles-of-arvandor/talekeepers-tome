package net.arvandor.talekeeper.mixpanel.event.character

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.unit.HeightUnit.METRES
import com.rpkit.players.bukkit.unit.WeightUnit.KILOGRAMS
import net.arvandor.magistersmonths.MagistersMonths
import net.arvandor.magistersmonths.datetime.MmDateTime
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacterCreationContext
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.mixpanel.TtMixpanelEvent
import net.arvandor.talekeeper.pronouns.TtPronounService

abstract class TtMixpanelCharacterCreationContextEvent(private val plugin: TalekeepersTome) : TtMixpanelEvent {

    abstract val creationContext: TtCharacterCreationContext
    val contextProps: Map<String, Any?>
        get() = mapOf(
            "Context" to creationContext.toEventProperties(),
        )

    fun TtCharacterCreationContext.toEventProperties(): Map<String, Any?> {
        val pronounService = Services.INSTANCE[TtPronounService::class.java]

        // We're being a little lazy here by assuming Magister's Months will always be installed
        // The proper solution would be to pull out these variables into another class that deals with MM interfaces
        // and avoid having any imports referencing MM here
        val magistersMonths = plugin.server.pluginManager.getPlugin("magisters-months") as MagistersMonths
        val calendar = magistersMonths.calendar
        val birthday = if (birthdayYear != null && birthdayDay != null) {
            MmDateTime(
                calendar,
                birthdayYear,
                birthdayDay,
                0,
                0,
                0,
            )
        } else {
            null
        }
        val month = birthday?.dayOfYear?.let(calendar::getMonthAt)
        val dayOfMonth = if (month != null) {
            (birthday.dayOfYear - calendar.getMonthAt(birthday.dayOfYear).startDay) + 1
        } else {
            0
        }

        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        val ancestry = ancestryId?.let(ancestryService::getAncestry)

        val classService = Services.INSTANCE[TtClassService::class.java]
        val clazz = firstClassId?.let(classService::getClass)

        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
        val background = backgroundId?.let(backgroundService::getBackground)

        val experienceService = Services.INSTANCE[TtExperienceService::class.java]
        val level = experienceService.getLevelAtExperience(experience)

        return mapOf(
            "ID" to id.value,
            "Name" to name,
            "Pronouns" to pronouns.mapKeys { (pronounSetId, _) ->
                pronounService.get(pronounSetId)?.name ?: "Unknown Pronoun Set"
            },
            "Birthday Year" to birthday?.year,
            "Birthday Month" to month?.name,
            "Birthday Day" to dayOfMonth,
            "Ancestry" to ancestry?.name,
            "Sub-Ancestry" to subAncestryId?.let { subAncestryId ->
                ancestry?.getSubAncestry(subAncestryId)?.name
            },
            "Class" to clazz?.name,
            "Background" to background?.name,
            "Alignment" to alignment?.displayName,
            "Ability Score Choices" to abilityScoreChoices.mapKeys { (ability, _) ->
                ability.displayName
            },
            "Experience" to experience,
            "Level" to level,
            "Description" to description,
            "Height" to height?.times(METRES.scaleFactor),
            "Weight" to weight?.times(KILOGRAMS.scaleFactor),
            "Profile Hidden" to isProfileHidden,
            "Name Hidden" to isNameHidden,
            "Age Hidden" to isAgeHidden,
            "Ancestry Hidden" to isAncestryHidden,
            "Description Hidden" to isDescriptionHidden,
            "Height Hidden" to isHeightHidden,
            "Weight Hidden" to isWeightHidden,
        )
    }
}
