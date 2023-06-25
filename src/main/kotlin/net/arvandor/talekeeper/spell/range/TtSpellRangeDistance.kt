package net.arvandor.talekeeper.spell.range

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtSpellRangeDistance : ConfigurationSerializable

@SerializableAs("SpellRangeDistanceFeet")
data class TtSpellRangeDistanceFeet(
    val amount: Int,
) : TtSpellRangeDistance {
    override fun serialize() = mapOf(
        "amount" to amount,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceFeet(
            serialized["amount"] as Int,
        )
    }
}

@SerializableAs("SpellRangeDistanceMile")
data class TtSpellRangeDistanceMile(
    val amount: Int,
) : TtSpellRangeDistance {
    override fun serialize() = mapOf(
        "amount" to amount,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceMile(
            serialized["amount"] as Int,
        )
    }
}

@SerializableAs("SpellRangeDistanceSelf")
object TtSpellRangeDistanceSelf : TtSpellRangeDistance {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceSelf
}

@SerializableAs("SpellRangeDistanceTouch")
object TtSpellRangeDistanceTouch : TtSpellRangeDistance {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceTouch
}

@SerializableAs("SpellRangeDistanceSight")
object TtSpellRangeDistanceSight : TtSpellRangeDistance {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceSight
}

@SerializableAs("SpellRangeDistanceUnlimited")
object TtSpellRangeDistanceUnlimited : TtSpellRangeDistance {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpellRangeDistanceUnlimited
}
