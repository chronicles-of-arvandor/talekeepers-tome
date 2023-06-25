package net.arvandor.talekeeper.spell.range

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtSpellRange : ConfigurationSerializable

@SerializableAs("PointSpellRange")
data class TtPointSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtPointSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("RadiusSpellRange")
data class TtRadiusSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtRadiusSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("SphereSpellRange")
data class TtSphereSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSphereSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("ConeSpellRange")
data class TtConeSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtConeSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("SpecialSpellRange")
object TtSpecialSpellRange : TtSpellRange {
    override fun serialize() = mapOf<String, Any>()

    @JvmStatic
    fun deserialize(serialized: Map<String, Any>) = TtSpecialSpellRange
}

@SerializableAs("LineSpellRange")
data class TtLineSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtLineSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("HemisphereSpellRange")
data class TtHemisphereSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtHemisphereSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}

@SerializableAs("CubeSpellRange")
data class TtCubeSpellRange(
    val distance: TtSpellRangeDistance,
) : TtSpellRange {
    override fun serialize() = mapOf(
        "distance" to distance,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtCubeSpellRange(
            serialized["distance"] as TtSpellRangeDistance,
        )
    }
}
