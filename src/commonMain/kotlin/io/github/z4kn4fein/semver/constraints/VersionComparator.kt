package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Patterns
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor
import io.github.z4kn4fein.semver.nextPatch

internal interface VersionComparator {
    fun isSatisfiedBy(version: Version): Boolean
    fun opposite(): String

    companion object {
        val greaterThanMin: VersionComparator = Condition(Op.GREATER_THAN_OR_EQUAL, Version.min)

        fun createFromOperator(operatorString: String, versionDescriptor: VersionDescriptor): VersionComparator =
            when (operatorString) {
                in Patterns.comparisonOperators, "" -> versionDescriptor.toComparator(operatorString.toOperator())
                in Patterns.tildeOperators -> fromTilde(versionDescriptor)
                Patterns.caretOperator -> fromCaret(versionDescriptor)
                else -> throw ConstraintFormatException(
                    "Invalid constraint operator: " +
                        "$operatorString in $versionDescriptor"
                )
            }

        fun createFromHyphenRange(
            startDescriptor: VersionDescriptor,
            endDescriptor: VersionDescriptor
        ): VersionComparator =
            Range(
                startDescriptor.toComparator(Op.GREATER_THAN_OR_EQUAL),
                endDescriptor.toComparator(Op.LESS_THAN_OR_EQUAL),
                Op.EQUAL
            )

        private fun fromTilde(versionDescriptor: VersionDescriptor): VersionComparator =
            when {
                versionDescriptor.isWildcard -> versionDescriptor.toComparator()
                else -> {
                    val version = Version(
                        versionDescriptor.major,
                        versionDescriptor.minor,
                        versionDescriptor.patch,
                        versionDescriptor.preRelease,
                        versionDescriptor.buildMetadata
                    )

                    Range(
                        start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                        end = Condition(Op.LESS_THAN, version.nextMinor(preRelease = "")),
                        Op.EQUAL
                    )
                }
            }

        private fun fromCaret(versionDescriptor: VersionDescriptor): VersionComparator =
            when {
                versionDescriptor.isMajorWildcard -> greaterThanMin
                versionDescriptor.isMinorWildcard -> fromMinorWildcardCaret(versionDescriptor)
                versionDescriptor.isPatchWildcard -> fromPatchWildcardCaret(versionDescriptor)
                else -> {
                    val version = Version(
                        versionDescriptor.major,
                        versionDescriptor.minor,
                        versionDescriptor.patch,
                        versionDescriptor.preRelease,
                        versionDescriptor.buildMetadata
                    )

                    val endVersion = when {
                        versionDescriptor.majorString != "0" -> version.nextMajor(preRelease = "")
                        versionDescriptor.minorString != "0" -> version.nextMinor(preRelease = "")
                        versionDescriptor.patchString != "0" -> version.nextPatch(preRelease = "")
                        else -> Version(patch = 1, preRelease = "") // ^0.0.0 -> <0.0.1-0
                    }

                    Range(
                        start = Condition(Op.GREATER_THAN_OR_EQUAL, version),
                        end = Condition(Op.LESS_THAN, endVersion),
                        Op.EQUAL
                    )
                }
            }

        private fun fromMinorWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
            when (versionDescriptor.majorString) {
                "0" ->
                    Range(
                        greaterThanMin,
                        Condition(Op.LESS_THAN, Version(major = 1, preRelease = "")),
                        Op.EQUAL
                    )
                else -> versionDescriptor.toComparator()
            }

        private fun fromPatchWildcardCaret(versionDescriptor: VersionDescriptor): VersionComparator =
            when {
                versionDescriptor.majorString == "0" && versionDescriptor.minorString == "0" ->
                    Range(
                        greaterThanMin,
                        Condition(Op.LESS_THAN, Version(minor = 1, preRelease = "")),
                        Op.EQUAL
                    )
                versionDescriptor.majorString != "0" -> {
                    val version = Version(major = versionDescriptor.major, minor = versionDescriptor.minor)
                    Range(
                        Condition(Op.GREATER_THAN_OR_EQUAL, version),
                        Condition(Op.LESS_THAN, version.nextMajor(preRelease = "")),
                        Op.EQUAL
                    )
                }
                else -> versionDescriptor.toComparator()
            }
    }
}
