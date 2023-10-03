package net.arvandor.talekeeper.rpkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.core.bukkit.location.LocationsKt
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import dev.forkhandles.result4k.onFailure
import net.arvandor.talekeeper.TalekeepersTome
import net.arvandor.talekeeper.ability.TtAbility.CHARISMA
import net.arvandor.talekeeper.ability.TtAbility.CONSTITUTION
import net.arvandor.talekeeper.ability.TtAbility.DEXTERITY
import net.arvandor.talekeeper.ability.TtAbility.INTELLIGENCE
import net.arvandor.talekeeper.ability.TtAbility.STRENGTH
import net.arvandor.talekeeper.ability.TtAbility.WISDOM
import net.arvandor.talekeeper.alignment.TtAlignment
import net.arvandor.talekeeper.background.TtBackgroundService
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.character.TtCharacterService
import net.arvandor.talekeeper.clazz.TtClassInfo
import net.arvandor.talekeeper.clazz.TtClassService
import net.arvandor.talekeeper.speed.TtSpeed
import net.arvandor.talekeeper.speed.TtSpeedUnit.FEET
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.logging.Level.SEVERE

class TtRpkCharacterService(private val plugin: TalekeepersTome) : RPKCharacterService {
    override fun getPlugin() = plugin

    override fun getPreloadedCharacter(id: RPKCharacterId): RPKCharacter? {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        val character = characterService.getPreloadedCharacter(id)
            ?: return null
        return TtRpkCharacterWrapper(character)
    }

