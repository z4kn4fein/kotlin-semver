package io.github.z4kn4fein.semver.constraints

internal enum class Op(private val stringValue: String) {
    EQUAL("="),
    NOT_EQUAL("!="),
    LOWER_THAN("<"),
    LOWER_THAN_OR_EQUAL("<="),
    GREATER_THAN(">"),
    GREATER_THAN_OR_EQUAL(">=");

    override fun toString(): String {
        return stringValue
    }
}

internal fun String.toOperator(): Op =
    when (this) {
        "=" -> Op.EQUAL
        "!=" -> Op.NOT_EQUAL
        ">" -> Op.GREATER_THAN
        "<" -> Op.LOWER_THAN
        ">=", "=>" -> Op.GREATER_THAN_OR_EQUAL
        "<=", "=<" -> Op.LOWER_THAN_OR_EQUAL
        else -> Op.EQUAL
    }
