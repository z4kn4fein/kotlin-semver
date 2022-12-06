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
public class Constraint private constructor(private val comparators: List<List<VersionComparator>>) {
    /**
     * Determines whether a [Constraint] is satisfied by a [Version] or not.
     *
     * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedBy
     */
    public fun isSatisfiedBy(version: Version): Boolean =
        comparators.any { comparator -> comparator.all { condition -> condition.isSatisfiedBy(version) } }

    override fun toString(): String = comparators.joinToString(" || ") { it.joinToString(" ") }

    public override fun equals(other: Any?): Boolean =
        when (val constraint = other as? Constraint) {
            null -> false
            else -> toString() == constraint.toString()
        }

    public override fun hashCode(): Int = toString().hashCode()

    /** Companion object of [Constraint]. */
    public companion object {
        private val default: Constraint = Constraint(listOf(listOf(VersionComparator.greaterThanMin)))
        private val conditionProcessors = arrayOf(
            HyphenConditionProcessor(),
            OperatorConditionProcessor()
        )

        /**
         * Parses the [constraintString] as a [Constraint] and returns the result or throws
         * a [ConstraintFormatException] if the string is not a valid representation of a constraint.
         *
         * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.parse
         */
        public fun parse(constraintString: String): Constraint {
            if (constraintString.isBlank()) {
                return default
            }
            val orParts = constraintString.split("|").filter { part -> part.isNotBlank() }
            val comparators = orParts.map { comparator ->
                val conditionsResult = mutableListOf<VersionComparator>()
                var processed = comparator
                conditionProcessors.forEach { processor ->
                    processed = processed.replace(processor.regex) { condition ->
                        conditionsResult.add(processor.processCondition(condition))
                        ""
                    }
                }
                when {
                    processed.isNotBlank() -> throw ConstraintFormatException("Invalid constraint: $comparator")
                    else -> conditionsResult
                }
            }
            return when {
                comparators.isEmpty() || comparators.all { it.isEmpty() } ->
                    throw ConstraintFormatException("Invalid constraint: $constraintString")
                else -> Constraint(comparators)
            }
        }
    }
}
