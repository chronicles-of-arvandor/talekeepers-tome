package net.arvandor.talekeeper.spell.entry

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

sealed interface TtSpellEntry : ConfigurationSerializable

@SerializableAs("StringSpellEntry")
@JvmInline
value class TtStringSpellEntry(val value: String) : TtSpellEntry {
    override fun serialize() = mapOf("value" to value)

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtStringSpellEntry(
            serialized["value"] as String,
        )
    }
}

@SerializableAs("EntriesSpellEntry")
data class TtEntriesSpellEntry(
    val name: String,
    val entries: List<String>,
) : TtSpellEntry {
    override fun serialize() = mapOf(
        "name" to name,
        "entries" to entries,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtEntriesSpellEntry(
            serialized["name"] as String,
            serialized["entries"] as List<String>,
        )
    }
}

@SerializableAs("TableSpellEntry")
data class TtTableSpellEntry(
    val caption: String,
    val colLabels: List<String>,
    val colStyles: List<String>,
    val rows: List<List<String>>,
) : TtSpellEntry {
    override fun serialize() = mapOf(
        "caption" to caption,
        "col-labels" to colLabels,
        "col-styles" to colStyles,
        "rows" to rows,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtTableSpellEntry(
            serialized["caption"] as String,
            serialized["col-labels"] as List<String>,
            serialized["col-styles"] as List<String>,
            serialized["rows"] as List<List<String>>,
        )
    }
}

@SerializableAs("ListSpellEntry")
data class TtListSpellEntry(
    val items: List<String>,
) : TtSpellEntry {
    override fun serialize() = mapOf(
        "items" to items,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtListSpellEntry(
            serialized["items"] as List<String>,
        )
    }
}
