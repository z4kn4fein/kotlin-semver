package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.isWildcard
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor

internal data class VersionDescriptor(
    val majorString: String,
    val minorString: String?,
    val patchString: String?,
    val preRelease: String? = null,
    val buildMetadata: String? = null
) {
    override fun toString(): String {
        return majorString +
            (minorString?.let { ".$minorString" } ?: "") +
            (patchString?.let { ".$patchString" } ?: "") +
            (preRelease?.let { "-$preRelease" } ?: "") +
            (buildMetadata?.let { "+$buildMetadata" } ?: "")
    }

    val isMajorWildcard: Boolean = majorString.isWildcard()
    val isMinorWildcard: Boolean = minorString?.isWildcard() ?: true
    val isPatchWildcard: Boolean = patchString?.isWildcard() ?: true

    val isWildcard: Boolean = isMajorWildcard || isMinorWildcard || isPatchWildcard

    val major: Int get() = majorString.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid MAJOR number in: $this")

    val minor: Int get() = minorString?.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid MINOR number in: $this")

    val patch: Int get() = patchString?.toIntOrNull()
        ?: throw ConstraintFormatException("Invalid PATCH number in: $this")

    fun toComparator(operator: Op = Op.EQUAL): VersionComparator {
        return when {
            isMajorWildcard ->
                when (operator) {
                    Op.GREATER_THAN, Op.LESS_THAN, Op.NOT_EQUAL ->
                        Condition(Op.LESS_THAN, Version.min.copy(preRelease = ""))
                    else -> Condition(Op.GREATER_THAN_OR_EQUAL, Version.min)
                }
            isMinorWildcard -> {
                val version = Version(major = major, preRelease = preRelease, buildMetadata = buildMetadata)
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.nextMajor(preRelease = "")),
                    operator
                )
            }
            isPatchWildcard -> {
                val version =
                    Version(major = major, minor = minor, preRelease = preRelease, buildMetadata = buildMetadata)
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.nextMinor(preRelease = "")),
                    operator
                )
            }
            else ->
                Condition(
                    operator,
                    Version(major = major, minor = minor, patch = patch, preRelease, buildMetadata)
                )
        }
    }
}
