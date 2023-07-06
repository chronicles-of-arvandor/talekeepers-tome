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
