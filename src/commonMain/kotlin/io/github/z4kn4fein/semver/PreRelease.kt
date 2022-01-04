package io.github.z4kn4fein.semver

public class PreRelease constructor(private val preReleaseText: String) {

    public fun increment(): PreRelease {
        return PreRelease("")
    }

    public companion object {
        public fun parse(preReleaseText: String): PreRelease {
            return PreRelease(preReleaseText)
        }

        public fun default(): PreRelease {
            return PreRelease("0")
        }
    }
}

public fun String.toPreRelease(): PreRelease {
    return PreRelease.parse(this)
}