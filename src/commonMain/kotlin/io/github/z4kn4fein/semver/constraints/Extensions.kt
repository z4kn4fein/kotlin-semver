package io.github.z4kn4fein.semver.constraints

import io.github.z4kn4fein.semver.Version

/**
 * Parses the string as a [Constraint] and returns the result or throws a [ConstraintFormatException]
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toConstraint
 */
public fun String.toConstraint(): Constraint = Constraint.parse(this)

/**
 * Parses the string as a [Constraint] and returns the result or null
 * if the string is not a valid representation of a constraint.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.toConstraintOrNull
 */
public fun String.toConstraintOrNull(): Constraint? = try { this.toConstraint() } catch (_: Exception) { null }

/**
 * Determines whether a [Constraint] is satisfied by a [Version] or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedBy
 */
public infix fun Constraint.satisfiedBy(version: Version): Boolean = this.isSatisfiedBy(version)

/**
 * Determines whether a [Constraint] is satisfied by each [Version] in a collection or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedByAll
 */
public infix fun Constraint.satisfiedByAll(versions: Iterable<Version>): Boolean =
    versions.all { version -> this.isSatisfiedBy(version) }

/**
 * Determines whether a [Constraint] is satisfied by at least one [Version] in a collection or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.satisfiedByAny
 */
public infix fun Constraint.satisfiedByAny(versions: Iterable<Version>): Boolean =
    versions.any { version -> this.isSatisfiedBy(version) }
