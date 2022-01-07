package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VersionTests {
    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { "-1.0.0".toVersion() }
        assertFailsWith<VersionFormatException> { "1.-1.0".toVersion() }
        assertFailsWith<VersionFormatException> { "0.0.-1".toVersion() }
        assertFailsWith<VersionFormatException> { "1".toVersion() }
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
        assertFailsWith<VersionFormatException> { Version(-1, 2, 3, "alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, -2, 3, ".alpha.") }
        assertFailsWith<VersionFormatException> { Version(1, 2, -3, "alpha. ") }
    }

    @Test
    fun testValidVersion() {
        "0.0.0".toVersion()
        "1.2.3-alpha.1+build".toVersion()

        assertTrue { "2.3.1".toVersion().isStable }
        assertFalse { "2.3.1-alpha".toVersion().isStable }
        assertFalse { "2.3.1+build".toVersion().isStable }
    }

    @Test
    fun testToString() {
        assertEquals("1.2.3", "1.2.3".toVersion().toString())
        assertEquals("1.2.3-alpha.b.3", "1.2.3-alpha.b.3".toVersion().toString())
        assertEquals("1.2.3-alpha+build", "1.2.3-alpha+build".toVersion().toString())
    }

    @Test
    fun testVersionComponents() {
        val version = "1.2.3-alpha.b.3+build".toVersion()
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
        assertEquals("alpha.b.3", version.preRelease)
        assertEquals("build", version.buildMetadata)
    }

    @Test
    fun testVersionComponentsOnlyNumbers() {
        val version = "1.2.3".toVersion()
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
        assertNull(version.preRelease)
        assertNull(version.buildMetadata)
    }

    @Test
    fun testVersionComponentsNullPreRelease() {
        val version = "1.2.3+build".toVersion()
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
        assertNull(version.preRelease)
        assertEquals("build", version.buildMetadata)
    }

    @Test
    fun testVersionComponentsNullBuildMeta() {
        val version = "1.2.3-alpha".toVersion()
        assertEquals(1, version.major)
        assertEquals(2, version.minor)
        assertEquals(3, version.patch)
        assertEquals("alpha", version.preRelease)
        assertNull(version.buildMetadata)
    }

    @Test
    fun testClone() {
        assertEquals("1.2.3-alpha+build", "1.2.3-alpha+build".toVersion().copy().toString())
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
}
