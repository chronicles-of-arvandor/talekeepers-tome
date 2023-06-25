package net.arvandor.talekeeper.choice.option

import java.util.*

@JvmInline
value class TtChoiceOptionId(val value: String) {
    companion object {
        fun generate() = TtChoiceOptionId(UUID.randomUUID().toString())
    }
}
