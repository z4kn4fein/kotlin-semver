package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.satisfiedBy

/**
 * Increments the version by its MAJOR number. When the [preRelease] parameter is set, a pre-release version
 * will be produced from the next MAJOR version. The value of [preRelease] will be the first
 * pre-release identifier of the new version.
 *
 * Returns a new version while the original remains unchanged.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.nextMajor
 */
public fun Version.nextMajor(preRelease: String? = null): Version =
    Version(
        major + 1,
        0,
        0,
        preRelease?.let { PreRelease(preRelease) },
    )

/**
 * Increments the version by its MINOR number. When the [preRelease] parameter is set, a pre-release version
 * will be produced from the next MINOR version. The value of [preRelease] will be the first
 * pre-release identifier of the new version.
 *
 * Returns a new version while the original remains unchanged.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.nextMinor
 */
public fun Version.nextMinor(preRelease: String? = null): Version =
    Version(
        major,
        minor + 1,
        0,
        preRelease?.let { PreRelease(preRelease) },
    )

/**
 * Increments the version by its PATCH number. When the version is pre-release, the PATCH number will not be
 * incremented, only the pre-release identifier will be removed.
 *
 * When the [preRelease] parameter is set, a pre-release version will be produced from the next PATCH version.
 * The value of [preRelease] will be the first pre-release identifier of the new version.
 *
 * Returns a new version while the original remains unchanged.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.nextPatch
 */
public fun Version.nextPatch(preRelease: String? = null): Version =
    Version(
        major,
        minor,
        if (parsedPreRelease == null || preRelease != null) patch + 1 else patch,
        preRelease?.let { PreRelease(preRelease) },
    )

/**
 * Increments the version by its PRE-RELEASE identifier or produces the next pre-release of a stable version.
 * The [preRelease] parameter's value is used for setting the pre-release identity when the version is stable or has
 * a different pre-release name. If the version is already pre-release and the first identifier matches with
 * the [preRelease] parameter, a simple incrementation will apply.
 *
 * Returns a new version while the original remains unchanged.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.nextPreRelease
 */
public fun Version.nextPreRelease(preRelease: String? = null): Version =
    Version(
        major,
        minor,
        parsedPreRelease?.let { patch } ?: (patch + 1),
        preRelease?.let {
            if (parsedPreRelease?.identity == it) parsedPreRelease.increment() else PreRelease(preRelease)
        } ?: parsedPreRelease?.increment() ?: PreRelease.default,
    )

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
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
 */
public fun Version.inc(
    by: Inc,
    preRelease: String? = null,
): Version =
    when (by) {
        Inc.MAJOR -> nextMajor(preRelease)
        Inc.MINOR -> nextMinor(preRelease)
        Inc.PATCH -> nextPatch(preRelease)
        Inc.PRE_RELEASE -> nextPreRelease(preRelease)
    }

/**
 * Produces a copy of the [Version] without the PRE-RELEASE and BUILD METADATA identities.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.withoutSuffixes
 */
public fun Version.withoutSuffixes(): Version = this.copy(preRelease = null, buildMetadata = null)

/**
 * Determines whether a [Version] satisfies a [Constraint] or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.satisfies
 */
public infix fun Version.satisfies(constraint: Constraint): Boolean = constraint satisfiedBy this

/**
 * Determines whether a [Version] satisfies each [Constraint] in a collection or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.satisfiesAll
 */
public infix fun Version.satisfiesAll(constraints: Iterable<Constraint>): Boolean =
    constraints.all { constraint -> constraint satisfiedBy this }

/**
 * Determines whether a [Version] satisfies at least one [Constraint] in a collection or not.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.satisfiesAny
 */
public infix fun Version.satisfiesAny(constraints: Iterable<Constraint>): Boolean =
    constraints.any { constraint -> constraint satisfiedBy this }
