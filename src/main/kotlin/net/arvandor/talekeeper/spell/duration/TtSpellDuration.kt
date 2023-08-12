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
    val type: TtTimedSpellDurationType,
    val amount: Int,
    val concentration: Boolean,
) : TtSpellDuration {
    enum class TtTimedSpellDurationType {
        MINUTE,
        HOUR,
        DAY,
        ROUND,
    }

    override fun serialize() = mapOf(
        "type" to type.name,
        "amount" to amount,
        "concentration" to concentration,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtTimedSpellDuration(
            TtTimedSpellDurationType.valueOf(serialized["type"] as String),
            serialized["amount"] as Int,
            serialized["concentration"] as? Boolean ?: false,
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
            (serialized["ends"] as List<String>).map(TtPermanentSpellEnd::valueOf),
        )
    }
}

object TtSpecialSpellDuration : TtSpellDuration {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpecialSpellDuration
}
