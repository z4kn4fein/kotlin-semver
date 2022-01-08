package io.github.z4kn4fein.semver

/**
 * This class describes a semantic version and related operations.
 */
public class Version private constructor(
    /** The MAJOR number of the version. */
    public val major: Int,

    /** The MINOR number of the version. */
    public val minor: Int,

    /** The PATCH number of the version. */
    public val patch: Int,

    internal val parsedPreRelease: PreRelease? = null,

    /** The BUILD-METADATA part of the version. */
    public val buildMetadata: String? = null
) : Comparable<Version> {

    /**
     * Constructs a semantic version from the given arguments following the pattern:
     * <[major]>.<[minor]>.<[patch]>-<[preRelease]>+<[buildMetadata]>
     */
    public constructor(
        major: Int,
        minor: Int,
        patch: Int,
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

    /** The PRE-RELEASE part of the version. */
    public val preRelease: String? = parsedPreRelease?.toString()

    /**
     * Returns true when the version is a pre-release version.
     */
    public val isPreRelease: Boolean = parsedPreRelease != null

    /**
     * Constructs a copy of the [Version]. The copied object's properties can be altered with the optional parameters.
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

    public operator fun component1(): Int = major
    public operator fun component2(): Int = minor
    public operator fun component3(): Int = patch
    public operator fun component4(): String? = preRelease
    public operator fun component5(): String? = buildMetadata

    public companion object {
        private const val VERSION_REGEX: String = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
            "(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?" +
            "(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"
        private val versionRegex: Regex = VERSION_REGEX.toRegex()

        /**
         * Parses the [versionText] as a [Version] and returns the result or throws a [VersionFormatException]
         * if the string is not a valid representation of a semantic version.
         */
        public fun parse(versionText: String): Version {
            val result = versionRegex.matchEntire(versionText)
            val major = result?.groups?.get(1)?.value?.toIntOrNull()
            val minor = result?.groups?.get(2)?.value?.toIntOrNull()
            val patch = result?.groups?.get(3)?.value?.toIntOrNull()
            val preRelease = result?.groups?.get(4)?.value
            val buildMetadata = result?.groups?.get(5)?.value

            if (major == null || minor == null || patch == null) {
                throw VersionFormatException("Invalid version: $versionText")
            }

            return Version(major, minor, patch, preRelease, buildMetadata)
        }

        internal operator fun invoke(
            major: Int,
            minor: Int,
            patch: Int,
            preRelease: PreRelease?,
            buildMetadata: String? = null
        ): Version = Version(major, minor, patch, preRelease, buildMetadata)
    }
}