    override fun loadCharacter(id: RPKCharacterId): CompletableFuture<out RPKCharacter?> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            val character = characterService.loadCharacter(id).onFailure {
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                throw it.reason.cause
            }
                ?: return@supplyAsync null
            return@supplyAsync TtRpkCharacterWrapper(character)
        }
    }

    override fun unloadCharacter(id: RPKCharacterId) {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        characterService.unloadCharacter(id)
    }

    override fun getCharacter(id: RPKCharacterId): CompletableFuture<out RPKCharacter?> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            val character = characterService.getCharacter(id).onFailure {
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                throw it.reason.cause
            }
                ?: return@supplyAsync null
            return@supplyAsync TtRpkCharacterWrapper(character)
        }
    }

    override fun getPreloadedActiveCharacter(minecraftProfile: RPKMinecraftProfile): RPKCharacter? {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile.id)
            ?: return null
        return TtRpkCharacterWrapper(character)
    }

    override fun loadActiveCharacter(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKCharacter?> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            val character = characterService.loadActiveCharacter(minecraftProfile.id)
                ?: return@supplyAsync null
            return@supplyAsync TtRpkCharacterWrapper(character)
        }
    }

    override fun unloadActiveCharacter(minecraftProfile: RPKMinecraftProfile) {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        characterService.unloadActiveCharacter(minecraftProfile.id)
    }

    override fun getActiveCharacter(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKCharacter?> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            val character = characterService.getActiveCharacter(minecraftProfile.id).onFailure {
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                throw it.reason.cause
            }
                ?: return@supplyAsync null
            return@supplyAsync TtRpkCharacterWrapper(character)
        }
    }

    override fun setActiveCharacter(minecraftProfile: RPKMinecraftProfile, character: RPKCharacter?): CompletableFuture<Void> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.runAsync {
            if (character is TtRpkCharacterWrapper?) {
                characterService.setActiveCharacter(minecraftProfile, character?.character)
            }
        }
    }

    override fun getCharacters(profile: RPKProfile): CompletableFuture<List<RPKCharacter>> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            val characters = characterService.getCharacters(profile.id).onFailure {
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                throw it.reason.cause
            }
            return@supplyAsync characters.map { TtRpkCharacterWrapper(it) }
        }
    }

    override fun addCharacter(character: RPKCharacter): CompletableFuture<Void> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.runAsync {
            if (character is TtRpkCharacterWrapper) {
                characterService.save(character.character)
            }
        }
    }

    override fun createCharacter(
        profile: RPKProfile?,
        name: String?,
        gender: String?,
        age: Int?,
        species: RPKRace?,
        description: String?,
        height: Double?,
        weight: Double?,
        isDead: Boolean?,
        location: RPKLocation?,
        inventoryContents: Array<ItemStack?>?,
        helmet: ItemStack?,
        chestplate: ItemStack?,
        leggings: ItemStack?,
        boots: ItemStack?,
        health: Double?,
        maxHealth: Double?,
        mana: Int?,
        maxMana: Int?,
        foodLevel: Int?,
        thirstLevel: Int?,
        isProfileHidden: Boolean?,
        isNameHidden: Boolean?,
        isGenderHidden: Boolean?,
        isAgeHidden: Boolean?,
        isSpeciesHidden: Boolean?,
        isDescriptionHidden: Boolean?,
        isHeightHidden: Boolean?,
        isWeightHidden: Boolean?,
    ): CompletableFuture<RPKCharacter> {
        // This isn't really called by RPKit so we don't have to be too precious about it, but it does need to return a
        // character to satisfy the contract. RPKit doesn't have all our fields so just choose the first one.
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        val classService = Services.INSTANCE[TtClassService::class.java]
        val backgroundService = Services.INSTANCE[TtBackgroundService::class.java]
        return CompletableFuture.supplyAsync {
            val clazz = classService.getAll().first()
            val classId = clazz.id
            val background = backgroundService.getAll().first()
            val backgroundId = background.id
            characterService.save(
                TtCharacter(
                    plugin,
                    profileId = profile?.id ?: throw RuntimeException("Profile cannot be null"),
                    minecraftProfileId = null,
                    name = name ?: "",
                    pronouns = emptyMap(),
                    ancestryId = (species as? TtRpkAncestryWrapper)?.ancestry?.id ?: throw RuntimeException("Species must be a Talekeeper's Tome ancestry"),
                    subAncestryId = null,
                    firstClassId = classId,
                    classes = mapOf(classId to TtClassInfo(1, null)),
                    backgroundId = backgroundId,
                    alignment = TtAlignment.NEUTRAL,
                    abilityScores = mapOf(
                        STRENGTH to 12,
                        DEXTERITY to 12,
                        CONSTITUTION to 12,
                        INTELLIGENCE to 12,
                        WISDOM to 12,
                        CHARISMA to 12,
                    ),
                    tempAbilityScores = emptyMap(),
                    hp = clazz.baseHp,
                    tempHp = 0,
                    experience = 0,
                    feats = emptyList(),
                    spells = emptyList(),
                    skillProficiencies = emptyList(),
                    skillExpertise = emptyList(),
                    jackOfAllTrades = false,
                    initiativeBonus = 0,
                    itemProficiencies = emptyList(),
                    savingThrowProficiencies = emptyList(),
                    speed = TtSpeed(0, FEET),
                    languages = emptyList(),
                    traits = emptyList(),
                    description = description ?: "",
                    height = height ?: 0.0,
                    weight = weight ?: 0.0,
                    isDead = isDead ?: false,
                    location = LocationsKt.toBukkitLocation(location ?: throw RuntimeException("Location cannot be null")),
                    inventoryContents = inventoryContents ?: emptyArray(),
                    health = 20.0,
                    foodLevel = 20,
                    exhaustion = 0f,
                    saturation = 5f,
                    isProfileHidden = isProfileHidden ?: false,
                    isNameHidden = isNameHidden ?: false,
                    isAgeHidden = isAgeHidden ?: false,
                    isAncestryHidden = isSpeciesHidden ?: false,
                    isDescriptionHidden = isDescriptionHidden ?: false,
                    isHeightHidden = isHeightHidden ?: false,
                    isWeightHidden = isWeightHidden ?: false,
                    choiceOptions = emptyMap(),
                ),
            ).onFailure {
                plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                throw it.reason.cause
            }.let(::TtRpkCharacterWrapper)
        }
    }

    override fun removeCharacter(character: RPKCharacter): CompletableFuture<Void> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.runAsync {
            if (character is TtRpkCharacterWrapper) {
                characterService.delete(character.character.id)
            }
        }
    }

    override fun updateCharacter(character: RPKCharacter): CompletableFuture<RPKCharacter?> {
        val characterService = Services.INSTANCE[TtCharacterService::class.java]
        return CompletableFuture.supplyAsync {
            if (character is TtRpkCharacterWrapper) {
                characterService.save(character.character).onFailure {
                    plugin.logger.log(SEVERE, it.reason.message, it.reason.cause)
                    throw it.reason.cause
                }
                return@supplyAsync character
            }
            return@supplyAsync null
        }
    }
}
