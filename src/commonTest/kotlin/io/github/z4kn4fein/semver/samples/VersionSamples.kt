package io.github.z4kn4fein.semver.samples

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.inc
import io.github.z4kn4fein.semver.nextMajor
import io.github.z4kn4fein.semver.nextMinor
import io.github.z4kn4fein.semver.nextPatch
import io.github.z4kn4fein.semver.nextPreRelease
import io.github.z4kn4fein.semver.satisfies
import io.github.z4kn4fein.semver.satisfiesAll
import io.github.z4kn4fein.semver.satisfiesAny
import io.github.z4kn4fein.semver.toVersion
import io.github.z4kn4fein.semver.toVersionOrNull

class VersionSamples {
    fun explode() {
        val version = "1.2.3-alpha.1+build.1".toVersion()
        println("Version: $version")
        println("Major: ${version.major}, Minor: ${version.minor}, Patch: ${version.patch}")
        println("Pre-release: ${version.preRelease}")
        println("Build metadata: ${version.buildMetadata}")
        println("Is it pre-release? ${version.isPreRelease}")
        println("Is it stable? ${version.isStable}")

        // equality
        println("Is 1.0.0 == 1.0.0? ${"1.0.0".toVersion() == "1.0.0".toVersion()}")
        println("Is 1.0.0 == 1.0.1? ${"1.0.0".toVersion() == "1.0.1".toVersion()}")

        // comparison
        println("Is 1.0.1 > 1.0.0? ${"1.0.1".toVersion() > "1.0.0".toVersion()}")
        println("Is 1.0.0-alpha.1 > 1.0.0-alpha.0? ${"1.0.0-alpha.1".toVersion() > "1.0.0-alpha.0".toVersion()}")

        // range
        println("Is 1.0.1 in 1.0.0 .. 1.0.2? ${"1.0.1".toVersion() in "1.0.0".toVersion().."1.0.2".toVersion()}")

        // destructuring
        print("Destructuring: ")
        val (major, minor, patch, preRelease, build) = "1.0.0-alpha+build".toVersion()
        print("$major $minor $patch $preRelease $build")
    }

    fun parse() {
        print(Version.parse("1.0.0-alpha.1+build.1"))
    }

    fun exception() {
        "1.0.a".toVersion()
    }

    fun construct() {
        println(Version(major = 1, preRelease = "alpha"))
        println(Version(major = 1, minor = 1, buildMetadata = "build"))
    }

    fun toVersion() {
        print("1.0.0-alpha.1+build.1".toVersion())
    }

    fun toVersionOrNull() {
        println("1.0.0-alpha.1+build.1".toVersionOrNull())
        println("1.1.a".toVersionOrNull())
    }

    fun copy() {
        val version = "1.0.0-alpha.1".toVersion()
        print(version.copy(minor = 1, preRelease = "beta.0"))
    }

    fun inc() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.inc(by = Inc.MAJOR))
        println(version.inc(by = Inc.MINOR))
        println(version.inc(by = Inc.PATCH))
        println(version.inc(by = Inc.PRE_RELEASE))

        println(version.inc(by = Inc.MAJOR, preRelease = ""))
        println(version.inc(by = Inc.MINOR, preRelease = "beta"))
    }

    fun nextMajor() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextMajor())
        println(version.nextMajor(preRelease = ""))
        println(version.nextMajor(preRelease = "alpha"))
    }

    fun nextMinor() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextMinor())
        println(version.nextMinor(preRelease = ""))
        println(version.nextMinor(preRelease = "alpha"))
    }

    fun nextPatch() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextPatch())
        println(version.nextPatch(preRelease = ""))
        println(version.nextPatch(preRelease = "alpha"))
    }

    fun nextPreRelease() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextPreRelease())
        println(version.nextPreRelease(preRelease = ""))
        println(version.nextPreRelease(preRelease = "alpha"))
    }

    fun min() {
        print(Version.min)
    }

    fun satisfies() {
        val constraint = ">=1.1.0".toConstraint()
        val version = "1.1.1".toVersion()
        print("$version satisfies $constraint? ${version satisfies constraint}")
    }

    fun satisfiesAll() {
        val constraints = listOf(">=1.1.0", "~1").map { it.toConstraint() }
        val version = "1.1.1".toVersion()
        print("$version satisfies ${constraints.joinToString(" and ")}? ${version satisfiesAll constraints}")
    }

    fun satisfiesAny() {
        val constraints = listOf(">=1.1.0", "~1").map { it.toConstraint() }
        val version = "1.1.1".toVersion()
        print("$version satisfies ${constraints.joinToString(" or ")}? ${version satisfiesAny constraints}")
    }
}
