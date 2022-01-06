package io.github.z4kn4fein.semver

private const val VERSION_REGEX: String = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"

/**
 * This class describes a semantic version and related operations.
 */
public class Version(
    /** The MAJOR number of the version. */
    public val major: Int,
    /** The MINOR number of the version. */
    public val minor: Int,
    /** The PATCH number of the version. */
    public val patch: Int,
    /** The PRE-RELEASE part of the version. */
    public val preRelease: String? = null,
    /** The BUILD-METADATA part of the version. */
    public val buildMetadata: String? = null
) : Comparable<Version> {

    init {
        when {
            major < 0 -> throw VersionFormatException("The major number must be >= 0.")
            minor < 0 -> throw VersionFormatException("The minor number must be >= 0.")
            patch < 0 -> throw VersionFormatException("The patch number must be >= 0.")
        }
    }

    /** Returns true when the version is considered stable. (When it doesn't have pre-release or build-metadata part.) */
    public val isStable: Boolean = preRelease == null && buildMetadata == null

    private val parsedPreRelease: PreRelease? = preRelease?.toPreRelease()

    /**
     * Increments the version by its MAJOR number. Returns with the new version while the original remains unchanged.
     */
    public fun nextMajor(): Version = Version(major + 1, 0, 0)
    /**
     * Increments the version by its MINOR number. Returns with the new version while the original remains unchanged.
     */
    public fun nextMinor(): Version = Version(major, minor + 1, 0)
    /**
     * Increments the version by its PATCH number. Returns with the new version while the original remains unchanged.
     */
    public fun nextPatch(): Version = Version(major, minor, if (parsedPreRelease != null) patch else patch + 1)
    /**
     * Increments the version by its PRE-RELEASE part. Returns with the new version while the original remains unchanged.
     */
    public fun nextPreRelease(): Version = Version(
        major,
        minor,
        if (parsedPreRelease != null) patch else patch + 1,
        parsedPreRelease?.increment()?.toString() ?: PreRelease.default().toString()
    )

    /**
     * By default, this method returns an exact copy of the version, when none of the optional override
     * parameters are set. Each segment of the copy can be modified with the parameters accordingly.
     */
    public fun clone(
        major: Int = this.major,
        minor: Int = this.minor,
        patch: Int = this.patch,
        preRelease: String? = this.preRelease,
        buildMetadata: String? = this.buildMetadata
    ): Version = Version(major, minor, patch, preRelease, buildMetadata)

    public operator fun component1(): Int = major
    public operator fun component2(): Int = minor
    public operator fun component3(): Int = patch
    public operator fun component4(): String? = preRelease
    public operator fun component5(): String? = buildMetadata

    /**
     * Compares the version with an [other] version.
     */
    override fun compareTo(other: Version): Int =
        when {
            major > other.major -> 1
            major < other.major -> -1
            minor > other.minor -> 1
            minor < other.minor -> -1
            patch > other.patch -> 1
            patch < other.patch -> -1
            parsedPreRelease != null && other.parsedPreRelease == null -> -1
            parsedPreRelease == null && other.parsedPreRelease != null -> 1
            parsedPreRelease != null && other.parsedPreRelease != null -> parsedPreRelease.compareTo(other.parsedPreRelease)
            else -> 0
        }

    /**
     * Checks the version's equality with an [other] version.
     */
    override fun equals(other: Any?): Boolean {
        val version = other as? Version
        return when {
            version == null -> false
            compareTo(version) == 0 -> true
            else -> false
        }
    }

    /**
     * Generates the hash of the version for equality check.
     */
    override fun hashCode(): Int {
        var hash = major.hashCode()
        hash *= 31 + minor.hashCode()
        hash *= 31 + patch.hashCode()
        hash *= if (parsedPreRelease != null) 31 + parsedPreRelease.hashCode() else 1
        return hash
    }

    /**
     * Produces the string representation of the version.
     */
    override fun toString(): String =
        "$major.$minor.$patch${if (parsedPreRelease != null) "-$parsedPreRelease" else ""}${if (buildMetadata != null) "+$buildMetadata" else ""}"

    public companion object {
        private val versionRegex: Regex = VERSION_REGEX.toRegex()

        /**
         * Parses the [versionText] string into a semantic version [Version].
         */
        public fun parse(versionText: String): Version {
            val result = versionRegex.matchEntire(versionText) ?: throw VersionFormatException("Invalid version: $versionText")
            val major = result.groups[1]?.value?.toIntOrNull() ?: throw VersionFormatException("Invalid version: $versionText")
            val minor = result.groups[2]?.value?.toIntOrNull() ?: throw VersionFormatException("Invalid version: $versionText")
            val patch = result.groups[3]?.value?.toIntOrNull() ?: throw VersionFormatException("Invalid version: $versionText")
            val preRelease = result.groups[4]?.value
            val buildMetadata = result.groups[5]?.value

            return Version(major, minor, patch, preRelease, buildMetadata)
        }
    }
}

/**
 * Converts the [String] into a semantic version [Version].
 */
public fun String.toVersion(): Version = Version.parse(this)
