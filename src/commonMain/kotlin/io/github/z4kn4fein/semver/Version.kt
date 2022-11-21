package io.github.z4kn4fein.semver

import kotlinx.serialization.Serializable

/**
 * This class describes a semantic version and related operations following the semver 2.0.0 specification.
 * Instances of this class are immutable, which makes them thread-safe.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.explode
 */
@Serializable(with = VersionSerializer::class)
public class Version private constructor(
    /** The MAJOR number of the version. */
    public val major: Int,

    /** The MINOR number of the version. */
    public val minor: Int,

    /** The PATCH number of the version. */
    public val patch: Int,

    internal val parsedPreRelease: PreRelease? = null,

    /** The BUILD metadata of the version. */
    public val buildMetadata: String? = null
) : Comparable<Version> {

    /**
     * Constructs a semantic version from the given arguments following the pattern:
     * <[major]>.<[minor]>.<[patch]>-<[preRelease]>+<[buildMetadata]>
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.construct
     */
    public constructor(
        major: Int = 0,
        minor: Int = 0,
        patch: Int = 0,
        preRelease: String? = null,
        buildMetadata: String? = null
    ) : this(major, minor, patch, preRelease?.toPreRelease(), buildMetadata)

    init {
        when {
            major < 0 -> throw VersionFormatException("The major number must be >= 0.")
            minor < 0 -> throw VersionFormatException("The minor number must be >= 0.")
            patch < 0 -> throw VersionFormatException("The patch number must be >= 0.")
        }
    }

    /** The PRE-RELEASE identifier of the version. */
    public val preRelease: String? = parsedPreRelease?.toString()

    /**
     * Determines whether the version is pre-release or not.
     */
    public val isPreRelease: Boolean = parsedPreRelease != null

    /**
     * Determines whether the version is considered stable or not.
     * Stable versions have a positive major number and no pre-release identifier.
     */
    public val isStable: Boolean = major > 0 && parsedPreRelease == null

    /**
     * Constructs a copy of the [Version]. The copied object's properties can be altered with the optional parameters.
     *
     * @sample io.github.z4kn4fein.semver.samples.VersionSamples.copy
     */
    public fun copy(
        major: Int = this.major,
        minor: Int = this.minor,
        patch: Int = this.patch,
        preRelease: String? = this.preRelease,
        buildMetadata: String? = this.buildMetadata
    ): Version = Version(major, minor, patch, preRelease, buildMetadata)

    public override fun compareTo(other: Version): Int =
        when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            parsedPreRelease != null && other.parsedPreRelease == null -> -1
            parsedPreRelease == null && other.parsedPreRelease != null -> 1
            parsedPreRelease != null && other.parsedPreRelease != null ->
                parsedPreRelease.compareTo(other.parsedPreRelease)
            else -> 0
        }

    public override fun equals(other: Any?): Boolean {
        val version = other as? Version
        return when {
            version == null -> false
            compareTo(version) == 0 -> true
            else -> false
        }
    }

    public override fun hashCode(): Int {
        var hash = major.hashCode()
        hash *= 31 + minor.hashCode()
        hash *= 31 + patch.hashCode()
        hash *= parsedPreRelease?.let { 31 + parsedPreRelease.hashCode() } ?: 1
        return hash
    }

    public override fun toString(): String =
        "$major.$minor.$patch${parsedPreRelease?.let { "-$parsedPreRelease" } ?: ""}" +
            (buildMetadata?.let { "+$buildMetadata" } ?: "")

    /** Component function that returns the MAJOR number of the version upon destructuring. */
    public operator fun component1(): Int = major
    /** Component function that returns the MINOR number of the version upon destructuring. */
    public operator fun component2(): Int = minor
    /** Component function that returns the PATCH number of the version upon destructuring. */
    public operator fun component3(): Int = patch
    /** Component function that returns the PRE-RELEASE identifier of the version upon destructuring. */
    public operator fun component4(): String? = preRelease
    /** Component function that returns the BUILD metadata of the version upon destructuring. */
    public operator fun component5(): String? = buildMetadata

    /** Companion object of [Version]. */
    public companion object {
        private val versionRegex: Regex = Patterns.VERSION_REGEX.toRegex()
        private val looseVersionRegex: Regex = Patterns.LOOSE_VERSION_REGEX.toRegex()

        /**
         * The 0.0.0 semantic version.
         *
         * @sample io.github.z4kn4fein.semver.samples.VersionSamples.min
         */
        public val min: Version = Version()

        /**
         * Parses the [versionString] as a [Version] and returns the result or throws a [VersionFormatException]
         * if the string is not a valid representation of a semantic version.
         *
         * Strict mode is on by default, which means partial versions (e.g. '1.0' or '1') and versions with 'v' prefix
         * are considered invalid. This behaviour can be turned off by setting [strict] to false.
         *
         * @sample io.github.z4kn4fein.semver.samples.VersionSamples.parseStrict
         * @sample io.github.z4kn4fein.semver.samples.VersionSamples.parseLoose
         */
        @Suppress("MagicNumber")
        public fun parse(versionString: String, strict: Boolean = true): Version {
            val regex = if (strict) versionRegex else looseVersionRegex
            val result = regex.matchEntire(versionString)
                ?: throw VersionFormatException("Invalid version: $versionString")
            val major = result.groupValues[1].toIntOrNull()
            val minor = result.groupValues[2].toIntOrNull()
            val patch = result.groupValues[3].toIntOrNull()
            val preRelease = result.groups[4]?.value
            val buildMetadata = result.groups[5]?.value

            return when {
                strict && major != null && minor != null && patch != null ->
                    Version(major, minor, patch, preRelease, buildMetadata)
                !strict && major != null ->
                    Version(major, minor ?: 0, patch ?: 0, preRelease, buildMetadata)
                else -> throw VersionFormatException("Invalid version: $versionString")
            }
        }

        // used by extensions only
        internal operator fun invoke(
            major: Int,
            minor: Int,
            patch: Int,
            preRelease: PreRelease?,
            buildMetadata: String? = null
        ): Version = Version(major, minor, patch, preRelease, buildMetadata)
    }
}
