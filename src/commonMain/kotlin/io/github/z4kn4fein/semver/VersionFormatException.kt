package io.github.z4kn4fein.semver

/**
 * [Version] throws this exception when the parsing fails due to an invalid format.
 */
public class VersionFormatException(message: String) : Exception(message)
