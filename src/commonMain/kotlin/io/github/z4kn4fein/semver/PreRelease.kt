package io.github.z4kn4fein.semver

import kotlin.math.min

private const val ONLY_NUMBER_REGEX: String = "^[0-9]+$"
private const val ONLY_ALPHANUMERIC_AND_HYPHEN_REGEX: String = "^[0-9A-Za-z-]+$"

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

        val lastNumericItem = newParts.lastOrNull { it.toIntOrNull() != null }
        if (lastNumericItem != null) {
            val lastNumericIndex = newParts.indexOf(lastNumericItem)
            newParts[lastNumericIndex] = (lastNumericItem.toInt() + 1).toString()
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

    override fun toString(): String = preReleaseText

    private fun validate() {
        for (part in parts) {
            val error = when {
                part.trim().isEmpty() -> "Empty pre-release part found."
                part.matches(onlyNumberRegex) && part.length > 1 && part[0] == '0' ->
                    "The pre-release part '$part' is numeric but contains a leading zero."
                !part.matches(onlyAlphaNumericAndHyphenRegex) ->
                    "The pre-release part '$part' contains an invalid character."
                else -> null
            }

            if (error != null) {
                throw VersionFormatException(error)
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
        private val onlyNumberRegex: Regex = ONLY_NUMBER_REGEX.toRegex()
        private val onlyAlphaNumericAndHyphenRegex: Regex = ONLY_ALPHANUMERIC_AND_HYPHEN_REGEX.toRegex()

        fun default(): PreRelease {
            return PreRelease("0")
        }
    }
}

internal fun String.toPreRelease(): PreRelease = PreRelease(this)
