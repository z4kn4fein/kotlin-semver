package io.github.z4kn4fein.semver

import kotlin.math.min

internal class PreRelease private constructor(private val parts: List<String>) : Comparable<PreRelease> {

    val identity: String get() = parts[0]

    fun increment(): PreRelease {
        val newParts = parts.toMutableList()

        val lastNumericItem = newParts.lastOrNull { it.toIntOrNull() != null }
        lastNumericItem?.let {
            val lastNumericIndex = newParts.indexOf(lastNumericItem)
            newParts[lastNumericIndex] = (lastNumericItem.toInt() + 1).toString()
        } ?: newParts.add(DEFAULT_INIT_PART)

        return PreRelease(newParts)
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
        val preRelease = other as? PreRelease
        return when {
            preRelease == null -> false
            compareTo(preRelease) == 0 -> true
            else -> false
        }
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    override fun toString(): String = parts.joinToString(".")

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
        private const val ONLY_NUMBER_REGEX: String = "^[0-9]+$"
        private const val ONLY_ALPHANUMERIC_AND_HYPHEN_REGEX: String = "^[0-9A-Za-z-]+$"
        private const val DEFAULT_INIT_PART = "0"
        private val onlyNumberRegex: Regex = ONLY_NUMBER_REGEX.toRegex()
        private val onlyAlphaNumericAndHyphenRegex: Regex = ONLY_ALPHANUMERIC_AND_HYPHEN_REGEX.toRegex()

        operator fun invoke(preReleaseText: String): PreRelease = PreRelease(validate(preReleaseText))

        fun default(preRelease: String? = null): PreRelease =
            preRelease?.let {
                if (it.isEmpty()) PreRelease(listOf(DEFAULT_INIT_PART)) else PreRelease(listOf(it, DEFAULT_INIT_PART))
            } ?: PreRelease(listOf(DEFAULT_INIT_PART))

        private fun validate(preReleaseText: String): List<String> {
            if (preReleaseText.isEmpty()) {
                throw VersionFormatException("The pre-release string cannot be empty.")
            }

            val parts = preReleaseText.trim().split('.')
            for (part in parts) {
                val error = when {
                    part.trim().isEmpty() -> "Empty pre-release part found."
                    part.matches(onlyNumberRegex) && part.length > 1 && part[0] == '0' ->
                        "The pre-release part '$part' is numeric but contains a leading zero."
                    !part.matches(onlyAlphaNumericAndHyphenRegex) ->
                        "The pre-release part '$part' contains an invalid character."
                    else -> null
                }

                error?.let { throw VersionFormatException(error) } ?: continue
            }

            return parts
        }
    }
}
