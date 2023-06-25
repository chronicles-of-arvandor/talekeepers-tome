package net.arvandor.talekeeper.spell.duration

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtSpellDuration : ConfigurationSerializable

@SerializableAs("InstantSpellDuration")
object TtInstantSpellDuration : TtSpellDuration {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtInstantSpellDuration
}

@SerializableAs("TimedSpellDuration")
data class TtTimedSpellDuration(
    val duration: TtSpellDuration,
    val concentration: Boolean,
) : TtSpellDuration {
    override fun serialize() = mapOf(
        "duration" to duration,
        "concentration" to concentration,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtTimedSpellDuration(
            serialized["duration"] as TtSpellDuration,
            serialized["concentration"] as Boolean,
        )
    }
}

@SerializableAs("PermanentSpellDuration")
data class TtPermanentSpellDuration(
    val ends: List<TtPermanentSpellEnd>,
) : TtSpellDuration {
    override fun serialize() = mapOf(
        "ends" to ends,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtPermanentSpellDuration(
            serialized["ends"] as List<TtPermanentSpellEnd>,
        )
    }
}

object TtSpecialSpellDuration : TtSpellDuration {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpecialSpellDuration
}
