package net.arvandor.talekeeper.distance

import kotlin.math.floor
import kotlin.math.roundToInt

enum class TtDistanceUnit(val format: (Double) -> String) {
    FEET({ amount ->
        val feet = floor(amount).roundToInt()
        val inches = ((amount % 1.0) * 12).roundToInt()
        if (inches > 0) {
            "$feet ft $inches in"
        } else {
            "$feet ft"
        }
    }),
}
