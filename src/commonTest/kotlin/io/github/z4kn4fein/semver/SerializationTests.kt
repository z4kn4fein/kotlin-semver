package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test

class SerializationTests {
    @Serializable
    data class ToSerialize(val version: Version)

    @Serializable
    data class ToLooseSerialize(
        @Serializable(with = LooseVersionSerializer::class)
        val version: Version
    )

    @Serializable
    data class ToConstraintSerialize(val constraint: Constraint)

    @Test
    fun testVersionSerialization() {
        val encoded = Json.encodeToString("1.0.0-alpha.1+build.3".toVersion())
        encoded shouldBe "\"1.0.0-alpha.1+build.3\""
    }

    @Test
    fun testVersionDeserialization() {
        val decoded = Json.decodeFromString<Version>("\"1.0.0-alpha.1+build.3\"")
        decoded shouldBe "1.0.0-alpha.1+build.3".toVersion()
    }

    @Test
    fun testMemberVersionSerialization() {
        val obj = ToSerialize(version = "1.0.0-alpha.1+build.3".toVersion())
        val encoded = Json.encodeToString(obj)
        encoded shouldBe "{\"version\":\"1.0.0-alpha.1+build.3\"}"
    }

    @Test
    fun testMemberVersionDeserialization() {
        val decoded = Json.decodeFromString<ToSerialize>("{\"version\":\"1.0.0-alpha.1+build.3\"}")
        decoded.version shouldBe "1.0.0-alpha.1+build.3".toVersion()
    }

    @Test
    fun testMemberLooseVersionSerialization() {
        val obj = ToLooseSerialize(version = "1-alpha.1+build.3".toVersion(strict = false))
        val encoded = Json.encodeToString(obj)
        encoded shouldBe "{\"version\":\"1.0.0-alpha.1+build.3\"}"
    }

    @Test
    fun testMemberLooseVersionDeserialization() {
        val decoded = Json.decodeFromString<ToLooseSerialize>("{\"version\":\"1-alpha.1+build.3\"}")
        decoded.version shouldBe "1-alpha.1+build.3".toVersion(strict = false)
    }

    @Test
    fun testConstraintSerialization() {
        val encoded = Json.encodeToString("> 1.2.3".toConstraint())
        encoded shouldBe "\">1.2.3\""
    }

    @Test
    fun testConstraintDeserialization() {
        val decoded = Json.decodeFromString<Constraint>("\"> 1.2.3\"")
        decoded shouldBe "> 1.2.3".toConstraint()
    }

    @Test
    fun testMemberConstraintSerialization() {
        val obj = ToConstraintSerialize(constraint = "> 1.2.3".toConstraint())
        val encoded = Json.encodeToString(obj)
        encoded shouldBe "{\"constraint\":\">1.2.3\"}"
    }

    @Test
    fun testMemberConstraintDeserialization() {
        val decoded = Json.decodeFromString<ToConstraintSerialize>("{\"constraint\":\"> 1.2.3\"}")
        decoded.constraint shouldBe "> 1.2.3".toConstraint()
    }
}
