package io.github.z4kn4fein.semver.constraints

/**
 * [Constraint] throws this exception when the constraint parsing fails due to an invalid format.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.exception
 */
public class ConstraintFormatException(message: String) : Exception(message)
