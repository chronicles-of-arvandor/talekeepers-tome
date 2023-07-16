package net.arvandor.talekeeper.rpkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.core.bukkit.location.LocationsKt
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.BukkitExtensionsKt
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.arvandor.talekeeper.ancestry.TtAncestryService
import net.arvandor.talekeeper.character.TtCharacter
import net.arvandor.talekeeper.pronouns.TtPronounService
import net.arvandor.talekeeper.pronouns.TtPronounSet
import org.bukkit.inventory.ItemStack

class TtRpkCharacterWrapper(var character: TtCharacter) : RPKCharacter {

    override fun getId(): RPKCharacterId {
        return character.rpkitId
    }

    override fun setId(id: RPKCharacterId) {
        character = character.copy(rpkitId = id)
    }

    override fun getProfile(): RPKProfile {
        val profileService = Services.INSTANCE[RPKProfileService::class.java]
        return profileService.getPreloadedProfile(character.profileId)
    }

    override fun setProfile(profile: RPKProfile) {
        character = character.copy(profileId = profile.id)
    }

    override fun getMinecraftProfile(): RPKMinecraftProfile {
        val minecraftProfileService = Services.INSTANCE[RPKMinecraftProfileService::class.java]
        return minecraftProfileService.getPreloadedMinecraftProfile(character.minecraftProfileId)
    }

    override fun setMinecraftProfile(minecraftProfile: RPKMinecraftProfile?) {
        character = character.copy(minecraftProfileId = minecraftProfile?.id)
    }

    override fun getName(): String {
        return character.name
    }

    override fun setName(name: String) {
        character = character.copy(name = name)
    }

    override fun getGender(): String {
        val pronounService = Services.INSTANCE[TtPronounService::class.java]
        return pronounService.pronoun(character, TtPronounSet::subject)
    }

    override fun setGender(gender: String) {
    }

    override fun getAge(): Int {
        // TODO calculate age when calendar is implemented
        return 18
    }

    override fun setAge(age: Int) {
        // TODO calculate birth date when calendar is implemented
    }

    @Deprecated("Use getSpecies", ReplaceWith("getSpecies()"))
    override fun getRace(): RPKRace? {
        return species
    }

    @Deprecated("Use setSpecies", ReplaceWith("setSpecies(race)"))
    override fun setRace(race: RPKRace?) {
        species = race
    }

    override fun getSpecies(): RPKRace? {
        val ancestryService = Services.INSTANCE[TtAncestryService::class.java]
        val ancestry = ancestryService.getAncestry(character.ancestryId)
            ?: return null
        return TtRpkAncestryWrapper(ancestry)
    }

    override fun setSpecies(species: RPKRace?) {
        if (species is TtRpkAncestryWrapper) {
            character = character.copy(ancestryId = species.ancestry.id)
        }
    }

    override fun getDescription(): String {
        return character.description
    }

    override fun setDescription(description: String) {
        character = character.copy(description = description)
    }

    override fun getHeight(): Double {
        return character.height
    }

    override fun setHeight(height: Double) {
        character = character.copy(height = height)
    }

    override fun getWeight(): Double {
        return character.weight
    }

    override fun setWeight(weight: Double) {
        character = character.copy(weight = weight)
    }

    override fun isDead(): Boolean {
        return character.isDead
    }

    override fun setDead(isDead: Boolean) {
        character = character.copy(isDead = isDead)
    }

    override fun getLocation(): RPKLocation {
        return LocationsKt.toRPKLocation(character.location)
    }

    override fun setLocation(location: RPKLocation) {
        character = character.copy(location = LocationsKt.toBukkitLocation(location))
    }

    override fun getInventoryContents(): Array<ItemStack?> {
        return character.inventoryContents
    }

    override fun setInventoryContents(inventoryContents: Array<ItemStack?>) {
        character = character.copy(inventoryContents = inventoryContents)
    }

    override fun getHelmet(): ItemStack? {
        return null
    }

    override fun setHelmet(helmet: ItemStack?) {
    }

    override fun getChestplate(): ItemStack? {
        return null
    }

    override fun setChestplate(chestplate: ItemStack?) {
    }

    override fun getLeggings(): ItemStack? {
        return null
    }

    override fun setLeggings(leggings: ItemStack?) {
    }

    override fun getBoots(): ItemStack? {
        return null
    }

    override fun setBoots(boots: ItemStack?) {
    }

    override fun getHealth(): Double {
        return character.health
    }

    override fun setHealth(health: Double) {
        character = character.copy(health = health)
    }

    override fun getMaxHealth(): Double {
        return 20.0
    }

    override fun setMaxHealth(maxHealth: Double) {
    }

    override fun getMana(): Int {
        return 0
    }

    override fun setMana(mana: Int) {
    }

    override fun getMaxMana(): Int {
        return 0
    }

    override fun setMaxMana(maxMana: Int) {
    }

    override fun getFoodLevel(): Int {
        return character.foodLevel
    }

    override fun setFoodLevel(foodLevel: Int) {
        character = character.copy(foodLevel = foodLevel)
    }

    override fun getThirstLevel(): Int {
        return 20
    }

    override fun setThirstLevel(thirstLevel: Int) {
    }

    override fun isProfileHidden(): Boolean {
        return character.isProfileHidden
    }

    override fun setProfileHidden(isProfileHidden: Boolean) {
        character = character.copy(isProfileHidden = isProfileHidden)
    }

    override fun isNameHidden(): Boolean {
        return character.isNameHidden
    }

    override fun setNameHidden(isNameHidden: Boolean) {
        character = character.copy(isNameHidden = isNameHidden)
    }

    override fun isGenderHidden(): Boolean {
        return false
    }

    override fun setGenderHidden(isGenderHidden: Boolean) {
    }

    override fun isAgeHidden(): Boolean {
        // TODO implement when age is implemented
        return true
    }

    override fun setAgeHidden(isAgeHidden: Boolean) {
        // TODO implement when age is implemented
    }

    @Deprecated("Use isSpeciesHidden", ReplaceWith("isSpeciesHidden"))
    override fun isRaceHidden(): Boolean {
        return isSpeciesHidden
    }

    @Deprecated("Use setSpeciesHidden", ReplaceWith("setSpeciesHidden(isRaceHidden)"))
    override fun setRaceHidden(isRaceHidden: Boolean) {
        isSpeciesHidden = isRaceHidden
    }

    override fun isSpeciesHidden(): Boolean {
        return character.isAncestryHidden
    }

    override fun setSpeciesHidden(isSpeciesHidden: Boolean) {
        character = character.copy(isAncestryHidden = isSpeciesHidden)
    }

    override fun isDescriptionHidden(): Boolean {
        return character.isDescriptionHidden
    }

    override fun setDescriptionHidden(isDescriptionHidden: Boolean) {
        character = character.copy(isDescriptionHidden = isDescriptionHidden)
    }

    override fun isHeightHidden(): Boolean {
        return character.isHeightHidden
    }

    override fun setHeightHidden(isHeightHidden: Boolean) {
        character = character.copy(isHeightHidden = isHeightHidden)
    }

    override fun isWeightHidden(): Boolean {
        return character.isWeightHidden
    }

    override fun setWeightHidden(isWeightHidden: Boolean) {
        character = character.copy(isWeightHidden = isWeightHidden)
    }

    override fun showCharacterCard(minecraftProfile: RPKMinecraftProfile) {
        val bukkitPlayer = BukkitExtensionsKt.toBukkitPlayer(minecraftProfile)
        if (bukkitPlayer != null) {
            character.display(bukkitPlayer)
        }
    }
}
