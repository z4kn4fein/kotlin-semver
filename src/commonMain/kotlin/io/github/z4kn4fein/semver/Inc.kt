package io.github.z4kn4fein.semver

/**
 * Determines by which identifier the given [Version] should be incremented.
 */
public enum class Inc {
    /** Indicates that the [Version] should be incremented by its MAJOR number. */
    MAJOR,
    /** Indicates that the [Version] should be incremented by its MINOR number. */
    MINOR,
    /** Indicates that the [Version] should be incremented by its PATCH number. */
    PATCH,
    /** Indicates that the [Version] should be incremented by its PRE-RELEASE identifier. */
    PRE_RELEASE
}
