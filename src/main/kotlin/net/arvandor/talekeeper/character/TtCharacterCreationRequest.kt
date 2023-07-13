package net.arvandor.talekeeper.character

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import java.time.Instant

data class TtCharacterCreationRequest(
    val minecraftProfileId: RPKMinecraftProfileId,
    val requestTime: Instant,
)
