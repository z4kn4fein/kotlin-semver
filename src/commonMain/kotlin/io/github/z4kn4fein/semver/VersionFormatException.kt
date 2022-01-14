package io.github.z4kn4fein.semver

/**
 * [Version] throws this exception when the semantic version parsing fails due to an invalid format.
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.exception
 */
public class VersionFormatException(message: String) : Exception(message)
