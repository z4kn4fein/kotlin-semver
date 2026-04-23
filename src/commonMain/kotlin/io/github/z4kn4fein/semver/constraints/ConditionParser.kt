package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Patterns
import io.github.z4kn4fein.semver.Version

/**
 * Interface for parsing conditional expressions into [Condition] instances.
 *
 * A [ConditionParser] is responsible for interpreting text patterns that match a certain syntax,
 * converting them into corresponding [Condition] objects which can then be evaluated against
 * specific versions.
 */
public interface ConditionParser {
    /**
     * A regular expression used for parsing and matching conditional expressions.
     * It serves as a pattern to identify and extract components from the input string
     * that conform to the expected condition syntax, enabling the creation of valid
     * [Condition] objects.
     */
    public val regex: Regex

    /**
     * The separator used to split the OR segments of a constraint expression.
     */
    public val separator: String get() = "|"

    /**
     * Parses the given [MatchResult] into a corresponding [Condition] instance.
     *
     * @param match the [MatchResult] containing the parsed components of a conditional expression.
     *              It is the result of applying the [regex] property of the parser.
     * @return a [Condition] instance derived from the parsed match.
     */
    public fun parseConditionMatch(match: MatchResult): Condition
}

/**
 * An interface extending [ConditionParser], designed for parsing and preprocessing
 * constraints in conditional expressions.
 *
 * This parser provides an additional layer of preprocessing to normalize
 * constraint strings before parsing them into [Condition] instances. It is typically
 * used when the raw input requires adjustments to conform to a specific
 * syntactical format expected by the [ConditionParser].
 */
public interface PreProcessingConditionParser : ConditionParser {
    /**
     * Preprocesses the given constraint string to normalize it
     * for further parsing.
     *
     * @param constraint the raw constraint string to be preprocessed.
     * @return the normalized constraint string.
     */
    public fun preProcessConstraint(constraint: String): String
}

internal class OperatorConditionParser : ConditionParser {
    override val regex: Regex = Patterns.OPERATOR_CONDITION_REGEX.toRegex()
    private val conditionBuilders =
        arrayOf(
            OperatorConditionBuilder(),
            TildeConditionBuilder(),
            CaretConditionBuilder(),
        )

    @Suppress("MagicNumber")
    override fun parseConditionMatch(match: MatchResult): Condition {
        val operator = match.groups[1]?.value ?: ""
        val major = match.groups[2]?.value ?: ""
        val minor = match.groups[3]?.value
        val patch = match.groups[4]?.value
        val preRelease = match.groups[5]?.value
        val buildMetadata = match.groups[6]?.value
        val descriptor = VersionDescriptor(major, minor, patch, preRelease, buildMetadata)
        conditionBuilders.forEach { builder ->
            if (operator in builder.acceptedOperators) {
                return builder.buildCondition(operator, descriptor)
            }
        }
        throw ConstraintFormatException(
            "Invalid constraint operator: " +
                "$operator in $descriptor",
        )
    }
}

internal class HyphenConditionParser : ConditionParser {
    override val regex: Regex = Patterns.HYPHEN_CONDITION_REGEX.toRegex()

    @Suppress("MagicNumber")
    override fun parseConditionMatch(match: MatchResult): Condition {
        val startCondition =
            VersionDescriptor(
                majorString = match.groups[1]?.value ?: "",
                minorString = match.groups[2]?.value,
                patchString = match.groups[3]?.value,
                preRelease = match.groups[4]?.value,
                buildMetadata = match.groups[5]?.value,
            ).toCondition(Op.GREATER_THAN_OR_EQUAL)
        val endCondition =
            VersionDescriptor(
                majorString = match.groups[6]?.value ?: "",
                minorString = match.groups[7]?.value,
                patchString = match.groups[8]?.value,
                preRelease = match.groups[9]?.value,
                buildMetadata = match.groups[10]?.value,
            ).toCondition(Op.LESS_THAN_OR_EQUAL)
        val start =
            when (startCondition) {
                is RangeCondition -> startCondition.start
                else -> startCondition as OperatorCondition
            }
        val end =
            when (endCondition) {
                is RangeCondition -> endCondition.end
                else -> endCondition as OperatorCondition
            }
        return RangeCondition(start, end)
    }
}

