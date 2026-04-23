package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor
import io.github.z4kn4fein.semver.nextPatch

internal interface ConditionBuilder {
    val acceptedOperators: Array<String>

    fun buildCondition(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): Condition
}

internal class OperatorConditionBuilder : ConditionBuilder {
    override val acceptedOperators: Array<String> = arrayOf("=", "!=", "", ">", ">=", "=>", "<", "<=", "=<")

    override fun buildCondition(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): Condition {
        val operator = operatorString.toOperator()
        return when (val condition = versionDescriptor.toCondition(operator)) {
            is RangeCondition ->
                when (operator) {
                    Op.EQUAL -> condition
                    Op.NOT_EQUAL ->
                        OrCondition(
                            OperatorCondition(condition.start.operator.negate(), condition.start.version),
                            OperatorCondition(condition.end.operator.negate(), condition.end.version),
                        )
                    Op.LESS_THAN -> OperatorCondition(condition.start.operator.negate(), condition.start.version)
                    Op.LESS_THAN_OR_EQUAL -> condition.end
                    Op.GREATER_THAN -> OperatorCondition(condition.end.operator.negate(), condition.end.version)
                    Op.GREATER_THAN_OR_EQUAL -> condition.start
                }
            else -> condition
        }
    }
}

internal class TildeConditionBuilder : ConditionBuilder {
    override val acceptedOperators: Array<String> = arrayOf("~>", "~")

    override fun buildCondition(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): Condition =
        when {
            versionDescriptor.isWildcard -> versionDescriptor.toCondition()
            else -> {
                val version =
                    Version(
                        versionDescriptor.major,
                        versionDescriptor.minor,
                        versionDescriptor.patch,
                        versionDescriptor.preRelease,
                        versionDescriptor.buildMetadata,
                    )
                RangeCondition(
                    start = OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = OperatorCondition(Op.LESS_THAN, version.nextMinor(preRelease = "")),
                )
            }
        }
}

internal class CaretConditionBuilder : ConditionBuilder {
    override val acceptedOperators: Array<String> = arrayOf("^")

    override fun buildCondition(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): Condition =
        when {
            versionDescriptor.isMajorWildcard -> Condition.greaterThanMin
            versionDescriptor.isMinorWildcard -> fromMinorWildcardCaret(versionDescriptor)
            versionDescriptor.isPatchWildcard -> fromPatchWildcardCaret(versionDescriptor)
            else -> {
                val version =
                    Version(
                        versionDescriptor.major,
                        versionDescriptor.minor,
                        versionDescriptor.patch,
                        versionDescriptor.preRelease,
                        versionDescriptor.buildMetadata,
                    )
                val endVersion =
                    when {
                        versionDescriptor.majorString != "0" -> version.nextMajor(preRelease = "")
                        versionDescriptor.minorString != "0" -> version.nextMinor(preRelease = "")
                        versionDescriptor.patchString != "0" -> version.nextPatch(preRelease = "")
                        else -> Version(patch = 1, preRelease = "") // ^0.0.0 -> <0.0.1-0
                    }
                RangeCondition(
                    start = OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = OperatorCondition(Op.LESS_THAN, endVersion),
                )
            }
        }

    private fun fromMinorWildcardCaret(versionDescriptor: VersionDescriptor): Condition =
        when (versionDescriptor.majorString) {
            "0" ->
                RangeCondition(
                    Condition.greaterThanMin,
                    OperatorCondition(Op.LESS_THAN, Version(major = 1, preRelease = "")),
                )
            else -> versionDescriptor.toCondition()
        }

    private fun fromPatchWildcardCaret(versionDescriptor: VersionDescriptor): Condition =
        when {
            versionDescriptor.majorString == "0" && versionDescriptor.minorString == "0" ->
                RangeCondition(
                    Condition.greaterThanMin,
                    OperatorCondition(Op.LESS_THAN, Version(minor = 1, preRelease = "")),
                )
            versionDescriptor.majorString != "0" -> {
                val version = Version(major = versionDescriptor.major, minor = versionDescriptor.minor)
                RangeCondition(
                    OperatorCondition(Op.GREATER_THAN_OR_EQUAL, version),
                    OperatorCondition(Op.LESS_THAN, version.nextMajor(preRelease = "")),
                )
            }
            else -> versionDescriptor.toCondition()
        }
}
