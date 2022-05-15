package io.github.z4kn4fein.semver.samples

import io.github.z4kn4fein.semver.Inc
import io.github.z4kn4fein.semver.LooseVersionSerializer
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
import io.github.z4kn4fein.semver.withoutSuffixes
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    fun parseStrict() {
        println(Version.parse("1.0.0-alpha.1+build.1"))
    }

    fun parseLoose() {
        println(Version.parse("v1.0-alpha.1+build.1", strict = false))
        println(Version.parse("1-alpha", strict = false))
        println(Version.parse("2", strict = false))
    }

    fun exception() {
        "1.0.a".toVersion()
    }

    fun preReleaseException() {
        "1.0.1".toVersion().nextPatch(preRelease = "alpha.01")
    }

    fun construct() {
        println(Version(major = 1, preRelease = "alpha"))
        println(Version(major = 1, minor = 1, buildMetadata = "build"))
    }

    fun toVersionStrict() {
        print("1.0.0-alpha.1+build.1".toVersion())
    }

    fun toVersionLoose() {
        println("v1.0.0-alpha.1+build.1".toVersion(strict = false))
        println("v1-alpha".toVersion(strict = false))
        println("2".toVersion(strict = false))
    }

    fun toVersionOrNullStrict() {
        println("1.0.0-alpha.1+build.1".toVersionOrNull())
        println("1.1.a".toVersionOrNull())
        println("v1.1.0".toVersionOrNull())
        println("1.1".toVersionOrNull())
    }

    fun toVersionOrNullLoose() {
        println("v1.1.0".toVersionOrNull(strict = false))
        println("1.1-alpha.1+build.1".toVersionOrNull(strict = false))
        println("1".toVersionOrNull(strict = false))
        println("v1".toVersionOrNull(strict = false))
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
        println(version.nextMajor(preRelease = "SNAPSHOT"))
    }

    fun nextMinor() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextMinor())
        println(version.nextMinor(preRelease = ""))
        println(version.nextMinor(preRelease = "alpha"))
        println(version.nextMinor(preRelease = "SNAPSHOT"))
    }

    fun nextPatch() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextPatch())
        println(version.nextPatch(preRelease = ""))
        println(version.nextPatch(preRelease = "alpha"))
        println(version.nextPatch(preRelease = "SNAPSHOT"))
    }

    fun nextPreRelease() {
        val version = "1.0.0-alpha.1".toVersion()
        println(version.nextPreRelease())
        println(version.nextPreRelease(preRelease = ""))
        println(version.nextPreRelease(preRelease = "alpha"))
        println(version.nextPreRelease(preRelease = "SNAPSHOT"))
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

    fun withoutSuffixes() {
        val version = "1.0.0-alpha.1+build".toVersion()
        print(version.withoutSuffixes())
    }

    fun serialization() {
        @Serializable
        data class Data(val version: Version)

        val data = Data(version = "1.0.0-alpha.1+build".toVersion())
        print(Json.encodeToString(data))
    }

    fun deserialization() {
        @Serializable
        data class Data(val version: Version)

        val decoded = Json.decodeFromString<Data>("{\"version\":\"1.0.0-alpha.1+build\"}")
        print(decoded.version)
    }

    fun looseDeserialization() {
        @Serializable
        data class Data(
            @Serializable(with = LooseVersionSerializer::class)
            val version: Version
        )

        val decoded = Json.decodeFromString<Data>("{\"version\":\"1+build.3\"}")
        print(decoded.version)
    }
}
