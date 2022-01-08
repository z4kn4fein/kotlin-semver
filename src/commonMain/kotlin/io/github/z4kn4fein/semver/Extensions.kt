package io.github.z4kn4fein.semver

internal fun String.toPreRelease(): PreRelease = PreRelease(this)

/**
 * Parses the string as a [Version] and returns the result or throws a [VersionFormatException]
 * if the string is not a valid representation of a semantic version.
 */
public fun String.toVersion(): Version = Version.parse(this)

/**
 * Parses the string as a [Version] and returns the result or `null`
 * if the string is not a valid representation of a semantic version.
 */
public fun String.toVersionOrNull(): Version? = try { this.toVersion() } catch (_: Exception) { null }

/**
 * Increments the version by its MAJOR number. Returns a new version while the original remains unchanged.
 */
public fun Version.nextMajor(): Version = Version(major + 1, 0, 0)

/**
 * Increments the version by its MINOR number. Returns a new version while the original remains unchanged.
 */
public fun Version.nextMinor(): Version = Version(major, minor + 1, 0)

/**
 * Increments the version by its PATCH number. Returns a new version while the original remains unchanged.
 */
public fun Version.nextPatch(): Version = Version(major, minor, preRelease?.let { patch } ?: (patch + 1))

/**
 * Increments the version by its PRE-RELEASE part. Returns a new version while the original remains unchanged.
 */
public fun Version.nextPreRelease(): Version = Version(
    major,
    minor,
    parsedPreRelease?.let { patch } ?: (patch + 1),
    parsedPreRelease?.increment() ?: PreRelease.default()
)
