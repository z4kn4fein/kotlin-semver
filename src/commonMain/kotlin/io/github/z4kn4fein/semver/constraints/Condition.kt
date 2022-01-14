package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

internal class Condition(private val operator: Op, private val version: Version) : VersionComparator {

    override fun isSatisfiedBy(version: Version): Boolean {
        return when (operator) {
            Op.EQUAL -> version == this.version
            Op.NOT_EQUAL -> version != this.version
            Op.LOWER_THAN -> version < this.version
            Op.LOWER_THAN_OR_EQUAL -> version <= this.version
            Op.GREATER_THAN -> version > this.version
            Op.GREATER_THAN_OR_EQUAL -> version >= this.version
        }
    }

    override fun opposite(): String {
        return when (operator) {
            Op.EQUAL -> "${Op.NOT_EQUAL}$version"
            Op.NOT_EQUAL -> "${Op.EQUAL}$version"
            Op.LOWER_THAN -> "${Op.GREATER_THAN_OR_EQUAL}$version"
            Op.LOWER_THAN_OR_EQUAL -> "${Op.GREATER_THAN}$version"
            Op.GREATER_THAN -> "${Op.LOWER_THAN_OR_EQUAL}$version"
            Op.GREATER_THAN_OR_EQUAL -> "${Op.LOWER_THAN}$version"
        }
    }

    override fun toString(): String {
        return "$operator$version"
    }
}
