package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

internal class Range(
    private val start: VersionComparator,
    private val end: VersionComparator,
    private val operator: Op
) : VersionComparator {

    override fun isSatisfiedBy(version: Version): Boolean {
        return when (operator) {
            Op.EQUAL -> start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.NOT_EQUAL -> !start.isSatisfiedBy(version) || !end.isSatisfiedBy(version)
            Op.LOWER_THAN -> !start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.LOWER_THAN_OR_EQUAL -> end.isSatisfiedBy(version)
            Op.GREATER_THAN -> start.isSatisfiedBy(version) && !end.isSatisfiedBy(version)
            Op.GREATER_THAN_OR_EQUAL -> start.isSatisfiedBy(version)
        }
    }

    override fun opposite(): String {
        return when (operator) {
            Op.EQUAL -> toStringByOperator(Op.NOT_EQUAL)
            Op.NOT_EQUAL -> toStringByOperator(Op.EQUAL)
            Op.LOWER_THAN -> toStringByOperator(Op.GREATER_THAN_OR_EQUAL)
            Op.LOWER_THAN_OR_EQUAL -> toStringByOperator(Op.GREATER_THAN)
            Op.GREATER_THAN -> toStringByOperator(Op.LOWER_THAN_OR_EQUAL)
            Op.GREATER_THAN_OR_EQUAL -> toStringByOperator(Op.LOWER_THAN)
        }
    }

    override fun toString(): String {
        return toStringByOperator(operator)
    }

    private fun toStringByOperator(operator: Op): String {
        return when (operator) {
            Op.EQUAL -> "$start $end"
            Op.NOT_EQUAL -> "(${start.opposite()} || ${end.opposite()})"
            Op.LOWER_THAN -> start.opposite()
            Op.LOWER_THAN_OR_EQUAL -> "$end"
            Op.GREATER_THAN -> end.opposite()
            Op.GREATER_THAN_OR_EQUAL -> "$start"
        }
    }
}
