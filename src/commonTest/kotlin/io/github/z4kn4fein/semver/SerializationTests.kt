package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.toConstraint
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class SerializationTests {
    @Serializable
    data class ToSerialize(val version: Version)

    @Serializable
    data class ToLooseSerialize(
        @Serializable(with = LooseVersionSerializer::class)
        val version: Version,
    )

    @Serializable
    data class ToConstraintSerialize(val constraint: Constraint)

    @Test
    fun testVersionSerialization() {
        val encoded = Json.encodeToString("1.0.0-alpha.1+build.3".toVersion())
        assertEquals("\"1.0.0-alpha.1+build.3\"", encoded)
    }

    @Test
    fun testVersionDeserialization() {
        val decoded = Json.decodeFromString<Version>("\"1.0.0-alpha.1+build.3\"")
        assertEquals("1.0.0-alpha.1+build.3".toVersion(), decoded)
    }

    @Test
    fun testMemberVersionSerialization() {
        val obj = ToSerialize(version = "1.0.0-alpha.1+build.3".toVersion())
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"version\":\"1.0.0-alpha.1+build.3\"}", encoded)
    }

    @Test
    fun testMemberVersionDeserialization() {
        val decoded = Json.decodeFromString<ToSerialize>("{\"version\":\"1.0.0-alpha.1+build.3\"}")
        assertEquals("1.0.0-alpha.1+build.3".toVersion(), decoded.version)
    }

    @Test
    fun testMemberLooseVersionSerialization() {
        val obj = ToLooseSerialize(version = "1-alpha.1+build.3".toVersion(strict = false))
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"version\":\"1.0.0-alpha.1+build.3\"}", encoded)
    }

    @Test
    fun testMemberLooseVersionDeserialization() {
        val decoded = Json.decodeFromString<ToLooseSerialize>("{\"version\":\"1-alpha.1+build.3\"}")
        assertEquals("1-alpha.1+build.3".toVersion(strict = false), decoded.version)
    }

    @Test
    fun testConstraintSerialization() {
        val encoded = Json.encodeToString("> 1.2.3".toConstraint())
        assertEquals("\">1.2.3\"", encoded)
    }

    @Test
    fun testConstraintDeserialization() {
        val decoded = Json.decodeFromString<Constraint>("\"> 1.2.3\"")
        assertEquals("> 1.2.3".toConstraint(), decoded)
    }

    @Test
    fun testMemberConstraintSerialization() {
        val obj = ToConstraintSerialize(constraint = "> 1.2.3".toConstraint())
        val encoded = Json.encodeToString(obj)
        assertEquals("{\"constraint\":\">1.2.3\"}", encoded)
    }

    @Test
    fun testMemberConstraintDeserialization() {
        val decoded = Json.decodeFromString<ToConstraintSerialize>("{\"constraint\":\"> 1.2.3\"}")
        assertEquals("> 1.2.3".toConstraint(), decoded.constraint)
    }
}
