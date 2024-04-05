package io.github.z4kn4fein.semver

/**
 * Parses the string as a [Version] and returns the result or throws a [VersionFormatException]
 * if the string is not a valid representation of a semantic version.
 *
 * Strict mode is on by default, which means partial versions (e.g. '1.0' or '1') and versions with 'v' prefix are
 * considered invalid. This behaviour can be turned off by setting [strict] to false.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.toVersionStrict
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.toVersionLoose
 */
public fun String.toVersion(strict: Boolean = true): Version = Version.parse(this, strict)

/**
 * Parses the string as a [Version] and returns the result or null
 * if the string is not a valid representation of a semantic version.
 *
 * Strict mode is on by default, which means partial versions (e.g. '1.0' or '1') and versions with 'v' prefix are
 * considered invalid. This behaviour can be turned off by setting [strict] to false.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.toVersionOrNullStrict
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.toVersionOrNullLoose
 */
public fun String.toVersionOrNull(strict: Boolean = true): Version? =
    try {
        this.toVersion(strict)
    } catch (_: Exception) {
        null
    }

internal fun String.toPreRelease(): PreRelease = PreRelease(this)
