package net.arvandor.talekeeper.character

import com.rpkit.players.bukkit.profile.RPKProfileId
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_SHELVE_COOLDOWN
import org.jooq.DSLContext
import java.time.Instant
import java.time.ZoneOffset

class TtShelveCooldownRepository(private val dsl: DSLContext) {

    fun get(profileId: RPKProfileId): Instant? {
        return dsl.selectFrom(TT_CHARACTER_SHELVE_COOLDOWN)
            .where(TT_CHARACTER_SHELVE_COOLDOWN.PROFILE_ID.eq(profileId.value))
            .fetchOne()
            ?.cooldownStartTime
            ?.toInstant(ZoneOffset.UTC)
    }

    fun upsert(profileId: RPKProfileId, cooldownStartTime: Instant) {
        dsl.insertInto(TT_CHARACTER_SHELVE_COOLDOWN)
            .set(TT_CHARACTER_SHELVE_COOLDOWN.PROFILE_ID, profileId.value)
            .set(TT_CHARACTER_SHELVE_COOLDOWN.COOLDOWN_START_TIME, cooldownStartTime.atOffset(ZoneOffset.UTC).toLocalDateTime())
            .onConflict(TT_CHARACTER_SHELVE_COOLDOWN.PROFILE_ID).doUpdate()
            .set(TT_CHARACTER_SHELVE_COOLDOWN.COOLDOWN_START_TIME, cooldownStartTime.atOffset(ZoneOffset.UTC).toLocalDateTime())
            .execute()
    }
}
