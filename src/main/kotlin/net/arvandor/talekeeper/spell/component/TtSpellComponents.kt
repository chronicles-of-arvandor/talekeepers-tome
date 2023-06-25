package net.arvandor.talekeeper.spell.component

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtSpellComponents : ConfigurationSerializable {
    val verbal: Boolean
    val somatic: Boolean
}

@SerializableAs("SpellComponentsWithNoMaterial")
data class TtSpellComponentsWithNoMaterial(
    override val verbal: Boolean,
    override val somatic: Boolean,
) : TtSpellComponents {
    override fun serialize() = mapOf(
        "verbal" to verbal,
        "somatic" to somatic,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellComponentsWithNoMaterial(
            serialized["verbal"] as Boolean,
            serialized["somatic"] as Boolean,
        )
    }
}

@SerializableAs("SpellComponentsWithStringMaterial")
data class TtSpellComponentsWithStringMaterial(
    override val verbal: Boolean,
    override val somatic: Boolean,
    val material: String,
) : TtSpellComponents {
    override fun serialize() = mapOf(
        "verbal" to verbal,
        "somatic" to somatic,
        "material" to material,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellComponentsWithStringMaterial(
            serialized["verbal"] as Boolean,
            serialized["somatic"] as Boolean,
            serialized["material"] as String,
        )
    }
}

@SerializableAs("SpellComponentsWithObjectMaterial")
data class TtSpellComponentsWithObjectMaterial(
    override val verbal: Boolean,
    override val somatic: Boolean,
    val material: TtMaterialSpellComponent,
) : TtSpellComponents {
    override fun serialize() = mapOf(
        "verbal" to verbal,
        "somatic" to somatic,
        "material" to material,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtSpellComponentsWithObjectMaterial(
            serialized["verbal"] as Boolean,
            serialized["somatic"] as Boolean,
            serialized["material"] as TtMaterialSpellComponent,
        )
    }
}
