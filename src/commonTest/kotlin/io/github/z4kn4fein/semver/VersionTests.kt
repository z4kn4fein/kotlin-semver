package io.github.z4kn4fein.semver

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class VersionTests {
    @Test
    fun testInvalidVersions() {
        shouldThrow<VersionFormatException> { "-1.0.0".toVersion() }
        shouldThrow<VersionFormatException> { "1.-1.0".toVersion() }
        shouldThrow<VersionFormatException> { "0.0.-1".toVersion() }
        shouldThrow<VersionFormatException> { "1".toVersion() }
        shouldThrow<VersionFormatException> { "1.0".toVersion() }
        shouldThrow<VersionFormatException> { "1.0-alpha".toVersion() }
        shouldThrow<VersionFormatException> { "1.0-alpha.01".toVersion() }
        shouldThrow<VersionFormatException> { "a1.0.0".toVersion() }
        shouldThrow<VersionFormatException> { "1.a0.0".toVersion() }
        shouldThrow<VersionFormatException> { "1.0.a0".toVersion() }
        shouldThrow<VersionFormatException> { "92233720368547758072.0.0".toVersion() }
        shouldThrow<VersionFormatException> { "0.92233720368547758072.0".toVersion() }
        shouldThrow<VersionFormatException> { "0.0.92233720368547758072".toVersion() }
        shouldThrow<VersionFormatException> { Version(major = 1, minor = 2, patch = 3, preRelease = ".alpha") }
        shouldThrow<VersionFormatException> { Version(1, 2, 3, "alpha.") }
        shouldThrow<VersionFormatException> { Version(1, 2, 3, ".alpha.") }
        shouldThrow<VersionFormatException> { Version(1, 2, 3, "alpha. ") }
        shouldThrow<VersionFormatException> { Version(-1, 2, 3) }
        shouldThrow<VersionFormatException> { Version(1, -2, 3) }
        shouldThrow<VersionFormatException> { Version(1, 2, -3) }
    }

    @Test
    fun testInvalidVersionsWithNull() {
        "-1.0.0".toVersionOrNull().shouldBeNull()
        "1.-1.0".toVersionOrNull().shouldBeNull()
        "0.0.-1".toVersionOrNull().shouldBeNull()
        "1".toVersionOrNull().shouldBeNull()
        "1.0".toVersionOrNull().shouldBeNull()
        "1.0-alpha".toVersionOrNull().shouldBeNull()
        "1.0-alpha.01".toVersionOrNull().shouldBeNull()
        "a1.0.0".toVersionOrNull().shouldBeNull()
        "1.a0.0".toVersionOrNull().shouldBeNull()
        "1.0.a0".toVersionOrNull().shouldBeNull()
        "92233720368547758072.0.0".toVersionOrNull().shouldBeNull()
        "0.92233720368547758072.0".toVersionOrNull().shouldBeNull()
        "0.0.92233720368547758072".toVersionOrNull().shouldBeNull()
    }

    @Test
    fun testValidVersion() {
        "0.0.0".toVersion()
        "1.2.3-alpha.1+build".toVersion()

        "2.3.1".toVersion().isPreRelease.shouldBeFalse()
        "2.3.1-alpha".toVersion().isPreRelease.shouldBeTrue()
        "2.3.1+build".toVersion().isPreRelease.shouldBeFalse()
    }

    @Test
    fun testToString() {
        "1.2.3".toVersion().toString() shouldBe "1.2.3"
        "1.2.3-alpha.b.3".toVersion().toString() shouldBe "1.2.3-alpha.b.3"
        "1.2.3-alpha+build".toVersion().toString() shouldBe "1.2.3-alpha+build"
        "1.2.3+build".toVersion().toString() shouldBe "1.2.3+build"
    }

    @Test
    fun testVersionComponents() {
        with("1.2.3-alpha.b.3+build".toVersion()) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease shouldBe "alpha.b.3"
            buildMetadata shouldBe "build"
            isPreRelease.shouldBeTrue()
            isStable.shouldBeFalse()
        }
    }

    @Test
    fun testVersionComponentsOnlyNumbers() {
        with("1.2.3".toVersion()) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease.shouldBeNull()
            buildMetadata.shouldBeNull()
            isStable.shouldBeTrue()
            isPreRelease.shouldBeFalse()
        }
    }

    @Test
    fun testVersionComponentsNullPreRelease() {
        with("1.2.3+build".toVersion()) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease.shouldBeNull()
            buildMetadata shouldBe "build"
            isStable.shouldBeTrue()
            isPreRelease.shouldBeFalse()
        }
    }

    @Test
    fun testVersionDefault() {
        with(Version()) {
            major shouldBe 0
            minor shouldBe 0
            patch shouldBe 0
            preRelease.shouldBeNull()
            buildMetadata.shouldBeNull()
            isStable.shouldBeFalse()
            isPreRelease.shouldBeFalse()
        }

        with(Version(major = 1)) {
            major shouldBe 1
            minor shouldBe 0
            patch shouldBe 0
            preRelease.shouldBeNull()
            buildMetadata.shouldBeNull()
            isStable.shouldBeTrue()
            isPreRelease.shouldBeFalse()
        }

        with(Version(major = 1, minor = 2)) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 0
            preRelease.shouldBeNull()
            buildMetadata.shouldBeNull()
            isStable.shouldBeTrue()
            isPreRelease.shouldBeFalse()
        }

        with(Version(major = 1, minor = 2, patch = 3)) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease.shouldBeNull()
            buildMetadata.shouldBeNull()
            isStable.shouldBeTrue()
            isPreRelease.shouldBeFalse()
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha")) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease shouldBe "alpha"
            buildMetadata.shouldBeNull()
            isStable.shouldBeFalse()
            isPreRelease.shouldBeTrue()
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha", buildMetadata = "build")) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease shouldBe "alpha"
            buildMetadata shouldBe "build"
            isStable.shouldBeFalse()
            isPreRelease.shouldBeTrue()
        }
    }

    @Test
    fun testVersionComponentsNullBuildMeta() {
        with("1.2.3-alpha".toVersion()) {
            major shouldBe 1
            minor shouldBe 2
            patch shouldBe 3
            preRelease shouldBe "alpha"
            buildMetadata.shouldBeNull()
        }
    }

    @Test
    fun testClone() {
        "1.2.3-alpha+build".toVersion().copy().toString() shouldBe "1.2.3-alpha+build"
        "1.2.3-alpha+build".toVersion().copy(major = 2).toString() shouldBe "2.2.3-alpha+build"
        "1.2.4".toVersion().copy(major = 2).toString() shouldBe "2.2.4"
        "1.2.4".toVersion().copy(minor = 3).toString() shouldBe "1.3.4"
        "1.2.4".toVersion().copy(patch = 5).toString() shouldBe "1.2.5"
        "1.2.4".toVersion().copy(major = 2, minor = 3, patch = 5).toString() shouldBe "2.3.5"
        "1.2.4-alpha".toVersion().copy(major = 2, minor = 3, patch = 5).toString() shouldBe "2.3.5-alpha"
        "1.2.4".toVersion().copy(preRelease = "alpha").toString() shouldBe "1.2.4-alpha"
        "1.2.4-alpha".toVersion().copy(preRelease = "beta").toString() shouldBe "1.2.4-beta"
        "1.2.4".toVersion().copy(buildMetadata = "build").toString() shouldBe "1.2.4+build"
        "1.2.4+build".toVersion().copy(buildMetadata = "build12").toString() shouldBe "1.2.4+build12"
        "1.2.4-alpha".toVersion().copy(buildMetadata = "build").toString() shouldBe "1.2.4-alpha+build"
    }

    @Test
    fun testDestructuring() {
        val (major, minor, patch, preRelease, build) = "1.2.3-alpha+build".toVersion()
        major shouldBe 1
        minor shouldBe 2
        patch shouldBe 3
        preRelease shouldBe "alpha"
        build shouldBe "build"

        val (ma, mi, pa) = "3.4.2".toVersion()
        ma shouldBe 3
        mi shouldBe 4
        pa shouldBe 2
    }

    @Test
    fun testRange() {
        ("1.0.1".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion()).shouldBeTrue()
        ("1.1.1".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion()).shouldBeFalse()
        ("1.0.0".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion()).shouldBeTrue()
        ("1.0.0-alpha.3".toVersion() in "1.0.0-alpha.2".toVersion().."1.0.0-alpha.5".toVersion()).shouldBeTrue()
        ("1.0.0-alpha.1".toVersion() in "1.0.0-alpha.2".toVersion().."1.0.0-alpha.5".toVersion()).shouldBeFalse()
        (("1.0.0".toVersion().."1.1.0".toVersion()).contains("1.0.1".toVersion())).shouldBeTrue()
        ("1.1.0-alpha".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion()).shouldBeTrue()
    }
}
