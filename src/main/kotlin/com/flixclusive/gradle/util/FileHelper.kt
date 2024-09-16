package com.flixclusive.gradle.util

import java.nio.charset.StandardCharsets

/**
 * Mutate the given filename to make it valid for a FAT filesystem,
 * replacing any invalid characters with "_".
 *
 */
internal fun buildValidFilename(name: String): String {
    if (name.isEmpty() || name == "." || name == "..") {
        return "(invalid)"
    }

    val res = StringBuilder(name.length)
    for (c in name) {
        if (isValidFilenameChar(c)) {
            res.append(c)
        } else {
            res.append('_')
        }
    }

    trimFilename(res)
    return res.toString()
}

internal fun isValidFilename(filename: String): Boolean {
    for (c in filename) {
        if (isValidFilenameChar(c))
            continue

        return false
    }

    return true
}

internal fun isValidFilenameChar(c: Char): Boolean {
    val charByte = c.code.toByte()
    // Control characters (0x00 to 0x1F) are not allowed
    if (charByte in 0x00..0x1F) {
        return false
    }
    // Specific characters are also not allowed
    if (c.code == 0x7F) {
        return false
    }

    return when (c) {
        '"', '*', '/', ':', '<', '>', '?', '\\', '|' -> false
        else -> true
    }
}

private fun trimFilename(res: StringBuilder, maxBytes: Int = 254) {
    var rawBytes = res.toString().toByteArray(StandardCharsets.UTF_8)
    if (rawBytes.size > maxBytes) {
        // Reduce max bytes to account for "..."
        val adjustedMaxBytes = maxBytes - 3
        while (rawBytes.size > adjustedMaxBytes) {
            // Remove character from the middle
            res.deleteCharAt(res.length / 2)
            rawBytes = res.toString().toByteArray(StandardCharsets.UTF_8)
        }
        // Insert "..." in the middle
        res.insert(res.length / 2, "...")
    }
}
