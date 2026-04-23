package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

/**
 * Represents a condition that can evaluate whether a given [Version] satisfies certain criteria.
 *
 * Implementations of this interface define specific rules that determine whether a version
 * meets the condition.
 */
public interface Condition {
    /**
     * Evaluates whether the specified [Version] satisfies the condition defined by the implementation.
     *
     * @param version The version to be evaluated against the condition.
     * @return true if the given version satisfies the condition, otherwise false.
     */
    public fun isSatisfiedBy(version: Version): Boolean

    /**
     * Companion object holding predefined conditions for version comparisons.
     */
    public companion object {
        /**
         * A predefined condition that checks if a given [Version] is greater than or equal to the minimum version.
         */
        public val greaterThanMin: OperatorCondition = OperatorCondition(Op.GREATER_THAN_OR_EQUAL, Version.min)
    }
}

/**
 * Represents a condition that applies an operator to compare a specified version against another version.
 *
 * @property operator The operator used for comparison, such as [Op.EQUAL], [Op.NOT_EQUAL], [Op.LESS_THAN], etc.
 * @property version The version to compare against using the specified operator.
 */
public class OperatorCondition(public val operator: Op, public val version: Version) : Condition {
    override fun isSatisfiedBy(version: Version): Boolean {
        return when (operator) {
            Op.EQUAL -> version == this.version
            Op.NOT_EQUAL -> version != this.version
            Op.LESS_THAN -> version < this.version
            Op.LESS_THAN_OR_EQUAL -> version <= this.version
            Op.GREATER_THAN -> version > this.version
            Op.GREATER_THAN_OR_EQUAL -> version >= this.version
        }
    }
}

/**
 * Represents a composite condition that evaluates whether a [Version] falls within a specific range
 * defined by two [OperatorCondition] instances: a start condition and an end condition.
 *
 * @property start The condition representing the lower bound of the range.
 * @property end The condition representing the upper bound of the range.
 */
public class RangeCondition(
    public val start: OperatorCondition,
    public val end: OperatorCondition,
) : Condition {
    override fun isSatisfiedBy(version: Version): Boolean = start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
}

/**
 * Represents a composite condition that evaluates whether at least one of two given conditions is satisfied.
 *
 * It's only used for the internal construction of the [Constraint] instance.
 * Whenever it's returned from the parser, the second operand is added to the next OR segment of the built [Constraint].
 *
 * It can be ignored in custom formatters.
 *
 * @property operandA The first condition to evaluate.
 * @property operandB The second condition to evaluate.
 */
public class OrCondition(public val operandA: OperatorCondition, public val operandB: OperatorCondition) : Condition {
    override fun isSatisfiedBy(version: Version): Boolean = operandA.isSatisfiedBy(version) || operandB.isSatisfiedBy(version)
}

/**
 * Finds the min upper bound from a list of conditions.
 * Used to determine whether there is an intersection among multiple range conditions.
 *
 * @return The [OperatorCondition] with the smallest version that represents an upper bound.
 */
public fun List<Condition>.minUpperBound(): OperatorCondition =
    this.mapNotNull {
        when (it) {
            is OperatorCondition ->
                when {
                    it.operator.isUpperBound() -> it
                    else -> null
                }
            is RangeCondition -> it.end
            else -> null
        }
    }.minBy { it.version }

/**
 * Determines the maximum lower bound among a list of conditions.
 * Used to determine whether there is an intersection among multiple range conditions.
 *
 * @return The [OperatorCondition] representing the maximum lower bound among the given conditions.
 */
public fun List<Condition>.maxLowerBound(): OperatorCondition =
    this.mapNotNull {
        when (it) {
            is OperatorCondition ->
                when {
                    it.operator.isLowerBound() -> it
                    else -> null
                }
            is RangeCondition -> it.start
            else -> null
        }
    }.maxBy { it.version }

/**
 * Converts an equality or inequality condition into a corresponding range condition.
 *
 * This method transforms an [OperatorCondition] with an equality operator ([Op.EQUAL] or [Op.NOT_EQUAL])
 * into a [RangeCondition] that expresses the equivalent range using logical bounds.
 * If the condition is already a range or does not use an equality-related operator,
 * it is returned unchanged.
 *
 * @return A modified [Condition] that represents a range for equality-based operators,
 *         or the original condition if no transformation is needed.
 */
public fun Condition.equalityToRange(): Condition =
    when (this) {
        is OperatorCondition ->
            when (this.operator) {
                Op.EQUAL ->
                    RangeCondition(
                        OperatorCondition(Op.GREATER_THAN_OR_EQUAL, this.version),
                        OperatorCondition(Op.LESS_THAN_OR_EQUAL, this.version),
                    )
                Op.NOT_EQUAL ->
                    RangeCondition(
                        OperatorCondition(Op.LESS_THAN, this.version),
                        OperatorCondition(Op.GREATER_THAN, this.version),
                    )
                else -> this
            }
        else -> this
    }

/**
 * Transforms a [RangeCondition] into an equivalent [OperatorCondition] if the range can be expressed
 * as a single equality or inequality condition.
 *
 * Specifically:
 * - Converts a range where the start operator is [Op.GREATER_THAN_OR_EQUAL] and the end operator is [Op.LESS_THAN_OR_EQUAL]
 *   for the same version into an [Op.EQUAL] condition.
 * - Converts a range where the start operator is [Op.LESS_THAN] and the end operator is [Op.GREATER_THAN]
 *   for the same version into a [Op.NOT_EQUAL] condition.
 *
 * If the condition does not match these cases, the original condition is returned unchanged.
 *
 * @return A simplified [OperatorCondition] if the range can be converted into equality condition, or the original [Condition] if no such simplification is possible.
 */
public fun Condition.rangeToEquality(): Condition =
    when (this) {
        is RangeCondition ->
            when {
                this.start.version == this.end.version ->
                    when {
                        this.start.operator == Op.GREATER_THAN_OR_EQUAL &&
                            this.end.operator == Op.LESS_THAN_OR_EQUAL -> OperatorCondition(Op.EQUAL, this.start.version)
                        this.start.operator == Op.LESS_THAN &&
                            this.end.operator == Op.GREATER_THAN -> OperatorCondition(Op.NOT_EQUAL, this.start.version)
                        else -> this
                    }
                else -> this
            }
        else -> this
    }
