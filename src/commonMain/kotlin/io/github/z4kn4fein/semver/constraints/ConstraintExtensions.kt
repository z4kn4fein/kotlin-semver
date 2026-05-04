package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

/**
 * Determines whether the [Constraint] is satisfied by a [Version].
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedBy
 */
public infix fun Constraint.satisfiedBy(version: Version): Boolean = this.isSatisfiedBy(version)

/**
 * Determines whether the [Constraint] is satisfied by each [Version] in a collection.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedByAll
 */
public infix fun Constraint.satisfiedByAll(versions: Iterable<Version>): Boolean =
    versions.all {
            version ->
        this.isSatisfiedBy(version)
    }

/**
 * Determines whether the [Constraint] is satisfied by at least one [Version] in a collection.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedByAny
 */
public infix fun Constraint.satisfiedByAny(versions: Iterable<Version>): Boolean =
    versions.any {
            version ->
        this.isSatisfiedBy(version)
    }

/**
 * Converts the current [Constraint] instance into a Maven-compatible string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toMavenFormat
 */
public fun Constraint.toMavenFormat(): String = this.format(MavenStyleFormatter())

/**
 * Parses the string as a [Constraint] and returns the result or throws a [ConstraintFormatException]
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toConstraint
 */
@Throws(ConstraintFormatException::class)
public fun String.toConstraint(): Constraint = Constraint.parse(this)

/**
 * Parses the string as a [Constraint] and returns the result or null
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toConstraintOrNull
 */
public fun String.toConstraintOrNull(): Constraint? =
    try {
        this.toConstraint()
    } catch (_: Exception) {
        null
    }

/**
 * Parses a maven range string as a [Constraint] and returns the result or throws a [ConstraintFormatException]
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toMavenConstraint
 */
@Throws(ConstraintFormatException::class)
public fun String.toMavenConstraint(): Constraint = Constraint.parseFormat(this, MavenStyleParser())

/**
 * Parses a maven range string as a [Constraint] and returns the result or null
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toMavenConstraintOrNull
 */
public fun String.toMavenConstraintOrNull(): Constraint? =
    try {
        this.toMavenConstraint()
    } catch (_: Exception) {
        null
    }
