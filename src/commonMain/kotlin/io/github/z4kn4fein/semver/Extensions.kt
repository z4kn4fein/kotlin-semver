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
 * Increments the version by its MAJOR number. When the [preRelease] parameter is set, a pre-release version
 * will be produced from the next MAJOR version. The value of [preRelease] will be the first
 * pre-release identifier of the new version suffixed with a `0`.
 *
 * Returns a new version while the original remains unchanged.
 */
public fun Version.nextMajor(preRelease: String? = null): Version = Version(
    major + 1,
    0,
    0,
    preRelease?.let { PreRelease.default(preRelease) }
)

/**
 * Increments the version by its MINOR number. When the [preRelease] parameter is set, a pre-release version
 * will be produced from the next MINOR version. The value of [preRelease] will be the first
 * pre-release identifier of the new version suffixed with a `0`.
 *
 * Returns a new version while the original remains unchanged.
 */
public fun Version.nextMinor(preRelease: String? = null): Version = Version(
    major,
    minor + 1,
    0,
    preRelease?.let { PreRelease.default(preRelease) }
)

/**
 * Increments the version by its PATCH number. When the version is pre-release, the PATCH number will not be
 * incremented, only the pre-release identifier will be removed.
 *
 * When the [preRelease] parameter is set, a pre-release version will be produced from the next PATCH version.
 * The value of [preRelease] will be the first pre-release identifier of the new version suffixed with a `0`.
 *
 * Returns a new version while the original remains unchanged.
 */
public fun Version.nextPatch(preRelease: String? = null): Version = Version(
    major,
    minor,
    if (parsedPreRelease == null || preRelease != null) patch + 1 else patch,
    preRelease?.let { PreRelease.default(preRelease) }
)

/**
 * Increments the version by its PRE-RELEASE identifier or produces the next pre-release of a stable version.
 * The [preRelease] parameter's value is used for setting the pre-release identity when the version is stable or has
 * a different pre-release name. If the version is already pre-release and the first identifier matches with
 * the [preRelease] parameter, a simple incrementation will apply.
 *
 * Returns a new version while the original remains unchanged.
 */
public fun Version.nextPreRelease(preRelease: String? = null): Version = Version(
    major,
    minor,
    parsedPreRelease?.let { patch } ?: (patch + 1),
    preRelease?.let {
        if (parsedPreRelease?.identity == it) parsedPreRelease.increment() else PreRelease.default(preRelease)
    }
        ?: parsedPreRelease?.increment()
        ?: PreRelease.default(preRelease)
)

/**
 * Constructs a copy of the [Version]. The copied object's properties can be altered with the optional parameters.
 */
public fun Version.copy(
    major: Int = this.major,
    minor: Int = this.minor,
    patch: Int = this.patch,
    preRelease: String? = this.preRelease,
    buildMetadata: String? = this.buildMetadata
): Version = Version(major, minor, patch, preRelease, buildMetadata)

/**
 * Increases the version [by] its [Inc.MAJOR], [Inc.MINOR], [Inc.PATCH], or [Inc.PRE_RELEASE] segment.
 *
 * [Inc.MAJOR] -> [nextMajor]
 *
 * [Inc.MINOR] -> [nextMinor]
 *
 * [Inc.PATCH] -> [nextPatch]
 *
 * [Inc.PRE_RELEASE] -> [nextPreRelease]
 *
 * Returns a new version while the original remains unchanged.
 */
public fun Version.inc(by: Inc, preRelease: String? = null): Version =
    when (by) {
        Inc.MAJOR -> nextMajor(preRelease)
        Inc.MINOR -> nextMinor(preRelease)
        Inc.PATCH -> nextPatch(preRelease)
        Inc.PRE_RELEASE -> nextPreRelease(preRelease)
    }
