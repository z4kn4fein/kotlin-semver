package io.github.z4kn4fein.semver

import kotlin.math.min

private const val ANY_BUT_NUMBER_REGEX: String = "[^0-9]"
private const val ANY_BUT_ALPHANUMERIC_AND_HYPHEN_REGEX: String = "[^0-9A-Za-z-]"

internal class PreRelease(val preReleaseText: String) : Comparable<PreRelease> {

    private val parts: List<String>

    init {
        if (preReleaseText.isEmpty()) {
            throw VersionFormatException("The pre-release cannot be empty.")
        }

        parts = preReleaseText.trim().split('.')
        validate()
    }

    fun increment(): PreRelease {
        val newParts = parts.toMutableList()

        var lastNumericIndex = 0
        var lastNumericItem = 0
        for (i in newParts.indices) {
            val numericPart = newParts[i].toIntOrNull()
            if (numericPart != null) {
                lastNumericIndex = i
                lastNumericItem = numericPart
            }
        }

        if (lastNumericIndex != 0) {
            newParts[lastNumericIndex] = (lastNumericItem + 1).toString()
        } else {
            newParts.add("0")
        }

        return PreRelease(newParts.joinToString("."))
    }

    override fun compareTo(other: PreRelease): Int {
        val thisSize = parts.size
        val otherSize = other.parts.size

        val count = min(thisSize, otherSize)

        for (i in 0 until count) {
            val partResult = compareParts(parts[i], other.parts[i])
            if (partResult != 0) return partResult
        }

        return thisSize.compareTo(otherSize)
    }

    override fun equals(other: Any?): Boolean {
        val version = other as? PreRelease
        return when {
            version == null -> false
            version.preReleaseText == preReleaseText -> true
            else -> false
        }
    }

    override fun hashCode(): Int = preReleaseText.hashCode()

    override fun toString(): String = preReleaseText

    private fun validate() {
        for (part in parts) {
            if (part.trim().isEmpty()) {
                throw VersionFormatException("Empty pre-release part found.")
            }

            if (!part.matches(anyButNumberRegex)) {
                if (part.length > 1 && part[0] == '0') {
                    throw VersionFormatException("The pre-release part '$part' is numeric but contains a leading zero.")
                } else {
                    continue
                }
            }

            if (part.matches(anyButAlphaNumericAndHyphenRegex)) {
                throw VersionFormatException("The pre-release part '$part' contains an invalid character.")
            }
        }
    }

    private fun compareParts(part1: String, part2: String): Int {
        val firstPart = part1.toIntOrNull()
        val secondPart = part2.toIntOrNull()

        return when {
            firstPart != null && secondPart == null -> -1
            firstPart == null && secondPart != null -> 1
            firstPart != null && secondPart != null -> firstPart.compareTo(secondPart)
            else -> part1.compareTo(part2)
        }
    }

    companion object {
        private val anyButNumberRegex: Regex = ANY_BUT_NUMBER_REGEX.toRegex()
        private val anyButAlphaNumericAndHyphenRegex: Regex = ANY_BUT_ALPHANUMERIC_AND_HYPHEN_REGEX.toRegex()

        fun default(): PreRelease {
            return PreRelease("0")
        }
    }
}

internal fun String.toPreRelease(): PreRelease = PreRelease(this)
