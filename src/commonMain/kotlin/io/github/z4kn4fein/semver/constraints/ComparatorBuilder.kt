package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor
import io.github.z4kn4fein.semver.nextPatch

internal interface ComparatorBuilder {
    val acceptedOperators: Array<String>

    fun buildComparator(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): VersionComparator
}

internal class RegularComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("=", "!=", ">", ">=", "=>", "<", "<=", "=<", "")

    override fun buildComparator(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): VersionComparator = versionDescriptor.toComparator(operatorString.toOperator())
}

internal class TildeComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("~>", "~")

    override fun buildComparator(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): VersionComparator =
        when {
            versionDescriptor.isWildcard -> versionDescriptor.toComparator()
            else -> {
                val version =
                    Version(
                        versionDescriptor.major,
                        versionDescriptor.minor,
                        versionDescriptor.patch,
                        versionDescriptor.preRelease,
                        versionDescriptor.buildMetadata,
                    )
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, version.nextMinor(preRelease = "")),
                    Op.EQUAL,
                )
            }
        }
}

internal class CaretComparatorBuilder : ComparatorBuilder {
    override val acceptedOperators: Array<String> = arrayOf("^")

    override fun buildComparator(
        operatorString: String,
        versionDescriptor: VersionDescriptor,
    ): VersionComparator =
        when {
            versionDescriptor.isMajorWildcard -> VersionComparator.greaterThanMin
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
                Range(
                    start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    end = Condition(Op.LESS_THAN, endVersion),
                    Op.EQUAL,
                )
            }
        }

    private fun fromMinorWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
        when (versionDescriptor.majorString) {
            "0" ->
                Range(
                    VersionComparator.greaterThanMin,
                    Condition(Op.LESS_THAN, Version(major = 1, preRelease = "")),
                    Op.EQUAL,
                )
            else -> versionDescriptor.toComparator()
        }

    private fun fromPatchWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
        when {
            versionDescriptor.majorString == "0" && versionDescriptor.minorString == "0" ->
                Range(
                    VersionComparator.greaterThanMin,
                    Condition(Op.LESS_THAN, Version(minor = 1, preRelease = "")),
                    Op.EQUAL,
                )
            versionDescriptor.majorString != "0" -> {
                val version = Version(major = versionDescriptor.major, minor = versionDescriptor.minor)
                Range(
                    Condition(Op.GREATER_THAN_OR_EQUAL, version),
                    Condition(Op.LESS_THAN, version.nextMajor(preRelease = "")),
                    Op.EQUAL,
                )
            }
            else -> versionDescriptor.toComparator()
        }
}
