package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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
        assertTrue { version < "5.2.3-alpha.2.a".toVersion() } // by pre-release part count
        assertTrue { version < "5.2.3-alpha.3".toVersion() } // by pre-release number comparison
        assertTrue { version < "5.2.3-beta".toVersion() } // by pre-release alphabetical comparison
        assertTrue { version <= "5.2.3-alpha.2".toVersion() }
    }

    @Test
    fun testCompareByPreReleaseNumberAlphabetical() {
        assertTrue { "5.2.3-alpha.2".toVersion() < "5.2.3-alpha.a".toVersion() }
        assertTrue { "5.2.3-alpha.a".toVersion() > "5.2.3-alpha.2".toVersion() }
    }

    @Test
    fun testCompareByPreReleaseAndStable() {
        assertTrue { "5.2.3-alpha".toVersion() < "5.2.3".toVersion() }
        assertTrue { "5.2.3".toVersion() > "5.2.3-alpha".toVersion() }
    }

    @Test
    fun testGreaterThanByNumbers() {
        val version = "5.2.3".toVersion()
        assertTrue { version > "4.0.0".toVersion() }
        assertTrue { version > "5.1.3".toVersion() }
        assertTrue { version > "5.2.2".toVersion() }
    }

    @Test
    fun testGreaterThanByPreRelease() {
        val version = "5.2.3-alpha.2".toVersion()
        assertTrue { version > "5.2.3-alpha".toVersion() } // by pre-release part count
        assertTrue { version > "5.2.3-alpha.1".toVersion() } // by pre-release number comparison
        assertTrue { version > "5.2.3-a".toVersion() } // by pre-release alphabetical comparison
        assertTrue { version >= "5.2.3-alpha.2".toVersion() }
    }

    @Test
    fun testEqual() {
        assertEquals("5.2.3-alpha.2".toVersion(), "5.2.3-alpha.2".toVersion())
        assertNotEquals("5.2.3-alpha.2".toVersion(), "5.2.3-alpha.5".toVersion())
        assertEquals("5.2.3".toVersion(), "5.2.3".toVersion())
        assertNotEquals("5.2.3".toVersion(), "5.2.4".toVersion())
        assertEquals("0.0.0".toVersion(), "0.0.0".toVersion())
    }

    @Test
    fun testEqualIgnoreBuild() {
        assertEquals("5.2.3-alpha.2+build.34".toVersion(), "5.2.3-alpha.2".toVersion())
        assertEquals("5.2.3-alpha.2+build.34".toVersion(), "5.2.3-alpha.2+build.35".toVersion())
    }
}
