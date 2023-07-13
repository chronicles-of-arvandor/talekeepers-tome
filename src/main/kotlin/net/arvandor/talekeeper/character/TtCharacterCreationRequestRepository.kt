package net.arvandor.talekeeper.character

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import net.arvandor.talekeeper.jooq.Tables.TT_CHARACTER_CREATION_REQUEST
import net.arvandor.talekeeper.jooq.tables.records.TtCharacterCreationRequestRecord
import org.jooq.DSLContext
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC

class TtCharacterCreationRequestRepository(private val dsl: DSLContext) {

    fun getCharacterCreationRequest(minecraftProfileId: RPKMinecraftProfileId): TtCharacterCreationRequest? = dsl
        .selectFrom(TT_CHARACTER_CREATION_REQUEST)
        .where(TT_CHARACTER_CREATION_REQUEST.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
        .fetchOne()
        ?.toDomain()

    fun getAll(): List<TtCharacterCreationRequest> {
        return dsl
            .selectFrom(TT_CHARACTER_CREATION_REQUEST)
            .fetch()
            .map { it.toDomain() }
    }

    fun upsert(request: TtCharacterCreationRequest): TtCharacterCreationRequest {
        dsl.insertInto(TT_CHARACTER_CREATION_REQUEST)
            .set(TT_CHARACTER_CREATION_REQUEST.MINECRAFT_PROFILE_ID, request.minecraftProfileId.value)
            .set(TT_CHARACTER_CREATION_REQUEST.REQUEST_TIME, LocalDateTime.ofInstant(request.requestTime, UTC))
            .onConflict().doNothing()
            .execute()
        return dsl.selectFrom(TT_CHARACTER_CREATION_REQUEST)
            .where(TT_CHARACTER_CREATION_REQUEST.MINECRAFT_PROFILE_ID.eq(request.minecraftProfileId.value))
            .fetchOne()
            .let(::requireNotNull)
            .toDomain()
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId) {
        dsl.deleteFrom(TT_CHARACTER_CREATION_REQUEST)
            .where(TT_CHARACTER_CREATION_REQUEST.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
    }

    private fun TtCharacterCreationRequestRecord.toDomain(): TtCharacterCreationRequest {
        return TtCharacterCreationRequest(
            RPKMinecraftProfileId(minecraftProfileId),
            requestTime.toInstant(UTC),
        )
    }
}
