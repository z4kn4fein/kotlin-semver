package io.github.z4kn4fein.semver.samples

import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.ConstraintSerializer
import io.github.z4kn4fein.semver.constraints.satisfiedBy
import io.github.z4kn4fein.semver.constraints.satisfiedByAll
import io.github.z4kn4fein.semver.constraints.satisfiedByAny
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.constraints.toConstraintOrNull
import io.github.z4kn4fein.semver.toVersion
import kotlinx.serialization.json.Json

class ConstraintSamples {
    fun constraint() {
        val constraints = listOf(
            "1.0.0",
            "!=1.0.0",
            "~1.0",
            "^1.x",
            "1.1.0 - 1.2.*",
            ">=1.1.0 <3 || =0.1 || 5 - 6",
            "v1",
            "v3 - v4",
            ">=v2.3"
        )

        constraints.forEach { println("[$it]: [${it.toConstraint()}]") }
    }

    fun parse() {
        print(Constraint.parse(">=1.0.0 || <5.x"))
    }

    fun toConstraint() {
        print(">=1.0".toConstraint())
    }

    fun toConstraintOrNull() {
        println(">=1.2".toConstraintOrNull())
        println(">=1.2a".toConstraintOrNull())
    }

    fun exception() {
        ">=1.2a".toConstraint()
    }

    fun satisfiedBy() {
        val constraint = ">=1.1.0".toConstraint()
        val version = "1.1.0".toVersion()
        print("$constraint satisfiedBy $version? ${constraint satisfiedBy version}")
    }

    fun satisfiedByAll() {
        val constraint = ">=1.1.0".toConstraint()
        val versions = listOf("1.1.0", "1.2.0").map { it.toVersion() }
        print("$constraint satisfied by ${versions.joinToString(" and ")}? ${constraint satisfiedByAll versions}")
    }

    fun satisfiedByAny() {
        val constraint = ">=1.1.0".toConstraint()
        val versions = listOf("1.1.0", "1.0.0").map { it.toVersion() }
        print("$constraint satisfied by ${versions.joinToString(" or ")}? ${constraint satisfiedByAny versions}")
    }

    fun serialization() {
        print(Json.encodeToString(ConstraintSerializer, ">1.2".toConstraint()))
    }

    fun deserialization() {
        val decoded = Json.decodeFromString(ConstraintSerializer, "\">1.2\"")
        print(decoded)
    }
}
