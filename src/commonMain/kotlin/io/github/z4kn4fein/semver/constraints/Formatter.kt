package io.github.z4kn4fein.semver.constraints

/**
 * Abstract class that provides a framework for formatting constraints represented by
 * a list of conditions.
 */
public abstract class ConstraintFormatter {
    /**
     * The string used to join OR segments when formatting constraints.
     */
    protected abstract val orSeparator: String

    /**
     * Formats a list of conditions into a single string representation.
     *
     * @param conditions The list of conditions to be formatted.
     * @return The formatted string representation of the given conditions.
     */
    protected abstract fun formatConditions(conditions: List<Condition>): String

    /**
     * Formats a list of condition segments into a single string representation, where each segment is joined
     * using the defined OR separator.
     *
     * @param segments A list of condition segments, where each segment is a list of conditions
     *                 that are grouped together.
     * @return A string representation of the formatted condition segments, where each segment
     *         is formatted and joined with the OR separator.
     */
    public fun format(segments: List<List<Condition>>): String = segments.joinToString(orSeparator) { formatConditions(it) }
}

/**
 * The default formatter used to format [Constraint] instances.
 */
public class DefaultConstraintFormatter : ConstraintFormatter() {
    override val orSeparator: String = " || "

    override fun formatConditions(conditions: List<Condition>): String =
        conditions.joinToString(" ") { condition ->
            when (condition) {
                is OperatorCondition -> formatOperatorCondition(condition)
                is RangeCondition -> "${formatOperatorCondition(condition.start)} ${formatOperatorCondition(condition.end)}"
                else -> ""
            }
        }

    private fun formatOperatorCondition(operatorCondition: OperatorCondition): String =
        "${operatorCondition.operator}${operatorCondition.version}"
}

/**
 * A formatter for Maven-style version constraints. This class transforms conditions
 * into a string representation that adheres to the Maven format for version ranges.
 */
public class MavenConstraintFormatter : ConstraintFormatter() {
    override val orSeparator: String = ","

    override fun formatConditions(conditions: List<Condition>): String {
        if (conditions.size == 1) {
            return formatResult(conditions[0])
        }

        if (conditions.size > 1) {
            val transformed = conditions.map { it.equalityToRange() }
            val maxLowerBound = transformed.maxLowerBound()
            val minUpperBound = transformed.minUpperBound()

            if (maxLowerBound.version > minUpperBound.version) {
                throw ConstraintFormatException("Constraint produces an invalid range: ${maxLowerBound.version}..${minUpperBound.version}")
            }

            return formatResult(RangeCondition(maxLowerBound, minUpperBound).rangeToEquality())
        }

        throw ConstraintFormatException("Cannot write constraint in Maven format.")
    }

    private fun formatResult(condition: Condition): String =
        when (condition) {
            is OperatorCondition -> formatOperatorCondition(condition)
            is RangeCondition -> formatRangeCondition(condition)
            else -> ""
        }

    private fun formatRangeCondition(rangeCondition: RangeCondition): String =
        "${lowerBoundFromOp(
            rangeCondition.start.operator,
        )}${rangeCondition.start.version},${rangeCondition.end.version}${upperBoundFromOp(rangeCondition.end.operator)}"

    private fun formatOperatorCondition(operatorCondition: OperatorCondition): String =
        when (operatorCondition.operator) {
            Op.EQUAL -> "[${operatorCondition.version}]"
            Op.NOT_EQUAL -> "(,${operatorCondition.version}),(${operatorCondition.version},)"
            Op.LESS_THAN -> "(,${operatorCondition.version})"
            Op.LESS_THAN_OR_EQUAL -> "(,${operatorCondition.version}]"
            Op.GREATER_THAN -> "(${operatorCondition.version},)"
            Op.GREATER_THAN_OR_EQUAL -> "[${operatorCondition.version},)"
        }

    private fun lowerBoundFromOp(op: Op): String =
        when (op) {
            Op.GREATER_THAN -> "("
            Op.GREATER_THAN_OR_EQUAL -> "["
            else -> ""
        }

    private fun upperBoundFromOp(op: Op): String =
        when (op) {
            Op.LESS_THAN -> ")"
            Op.LESS_THAN_OR_EQUAL -> "]"
            else -> ""
        }
}
