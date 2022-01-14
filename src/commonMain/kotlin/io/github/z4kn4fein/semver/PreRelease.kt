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
        } ?: newParts.add("0")

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
        operator fun invoke(preReleaseString: String): PreRelease = PreRelease(validate(preReleaseString))

        fun default(preRelease: String? = null): PreRelease =
            preRelease?.let { if (it.isBlank()) PreRelease(listOf("0")) else PreRelease(listOf(it, "0")) }
                ?: PreRelease(listOf("0"))

        private fun validate(preReleaseString: String): List<String> {
            if (preReleaseString.isBlank()) {
                return listOf("0")
            }

            val parts = preReleaseString.trim().split('.')
            for (part in parts) {
                val error = when {
                    part.isBlank() -> "Pre-release identity contains an empty part."
                    part.matches(Patterns.onlyNumberRegex) && part.length > 1 && part[0] == '0' ->
                        "Pre-release part '$part' is numeric but contains a leading zero."
                    !part.matches(Patterns.onlyAlphaNumericAndHyphenRegex) ->
                        "Pre-release part '$part' contains an invalid character."
                    else -> null
                }

                error?.let { throw VersionFormatException("$error ($preReleaseString)") } ?: continue
            }

            return parts
        }
    }
}

internal fun String.toPreRelease(): PreRelease = PreRelease(this)
