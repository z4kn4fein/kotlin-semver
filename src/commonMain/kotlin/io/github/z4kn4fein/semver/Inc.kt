package io.github.z4kn4fein.semver

/**
 * Determines by which identifier the given [Version] should be incremented.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
 */
public enum class Inc {
    /**
     * Indicates that the [Version] should be incremented by its MAJOR number.
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
     */
    MAJOR,

    /**
     * Indicates that the [Version] should be incremented by its MINOR number.
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
     */
    MINOR,

    /**
     * Indicates that the [Version] should be incremented by its PATCH number.
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
     */
    PATCH,

    /**
     * Indicates that the [Version] should be incremented by its PRE-RELEASE identifier.
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.inc
     */
    PRE_RELEASE,
}
