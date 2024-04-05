package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Patterns

internal interface ConditionProcessor {
    val regex: Regex

    fun processCondition(match: MatchResult): VersionComparator
}

internal class OperatorConditionProcessor : ConditionProcessor {
    override val regex: Regex = Patterns.OPERATOR_CONDITION_REGEX.toRegex()
    private val comparatorBuilders =
        arrayOf(
            RegularComparatorBuilder(),
            TildeComparatorBuilder(),
            CaretComparatorBuilder(),
        )

    @Suppress("MagicNumber")
    override fun processCondition(match: MatchResult): VersionComparator {
        val operator = match.groups[1]?.value ?: ""
        val major = match.groups[2]?.value ?: ""
        val minor = match.groups[3]?.value
        val patch = match.groups[4]?.value
        val preRelease = match.groups[5]?.value
        val buildMetadata = match.groups[6]?.value
        val descriptor = VersionDescriptor(major, minor, patch, preRelease, buildMetadata)
        comparatorBuilders.forEach { builder ->
            if (operator in builder.acceptedOperators) {
                return builder.buildComparator(operator, descriptor)
            }
        }
        throw ConstraintFormatException(
            "Invalid constraint operator: " +
                "$operator in $descriptor",
        )
    }
}

internal class HyphenConditionProcessor : ConditionProcessor {
    override val regex: Regex = Patterns.HYPHEN_CONDITION_REGEX.toRegex()

    @Suppress("MagicNumber")
    override fun processCondition(match: MatchResult): VersionComparator {
        val start =
            VersionDescriptor(
                majorString = match.groups[1]?.value ?: "",
                minorString = match.groups[2]?.value,
                patchString = match.groups[3]?.value,
                preRelease = match.groups[4]?.value,
                buildMetadata = match.groups[5]?.value,
            )
        val end =
            VersionDescriptor(
                majorString = match.groups[6]?.value ?: "",
                minorString = match.groups[7]?.value,
                patchString = match.groups[8]?.value,
                preRelease = match.groups[9]?.value,
                buildMetadata = match.groups[10]?.value,
            )
        return Range(
            start.toComparator(Op.GREATER_THAN_OR_EQUAL),
            end.toComparator(Op.LESS_THAN_OR_EQUAL),
            Op.EQUAL,
        )
    }
}
