package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Patterns
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

            val orParts = constraintString.split("||")
            val comparators = orParts.map { comparator ->
                val conditionsResult = mutableListOf<VersionComparator>()
                val hyphensEscaped = Patterns.hyphenConditionRegex.replace(comparator) { hyphenCondition ->
                    conditionsResult.add(hyphenToComparator(hyphenCondition))
                    ""
                }

                if (hyphensEscaped.isNotBlank() && !Patterns.validOperatorConstraintRegex.matches(hyphensEscaped)) {
                    throw ConstraintFormatException("Invalid constraint: $constraintString")
                }

                val operatorConditions = Patterns.operatorConditionRegex.findAll(hyphensEscaped)
                conditionsResult.addAll(operatorConditions.map { condition -> operatorToComparator(condition) })
                conditionsResult
            }

            return if (comparators.isEmpty() || comparators.all { it.isEmpty() })
                throw ConstraintFormatException("Invalid constraint: $constraintString")
            else Constraint(comparators)
        }

        @Suppress("MagicNumber")
        private fun operatorToComparator(result: MatchResult): VersionComparator {
            val operator = result.groups[1]?.value ?: ""
            val major = result.groups[2]?.value ?: ""
            val minor = result.groups[3]?.value
            val patch = result.groups[4]?.value
            val preRelease = result.groups[5]?.value
            val buildMetadata = result.groups[6]?.value
            return VersionComparator.createFromOperator(
                operator,
                VersionDescriptor(major, minor, patch, preRelease, buildMetadata)
            )
        }

        @Suppress("MagicNumber")
        private fun hyphenToComparator(result: MatchResult): VersionComparator =
            VersionComparator.createFromHyphenRange(
                VersionDescriptor(
                    majorString = result.groups[1]?.value ?: "",
                    minorString = result.groups[2]?.value,
                    patchString = result.groups[3]?.value,
                    preRelease = result.groups[4]?.value,
                    buildMetadata = result.groups[5]?.value
                ),
                VersionDescriptor(
                    majorString = result.groups[6]?.value ?: "",
                    minorString = result.groups[7]?.value,
                    patchString = result.groups[8]?.value,
                    preRelease = result.groups[9]?.value,
                    buildMetadata = result.groups[10]?.value
                )
            )
    }
}
