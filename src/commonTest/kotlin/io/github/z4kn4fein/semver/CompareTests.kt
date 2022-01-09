package io.github.z4kn4fein.semver

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldHaveSameHashCodeAs
import kotlin.test.Test
import kotlin.test.assertTrue

class CompareTests {
    @Test
    fun testLessThanByNumbers() {
        val version = "5.2.3".toVersion()
        assertTrue { version < "6.0.0".toVersion() }
        assertTrue { version < "5.3.3".toVersion() }
        assertTrue { version < "5.2.4".toVersion() }
    }

    @Test
    fun testLessThanByPreRelease() {
        val version = "5.2.3-alpha.2".toVersion()
        version shouldBeLessThan "5.2.3-alpha.2.a".toVersion() // by pre-release part count
        version shouldBeLessThan "5.2.3-alpha.3".toVersion() // by pre-release number comparison
        version shouldBeLessThan "5.2.3-beta".toVersion() // by pre-release alphabetical comparison
        version shouldBeLessThanOrEqualTo "5.2.3-alpha.2".toVersion()
    }

    @Test
    fun testPrecedenceFromSpec() {
        "1.0.0".toVersion() shouldBeLessThan "2.0.0".toVersion()
        "2.0.0".toVersion() shouldBeLessThan "2.1.0".toVersion()
        "2.1.0".toVersion() shouldBeLessThan "2.1.1".toVersion()

        "1.0.0-alpha".toVersion() shouldBeLessThan "1.0.0".toVersion()

        "1.0.0-alpha".toVersion() shouldBeLessThan "1.0.0-alpha.1".toVersion()
        "1.0.0-alpha.1".toVersion() shouldBeLessThan "1.0.0-alpha.beta".toVersion()
        "1.0.0-alpha.beta".toVersion() shouldBeLessThan "1.0.0-beta".toVersion()
        "1.0.0-beta".toVersion() shouldBeLessThan "1.0.0-beta.2".toVersion()
        "1.0.0-beta.2".toVersion() shouldBeLessThan "1.0.0-beta.11".toVersion()
        "1.0.0-beta.11".toVersion() shouldBeLessThan "1.0.0-rc.1".toVersion()
        "1.0.0-rc.1".toVersion() shouldBeLessThan "1.0.0".toVersion()
    }

    @Test
    fun testCompareByPreReleaseNumberAlphabetical() {
        "5.2.3-alpha.2".toVersion() shouldBeLessThan "5.2.3-alpha.a".toVersion()
        "5.2.3-alpha.a".toVersion() shouldBeGreaterThan "5.2.3-alpha.2".toVersion()
    }

    @Test
    fun testCompareByPreReleaseAndStable() {
        "5.2.3-alpha".toVersion() shouldBeLessThan "5.2.3".toVersion()
        "5.2.3".toVersion() shouldBeGreaterThan "5.2.3-alpha".toVersion()
    }

    @Test
    fun testGreaterThanByNumbers() {
        val version = "5.2.3".toVersion()
        version shouldBeGreaterThan "4.0.0".toVersion()
        version shouldBeGreaterThan "5.1.3".toVersion()
        version shouldBeGreaterThan "5.2.2".toVersion()
    }

    @Test
    fun testGreaterThanByPreRelease() {
        val version = "5.2.3-alpha.2".toVersion()
        version shouldBeGreaterThan "5.2.3-alpha".toVersion() // by pre-release part count
        version shouldBeGreaterThan "5.2.3-alpha.1".toVersion() // by pre-release number comparison
        version shouldBeGreaterThan "5.2.3-a".toVersion() // by pre-release alphabetical comparison
        version shouldBeGreaterThanOrEqualTo "5.2.3-alpha.2".toVersion()
    }

    @Test
    fun testEqual() {
        "5.2.3-alpha.2".toVersion() shouldBe "5.2.3-alpha.2".toVersion()
        "5.2.3-alpha.2".toVersion() shouldNotBe "5.2.3-alpha.5".toVersion()
        "5.2.3".toVersion() shouldBe "5.2.3".toVersion()
        "5.2.3".toVersion() shouldNotBe "5.2.4".toVersion()
        "0.0.0".toVersion() shouldBe "0.0.0".toVersion()
        "0.0.0-alpha.2".toVersion().shouldHaveSameHashCodeAs("0.0.0-alpha.2".toVersion())
        "0.0.0".toVersion().shouldHaveSameHashCodeAs("0.0.0".toVersion())
        "0.0.0".toVersion().equals(null).shouldBeFalse()
    }

    @Test
    fun testEqualIgnoreBuild() {
        "5.2.3-alpha.2+build.34".toVersion() shouldBe "5.2.3-alpha.2".toVersion()
        "5.2.3-alpha.2+build.34".toVersion() shouldBe "5.2.3-alpha.2+build.35".toVersion()
    }

    @Test
    fun testListOrder() {
        val list: List<Version> = listOf(
            "1.0.1".toVersion(),
            "1.0.1-alpha".toVersion(),
            "1.0.1-alpha.beta".toVersion(),
            "1.0.1-alpha.3".toVersion(),
            "1.0.1-alpha.2".toVersion(),
            "1.1.0".toVersion(),
            "1.1.0+build".toVersion(),
        )

        list.sorted() shouldBe listOf(
            "1.0.1-alpha".toVersion(),
            "1.0.1-alpha.2".toVersion(),
            "1.0.1-alpha.3".toVersion(),
            "1.0.1-alpha.beta".toVersion(),
            "1.0.1".toVersion(),
            "1.1.0".toVersion(),
            "1.1.0+build".toVersion(),
        )
    }
}
