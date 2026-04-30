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

    fun toCondition(operator: Operator = EqualityOp.EQUAL): Condition {
        return when {
            isMajorWildcard ->
                when (operator) {
                    LowerBoundOp.GREATER_THAN, UpperBoundOp.LESS_THAN, EqualityOp.NOT_EQUAL ->
                        UpperBoundCondition(UpperBoundOp.LESS_THAN, Version.min.copy(preRelease = ""))
                    else -> Condition.greaterThanMin
                }
            isMinorWildcard -> {
                val version = Version(major = major, preRelease = preRelease, buildMetadata = buildMetadata)
                RangeCondition(
                    start = LowerBoundCondition(LowerBoundOp.GREATER_THAN_OR_EQUAL, version),
                    end = UpperBoundCondition(UpperBoundOp.LESS_THAN, version.nextMajor(preRelease = "")),
                )
            }
            isPatchWildcard -> {
                val version =
                    Version(major = major, minor = minor, preRelease = preRelease, buildMetadata = buildMetadata)
                RangeCondition(
                    start = LowerBoundCondition(LowerBoundOp.GREATER_THAN_OR_EQUAL, version),
                    end = UpperBoundCondition(UpperBoundOp.LESS_THAN, version.nextMinor(preRelease = "")),
                )
            }
            else ->
                when (operator) {
                    is EqualityOp ->
                        EqualityCondition(
                            operator,
                            Version(major = major, minor = minor, patch = patch, preRelease, buildMetadata),
                        )
                    is LowerBoundOp ->
                        LowerBoundCondition(
                            operator,
                            Version(major = major, minor = minor, patch = patch, preRelease, buildMetadata),
                        )
                    is UpperBoundOp ->
                        UpperBoundCondition(
                            operator,
                            Version(major = major, minor = minor, patch = patch, preRelease, buildMetadata),
                        )
                    else -> throw ConstraintFormatException("Invalid operator: $operator")
                }
        }
    }

    companion object {
        private val wildcards = arrayOf("*", "x", "X")
    }
}
