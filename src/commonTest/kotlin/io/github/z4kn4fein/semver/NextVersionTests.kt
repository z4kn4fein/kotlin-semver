package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals

class NextVersionTests {
    @Test
    fun testVersionComponentsNullBuildMeta() {
        val version = "1.2.3-alpha.4+build.3".toVersion()
        assertEquals("2.0.0", version.nextMajor().toString())
        assertEquals("1.3.0", version.nextMinor().toString())
        assertEquals("1.2.3", version.nextPatch().toString())
        assertEquals("1.2.3-alpha.5", version.nextPreRelease().toString())
    }

    @Test
    fun testNextVersionsWithoutPreRelease() {
        val version = "1.2.3".toVersion()
        assertEquals("2.0.0", version.nextMajor().toString())
        assertEquals("1.3.0", version.nextMinor().toString())
        assertEquals("1.2.4", version.nextPatch().toString())
        assertEquals("1.2.4-0", version.nextPreRelease().toString())
    }

    @Test
    fun testNextVersionsWithNonNumericPreRelease() {
        val version = "1.2.3-alpha".toVersion()
        assertEquals("2.0.0", version.nextMajor().toString())
        assertEquals("1.3.0", version.nextMinor().toString())
        assertEquals("1.2.3", version.nextPatch().toString())
        assertEquals("1.2.3-alpha.0", version.nextPreRelease().toString())
    }
}