package net.arvandor.talekeeper

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.unit.RPKUnitService
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import net.arvandor.talekeeper.ability.TtAbilityService
import net.arvandor.talekeeper.ancestry.TtAncestry
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.ancestry.TtAncestryTrait
import net.arvandor.talekeeper.ancestry.TtSubAncestry
import net.arvandor.talekeeper.background.TtBackground
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacterCreationContextRepository
import net.arvandor.talekeeper.character.TtCharacterCreationRequestRepository
import net.arvandor.talekeeper.character.TtCharacterRepository
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.choice.TtChoice
import net.arvandor.talekeeper.choice.TtChoiceService
import net.arvandor.talekeeper.choice.option.TtChoiceOption
import net.arvandor.talekeeper.choice.option.TtChoiceOptionRepository
import net.arvandor.talekeeper.clazz.TtClass
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.clazz.TtSubClass
import net.arvandor.talekeeper.command.ancestry.TtAncestryCommand
import net.arvandor.talekeeper.command.background.TtBackgroundCommand
import net.arvandor.talekeeper.command.character.TtCharacterCommand
import net.arvandor.talekeeper.command.choice.TtChoiceCommand
import net.arvandor.talekeeper.command.hp.TtHpCommand
import net.arvandor.talekeeper.distance.TtDistance
import net.arvandor.talekeeper.effect.TtAbilityEffect
import net.arvandor.talekeeper.effect.TtCharacterTraitEffect
import net.arvandor.talekeeper.effect.TtEffectService
import net.arvandor.talekeeper.effect.TtFeatEffect
import net.arvandor.talekeeper.effect.TtItemProficiencyEffect
import net.arvandor.talekeeper.effect.TtLanguageEffect
import net.arvandor.talekeeper.effect.TtSavingThrowProficiencyEffect
import net.arvandor.talekeeper.effect.TtSkillProficiencyEffect
import net.arvandor.talekeeper.effect.TtSpeedEffect
import net.arvandor.talekeeper.effect.TtSpellEffect
import net.arvandor.talekeeper.experience.TtExperienceService
import net.arvandor.talekeeper.feat.TtFeat
import net.arvandor.talekeeper.feat.TtFeatService
import net.arvandor.talekeeper.item.TtItemService
import net.arvandor.talekeeper.language.TtLanguage
import net.arvandor.talekeeper.language.TtLanguageService
import net.arvandor.talekeeper.listener.AsyncPlayerPreLoginListener
import net.arvandor.talekeeper.listener.InventoryClickListener
import net.arvandor.talekeeper.listener.PlayerJoinListener
import net.arvandor.talekeeper.listener.PlayerQuitListener
import net.arvandor.talekeeper.prerequisite.TtAncestryPrerequisite
import net.arvandor.talekeeper.prerequisite.TtAndPrerequisite
import net.arvandor.talekeeper.prerequisite.TtBackgroundPrerequisite
import net.arvandor.talekeeper.prerequisite.TtChoicePrerequisite
import net.arvandor.talekeeper.prerequisite.TtClassPrerequisite
import net.arvandor.talekeeper.prerequisite.TtFeatPrerequisite
import net.arvandor.talekeeper.prerequisite.TtItemProficiencyPrerequisite
import net.arvandor.talekeeper.prerequisite.TtLanguagePrerequisite
import net.arvandor.talekeeper.prerequisite.TtLevelPrerequisite
import net.arvandor.talekeeper.prerequisite.TtNotPrerequisite
import net.arvandor.talekeeper.prerequisite.TtOrPrerequisite
import net.arvandor.talekeeper.prerequisite.TtSavingThrowProficiencyPrerequisite
import net.arvandor.talekeeper.prerequisite.TtSkillProficiencyPrerequisite
import net.arvandor.talekeeper.prerequisite.TtSpellPrerequisite
import net.arvandor.talekeeper.prerequisite.TtSubAncestryPrerequisite
import net.arvandor.talekeeper.prerequisite.TtSubClassPrerequisite
import net.arvandor.talekeeper.pronouns.TtPronounRepository
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.rpkit.TtRpkCharacterCardFieldService
import net.arvandor.talekeeper.rpkit.TtRpkCharacterService
import net.arvandor.talekeeper.source.TtSource
import net.arvandor.talekeeper.spawn.TtSpawnService
import net.arvandor.talekeeper.spell.TtSpellService
import net.arvandor.talekeeper.spell.component.TtMaterialSpellComponent
import net.arvandor.talekeeper.spell.component.TtSpellComponentsWithNoMaterial
import net.arvandor.talekeeper.spell.component.TtSpellComponentsWithObjectMaterial
import net.arvandor.talekeeper.spell.component.TtSpellComponentsWithStringMaterial
import net.arvandor.talekeeper.spell.duration.TtInstantSpellDuration
import net.arvandor.talekeeper.spell.duration.TtPermanentSpellDuration
import net.arvandor.talekeeper.spell.duration.TtSpecialSpellDuration
import net.arvandor.talekeeper.spell.duration.TtTimedSpellDuration
import net.arvandor.talekeeper.spell.entry.TtEntriesSpellEntry
import net.arvandor.talekeeper.spell.entry.TtListSpellEntry
import net.arvandor.talekeeper.spell.entry.TtStringSpellEntry
import net.arvandor.talekeeper.spell.entry.TtTableSpellEntry
import net.arvandor.talekeeper.spell.meta.TtSpellMeta
import net.arvandor.talekeeper.spell.range.TtConeSpellRange
import net.arvandor.talekeeper.spell.range.TtCubeSpellRange
import net.arvandor.talekeeper.spell.range.TtHemisphereSpellRange
import net.arvandor.talekeeper.spell.range.TtLineSpellRange
import net.arvandor.talekeeper.spell.range.TtPointSpellRange
import net.arvandor.talekeeper.spell.range.TtRadiusSpellRange
import net.arvandor.talekeeper.spell.range.TtSpecialSpellRange
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceFeet
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceMile
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceSelf
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceSight
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceTouch
import net.arvandor.talekeeper.spell.range.TtSpellRangeDistanceUnlimited
import net.arvandor.talekeeper.spell.range.TtSphereSpellRange
import net.arvandor.talekeeper.spell.scaling.TtSpellScalingLevelDice
import net.arvandor.talekeeper.spell.time.TtSpellTime
import net.arvandor.talekeeper.staff.TtStaffService
import net.arvandor.talekeeper.trait.TtCharacterTrait
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.plugin.java.JavaPlugin
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL

