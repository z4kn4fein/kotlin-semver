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
     * Formats a condition into its string representation.
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
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.format
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
            EqualityOp.EQUAL -> "[${operatorCondition.version}]"
            EqualityOp.NOT_EQUAL -> "(,${operatorCondition.version}),(${operatorCondition.version},)"
            UpperBoundOp.LESS_THAN -> "(,${operatorCondition.version})"
            UpperBoundOp.LESS_THAN_OR_EQUAL -> "(,${operatorCondition.version}]"
            LowerBoundOp.GREATER_THAN -> "(${operatorCondition.version},)"
            LowerBoundOp.GREATER_THAN_OR_EQUAL -> "[${operatorCondition.version},)"
            else -> ""
        }

    private fun lowerBoundFromOp(op: LowerBoundOp): String =
        when (op) {
            LowerBoundOp.GREATER_THAN -> "("
            LowerBoundOp.GREATER_THAN_OR_EQUAL -> "["
        }

    private fun upperBoundFromOp(op: UpperBoundOp): String =
        when (op) {
            UpperBoundOp.LESS_THAN -> ")"
            UpperBoundOp.LESS_THAN_OR_EQUAL -> "]"
        }
}
