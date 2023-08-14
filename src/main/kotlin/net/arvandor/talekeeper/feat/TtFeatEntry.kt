package net.arvandor.talekeeper.feat

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtFeatEntry : ConfigurationSerializable

@SerializableAs("FeatStringEntry")
data class TtFeatStringEntry(
    val value: String,
) : TtFeatEntry {
    override fun serialize() = mapOf(
        "value" to value,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatStringEntry(
            serialized["value"] as String,
        )
    }
}

@SerializableAs("FeatListEntry")
data class TtFeatListEntry(
    val items: List<String>,
) : TtFeatEntry {
    override fun serialize() = mapOf(
        "items" to items,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatListEntry(
            serialized["items"] as List<String>,
        )
    }
}

@SerializableAs("FeatTableEntry")
data class TtFeatTableEntry(
    val caption: String,
    val colLabels: List<String>,
    val colStyles: List<String>,
    val rows: List<List<String>>,
) : TtFeatEntry {
    override fun serialize() = mapOf(
        "caption" to caption,
        "col-labels" to colLabels,
        "col-styles" to colStyles,
        "rows" to rows,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatTableEntry(
            serialized["caption"] as String,
            serialized["col-labels"] as List<String>,
            serialized["col-styles"] as List<String>,
            serialized["rows"] as List<List<String>>,
        )
    }
}

@SerializableAs("FeatEntriesEntry")
data class TtFeatEntriesEntry(
    val name: String,
    val entries: List<String>,
) : TtFeatEntry {
    override fun serialize() = mapOf(
        "name" to name,
        "entries" to entries,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtFeatEntriesEntry(
            serialized["name"] as String,
            serialized["entries"] as List<String>,
        )
    }
}

@SerializableAs("FeatInsetEntry")
data class TtFeatInsetEntry(
    val name: String,
    val entries: List<String>,
) : TtFeatEntry {
    override fun serialize() = mapOf(
        "name" to name,
        "entries" to entries,
    )
}
