package net.arvandor.talekeeper.util

fun String.toLinesWithMaxLength(maxLength: Int) = split(" ").fold(mutableListOf<String>()) { lines, word ->
    if (lines.isEmpty()) {
        lines.add(word)
    } else {
        val lastLine = lines.last()
        if (lastLine.length + word.length + 1 > maxLength) {
            lines.add(word)
        } else {
            lines[lines.lastIndex] = "$lastLine $word"
        }
    }
    return@fold lines
}

fun CharSequence.levenshtein(other: CharSequence): Int {
    val otherLength = other.length

    var cost = Array(length) { it }
    var newCost = Array(length) { 0 }

    for (i in 1 until otherLength) {
        newCost[0] = i

        for (j in 1 until length) {
            val match = if (this[j - 1] == other[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = costInsert.coerceAtMost(costDelete).coerceAtMost(costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[length - 1]
}
