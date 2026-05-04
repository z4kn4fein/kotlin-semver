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
        public val greaterThanMin: LowerBoundCondition = LowerBoundCondition(LowerBoundOp.GREATER_THAN_OR_EQUAL, Version.min)
    }
}

/**
 * Represents a condition that compares a specific semantic version against a constraint
 * defined by an operator and a target version.
 *
 * @property operator The comparison operator used to evaluate the condition.
 * @property version The target semantic version to be compared using the operator.
 */
public interface OperatorCondition : Condition {
    /**
     * Returns the comparison operator used to evaluate the condition.
     */
    public val operator: Operator

    /**
     * Returns the target semantic version to be compared using the operator.
     */
    public val version: Version
}

/**
 * Represents a condition with a defined boundary for semantic version constraints.
 * This interface extends the behavior of [OperatorCondition] by adding comparability.
 */
public interface BoundCondition : OperatorCondition, Comparable<BoundCondition>

/**
 * Represents an entity that can be negated to form its logical opposite.
 *
 * Classes implementing this interface provide the ability to transform a condition
 * into its negated equivalent.
 *
 * @param T The specific type of condition that this interface operates on.
 */
public interface Negatable<T : Condition> {
    /**
     * Returns a new condition that represents the negation of the current condition.
     */
    public fun negate(): T
}

/**
 * Represents a condition that checks for equality or inequality of a version
 * using the specified equality operator.
 *
 * @constructor Creates an instance of [EqualityCondition] with the given operator and version.
 * @property operator The equality operator to be used for comparison.
 * @property version The version to be compared against.
 */
public class EqualityCondition(
    public override val operator: EqualityOp,
    public override val version: Version,
) : OperatorCondition, Negatable<EqualityCondition> {
    override fun isSatisfiedBy(version: Version): Boolean =
        when (operator) {
            EqualityOp.EQUAL -> version == this.version
            EqualityOp.NOT_EQUAL -> version != this.version
        }

    override fun negate(): EqualityCondition =
        when (operator) {
            EqualityOp.EQUAL -> EqualityCondition(EqualityOp.NOT_EQUAL, version)
            EqualityOp.NOT_EQUAL -> EqualityCondition(EqualityOp.EQUAL, version)
        }
}

/**
 * Represents a condition that checks if a version is greater than or equal to a specified version.
 *
 * @constructor Creates an instance of [LowerBoundCondition] with the given operator and version.
 * @property operator The lower-bound operator to be used for comparison.
 * @property version The version to be compared against.
 */
public class LowerBoundCondition(
    public override val operator: LowerBoundOp,
    public override val version: Version,
) : BoundCondition, Negatable<UpperBoundCondition> {
    override fun isSatisfiedBy(version: Version): Boolean =
        when (operator) {
            LowerBoundOp.GREATER_THAN -> version > this.version
            LowerBoundOp.GREATER_THAN_OR_EQUAL -> version >= this.version
        }

    override fun negate(): UpperBoundCondition =
        when (operator) {
            LowerBoundOp.GREATER_THAN -> UpperBoundCondition(UpperBoundOp.LESS_THAN_OR_EQUAL, version)
            LowerBoundOp.GREATER_THAN_OR_EQUAL -> UpperBoundCondition(UpperBoundOp.LESS_THAN, version)
        }

    override fun compareTo(other: BoundCondition): Int =
        when {
            version == other.version ->
                when (other) {
                    is UpperBoundCondition -> 1
                    is LowerBoundCondition ->
                        when {
                            operator == LowerBoundOp.GREATER_THAN && other.operator == LowerBoundOp.GREATER_THAN_OR_EQUAL -> 1
                            operator == LowerBoundOp.GREATER_THAN_OR_EQUAL && other.operator == LowerBoundOp.GREATER_THAN -> -1
                            else -> 0
                        }
                    else -> 0
                }
            else -> version.compareTo(other.version)
        }
}

/**
 * Represents a condition that checks if a version is less than or equal to a specified version.
 *
 * @constructor Creates an instance of [UpperBoundCondition] with the given operator and version.
 * @property operator The upper-bound operator to be used for comparison.
 * @property version The version to be compared against.
 */
