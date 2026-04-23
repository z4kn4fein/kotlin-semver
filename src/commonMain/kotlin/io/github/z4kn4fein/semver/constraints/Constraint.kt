package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.satisfies
import kotlinx.serialization.Serializable

/**
 * This class describes a semantic version constraint. It provides ability to verify whether a version
 * [satisfies] one or more conditions within a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.constraint
 */
@Serializable(with = ConstraintSerializer::class)
public class Constraint private constructor(private val conditions: List<List<Condition>>) {
    /**
     * Determines whether the [Constraint] is satisfied by a [Version].
     *
     * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedBy
     */
    public fun isSatisfiedBy(version: Version): Boolean =
        conditions.any { comparator -> comparator.all { condition -> condition.isSatisfiedBy(version) } }

    /**
     * Formats the [Constraint] using the provided [ConstraintFormatter].
     *
     * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.format
     */
    public fun format(formatter: ConstraintFormatter): String = formatter.format(conditions)

    override fun toString(): String = format(defaultFormatter)

    public override fun equals(other: Any?): Boolean =
        when (val constraint = other as? Constraint) {
            null -> false
            else -> toString() == constraint.toString()
        }

    public override fun hashCode(): Int = toString().hashCode()

    /** Companion object of [Constraint]. */
    public companion object {
        private val default: Constraint = Constraint(listOf(listOf(Condition.greaterThanMin)))
        private val conditionParsers =
            arrayOf(
                HyphenConditionParser(),
                OperatorConditionParser(),
            )

        /**
         * Default [ConstraintFormatter] used for formatting [Constraint] instances.
         */
        public val defaultFormatter: ConstraintFormatter = DefaultConstraintFormatter()

        /**
         * Parses the [constraintString] as a [Constraint] and returns the result or throws
         * a [ConstraintFormatException] if the string is not a valid representation of a constraint.
         *
         * @throws ConstraintFormatException if the [constraintString] is not a valid constraint representation.
         * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.parse
         */
        @Throws(ConstraintFormatException::class)
        public fun parse(constraintString: String): Constraint {
            if (constraintString.isBlank()) {
                return default
            }
            val orParts = constraintString.split("|").filter { part -> part.isNotBlank() }
            val orSegments = mutableListOf<List<Condition>>()
            for (or in orParts) {
                val andSegments = mutableListOf<Condition>()
                var processed = or
                conditionParsers.forEach { parser ->
                    processed =
                        processed.replace(parser.regex) { match ->
                            when (val result = parser.parseConditionMatch(match)) {
                                is OrCondition -> {
                                    andSegments.add(result.operandA)
                                    orSegments.add(mutableListOf(result.operandB))
                                }
                                else -> andSegments.add(result)
                            }
                            ""
                        }
                }
                when {
                    processed.isNotBlank() -> throw ConstraintFormatException("Invalid constraint: $or")
                    else -> orSegments.add(andSegments)
                }
            }
            return when {
                orSegments.isEmpty() || orSegments.all { it.isEmpty() } ->
                    throw ConstraintFormatException("Invalid constraint: $constraintString")
                else -> Constraint(orSegments)
            }
        }

        /**
         * Parses the [constraintString] as a [Constraint] using the provided [parser] and returns
         * the result or throws a [ConstraintFormatException] if the string is not a valid representation
         * of a constraint.
         *
         * @throws ConstraintFormatException if the [constraintString] is not a valid constraint representation.
         * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.parseFormat
         */
        @Throws(ConstraintFormatException::class)
        public fun parseFormat(
            constraintString: String,
            parser: ConditionParser,
        ): Constraint {
            if (constraintString.isBlank()) {
                return default
            }
            var constraint = constraintString
            if (parser is PreProcessingConditionParser) {
                constraint = parser.preProcessConstraint(constraintString)
            }
            val orParts = constraint.split(parser.separator).filter { part -> part.isNotBlank() }
            val orSegments = mutableListOf<List<Condition>>()
            for (or in orParts) {
                val andSegments = mutableListOf<Condition>()
                val processed =
                    or.replace(parser.regex) { match ->
                        when (val result = parser.parseConditionMatch(match)) {
                            is OrCondition -> {
                                andSegments.add(result.operandA)
                                orSegments.add(mutableListOf(result.operandB))
                            }
                            else -> andSegments.add(result)
                        }
                        ""
                    }
                when {
                    processed.isNotBlank() -> throw ConstraintFormatException("Invalid constraint: $or")
                    else -> orSegments.add(andSegments)
                }
            }
            return when {
                orSegments.isEmpty() || orSegments.all { it.isEmpty() } ->
                    throw ConstraintFormatException("Invalid constraint: $constraintString")
                else -> Constraint(orSegments)
            }
        }
    }
}
