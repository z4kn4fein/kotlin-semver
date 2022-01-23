package io.github.z4kn4fein.semver

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class NextVersionTests {
    @Test
    fun testVersionComponentsNullBuildMeta() {
        val version = "1.2.3-alpha.4+build.3".toVersion()
        version.nextMajor().toString() shouldBe "2.0.0"
        version.nextMinor().toString() shouldBe "1.3.0"
        version.nextPatch().toString() shouldBe "1.2.3"
        version.nextPreRelease().toString() shouldBe "1.2.3-alpha.5"
    }

    @Test
    fun testNextVersionsWithoutPreRelease() {
        val version = "1.2.3".toVersion()
        version.nextMajor().toString() shouldBe "2.0.0"
        version.nextMinor().toString() shouldBe "1.3.0"
        version.nextPatch().toString() shouldBe "1.2.4"
        version.nextPreRelease().toString() shouldBe "1.2.4-0"
    }

    @Test
    fun testNextVersionsWithNonNumericPreRelease() {
        val version = "1.2.3-alpha".toVersion()
        version.nextMajor().toString() shouldBe "2.0.0"
        version.nextMinor().toString() shouldBe "1.3.0"
        version.nextPatch().toString() shouldBe "1.2.3"
        version.nextPreRelease().toString() shouldBe "1.2.3-alpha.0"
    }

    @Test
    fun testInc() {
        val version = "1.2.3-alpha".toVersion()
        version.inc(by = Inc.MAJOR).toString() shouldBe "2.0.0"
        version.inc(by = Inc.MINOR).toString() shouldBe "1.3.0"
        version.inc(by = Inc.PATCH).toString() shouldBe "1.2.3"
        version.inc(by = Inc.PRE_RELEASE).toString() shouldBe "1.2.3-alpha.0"
    }

    @Test
    fun testInvalidPreReleases() {
        val version = "1.2.3-alpha".toVersion()
        shouldThrow<VersionFormatException> { version.nextMajor(preRelease = "01") }
        shouldThrow<VersionFormatException> { version.nextMinor(preRelease = "01") }
        shouldThrow<VersionFormatException> { version.nextPatch(preRelease = "01") }
        shouldThrow<VersionFormatException> { version.nextPreRelease(preRelease = "01") }
    }

    @Test
    fun testWithData() {
        forAll(
            table(
                headers("source", "inc by", "expected", "with pre-release"),
                row("1.2.3", Inc.MAJOR, "2.0.0", null),
                row("1.2.3", Inc.MINOR, "1.3.0", null),
                row("1.2.3", Inc.PATCH, "1.2.4", null),
                row("1.2.3-alpha", Inc.MAJOR, "2.0.0", null),
                row("1.2.0-0", Inc.PATCH, "1.2.0", null),
                row("1.2.3-4", Inc.MAJOR, "2.0.0", null),
                row("1.2.3-4", Inc.MINOR, "1.3.0", null),
                row("1.2.3-4", Inc.PATCH, "1.2.3", null),
                row("1.2.3-alpha.0.beta", Inc.MAJOR, "2.0.0", null),
                row("1.2.3-alpha.0.beta", Inc.MINOR, "1.3.0", null),
                row("1.2.3-alpha.0.beta", Inc.PATCH, "1.2.3", null),
                row("1.2.4", Inc.PRE_RELEASE, "1.2.5-0", null),
                row("1.2.3-0", Inc.PRE_RELEASE, "1.2.3-1", null),
                row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-alpha.1", null),
                row("1.2.3-alpha.1", Inc.PRE_RELEASE, "1.2.3-alpha.2", null),
                row("1.2.3-alpha.2", Inc.PRE_RELEASE, "1.2.3-alpha.3", null),
                row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.1.beta", null),
                row("1.2.3-alpha.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.2.beta", null),
                row("1.2.3-alpha.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.3.beta", null),
                row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.1.beta", null),
                row("1.2.3-alpha.10.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.2.beta", null),
                row("1.2.3-alpha.10.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.3.beta", null),
                row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.1", null),
                row("1.2.3-alpha.10.beta.1", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.2", null),
                row("1.2.3-alpha.10.beta.2", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.3", null),
                row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta", null),
                row("1.2.3-alpha.10.beta", Inc.PRE_RELEASE, "1.2.3-alpha.11.beta", null),
                row("1.2.3-alpha.11.beta", Inc.PRE_RELEASE, "1.2.3-alpha.12.beta", null),
                row("1.2.0", Inc.PATCH, "1.2.1-0", ""),
                row("1.2.0-1", Inc.PATCH, "1.2.1-0", ""),
                row("1.2.0", Inc.MINOR, "1.3.0-0", ""),
                row("1.2.3-1", Inc.MINOR, "1.3.0-0", ""),
                row("1.2.0", Inc.MAJOR, "2.0.0-0", ""),
                row("1.2.3-1", Inc.MAJOR, "2.0.0-0", ""),

                row("1.2.4", Inc.PRE_RELEASE, "1.2.5-dev", "dev"),
                row("1.2.3-0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-alpha.1", "alpha"),
                row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.1.beta", "alpha"),
                row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.1.beta", "alpha"),
                row("1.2.3-alpha.10.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.2.beta", "alpha"),
                row("1.2.3-alpha.10.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.3.beta", "alpha"),
                row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.1", "alpha"),
                row("1.2.3-alpha.10.beta.1", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.2", "alpha"),
                row("1.2.3-alpha.10.beta.2", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.3", "alpha"),
                row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta", "alpha"),
                row("1.2.3-alpha.10.beta", Inc.PRE_RELEASE, "1.2.3-alpha.11.beta", "alpha"),
                row("1.2.3-alpha.11.beta", Inc.PRE_RELEASE, "1.2.3-alpha.12.beta", "alpha"),
                row("1.2.0", Inc.PATCH, "1.2.1-dev", "dev"),
                row("1.2.0-1", Inc.PATCH, "1.2.1-dev", "dev"),
                row("1.2.0", Inc.MINOR, "1.3.0-dev", "dev"),
                row("1.2.3-1", Inc.MINOR, "1.3.0-dev", "dev"),
                row("1.2.0", Inc.MAJOR, "2.0.0-dev", "dev"),
                row("1.2.3-1", Inc.MAJOR, "2.0.0-dev", "dev"),
                row("1.2.0-1", Inc.MINOR, "1.3.0", null),
                row("1.0.0-1", Inc.MAJOR, "2.0.0", null),
                row("1.2.3-dev.beta", Inc.PRE_RELEASE, "1.2.3-dev.beta.0", "dev"),
            )
        ) { source: String, inc: Inc, expected: String, preRelease: String? ->
            source.toVersion().inc(by = inc, preRelease) shouldBe expected.toVersion()
        }
    }
}