public class UpperBoundCondition(
    public override val operator: UpperBoundOp,
    public override val version: Version,
) : BoundCondition, Negatable<LowerBoundCondition> {
    override fun isSatisfiedBy(version: Version): Boolean =
        when (operator) {
            UpperBoundOp.LESS_THAN -> version < this.version
            UpperBoundOp.LESS_THAN_OR_EQUAL -> version <= this.version
        }

    override fun negate(): LowerBoundCondition =
        when (operator) {
            UpperBoundOp.LESS_THAN -> LowerBoundCondition(LowerBoundOp.GREATER_THAN_OR_EQUAL, version)
            UpperBoundOp.LESS_THAN_OR_EQUAL -> LowerBoundCondition(LowerBoundOp.GREATER_THAN, version)
        }

    override fun compareTo(other: BoundCondition): Int =
        when {
            version == other.version ->
                when (other) {
                    is LowerBoundCondition -> -1
                    is UpperBoundCondition ->
                        when {
                            operator == UpperBoundOp.LESS_THAN && other.operator == UpperBoundOp.LESS_THAN_OR_EQUAL -> -1
                            operator == UpperBoundOp.LESS_THAN_OR_EQUAL && other.operator == UpperBoundOp.LESS_THAN -> 1
                            else -> 0
                        }
                    else -> 0
                }
            else -> version.compareTo(other.version)
        }
}

/**
 * Represents a composite condition that evaluates whether a [Version] falls within a specific range
 * defined by a [LowerBoundCondition] and an [UpperBoundCondition].
 *
 * @property start The condition representing the lower bound of the range.
 * @property end The condition representing the upper bound of the range.
 */
public class RangeCondition(
    public val start: LowerBoundCondition,
    public val end: UpperBoundCondition,
) : Condition {
    override fun isSatisfiedBy(version: Version): Boolean = start.isSatisfiedBy(version) && end.isSatisfiedBy(version)
}

/**
 * Represents a non-intersecting range condition where bounds are pointing in opposite directions.
 *
 * @constructor Creates an instance of [NonIntersectingRangeCondition] with the specified lower and upper bounds.
 * @property lower The lower-bound condition to be evaluated.
 * @property upper The upper-bound condition to be evaluated.
 */
public class NonIntersectingRangeCondition(public val lower: LowerBoundCondition, public val upper: UpperBoundCondition) : Condition {
    override fun isSatisfiedBy(version: Version): Boolean = lower.isSatisfiedBy(version) || upper.isSatisfiedBy(version)
}

internal fun List<Condition>.reduce(): List<Condition> {
    if (this.size == 1) {
        val condition = this[0]
        return when {
            condition is NonIntersectingRangeCondition && condition.lower.version != condition.upper.version ->
                mutableListOf(condition.lower, condition.upper)
            else -> mutableListOf(condition)
        }
    }

    val nonIntersectingRanges = this.filterIsInstance<NonIntersectingRangeCondition>()
    return when {
        nonIntersectingRanges.isNotEmpty() -> {
            val reduced = this.reduceConditions()
            val allBounds = nonIntersectingRanges.getIntersectingBounds(reduced)
            val validBounds = allBounds.filter { !it.isInIgnoreRange(nonIntersectingRanges) }

            when {
                validBounds.size == 1 -> validBounds
                else -> {
                    val orConditions = mutableListOf<Condition>()
                    var currentLowerBound: LowerBoundCondition? = null
                    var minUpperBound: UpperBoundCondition? = null
                    validBounds.sorted().forEach {
                        when {
                            it is LowerBoundCondition -> {
                                currentLowerBound = it
                                if (minUpperBound != null) {
                                    orConditions.add(minUpperBound)
                                    minUpperBound = null
                                }
                            }
                            else ->
                                when {
                                    currentLowerBound != null -> {
                                        orConditions.add(RangeCondition(currentLowerBound, it as UpperBoundCondition))
                                        currentLowerBound = null
                                        minUpperBound = null
                                    }
                                    minUpperBound == null -> minUpperBound = it as UpperBoundCondition
                                }
                        }
                    }
                    when {
                        currentLowerBound != null -> orConditions.add(currentLowerBound)
                        minUpperBound != null -> orConditions.add(minUpperBound)
                    }
                    orConditions
                }
            }
        }
        else -> mutableListOf(this.reduceConditions())
    }
}