class TalekeepersTome : JavaPlugin() {

    internal lateinit var dsl: DSLContext

    override fun onEnable() {
        ConfigurationSerialization.registerClass(TtAncestry::class.java, "Ancestry")
        ConfigurationSerialization.registerClass(TtAncestryTrait::class.java, "AncestryTrait")
        ConfigurationSerialization.registerClass(TtSubAncestry::class.java, "SubAncestry")
        ConfigurationSerialization.registerClass(TtBackground::class.java, "Background")
        ConfigurationSerialization.registerClass(TtChoice::class.java, "Choice")
        ConfigurationSerialization.registerClass(TtChoiceOption::class.java, "ChoiceOption")
        ConfigurationSerialization.registerClass(TtClass::class.java, "Class")
        ConfigurationSerialization.registerClass(TtSubClass::class.java, "SubClass")
        ConfigurationSerialization.registerClass(TtDistance::class.java, "Distance")
        ConfigurationSerialization.registerClass(TtAbilityEffect::class.java, "AbilityEffect")
        ConfigurationSerialization.registerClass(TtCharacterTraitEffect::class.java, "CharacterTraitEffect")
        ConfigurationSerialization.registerClass(TtFeatEffect::class.java, "FeatEffect")
        ConfigurationSerialization.registerClass(TtItemProficiencyEffect::class.java, "ItemProficiencyEffect")
        ConfigurationSerialization.registerClass(TtLanguageEffect::class.java, "LanguageEffect")
        ConfigurationSerialization.registerClass(TtSavingThrowProficiencyEffect::class.java, "SavingThrowProficiencyEffect")
        ConfigurationSerialization.registerClass(TtSkillProficiencyEffect::class.java, "SkillProficiencyEffect")
        ConfigurationSerialization.registerClass(TtSpeedEffect::class.java, "SpeedEffect")
        ConfigurationSerialization.registerClass(TtSpellEffect::class.java, "SpellEffect")
        ConfigurationSerialization.registerClass(TtFeat::class.java, "Feat")
        ConfigurationSerialization.registerClass(TtLanguage::class.java, "Language")
        ConfigurationSerialization.registerClass(TtAncestryPrerequisite::class.java, "AncestryPrerequisite")
        ConfigurationSerialization.registerClass(TtAndPrerequisite::class.java, "AndPrerequisite")
        ConfigurationSerialization.registerClass(TtBackgroundPrerequisite::class.java, "BackgroundPrerequisite")
        ConfigurationSerialization.registerClass(TtChoicePrerequisite::class.java, "ChoicePrerequisite")
        ConfigurationSerialization.registerClass(TtClassPrerequisite::class.java, "ClassPrerequisite")
        ConfigurationSerialization.registerClass(TtFeatPrerequisite::class.java, "FeatPrerequisite")
        ConfigurationSerialization.registerClass(TtItemProficiencyPrerequisite::class.java, "ItemProficiencyPrerequisite")
        ConfigurationSerialization.registerClass(TtLanguagePrerequisite::class.java, "LanguagePrerequisite")
        ConfigurationSerialization.registerClass(TtLevelPrerequisite::class.java, "LevelPrerequisite")
        ConfigurationSerialization.registerClass(TtNotPrerequisite::class.java, "NotPrerequisite")
        ConfigurationSerialization.registerClass(TtOrPrerequisite::class.java, "OrPrerequisite")
        ConfigurationSerialization.registerClass(TtSavingThrowProficiencyPrerequisite::class.java, "SavingThrowProficiencyPrerequisite")
        ConfigurationSerialization.registerClass(TtSkillProficiencyPrerequisite::class.java, "SkillProficiencyPrerequisite")
        ConfigurationSerialization.registerClass(TtSpellPrerequisite::class.java, "SpellPrerequisite")
        ConfigurationSerialization.registerClass(TtSubAncestryPrerequisite::class.java, "SubAncestryPrerequisite")
        ConfigurationSerialization.registerClass(TtSubClassPrerequisite::class.java, "SubClassPrerequisite")
        ConfigurationSerialization.registerClass(TtSource::class.java, "Source")
        ConfigurationSerialization.registerClass(TtMaterialSpellComponent::class.java, "MaterialSpellComponent")
        ConfigurationSerialization.registerClass(TtSpellComponentsWithNoMaterial::class.java, "SpellComponentsWithNoMaterial")
        ConfigurationSerialization.registerClass(TtSpellComponentsWithStringMaterial::class.java, "SpellComponentsWithStringMaterial")
        ConfigurationSerialization.registerClass(TtSpellComponentsWithObjectMaterial::class.java, "SpellComponentsWithObjectMaterial")
        ConfigurationSerialization.registerClass(TtInstantSpellDuration::class.java, "InstantSpellDuration")
        ConfigurationSerialization.registerClass(TtTimedSpellDuration::class.java, "TimedSpellDuration")
        ConfigurationSerialization.registerClass(TtPermanentSpellDuration::class.java, "PermanentSpellDuration")
        ConfigurationSerialization.registerClass(TtSpecialSpellDuration::class.java, "SpecialSpellDuration")
        ConfigurationSerialization.registerClass(TtStringSpellEntry::class.java, "StringSpellEntry")
        ConfigurationSerialization.registerClass(TtEntriesSpellEntry::class.java, "EntriesSpellEntry")
        ConfigurationSerialization.registerClass(TtTableSpellEntry::class.java, "TableSpellEntry")
        ConfigurationSerialization.registerClass(TtListSpellEntry::class.java, "ListSpellEntry")
        ConfigurationSerialization.registerClass(TtSpellMeta::class.java, "SpellMeta")
        ConfigurationSerialization.registerClass(TtPointSpellRange::class.java, "PointSpellRange")
        ConfigurationSerialization.registerClass(TtRadiusSpellRange::class.java, "RadiusSpellRange")
        ConfigurationSerialization.registerClass(TtSphereSpellRange::class.java, "SphereSpellRange")
        ConfigurationSerialization.registerClass(TtConeSpellRange::class.java, "ConeSpellRange")
        ConfigurationSerialization.registerClass(TtSpecialSpellRange::class.java, "SpecialSpellRange")
        ConfigurationSerialization.registerClass(TtLineSpellRange::class.java, "LineSpellRange")
        ConfigurationSerialization.registerClass(TtHemisphereSpellRange::class.java, "HemisphereSpellRange")
        ConfigurationSerialization.registerClass(TtCubeSpellRange::class.java, "CubeSpellRange")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceFeet::class.java, "SpellRangeDistanceFeet")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceMile::class.java, "SpellRangeDistanceMile")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceSelf::class.java, "SpellRangeDistanceSelf")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceTouch::class.java, "SpellRangeDistanceTouch")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceSight::class.java, "SpellRangeDistanceSight")
        ConfigurationSerialization.registerClass(TtSpellRangeDistanceUnlimited::class.java, "SpellRangeDistanceUnlimited")
        ConfigurationSerialization.registerClass(TtSpellScalingLevelDice::class.java, "SpellScalingLevelDice")
        ConfigurationSerialization.registerClass(TtSpellTime::class.java, "SpellTime")
        ConfigurationSerialization.registerClass(TtCharacterTrait::class.java, "CharacterTrait")

        saveDefaultConfig()

        Class.forName("org.mariadb.jdbc.Driver")

        val hikariConfig = HikariConfig()
        hikariConfig.jdbcUrl = config.getString("database.url")
        val databaseUsername = config.getString("database.username")
        if (databaseUsername != null) {
            hikariConfig.username = databaseUsername
        }
        val databasePassword = config.getString("database.password")
        if (databasePassword != null) {
            hikariConfig.password = databasePassword
        }
        val dataSource = HikariDataSource(hikariConfig)

        val flyway = Flyway.configure(classLoader)
            .dataSource(dataSource)
            .locations("classpath:net/arvandor/talekeeper/db/migration")
            .table("tt_schema_history")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .validateOnMigrate(false)
            .load()
        flyway.migrate()

        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")

        val dialect = config.getString("database.dialect")?.let(SQLDialect::valueOf)
        val jooqSettings = Settings().withRenderSchema(false)

        dsl = DSL.using(
            dataSource,
            dialect,
            jooqSettings,
        )

        val characterCreationRequestRepo = TtCharacterCreationRequestRepository(dsl)
        val characterCreationContextRepo = TtCharacterCreationContextRepository(this, dsl)
        val characterRepo = TtCharacterRepository(this, dsl)
        val optionRepo = TtChoiceOptionRepository(dsl)
        val pronounRepo = TtPronounRepository(dsl)

        // Having access to unit formatting is required for ancestry serialization when saving default ancestries
        Services.INSTANCE.require(RPKUnitService::class.java).whenAvailable {
            Services.INSTANCE[TtAncestryService::class.java] = TtAncestryService(this)
        }
        Services.INSTANCE[TtBackgroundService::class.java] = TtBackgroundService(this)
        Services.INSTANCE[TtCharacterService::class.java] = TtCharacterService(this, dsl, characterRepo, characterCreationContextRepo, characterCreationRequestRepo)
        Services.INSTANCE[TtChoiceService::class.java] = TtChoiceService(this, optionRepo)
        Services.INSTANCE[TtClassService::class.java] = TtClassService(this)
        Services.INSTANCE[TtEffectService::class.java] = TtEffectService(this)
        Services.INSTANCE[TtFeatService::class.java] = TtFeatService(this)
        Services.INSTANCE[TtItemService::class.java] = TtItemService(this)
        Services.INSTANCE[TtLanguageService::class.java] = TtLanguageService(this)
        Services.INSTANCE[TtPronounService::class.java] = TtPronounService(this, pronounRepo)
        Services.INSTANCE[TtSpellService::class.java] = TtSpellService(this)
        Services.INSTANCE[TtAbilityService::class.java] = TtAbilityService(this)
        Services.INSTANCE[TtSpawnService::class.java] = TtSpawnService(this)
        Services.INSTANCE[TtStaffService::class.java] = TtStaffService(this)
        Services.INSTANCE[TtExperienceService::class.java] = TtExperienceService(this)

        // RPKit services
        Services.INSTANCE[RPKCharacterService::class.java] = TtRpkCharacterService(this)
        Services.INSTANCE[RPKCharacterCardFieldService::class.java] = TtRpkCharacterCardFieldService(this)

        server.pluginManager.registerEvents(AsyncPlayerPreLoginListener(), this)
        server.pluginManager.registerEvents(InventoryClickListener(), this)
        server.pluginManager.registerEvents(PlayerJoinListener(this), this)
        server.pluginManager.registerEvents(PlayerQuitListener(), this)

        getCommand("character")?.setExecutor(TtCharacterCommand(this))
        getCommand("ancestry")?.setExecutor(TtAncestryCommand())
        getCommand("background")?.setExecutor(TtBackgroundCommand())
        getCommand("choice")?.setExecutor(TtChoiceCommand(this))
        getCommand("hp")?.setExecutor(TtHpCommand(this))
    }
}
