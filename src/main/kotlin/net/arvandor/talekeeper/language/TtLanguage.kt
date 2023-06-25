package net.arvandor.talekeeper.language

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("Language")
data class TtLanguage(
    val id: TtLanguageId,
    val name: String,
    val cypher: Map<String, String>,
) : ConfigurationSerializable {

    fun applyCypher(message: String): String {
        var pos = 0
        var result = ""
        msg@while (pos < message.length) {
            for (key in cypher.keys) {
                if (pos + key.length <= message.length && message.substring(pos, pos + key.length) == key) {
                    result += cypher[key]
                    pos += key.length
                    continue@msg
                }
            }
            result += message[pos++]
        }
        return result
    }

    override fun serialize() = mapOf(
        "id" to id.value,
        "name" to name,
        "cypher" to cypher,
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = TtLanguage(
            TtLanguageId(serialized["id"] as String),
            serialized["name"] as String,
            (serialized["cypher"] as Map<String, String>),
        )
    }
}