internal fun List<NonIntersectingRangeCondition>.getIntersectingBounds(condition: Condition): List<BoundCondition> {
    val bounds = mutableListOf<BoundCondition>()
    this.forEach {
        when (condition) {
            is LowerBoundCondition -> {
                if (it.lower > condition) bounds.add(it.lower)
                if (it.upper > condition) bounds.add(it.upper)
            }

            is UpperBoundCondition -> {
                if (it.lower < condition) bounds.add(it.lower)
                if (it.upper < condition) bounds.add(it.upper)
            }

            is RangeCondition -> {
                if (condition.start < it.lower && condition.end > it.lower) bounds.add(it.lower)
                if (condition.start < it.upper && condition.end > it.upper) bounds.add(it.upper)
                if (condition.start > it.upper && condition.end < it.lower) {
                    throw ConstraintFormatException(
                        "Conflicting range conditions: " +
                            "(${condition.start.operator}${condition.start.version} " +
                            "${condition.end.operator}${condition.end.version}) && " +
                            "(${it.lower.operator}${it.lower.version} || ${it.upper.operator}${it.upper.version})",
                    )
                }
            }
        }
    }
    when (condition) {
        is BoundCondition -> bounds.add(condition)
        is RangeCondition -> {
            bounds.add(condition.start)
            bounds.add(condition.end)
        }
    }
    return bounds
}

internal fun Condition.isInIgnoreRange(nonIntersectingRanges: List<NonIntersectingRangeCondition>): Boolean =
    when (this) {
        is BoundCondition -> nonIntersectingRanges.any { it.lower > this && it.upper < this }
        is RangeCondition -> {
            nonIntersectingRanges.any { (it.lower > this.start && it.upper < this.start) || (it.lower > this.end && it.upper < this.end) }
        }
        else -> false
    }

internal fun Condition.rangeToEquality(): Condition =
    when (this) {
        is RangeCondition ->
            when {
                this.start.version == this.end.version && this.start.operator == LowerBoundOp.GREATER_THAN_OR_EQUAL &&
                    this.end.operator == UpperBoundOp.LESS_THAN_OR_EQUAL -> EqualityCondition(EqualityOp.EQUAL, this.start.version)
                this.start.version == this.end.version && this.start.operator == LowerBoundOp.GREATER_THAN &&
                    this.end.operator == UpperBoundOp.LESS_THAN -> EqualityCondition(EqualityOp.NOT_EQUAL, this.start.version)
                else -> this
            }
        is NonIntersectingRangeCondition ->
            when {
                this.lower.version == this.upper.version && this.lower.operator == LowerBoundOp.GREATER_THAN &&
                    this.upper.operator == UpperBoundOp.LESS_THAN -> EqualityCondition(EqualityOp.NOT_EQUAL, this.lower.version)
                else -> this
            }
        else -> this
    }

internal fun List<Condition>.reduceConditions(): Condition {
    if (this.size == 1) {
        return this[0]
    }

    val maxLowerBound = this.maxLowerBoundWithoutNonIntersecting()
    val minUpperBound = this.minUpperBoundWithoutNonIntersecting()

    return when {
        maxLowerBound != null && minUpperBound != null -> {
            if (maxLowerBound.version > minUpperBound.version) {
                throw ConstraintFormatException(
                    "Condition produces an invalid range: " +
                        "${maxLowerBound.operator}${maxLowerBound.version} ${minUpperBound.operator}${minUpperBound.version}",
                )
            }
            RangeCondition(maxLowerBound, minUpperBound)
        }

        maxLowerBound == null && minUpperBound != null -> minUpperBound
        maxLowerBound != null && minUpperBound == null -> maxLowerBound
        else -> throw ConstraintFormatException("Invalid condition.")
    }
}

internal fun List<Condition>.minUpperBoundWithoutNonIntersecting(): UpperBoundCondition? =
    this.mapNotNull {
        when (it) {
            is UpperBoundCondition -> it
            is RangeCondition -> it.end
            else -> null
        }
    }.minOrNull()

internal fun List<Condition>.maxLowerBoundWithoutNonIntersecting(): LowerBoundCondition? =
    this.mapNotNull {
        when (it) {
            is LowerBoundCondition -> it
            is RangeCondition -> it.start
            else -> null
        }
    }.maxOrNull()
