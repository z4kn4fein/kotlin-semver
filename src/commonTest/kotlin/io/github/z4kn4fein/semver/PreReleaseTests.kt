package io.github.z4kn4fein.semver

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class PreReleaseTests {
    @Test
    fun testInvalidVersions() {
        shouldThrow<VersionFormatException> { ".alpha".toPreRelease() }
        shouldThrow<VersionFormatException> { "alpha.".toPreRelease() }
        shouldThrow<VersionFormatException> { ".alpha.".toPreRelease() }
        shouldThrow<VersionFormatException> { "alpha. ".toPreRelease() }
        shouldThrow<VersionFormatException> { "alpha.01".toPreRelease() }
        shouldThrow<VersionFormatException> { "+alpha.01".toPreRelease() }
        shouldThrow<VersionFormatException> { "%alpha".toPreRelease() }
        shouldThrow<VersionFormatException> { "".toPreRelease() }
        shouldThrow<VersionFormatException> { " ".toPreRelease() }
    }

    @Test
    fun testIncrement() {
        "alpha-3.Beta".toPreRelease().increment().toString() shouldBe "alpha-3.Beta.0"
        "alpha-3.13.Beta".toPreRelease().increment().toString() shouldBe "alpha-3.14.Beta"
        "alpha.5.Beta.7".toPreRelease().increment().toString() shouldBe "alpha.5.Beta.8"
    }

    @Test
    fun testEquality() {
        "alpha-3.Beta.0".toPreRelease().toString() shouldBe "alpha-3.Beta.0"
    }

    @Test
    fun testIdentity() {
        "alpha-3.beta.0".toPreRelease().identity shouldBe "alpha-3"
        "beta.0".toPreRelease().identity shouldBe "beta"
        "3.Beta.0".toPreRelease().identity shouldBe "3"
        "3.0".toPreRelease().identity shouldBe "3"
    }

    @Test
    fun testEquals() {
        "alpha-3.Beta.0".toPreRelease() shouldBe "alpha-3.Beta.0".toPreRelease()
        "alpha-3.Beta.0".toPreRelease() shouldNotBe "alpha-3.Beta.1".toPreRelease()
        "alpha-3.Beta.1".toPreRelease().equals(null).shouldBeFalse()
    }

    @Test
    fun testDefault() {
        PreRelease.default().toString() shouldBe "0"
        PreRelease.default("").toString() shouldBe "0"
        PreRelease.default("alpha").toString() shouldBe "alpha.0"
    }
}
