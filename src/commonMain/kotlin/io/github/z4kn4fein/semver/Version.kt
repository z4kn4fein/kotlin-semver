package io.github.z4kn4fein.semver

private const val VERSION_REGEX: String = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"

public class Version private constructor(public val major: Int,
                                         public val minor: Int,
                                         public val patch: Int,
                                         public val preRelease: PreRelease? = null,
                                         public val buildMetadata: String? = null) : Comparable<Version> {

    public val isPreRelease: Boolean = preRelease != null

    public fun nextMajor(): Version {
        return Version(major + 1, 0, 0)
    }

    public fun nextMinor(): Version {
        return Version(major, minor + 1, 0)
    }

    public fun nextPatch(): Version {
        return Version(major, minor, if (isPreRelease) patch else patch + 1)
    }

    public fun nextPreRelease(): Version {
        return Version(
            major,
            minor,
            if (isPreRelease) patch else patch + 1,
            if (isPreRelease) preRelease!!.increment() else PreRelease.default()
        )
    }

    override fun compareTo(other: Version): Int {
        return 0
    }

    override fun toString(): String {
        return "$major.$minor.$patch${if (preRelease != null) "-$preRelease" else ""}${if (buildMetadata != null) "+$buildMetadata" else ""}"
    }

    public companion object {
        private val versionRegex: Regex = VERSION_REGEX.toRegex()

        public fun parse(versionText: String) : Version {
            val result = versionRegex.matchEntire(versionText) ?: throw VersionFormatException(versionText)
            val major = result.groups[0]?.value?.toInt() ?: throw VersionFormatException(versionText)
            val minor = result.groups[1]?.value?.toInt() ?: throw VersionFormatException(versionText)
            val patch = result.groups[2]?.value?.toInt() ?: throw VersionFormatException(versionText)
            val preRelease = result.groups[3]?.value?.toPreRelease()
            val buildMetadata = result.groups[4]?.value

            return Version(major, minor, patch, preRelease, buildMetadata)
        }
    }
}

public fun String.toVersion(): Version {
    return Version.parse(this)
}