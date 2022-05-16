package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTests {
    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { "-1.0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "1.-1.0".toVersion() }
        assertFailsWith<VersionFormatException> { "0.0.-1".toVersion() }
        assertFailsWith<VersionFormatException> { "1".toVersion() }
        assertFailsWith<VersionFormatException> { "".toVersion() }
        assertFailsWith<VersionFormatException> { "".toVersion(strict = false) }
        assertFailsWith<VersionFormatException> { "1.0".toVersion() }
        assertFailsWith<VersionFormatException> { "1.0-alpha".toVersion() }
        assertFailsWith<VersionFormatException> { "1.0-alpha.01".toVersion() }
        assertFailsWith<VersionFormatException> { "a1.0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "1.a0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "1.0.a0".toVersion() }
        assertFailsWith<VersionFormatException> { "92233720368547758072.0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "0.92233720368547758072.0".toVersion() }
        assertFailsWith<VersionFormatException> { "0.0.92233720368547758072".toVersion() }
        assertFailsWith<VersionFormatException> { Version(major = 1, minor = 2, patch = 3, preRelease = ".alpha") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, "alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, ".alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, 2, 3, "alpha. ") }
        assertFailsWith<VersionFormatException> { Version(-1, 2, 3) }
        assertFailsWith<VersionFormatException> { Version(1, -2, 3) }
        assertFailsWith<VersionFormatException> { Version(1, 2, -3) }
        assertFailsWith<VersionFormatException> { "v1.0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "92233720368547758072".toVersion(strict = false) }

        assertFailsWith<VersionFormatException> { Version.parse("-1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.-1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.-1") }
        assertFailsWith<VersionFormatException> { Version.parse("1") }
        assertFailsWith<VersionFormatException> { Version.parse("") }
        assertFailsWith<VersionFormatException> { Version.parse("", strict = false) }
        assertFailsWith<VersionFormatException> { Version.parse("1.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0-alpha.01") }
        assertFailsWith<VersionFormatException> { Version.parse("a1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.a0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("1.0.a0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.92233720368547758072.0") }
        assertFailsWith<VersionFormatException> { Version.parse("0.0.92233720368547758072") }
        assertFailsWith<VersionFormatException> { Version.parse("v1.0.0") }
        assertFailsWith<VersionFormatException> { Version.parse("92233720368547758072", strict = false) }
    }

    @Test
    fun testInvalidVersionsWithNull() {
        assertNull("-1.0.0".toVersionOrNull())
        assertNull("1.-1.0".toVersionOrNull())
        assertNull("0.0.-1".toVersionOrNull())
        assertNull("1".toVersionOrNull())
        assertNull("1.0".toVersionOrNull())
        assertNull("1.0-alpha".toVersionOrNull())
        assertNull("1.0-alpha.01".toVersionOrNull())
        assertNull("a1.0.0".toVersionOrNull())
        assertNull("1.a0.0".toVersionOrNull())
        assertNull("1.0.a0".toVersionOrNull())
        assertNull("92233720368547758072.0.0".toVersionOrNull())
        assertNull("0.92233720368547758072.0".toVersionOrNull())
        assertNull("0.0.92233720368547758072".toVersionOrNull())
        assertNull("v1.0.0".toVersionOrNull())
        assertNotNull("v1.0.0".toVersionOrNull(strict = false))
    }

    @Test
    fun testValidVersion() {
        "0.0.0".toVersion()
        "1.2.3-alpha.1+build".toVersion()
        "v1.0.0".toVersion(strict = false)
        "1.0".toVersion(strict = false)
        "v1".toVersion(strict = false)
        "1".toVersion(strict = false)

        assertFalse("2.3.1".toVersion().isPreRelease)
        assertTrue("2.3.1-alpha".toVersion().isPreRelease)
        assertFalse("2.3.1+build".toVersion().isPreRelease)
    }

    @Test
    fun testToString() {
        assertEquals("1.2.3", "1.2.3".toVersion().toString())
        assertEquals("1.2.3-alpha.b.3", "1.2.3-alpha.b.3".toVersion().toString())
        assertEquals("1.2.3-alpha+build", "1.2.3-alpha+build".toVersion().toString())
        assertEquals("1.2.3+build", "1.2.3+build".toVersion().toString())
        assertEquals("1.2.3", "v1.2.3".toVersion(strict = false).toString())
        assertEquals("1.0.0", "v1".toVersion(strict = false).toString())
        assertEquals("1.0.0", "1".toVersion(strict = false).toString())
        assertEquals("1.2.0", "1.2".toVersion(strict = false).toString())
        assertEquals("1.2.0", "v1.2".toVersion(strict = false).toString())

        assertEquals("1.2.3-alpha+build", "v1.2.3-alpha+build".toVersion(strict = false).toString())
        assertEquals("1.0.0-alpha+build", "v1-alpha+build".toVersion(strict = false).toString())
        assertEquals("1.0.0-alpha+build", "1-alpha+build".toVersion(strict = false).toString())
        assertEquals("1.2.0-alpha+build", "1.2-alpha+build".toVersion(strict = false).toString())
        assertEquals("1.2.0-alpha+build", "v1.2-alpha+build".toVersion(strict = false).toString())
    }

    @Test
    fun testVersionComponents() {
        with("1.2.3-alpha.b.3+build".toVersion()) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals("alpha.b.3", preRelease)
            assertEquals("build", buildMetadata)
            assertTrue(isPreRelease)
            assertFalse(isStable)
        }
    }

    @Test
    fun testVersionComponentsOnlyNumbers() {
        with("1.2.3".toVersion()) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }
    }

    @Test
    fun testVersionComponentsNullPreRelease() {
        with("1.2.3+build".toVersion()) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertEquals("build", buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }
    }

    @Test
    fun testVersionDefault() {
        with(Version()) {
            assertEquals(0, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1)) {
            assertEquals(1, major)
            assertEquals(0, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(0, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3)) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertNull(preRelease)
            assertNull(buildMetadata)
            assertTrue(isStable)
            assertFalse(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
            assertFalse(isStable)
            assertTrue(isPreRelease)
        }

        with(Version(major = 1, minor = 2, patch = 3, preRelease = "alpha", buildMetadata = "build")) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertEquals(buildMetadata, "build")
            assertFalse(isStable)
            assertTrue(isPreRelease)
        }
    }

    @Test
    fun testVersionComponentsNullBuildMeta() {
        with("1.2.3-alpha".toVersion()) {
            assertEquals(1, major)
            assertEquals(2, minor)
            assertEquals(3, patch)
            assertEquals(preRelease, "alpha")
            assertNull(buildMetadata)
        }
    }

    @Test
    fun testClone() {
        assertEquals("1.2.3-alpha+build", "1.2.3-alpha+build".toVersion().copy().toString())
        assertEquals("2.2.3-alpha+build", "1.2.3-alpha+build".toVersion().copy(major = 2).toString())
        assertEquals("2.2.4", "1.2.4".toVersion().copy(major = 2).toString())
        assertEquals("1.3.4", "1.2.4".toVersion().copy(minor = 3).toString())
        assertEquals("1.2.5", "1.2.4".toVersion().copy(patch = 5).toString())
        assertEquals("2.3.5", "1.2.4".toVersion().copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("2.3.5-alpha", "1.2.4-alpha".toVersion().copy(major = 2, minor = 3, patch = 5).toString())
        assertEquals("1.2.4-alpha", "1.2.4".toVersion().copy(preRelease = "alpha").toString())
        assertEquals("1.2.4-beta", "1.2.4-alpha".toVersion().copy(preRelease = "beta").toString())
        assertEquals("1.2.4+build", "1.2.4".toVersion().copy(buildMetadata = "build").toString())
        assertEquals("1.2.4+build12", "1.2.4+build".toVersion().copy(buildMetadata = "build12").toString())
        assertEquals("1.2.4-alpha+build", "1.2.4-alpha".toVersion().copy(buildMetadata = "build").toString())
    }

    @Test
    fun testWithoutSuffixes() {
        assertEquals("1.2.3", "1.2.3-alpha+build".toVersion().withoutSuffixes().toString())
        assertEquals("1.2.4", "1.2.4".toVersion().withoutSuffixes().toString())
        assertEquals("1.2.4", "1.2.4-alpha".toVersion().withoutSuffixes().toString())
        assertEquals("1.2.4", "1.2.4+build".toVersion().withoutSuffixes().toString())
    }

    @Test
    fun testDestructuring() {
        val (major, minor, patch, preRelease, build) = "1.2.3-alpha+build".toVersion()
        assertEquals(1, major)
        assertEquals(2, minor)
        assertEquals(3, patch)
        assertEquals("alpha", preRelease)
        assertEquals("build", build)

        val (ma, mi, pa) = "3.4.2".toVersion()
        assertEquals(3, ma)
        assertEquals(4, mi)
        assertEquals(2, pa)
    }

    @Test
    fun testRange() {
        assertTrue("1.0.1".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion())
        assertFalse("1.1.1".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion())
        assertTrue("1.0.0".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion())
        assertTrue("1.0.0-alpha.3".toVersion() in "1.0.0-alpha.2".toVersion().."1.0.0-alpha.5".toVersion())
        assertFalse("1.0.0-alpha.1".toVersion() in "1.0.0-alpha.2".toVersion().."1.0.0-alpha.5".toVersion())
        assertTrue(("1.0.0".toVersion().."1.1.0".toVersion()).contains("1.0.1".toVersion()))
        assertTrue("1.1.0-alpha".toVersion() in "1.0.0".toVersion().."1.1.0".toVersion())
    }
}
