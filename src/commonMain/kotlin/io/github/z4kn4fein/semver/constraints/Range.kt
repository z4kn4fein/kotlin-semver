package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

internal class Range(
    private val start: VersionComparator,
    private val end: VersionComparator,
    private val operator: Op,
) : VersionComparator {
    override fun isSatisfiedBy(version: Version): Boolean =
        when (operator) {
            Op.EQUAL -> start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.NOT_EQUAL -> !start.isSatisfiedBy(version) || !end.isSatisfiedBy(version)
            Op.LESS_THAN -> !start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
            Op.LESS_THAN_OR_EQUAL -> end.isSatisfiedBy(version)
            Op.GREATER_THAN -> start.isSatisfiedBy(version) && !end.isSatisfiedBy(version)
            Op.GREATER_THAN_OR_EQUAL -> start.isSatisfiedBy(version)
        }

    override fun opposite(): String =
        when (operator) {
            Op.EQUAL -> toStringByOperator(Op.NOT_EQUAL)
            Op.NOT_EQUAL -> toStringByOperator(Op.EQUAL)
            Op.LESS_THAN -> toStringByOperator(Op.GREATER_THAN_OR_EQUAL)
            Op.LESS_THAN_OR_EQUAL -> toStringByOperator(Op.GREATER_THAN)
            Op.GREATER_THAN -> toStringByOperator(Op.LESS_THAN_OR_EQUAL)
            Op.GREATER_THAN_OR_EQUAL -> toStringByOperator(Op.LESS_THAN)
        }

    override fun toString(): String = toStringByOperator(operator)

    private fun toStringByOperator(operator: Op): String =
        when (operator) {
            Op.EQUAL -> "$start $end"
            Op.NOT_EQUAL -> "${start.opposite()} || ${end.opposite()}"
            Op.LESS_THAN -> start.opposite()
            Op.LESS_THAN_OR_EQUAL -> "$end"
            Op.GREATER_THAN -> end.opposite()
            Op.GREATER_THAN_OR_EQUAL -> "$start"
        }
}