/**
 * A parser that converts Maven-style version range expressions into [Condition] objects.
 *
 * The class supports:
 * - Parsing single-version constraints (e.g., "[1.0.0]")
 * - Parsing range constraints (e.g., "[1.0.0,2.0.0)")
 * - Parsing multiple constraints (e.g., "[1.0.0,2.0.0),[3.0.0]")
 *
 * @constructor Creates an instance of the [MavenConditionParser].
 */
public class MavenConditionParser : PreProcessingConditionParser {
    private val separatorPreprocessingBracketRegex = Regex("]\\s*,")
    private val separatorPreprocessingParenthesisRegex = Regex("\\)\\s*,")

    override val regex: Regex = Patterns.MAVEN_RANGE_REGEX.toRegex()

    override fun parseConditionMatch(match: MatchResult): Condition {
        val lowerOperator = match.groups[1]?.value ?: ""
        val upperOperator = match.groups[13]?.value ?: ""
        val lowerMajor = match.groups[2]?.value?.toIntOrNull()
        val lowerMinor = match.groups[3]?.value?.toIntOrNull() ?: 0
        val lowerPatch = match.groups[4]?.value?.toIntOrNull() ?: 0
        val lowerPreRelease = match.groups[5]?.value
        val lowerBuildMetadata = match.groups[6]?.value
        val hasComma = match.groups[7]?.value != null
        val higherMajor = match.groups[8]?.value?.toIntOrNull()
        val higherMinor = match.groups[9]?.value?.toIntOrNull() ?: 0
        val higherPatch = match.groups[10]?.value?.toIntOrNull() ?: 0
        val higherPreRelease = match.groups[11]?.value
        val higherBuildMetadata = match.groups[12]?.value

        return when {
            !hasComma -> {
                val version =
                    when {
                        lowerMajor == null -> throw ConstraintFormatException("Invalid constraint.")
                        else -> Version(lowerMajor, lowerMinor, lowerPatch, lowerPreRelease, lowerBuildMetadata)
                    }
                when {
                    lowerOperator == "[" -> OperatorCondition(Op.EQUAL, version)
                    else -> OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version)
                }
            }
            else -> {
                val start =
                    when {
                        lowerMajor == null -> null
                        else -> Version(lowerMajor, lowerMinor, lowerPatch, lowerPreRelease, lowerBuildMetadata)
                    }
                val end =
                    when {
                        higherMajor == null -> null
                        else -> Version(higherMajor, higherMinor, higherPatch, higherPreRelease, higherBuildMetadata)
                    }

                when {
                    start != null && end != null ->
                        RangeCondition(
                            OperatorCondition(opFromLowerOperator(lowerOperator), start),
                            OperatorCondition(opFromUpperOperator(upperOperator), end),
                        )
                    start != null && end == null -> OperatorCondition(opFromLowerOperator(lowerOperator), start)
                    start == null && end != null -> OperatorCondition(opFromUpperOperator(upperOperator), end)
                    else -> throw ConstraintFormatException("Invalid constraint.")
                }
            }
        }
    }

    override fun preProcessConstraint(constraint: String): String {
        return constraint.replace(separatorPreprocessingParenthesisRegex, ")|")
            .replace(separatorPreprocessingBracketRegex, "]|")
    }

    private fun opFromLowerOperator(operator: String): Op =
        when (operator) {
            "[" -> Op.GREATER_THAN_OR_EQUAL
            "(" -> Op.GREATER_THAN
            else -> throw ConstraintFormatException("Invalid operator: $operator")
        }

    private fun opFromUpperOperator(operator: String): Op =
        when (operator) {
            "]" -> Op.LESS_THAN_OR_EQUAL
            ")" -> Op.LESS_THAN
            else -> throw ConstraintFormatException("Invalid operator: $operator")
        }
}
