package io.github.z4kn4fein.semver.constraints

/**
 * Interface that provides a framework for formatting constraints represented by
 * a list of conditions.
 */
public interface ConditionFormatter {
    /**
     * The string used to join conditions into OR segments when formatting constraints.
     */
    public val orSeparator: String

    /**
     * Formats a condition into a single string representation.
     *
     * @param condition The condition to be formatted.
     * @return The formatted string representation of the given condition.
     */
    public fun formatCondition(condition: Condition): String
}

/**
 * The default formatter used to format [Constraint] instances.
 */
public class DefaultFormatter : ConditionFormatter {
    override val orSeparator: String = " || "

    override fun formatCondition(condition: Condition): String =
        when (condition) {
            is OperatorCondition -> formatOperatorCondition(condition)
            is RangeCondition -> "${formatOperatorCondition(condition.start)} ${formatOperatorCondition(condition.end)}"
            else -> ""
        }

    private fun formatOperatorCondition(operatorCondition: OperatorCondition): String =
        "${operatorCondition.operator}${operatorCondition.version}"
}

/**
 * A formatter for Maven-style version constraints. This class transforms conditions
 * into a string representation that adheres to the Maven format for version ranges.
 */
public class MavenStyleFormatter : ConditionFormatter {
    override val orSeparator: String = ","

    override fun formatCondition(condition: Condition): String =
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
