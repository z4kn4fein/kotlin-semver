package io.github.z4kn4fein.semver.constraints

/**
 * Represents a set of comparison operators used for evaluating conditions.
 */
public enum class Op(private val stringValue: String) {
    /**
     * Represents the equality comparison operator.
     */
    EQUAL("="),

    /**
     * Represents a comparison operator that checks for inequality between two values.
     */
    NOT_EQUAL("!="),

    /**
     * Represents the 'less than' comparison operator for version constraints.
     */
    LESS_THAN("<"),

    /**
     * Represents the "less than or equal to" comparison operator.
     */
    LESS_THAN_OR_EQUAL("<="),

    /**
     * Represents the "greater than" comparison operator.
     */
    GREATER_THAN(">"),

    /**
     * Represents the "greater than or equal to" comparison operator.
     */
    GREATER_THAN_OR_EQUAL(">="),
    ;

    override fun toString(): String {
        return stringValue
    }

    /**
     * Returns the logical negation of the current comparison operator.
     *
     * @return The negated comparison operator.
     */
    public fun negate(): Op =
        when (this) {
            EQUAL -> NOT_EQUAL
            NOT_EQUAL -> EQUAL
            LESS_THAN -> GREATER_THAN_OR_EQUAL
            LESS_THAN_OR_EQUAL -> GREATER_THAN
            GREATER_THAN -> LESS_THAN_OR_EQUAL
            GREATER_THAN_OR_EQUAL -> LESS_THAN
        }

    /**
     * Determines whether the current operator represents a lower bound in a range condition.
     *
     * A lower bound is defined as either [GREATER_THAN] or [GREATER_THAN_OR_EQUAL].
     *
     * @return true if the operator is a lower bound, false otherwise.
     */
    public fun isLowerBound(): Boolean = this == GREATER_THAN || this == GREATER_THAN_OR_EQUAL

    /**
     * Determines whether the current operator represents an upper bound in a range condition.
     *
     * An upper bound is defined as either [LESS_THAN] or [LESS_THAN_OR_EQUAL].
     *
     * @return true if the operator is an upper bound, false otherwise.
     */
    public fun isUpperBound(): Boolean = this == LESS_THAN || this == LESS_THAN_OR_EQUAL

    /**
     * Determines whether the current operator represents an equality comparison.
     *
     * An equality comparison operator is defined as either [EQUAL] or [NOT_EQUAL].
     *
     * @return true if the operator is [EQUAL] or [NOT_EQUAL], false otherwise.
     */
    public fun isEquality(): Boolean = this == EQUAL || this == NOT_EQUAL
}

internal fun String.toOperator(): Op =
    when (this) {
        "=" -> Op.EQUAL
        "!=" -> Op.NOT_EQUAL
        ">" -> Op.GREATER_THAN
        "<" -> Op.LESS_THAN
        ">=", "=>" -> Op.GREATER_THAN_OR_EQUAL
        "<=", "=<" -> Op.LESS_THAN_OR_EQUAL
        else -> Op.EQUAL
    }
