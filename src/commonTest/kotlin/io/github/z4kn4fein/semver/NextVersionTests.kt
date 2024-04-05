package io.github.z4kn4fein.semver

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun testInc() {
        val version = "1.2.3-alpha".toVersion()
        assertEquals("2.0.0", version.inc(by = Inc.MAJOR).toString())
        assertEquals("1.3.0", version.inc(by = Inc.MINOR).toString())
        assertEquals("1.2.3", version.inc(by = Inc.PATCH).toString())
        assertEquals("1.2.3-alpha.0", version.inc(by = Inc.PRE_RELEASE).toString())
    }

    @Test
    fun testInvalidPreReleases() {
        val version = "1.2.3-alpha".toVersion()
        assertFailsWith<VersionFormatException> { version.nextMajor(preRelease = "01") }
        assertFailsWith<VersionFormatException> { version.nextMinor(preRelease = "01") }
        assertFailsWith<VersionFormatException> { version.nextPatch(preRelease = "01") }
        assertFailsWith<VersionFormatException> { version.nextPreRelease(preRelease = "01") }
    }

    @Test
    fun testWithData() {
        data class Row(val source: String, val incBy: Inc, val expected: String, val preRelease: String?)
        val data: List<Row> =
            listOf(
                Row("1.2.3", Inc.MAJOR, "2.0.0", null),
                Row("1.2.3", Inc.MINOR, "1.3.0", null),
                Row("1.2.3", Inc.PATCH, "1.2.4", null),
                Row("1.2.3-alpha", Inc.MAJOR, "2.0.0", null),
                Row("1.2.0-0", Inc.PATCH, "1.2.0", null),
                Row("1.2.3-4", Inc.MAJOR, "2.0.0", null),
                Row("1.2.3-4", Inc.MINOR, "1.3.0", null),
                Row("1.2.3-4", Inc.PATCH, "1.2.3", null),
                Row("1.2.3-alpha.0.beta", Inc.MAJOR, "2.0.0", null),
                Row("1.2.3-alpha.0.beta", Inc.MINOR, "1.3.0", null),
                Row("1.2.3-alpha.0.beta", Inc.PATCH, "1.2.3", null),
                Row("1.2.4", Inc.PRE_RELEASE, "1.2.5-0", null),
                Row("1.2.3-0", Inc.PRE_RELEASE, "1.2.3-1", null),
                Row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-alpha.1", null),
                Row("1.2.3-alpha.1", Inc.PRE_RELEASE, "1.2.3-alpha.2", null),
                Row("1.2.3-alpha.2", Inc.PRE_RELEASE, "1.2.3-alpha.3", null),
                Row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.1.beta", null),
                Row("1.2.3-alpha.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.2.beta", null),
                Row("1.2.3-alpha.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.3.beta", null),
                Row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.1.beta", null),
                Row("1.2.3-alpha.10.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.2.beta", null),
                Row("1.2.3-alpha.10.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.3.beta", null),
                Row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.1", null),
                Row("1.2.3-alpha.10.beta.1", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.2", null),
                Row("1.2.3-alpha.10.beta.2", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.3", null),
                Row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta", null),
                Row("1.2.3-alpha.10.beta", Inc.PRE_RELEASE, "1.2.3-alpha.11.beta", null),
                Row("1.2.3-alpha.11.beta", Inc.PRE_RELEASE, "1.2.3-alpha.12.beta", null),
                Row("1.2.0", Inc.PATCH, "1.2.1-0", ""),
                Row("1.2.0-1", Inc.PATCH, "1.2.1-0", ""),
                Row("1.2.0", Inc.MINOR, "1.3.0-0", ""),
                Row("1.2.3-1", Inc.MINOR, "1.3.0-0", ""),
                Row("1.2.0", Inc.MAJOR, "2.0.0-0", ""),
                Row("1.2.3-1", Inc.MAJOR, "2.0.0-0", ""),
                Row("1.2.4", Inc.PRE_RELEASE, "1.2.5-dev", "dev"),
                Row("1.2.3-0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.0", Inc.PRE_RELEASE, "1.2.3-alpha.1", "alpha"),
                Row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.1.beta", "alpha"),
                Row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.10.0.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.1.beta", "alpha"),
                Row("1.2.3-alpha.10.1.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.2.beta", "alpha"),
                Row("1.2.3-alpha.10.2.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.3.beta", "alpha"),
                Row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.10.beta.0", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.1", "alpha"),
                Row("1.2.3-alpha.10.beta.1", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.2", "alpha"),
                Row("1.2.3-alpha.10.beta.2", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta.3", "alpha"),
                Row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-dev", "dev"),
                Row("1.2.3-alpha.9.beta", Inc.PRE_RELEASE, "1.2.3-alpha.10.beta", "alpha"),
                Row("1.2.3-alpha.10.beta", Inc.PRE_RELEASE, "1.2.3-alpha.11.beta", "alpha"),
                Row("1.2.3-alpha.11.beta", Inc.PRE_RELEASE, "1.2.3-alpha.12.beta", "alpha"),
                Row("1.2.0", Inc.PATCH, "1.2.1-dev", "dev"),
                Row("1.2.0-1", Inc.PATCH, "1.2.1-dev", "dev"),
                Row("1.2.0", Inc.MINOR, "1.3.0-dev", "dev"),
                Row("1.2.3-1", Inc.MINOR, "1.3.0-dev", "dev"),
                Row("1.2.0", Inc.MAJOR, "2.0.0-dev", "dev"),
                Row("1.2.3-1", Inc.MAJOR, "2.0.0-dev", "dev"),
                Row("1.2.0-1", Inc.MINOR, "1.3.0", null),
                Row("1.0.0-1", Inc.MAJOR, "2.0.0", null),
                Row("1.2.3-dev.beta", Inc.PRE_RELEASE, "1.2.3-dev.beta.0", "dev"),
            )

        data.forEach {
            assertEquals(it.expected.toVersion(), it.source.toVersion().inc(by = it.incBy, it.preRelease))
        }
    }
}
