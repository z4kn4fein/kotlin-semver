package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class PreReleaseTests {
    @Test
    fun testInvalidVersions() {
        assertFailsWith<VersionFormatException> { ".alpha".toPreRelease() }
        assertFailsWith<VersionFormatException> { "alpha.".toPreRelease() }
        assertFailsWith<VersionFormatException> { ".alpha.".toPreRelease() }
        assertFailsWith<VersionFormatException> { "alpha. ".toPreRelease() }
        assertFailsWith<VersionFormatException> { "alpha.01".toPreRelease() }
        assertFailsWith<VersionFormatException> { "+alpha.01".toPreRelease() }
        assertFailsWith<VersionFormatException> { "%alpha".toPreRelease() }
        assertFailsWith<VersionFormatException> { "".toPreRelease() }
        assertFailsWith<VersionFormatException> { " ".toPreRelease() }
    }

    @Test
    fun testIncrement() {
        assertEquals("alpha-3.Beta.0", "alpha-3.Beta".toPreRelease().increment().toString())
        assertEquals("alpha-3.14.Beta", "alpha-3.13.Beta".toPreRelease().increment().toString())
        assertEquals("alpha.5.Beta.8", "alpha.5.Beta.7".toPreRelease().increment().toString())
    }

    @Test
    fun testComponents() {
        assertEquals("alpha-3.Beta.0", "alpha-3.Beta.0".toPreRelease().toString())
    }

    @Test
    fun testEquals() {
        assertEquals("alpha-3.Beta.0".toPreRelease(), "alpha-3.Beta.0".toPreRelease())
        assertNotEquals("alpha-3.Beta.1".toPreRelease(), "alpha-3.Beta.0".toPreRelease())
        assertFalse("alpha-3.Beta.1".toPreRelease().equals(null))
    }
}
