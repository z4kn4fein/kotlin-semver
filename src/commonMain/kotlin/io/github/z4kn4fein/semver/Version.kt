package io.github.z4kn4fein.semver

private const val VERSION_REGEX: String = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"

public class Version(
    public val major: Int,
    public val minor: Int,
    public val patch: Int,
    public val preRelease: String? = null,
    public val buildMetadata: String? = null
) : Comparable<Version> {

    init {
        when {
            major < 0 -> throw VersionFormatException("The major number must be >= 0.")
            minor < 0 -> throw VersionFormatException("The minor number must be >= 0.")
            patch < 0 -> throw VersionFormatException("The patch number must be >= 0.")
        }
    }

    public val isStable: Boolean = preRelease == null && buildMetadata == null

    private val parsedPreRelease: PreRelease? = preRelease?.toPreRelease()

    public fun nextMajor(): Version {
        return Version(major + 1, 0, 0)
    }

    public fun nextMinor(): Version {
        return Version(major, minor + 1, 0)
    }

    public fun nextPatch(): Version {
        return Version(major, minor, if (parsedPreRelease != null) patch else patch + 1)
    }

    public fun nextPreRelease(): Version {
        return Version(
            major,
            minor,
            if (parsedPreRelease != null) patch else patch + 1,
            parsedPreRelease?.increment()?.toString() ?: PreRelease.default().toString()
        )
    }

    public fun clone(
        major: Int = this.major,
        minor: Int = this.minor,
        patch: Int = this.patch,
        preRelease: String? = this.preRelease,
        buildMetadata: String? = this.buildMetadata
    ): Version = Version(major, minor, patch, preRelease, buildMetadata)

    override fun compareTo(other: Version): Int {
        return when {
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
    }

    override fun equals(other: Any?): Boolean {
        val version = other as? Version
        return when {
            version == null -> false
            compareTo(version) == 0 -> true
            else -> false
        }
    }

    override fun hashCode(): Int {
        var hash = major.hashCode()
        hash *= 31 + minor.hashCode()
        hash *= 31 + patch.hashCode()
        hash *= if (parsedPreRelease != null) 31 + parsedPreRelease.hashCode() else 1
        return hash
    }

    override fun toString(): String {
        return "$major.$minor.$patch${if (parsedPreRelease != null) "-$parsedPreRelease" else ""}${if (buildMetadata != null) "+$buildMetadata" else ""}"
    }

    public companion object {
        private val versionRegex: Regex = VERSION_REGEX.toRegex()

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

public fun String.toVersion(): Version {
    return Version.parse(this)
}
