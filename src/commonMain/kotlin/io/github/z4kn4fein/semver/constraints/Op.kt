package io.github.z4kn4fein.semver.constraints

/**
 * Represents a comparison operator for version constraints.
 */
public interface Operator

/**
 * Defines equality comparison operators that can be used to evaluate version constraints.
 *
 * @property stringValue The string representation of the operator.
 */
public enum class EqualityOp(private val stringValue: String) : Operator {
    /**
     * Represents the equality comparison operator.
     */
    EQUAL("="),

    /**
     * Represents a comparison operator that checks for inequality between two values.
     */
    NOT_EQUAL("!="),
    ;

    override fun toString(): String {
        return stringValue
    }
}

/**
 * Enum representing lower-bound comparison operators used in version constraints.
 *
 * @property stringValue The string representation of the operator.
 */
public enum class LowerBoundOp(private val stringValue: String) : Operator {
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
}

/**
 * Represents the upper-bound comparison operator for version constraints.
 *
 * @property stringValue The string representation of the operator.
 */
public enum class UpperBoundOp(private val stringValue: String) : Operator {
    /**
     * Represents the 'less than' comparison operator for version constraints.
     */
    LESS_THAN("<"),

    /**
     * Represents the "less than or equal to" comparison operator.
     */
    LESS_THAN_OR_EQUAL("<="),
    ;

    override fun toString(): String {
        return stringValue
    }
}

internal fun String.toOperator(): Operator =
    when (this) {
        "=", "" -> EqualityOp.EQUAL
        "!=" -> EqualityOp.NOT_EQUAL
        ">" -> LowerBoundOp.GREATER_THAN
        "<" -> UpperBoundOp.LESS_THAN
        ">=", "=>" -> LowerBoundOp.GREATER_THAN_OR_EQUAL
        "<=", "=<" -> UpperBoundOp.LESS_THAN_OR_EQUAL
        else -> throw ConstraintFormatException("Invalid operator: $this")
    }
