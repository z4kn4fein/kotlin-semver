package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor

internal data class VersionDescriptor(
    val majorString: String,
    val minorString: String?,
    val patchString: String?,
    val preRelease: String? = null,
    val buildMetadata: String? = null,
) {
    override fun toString(): String {
        return majorString +
            (minorString?.let { ".$minorString" } ?: "") +
            (patchString?.let { ".$patchString" } ?: "") +
            (preRelease?.let { "-$preRelease" } ?: "") +
            (buildMetadata?.let { "+$buildMetadata" } ?: "")
    }

    val isMajorWildcard: Boolean = wildcards.contains(majorString)
    val isMinorWildcard: Boolean = minorString?.let { wildcards.contains(it) } ?: true
    val isPatchWildcard: Boolean = patchString?.let { wildcards.contains(it) } ?: true

    val isWildcard: Boolean = isMajorWildcard || isMinorWildcard || isPatchWildcard

    val major: Int get() =
        majorString.toIntOrNull()
            ?: throw ConstraintFormatException("Invalid MAJOR number in: $this")

    val minor: Int get() =
        minorString?.toIntOrNull()
            ?: throw ConstraintFormatException("Invalid MINOR number in: $this")

    val patch: Int get() =
        patchString?.toIntOrNull()
            ?: throw ConstraintFormatException("Invalid PATCH number in: $this")

    fun toCondition(operator: Op = Op.EQUAL): Condition {
        return when {
            isMajorWildcard ->
                when (operator) {
                    Op.GREATER_THAN, Op.LESS_THAN, Op.NOT_EQUAL ->
                        OperatorCondition(Op.LESS_THAN, Version.min.copy(preRelease = ""))
                    else -> Condition.greaterThanMin
                }
            isMinorWildcard -> {
                val version = Version(major = major, preRelease = preRelease, buildMetadata = buildMetadata)
                RangeCondition(
                    start = OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = OperatorCondition(Op.LESS_THAN, version.nextMajor(preRelease = "")),
                )
            }
            isPatchWildcard -> {
                val version =
                    Version(major = major, minor = minor, preRelease = preRelease, buildMetadata = buildMetadata)
                RangeCondition(
                    start = OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = OperatorCondition(Op.LESS_THAN, version.nextMinor(preRelease = "")),
                )
            }
            else ->
                OperatorCondition(
                    operator,
                    Version(major = major, minor = minor, patch = patch, preRelease, buildMetadata),
                )
        }
    }

    companion object {
        private val wildcards = arrayOf("*", "x", "X")
    }
}
