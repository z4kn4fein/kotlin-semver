package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.Condition
import io.github.z4kn4fein.semver.constraints.ConstraintFormatException
import io.github.z4kn4fein.semver.constraints.Op
import io.github.z4kn4fein.semver.constraints.Range
import io.github.z4kn4fein.semver.constraints.VersionDescriptor
import io.github.z4kn4fein.semver.constraints.satisfiedByAll
import io.github.z4kn4fein.semver.constraints.satisfiedByAny
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.constraints.toConstraintOrNull
import io.github.z4kn4fein.semver.constraints.toOperator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConstraintTests {
    @Test
    fun testInvalidConstraints() {
        assertFailsWith<ConstraintFormatException> { "a".toConstraint() }
        assertFailsWith<ConstraintFormatException> { "||".toConstraint() }
        assertFailsWith<ConstraintFormatException> { ">1.a".toConstraint() }
        assertFailsWith<ConstraintFormatException> { "1.1-3".toConstraint() }
        assertFailsWith<ConstraintFormatException> { ">0-alpha".toConstraint() }
        assertFailsWith<ConstraintFormatException> { ">=0.0-0".toConstraint() }
        assertFailsWith<ConstraintFormatException> { ">=1.2a".toConstraint() }
    }

    @Test
    fun testInvalidConstraintsNull() {
        assertNull("a".toConstraintOrNull())
        assertNull("||".toConstraintOrNull())
        assertNull(">1.a".toConstraintOrNull())
        assertNull("1.1-3".toConstraintOrNull())
        assertNull(">0-alpha".toConstraintOrNull())
        assertNull(">=0.0-0".toConstraintOrNull())
        assertNull(">=1.2a".toConstraintOrNull())
    }

    @Test
    fun testConstraintsEquals() {
        assertEquals(">1.0".toConstraint(), "> 1.0".toConstraint())
        assertEquals(">=1.1.0-0".toConstraint(), ">1.0".toConstraint())
        assertEquals(">=1.0.0 <2.1.0-0".toConstraint(), "1.0 - 2.0".toConstraint())
        assertEquals("<1.0.0 || >=1.1.0-0".toConstraint(), "!=1.0".toConstraint())
    }

    @Test
    fun testSatisfiesAll() {
        val constraints = listOf("!=1.2.4", "=1.2.3", ">1.0.0").map { it.toConstraint() }
        assertTrue("1.2.3".toVersion() satisfiesAll constraints)
        assertFalse("1.2.4".toVersion() satisfiesAll constraints)

        val versions = listOf("1.0.0", "1.0.1").map { it.toVersion() }
        assertTrue(">=1.0.0".toConstraint() satisfiedByAll versions)
        assertFalse(">=1.0.1".toConstraint() satisfiedByAll versions)
    }

    @Test
    fun testSatisfiesAny() {
        val constraints = listOf("!=1.2.4", "=1.2.3", ">1.0.0").map { it.toConstraint() }
        assertTrue("1.2.3".toVersion() satisfiesAny constraints)
        assertTrue("1.2.4".toVersion() satisfiesAny constraints)

        val versions = listOf("1.0.0", "1.0.1").map { it.toVersion() }
        assertTrue(">=1.0.0".toConstraint() satisfiedByAny versions)
        assertTrue(">=1.0.1".toConstraint() satisfiedByAny versions)
    }

    @Test
    fun testToOperator() {
        assertEquals(Op.EQUAL, "=".toOperator())
        assertEquals(Op.NOT_EQUAL, "!=".toOperator())
        assertEquals(Op.GREATER_THAN, ">".toOperator())
        assertEquals(Op.LESS_THAN, "<".toOperator())
        assertEquals(Op.GREATER_THAN_OR_EQUAL, ">=".toOperator())
        assertEquals(Op.GREATER_THAN_OR_EQUAL, "=>".toOperator())
        assertEquals(Op.LESS_THAN_OR_EQUAL, "<=".toOperator())
        assertEquals(Op.LESS_THAN_OR_EQUAL, "=<".toOperator())
        assertEquals(Op.EQUAL, "non-existing".toOperator())
    }

    @Test
    fun testCondition() {
        val version = "1.0.0".toVersion()
        assertEquals("${Op.NOT_EQUAL}1.0.0", Condition(Op.EQUAL, version).opposite())
        assertEquals("${Op.EQUAL}1.0.0", Condition(Op.NOT_EQUAL, version).opposite())
        assertEquals("${Op.GREATER_THAN_OR_EQUAL}1.0.0", Condition(Op.LESS_THAN, version).opposite())
        assertEquals("${Op.GREATER_THAN}1.0.0", Condition(Op.LESS_THAN_OR_EQUAL, version).opposite())
        assertEquals("${Op.LESS_THAN_OR_EQUAL}1.0.0", Condition(Op.GREATER_THAN, version).opposite())
        assertEquals("${Op.LESS_THAN}1.0.0", Condition(Op.GREATER_THAN_OR_EQUAL, version).opposite())

        assertTrue(Condition(Op.EQUAL, version).isSatisfiedBy("1.0.0".toVersion()))
        assertTrue(Condition(Op.NOT_EQUAL, version).isSatisfiedBy("1.2.0".toVersion()))
        assertTrue(Condition(Op.LESS_THAN, version).isSatisfiedBy("0.1.0".toVersion()))
        assertTrue(Condition(Op.LESS_THAN_OR_EQUAL, version).isSatisfiedBy("1.0.0".toVersion()))
        assertTrue(Condition(Op.GREATER_THAN, version).isSatisfiedBy("1.0.1".toVersion()))
        assertTrue(Condition(Op.GREATER_THAN_OR_EQUAL, version).isSatisfiedBy("1.0.0".toVersion()))
    }

    @Test
    fun testRange() {
        val start = Condition(Op.GREATER_THAN, "1.0.0".toVersion())
        val end = Condition(Op.LESS_THAN, "1.1.0".toVersion())
        assertEquals("<=1.0.0 || >=1.1.0", Range(start, end, Op.EQUAL).opposite())
        assertEquals(">1.0.0 <1.1.0", Range(start, end, Op.NOT_EQUAL).opposite())
        assertEquals(">1.0.0", Range(start, end, Op.LESS_THAN).opposite())
        assertEquals(">=1.1.0", Range(start, end, Op.LESS_THAN_OR_EQUAL).opposite())
        assertEquals("<1.1.0", Range(start, end, Op.GREATER_THAN).opposite())
        assertEquals("<=1.0.0", Range(start, end, Op.GREATER_THAN_OR_EQUAL).opposite())

        assertTrue(Range(start, end, Op.EQUAL).isSatisfiedBy("1.0.1".toVersion()))
        assertTrue(Range(start, end, Op.NOT_EQUAL).isSatisfiedBy("1.2.0".toVersion()))
        assertFalse(Range(start, end, Op.LESS_THAN).isSatisfiedBy("1.1.1".toVersion()))
        assertTrue(Range(start, end, Op.LESS_THAN_OR_EQUAL).isSatisfiedBy("1.0.0".toVersion()))
        assertTrue(Range(start, end, Op.GREATER_THAN).isSatisfiedBy("1.2.0".toVersion()))
        assertTrue(Range(start, end, Op.GREATER_THAN_OR_EQUAL).isSatisfiedBy("1.0.1".toVersion()))
    }

    @Test
    fun testDescriptor() {
        assertEquals("1.2.3-pr+b", VersionDescriptor("1", "2", "3", "pr", "b").toString())
        assertEquals("1", VersionDescriptor("1", null, null).toString())
        assertEquals("1.1", VersionDescriptor("1", "1", null).toString())
        assertEquals("1.1.1", VersionDescriptor("1", "1", "1").toString())

        val desc1 = VersionDescriptor("a", "b", "c")
        assertFailsWith<ConstraintFormatException> { desc1.major }
        assertFailsWith<ConstraintFormatException> { desc1.minor }
        assertFailsWith<ConstraintFormatException> { desc1.patch }

        val desc2 = VersionDescriptor("a", null, null)
        assertFailsWith<ConstraintFormatException> { desc2.major }
        assertFailsWith<ConstraintFormatException> { desc2.minor }
        assertFailsWith<ConstraintFormatException> { desc2.patch }
    }

    @Test
    fun testEquals() {
        assertEquals("> 0.0.0".toConstraint(), ">0.0.0".toConstraint())
        assertEquals("1.2 - 2.0".toConstraint(), ">=1.2.0 <2.1.0-0".toConstraint())
        assertEquals("> 0.1.0".toConstraint().hashCode(), ">0.1.0".toConstraint().hashCode())
        assertEquals("1.2 - 2.0".toConstraint().hashCode(), ">=1.2.0 <2.1.0-0".toConstraint().hashCode())
        assertFalse("~1".toConstraint().equals(null))
    }

    @Test
    fun testSatisfies() {
        val data: List<Pair<String, String>> = listOf(
            Pair("<\t1.0.0", "0.1.2"),
            Pair("1.2.3", "1.2.3"),
            Pair("=1.2.3", "1.2.3"),
            Pair("!=1.2.3", "1.2.4"),
            Pair("1.0.0 - 2.0.0", "1.2.3"),
            Pair("^1.2.3+build", "1.2.3"),
            Pair("^1.2.3+build", "1.3.0"),
            Pair("x - 1.0.0", "0.9.7"),
            Pair("x - 1.x", "0.9.7"),
            Pair("1.0.0 - x", "1.9.7"),
            Pair("1.x - x", "1.9.7"),
            Pair("1.1 - 2", "1.1.1"),
            Pair("1 - 2", "2.0.0-alpha"),
            Pair("1 - 2", "1.0.0"),
            Pair("1.0 - 2", "1.0.0"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3-alpha.2"),
            Pair("1.2.3-alpha+beta - 2.4.5-alpha+beta", "2.4.5-alpha"),
            Pair("1.2.3+beta - 2.4.3+beta", "1.2.3"),
            Pair("1.0.0", "1.0.0"),
            Pair(">=1.0.0", "1.0.0"),
            Pair(">=1.0.0", "1.0.1"),
            Pair(">=1.0.0", "1.1.0"),
            Pair(">1.0.0", "1.0.1"),
            Pair(">1.0.0", "1.1.0"),
            Pair("<=2.0.0", "2.0.0"),
            Pair("<=2.0.0", "1.9.9"),
            Pair("<=2.0.0", "0.1.2"),
            Pair("<2.0.0", "1.9.9"),
            Pair("<2.0.0", "0.1.2"),
            Pair(">= 1.0.0", "1.0.0"),
            Pair(">=  1.0.0", "1.0.1"),
            Pair(">=   1.0.0", "1.1.0"),
            Pair("> 1.0.0", "1.0.1"),
            Pair(">  1.0.0", "1.1.0"),
            Pair("<=   2.0.0", "2.0.0"),
            Pair("<= 2.0.0", "1.9.9"),
            Pair("<=  2.0.0", "0.1.2"),
            Pair("<    2.0.0", "1.9.9"),
            Pair(">=0.1.2", "0.1.2"),
            Pair(">1.1 <2", "1.2.1"),
            Pair("0.1.2 || 1.2.4", "1.2.4"),
            Pair(">=0.1.2 || <0.0.1", "0.0.0"),
            Pair(">=0.1.2 || <0.0.1", "0.1.2"),
            Pair(">=0.1.2 || <0.0.1", "0.1.3"),
            Pair(">=1.1 <2 !=1.2.3 || > 3", "4.1.2"),
            Pair(">=1.1 <2 !=1.2.3 || >= 3", "3.0.0"),
            Pair(">=1", "1.0.0"),
            Pair(">= 1", "1.0.0"),
            Pair("<1.2", "1.1.1"),
            Pair("< 1.2", "1.1.1"),
            Pair("=0.7.x", "0.7.2"),
            Pair("<=0.7.x", "0.7.2"),
            Pair(">=0.7.x", "0.7.2"),
            Pair("<=0.7.x", "0.6.2"),
            Pair("2.x.x", "2.1.3"),
            Pair("1.2.x", "1.2.3"),
            Pair("1.2.x || 2.x", "2.1.3"),
            Pair("1.2.x || 2.x", "1.2.3"),
            Pair("4.1", "4.1.0"),
            Pair("4.1.x", "4.1.3"),
            Pair("1.x", "1.4.0"),
            Pair("x", "1.2.3"),
            Pair("2.*.*", "2.1.3"),
            Pair("1.2.*", "1.2.3"),
            Pair("1.2.* || 2.*", "2.1.3"),
            Pair("1.2.* || 2.*", "1.2.3"),
            Pair("*", "1.2.3"),
            Pair(">=*", "0.2.4"),
            Pair("*", "1.0.0-beta"),
            Pair("2", "2.1.2"),
            Pair("2.3", "2.3.1"),
            Pair("~0.0.1", "0.0.1"),
            Pair("~0.0.1", "0.0.2"),
            Pair("~x", "0.0.9"),
            Pair("~2", "2.0.9"),
            Pair("~2.4", "2.4.0"),
            Pair("~2.4", "2.4.5"),
            Pair("~>3.2.1", "3.2.2"),
            Pair("~1", "1.2.3"),
            Pair("~>1", "1.2.3"),
            Pair("~> 1", "1.2.3"),
            Pair("~1.0", "1.0.2"),
            Pair("~ 1.0", "1.0.2"),
            Pair("~ 1.0.3", "1.0.12"),
            Pair("~ 1.0.3-alpha", "1.0.12"),
            Pair("~0.5.4-alpha", "0.5.5"),
            Pair("~0.5.4-alpha", "0.5.4"),
            Pair("~1.2.1 >=1.2.3", "1.2.3"),
            Pair("~1.2.1 =1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3", "1.2.3"),
            Pair("~1.2.1 >=1.2.3 1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3 >=1.2.3", "1.2.3"),
            Pair("~1.2.1 1.2.3", "1.2.3"),
            Pair("~*", "2.1.1"),
            Pair("~1", "1.3.5"),
            Pair("~1.x", "1.3.5"),
            Pair("~1.3.5-alpha", "1.3.5-beta"),
            Pair("~1.x", "1.2.3"),
            Pair("~1.1", "1.1.1"),
            Pair("~1.2.3", "1.2.5"),
            Pair("~0.0.0", "0.0.1"),
            Pair("~1.2.3-beta.2", "1.2.4-beta.2"),
            Pair(">=1.2.1 1.2.3", "1.2.3"),
            Pair("1.2.3 >=1.2.1", "1.2.3"),
            Pair(">=1.2.3 >=1.2.1", "1.2.3"),
            Pair(">=1.2.1 >=1.2.3", "1.2.3"),
            Pair(">=1.2", "1.2.8"),
            Pair("^1.2.3", "1.8.1"),
            Pair("^0.1.2", "0.1.2"),
            Pair("^0.1", "0.1.2"),
            Pair("^0.0.1", "0.0.1"),
            Pair("^1.2", "1.4.2"),
            Pair("^1.2 ^1", "1.4.2"),
            Pair("^1.2.3-alpha", "1.2.3-alpha"),
            Pair("^1.2.0-alpha", "1.2.0-alpha"),
            Pair("^0.0.1-alpha", "0.0.1-beta"),
            Pair("^0.0.1-alpha", "0.0.1"),
            Pair("^0.1.1-alpha", "0.1.1-beta"),
            Pair("^x", "1.2.3"),
            Pair("<=7.x", "7.9.9"),
            Pair("2.x", "2.0.0"),
            Pair("2.x", "2.1.0-alpha.0"),
            Pair("1.1.x", "1.1.0"),
            Pair("1.1.x", "1.1.1-a"),
            Pair("^1.0.0-0", "1.0.1-beta"),
            Pair("^1.0.0-beta", "1.0.1-beta"),
            Pair("^1.0.0", "1.0.1-beta"),
            Pair("^1.0.0", "1.1.0-beta"),
            Pair("^1.2.3", "1.8.9"),
            Pair("^1.2.0-alpha.0", "1.2.1-alpha.0"),
            Pair("^1.2.0-alpha.0", "1.2.1-alpha.1"),
            Pair("^1.2", "1.8.9"),
            Pair("^1", "1.8.9"),
            Pair("^0.2.3", "0.2.5"),
            Pair("^0.2", "0.2.5"),
            Pair("^0.0.3", "0.0.3"),
            Pair("^0.0", "0.0.3"),
            Pair("^0", "0.2.3"),
            Pair("^0.2.3-beta.2", "0.2.3-beta.4"),
            Pair("^1.1", "1.1.1"),
            Pair("^1.x", "1.1.1"),
            Pair("^1.1.0", "1.1.1-alpha.1"),
            Pair("^1.1.1-alpha", "1.1.1-beta"),
            Pair("^0.1.2-alpha.1", "0.1.2-alpha.1"),
            Pair("^0.1.2-alpha.1", "0.1.3-alpha.1"),
            Pair("^0.0.1", "0.0.1"),
            Pair("=0.7.x", "0.7.0"),
            Pair(">=0.7.x", "0.7.0"),
            Pair("<=0.7.x", "0.7.0"),
            Pair(">=1.0.0 <=1.1.0", "1.1.0-alpha"),
            Pair("= 2.0", "2.0.0"),
            Pair("!=1.1", "1.0.0"),
            Pair("!=1.1", "1.2.3"),
            Pair("!=1.x", "2.1.0"),
            Pair("!=1.x", "1.0.0-alpha"),
            Pair("!=1.1.x", "1.0.0"),
            Pair("!=1.1.x", "1.2.3"),
            Pair(">=1.1", "4.1.0"),
            Pair("<=1.1", "1.1.0"),
            Pair("<=1.1", "0.1.0"),
            Pair(">=1.1", "1.1.0"),
            Pair("<=1.1", "1.1.1"),
            Pair("<=1.x", "1.1.0"),
            Pair(">1.1", "4.1.0"),
            Pair("<1.1", "0.1.0"),
            Pair("<2.x", "1.1.1"),
            Pair("<1.2.x", "1.1.1"),
        )

        data.forEach {
            assertTrue { it.second.toVersion() satisfies it.first.toConstraint() }
        }
    }

    @Test
    fun testNotSatisfies() {
        val data: List<Pair<String, String>> = listOf(
            Pair("~1.2.3-alpha.2", "1.3.4-alpha.2"),
            Pair("^1.2.3", "2.8.9"),
            Pair("^1.2.3", "1.2.1"),
            Pair("^1.1.0", "2.1.0"),
            Pair("^1.2.0", "2.2.1"),
            Pair("^1.2.0-alpha.2", "1.2.0-alpha.1"),
            Pair("^1.2", "2.8.9"),
            Pair("^1", "2.8.9"),
            Pair("^0.2.3", "0.5.6"),
            Pair("^0.2", "0.5.6"),
            Pair("^0.0.3", "0.0.4"),
            Pair("^0.0", "0.1.4"),
            Pair("^0.0", "1.0.4"),
            Pair("^0", "1.1.4"),
            Pair("^0.0.1", "0.0.2-alpha"),
            Pair("^0.0.1", "0.0.2"),
            Pair("^1.2.3", "2.0.0-alpha"),
            Pair("^1.2.3", "1.2.2"),
            Pair("^1.2", "1.1.9"),
            Pair("^1.0.0", "1.0.0-alpha"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.2.3-beta", "2.0.0"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.0.0", "2.0.0-alpha"),
            Pair("^1.2.3+build", "2.0.0"),
            Pair("^1.2.3+build", "1.2.0"),
            Pair("^1.2.3", "1.2.3-beta"),
            Pair("^1.2", "1.2.0-beta"),
            Pair("^1.1", "1.1.0-alpha"),
            Pair("^1.1.1-beta", "1.1.1-alpha"),
            Pair("^1.1", "3.0.0"),
            Pair("^2.x", "1.1.1"),
            Pair("^1.x", "2.1.1"),
            Pair("^0.0.1", "0.1.3"),
            Pair("1 - 2", "3.0.0-alpha"),
            Pair("1 - 2", "1.0.0-alpha"),
            Pair("1.0 - 2", "1.0.0-alpha"),
            Pair("1.0.0 - 2.0.0", "2.2.3"),
            Pair("1.2.3+alpha - 2.4.3+alpha", "1.2.3-alpha.1"),
            Pair("1.2.3+alpha - 2.4.3-alpha", "2.4.3-alpha.1"),
            Pair("1.1.x", "1.0.0-alpha"),
            Pair("1.1.x", "1.1.0-alpha"),
            Pair("1.1.x", "1.2.0-alpha"),
            Pair("1.1.x", "1.2.0-alpha"),
            Pair("1.1.x", "1.0.0-alpha"),
            Pair("1.x", "1.0.0-alpha"),
            Pair("1.x", "0.0.0-alpha"),
            Pair("1.x", "2.0.0-alpha"),
            Pair(">1.1", "1.1.0"),
            Pair("<1.1", "1.1.0"),
            Pair("<1.1", "1.1.1"),
            Pair("<1.x", "1.1.1"),
            Pair("<1.x", "2.1.1"),
            Pair("<1.1.x", "1.2.1"),
            Pair("<1.1.x", "1.1.1"),
            Pair(">=1.1", "0.0.9"),
            Pair("<=2.x", "3.1.0"),
            Pair("<=1.1.x", "1.2.1"),
            Pair(">1.1 <2", "1.1.1"),
            Pair(">1.1 <3", "4.3.2"),
            Pair(">=1.1 <2 !=1.1.1", "1.1.1"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "1.1.1"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "3.1.2"),
            Pair(">=1.1 <2 !=1.1.1 || > 3", "3.0.0"),
            Pair("~1", "2.1.1"),
            Pair("~1", "2.0.0-alpha"),
            Pair("~1.x", "2.1.1"),
            Pair("~1.x", "2.0.0-alpha"),
            Pair("~1.3.6-alpha", "1.3.5-beta"),
            Pair("~1.3.5-beta", "1.3.5-alpha"),
            Pair("~1.2.3", "1.2.2"),
            Pair("~1.2.3", "1.3.2"),
            Pair("~1.1", "1.2.3"),
            Pair("~1.3", "2.4.5"),
            Pair(">1.2", "1.2.0"),
            Pair("<=1.2.3", "1.2.4-beta"),
            Pair("^1.2.3", "1.2.3-beta"),
            Pair("=0.7.x", "0.7.0-alpha"),
            Pair(">=0.7.x", "0.7.0-alpha"),
            Pair("<=0.7.x", "0.8.0-alpha"),
            Pair("1", "1.0.0-beta"),
            Pair("<1", "1.0.1"),
            Pair("< 1", "1.0.1-beta"),
            Pair("1.0.0", "1.0.1"),
            Pair(">=1.0.0", "0.0.0"),
            Pair(">=1.0.0", "0.0.1"),
            Pair(">=1.0.0", "0.1.0"),
            Pair(">1.0.0", "0.0.1"),
            Pair(">1.0.0", "0.1.0"),
            Pair("<=2.0.0", "3.0.0"),
            Pair("=<2.0.0", "2.1.0"),
            Pair("=<2.0.0", "2.0.1"),
            Pair("<2.0.0", "2.0.0"),
            Pair("<2.0.0", "2.0.1"),
            Pair(">=0.1.2", "0.1.1"),
            Pair("0.1.1 || 1.2.4", "1.2.3"),
            Pair(">=0.1.0 || <0.0.1", "0.0.1"),
            Pair(">=0.1.1 || <0.0.1", "0.1.0"),
            Pair("2.x.x", "1.1.3",),
            Pair("2.x.x", "3.1.3"),
            Pair("1.2.X", "1.3.3"),
            Pair("1.2.X || 2.x", "3.1.3"),
            Pair("1.2.X || 2.x", "1.1.3"),
            Pair("2.*.*", "1.1.3"),
            Pair("2.*.*", "3.1.3"),
            Pair("1.2.*", "1.3.3"),
            Pair("2", "1.1.3"),
            Pair("2", "3.1.3"),
            Pair("1.2", "1.3.3"),
            Pair("1.2.* || 2.*", "3.1.3"),
            Pair("1.2.* || 2.*", "1.1.3"),
            Pair("2", "1.1.2"),
            Pair("2.3", "2.4.1"),
            Pair("<1", "1.0.0"),
            Pair("=>1.2", "1.1.1"),
            Pair("1", "2.0.0-beta"),
            Pair("~0.1.1-alpha.2", "0.1.1-alpha.1"),
            Pair("=0.1.x", "0.2.0"),
            Pair(">=0.1.x", "0.0.1"),
            Pair("<0.1.x", "0.1.1"),
            Pair("<1.2.3", "1.2.4-beta"),
            Pair("=1.2.3", "1.2.3-beta"),
            Pair(">1.2", "1.2.8"),
            Pair("2.x", "3.0.0-beta.0"),
            Pair(">=1.0.0 <1.1.0", "1.1.0"),
            Pair(">=1.0.0 <1.1.0", "1.1.0"),
            Pair(">=1.0.0 <1.1.0-beta", "1.1.0-beta"),
            Pair("=2.0.0", "1.2.3"),
            Pair("=2.0", "1.2.3"),
            Pair("= 2.0", "1.2.3"),
            Pair("!=4.1", "4.1.0"),
            Pair("!=4.x", "4.1.0"),
            Pair("!=4.2.x", "4.2.3"),
            Pair("!=1.1.0", "1.1.0"),
            Pair("!=1.1", "1.1.0"),
            Pair("!=1.1", "1.1.1"),
            Pair("!=1.1", "1.1.1-alpha"),
            Pair("<1", "1.1.0"),
            Pair("<1.1", "1.1.0"),
            Pair("<1.1", "1.1.1"),
            Pair("<=1", "2.0.0"),
            Pair("<=1.1", "1.2.3"),
            Pair(">1.1", "1.1.0"),
            Pair(">0", "0.0.0"),
            Pair(">0", "0.0.1-alpha"),
            Pair(">0.0", "0.0.1-alpha"),
            Pair(">0", "0.0.0-alpha"),
            Pair(">1", "1.1.0"),
            Pair(">1.1", "1.1.0"),
            Pair(">1.1", "1.1.1"),
            Pair(">=1.1", "1.0.2"),
            Pair(">=1.1", "0.0.9"),
            Pair(">=0", "0.0.0-alpha"),
            Pair(">=0.0", "0.0.0-alpha"),
            Pair("<0", "0.0.0"),
            Pair("=0", "1.0.0"),
            Pair("2.*", "3.0.0"),
            Pair("2", "2.0.0-alpha"),
            Pair("2.1.*", "2.2.1"),
            Pair("2", "3.0.0"),
            Pair("2.1", "2.2.1"),
            Pair("~1.2.3", "1.3.0"),
            Pair("~1.2", "1.3.0"),
            Pair("~1", "2.0.0"),
            Pair("~0.1.2", "0.2.0"),
            Pair("~0.0.1", "0.1.0-alpha"),
            Pair("~0.0.1", "0.1.0"),
            Pair("~2.4", "2.5.0"),
            Pair("~2.4", "2.3.9"),
            Pair("~>3.2.1", "3.3.2"),
            Pair("~>3.2.1", "3.2.0"),
            Pair("~1", "0.2.3"),
            Pair("~>1", "2.2.3"),
            Pair("~1.0", "1.1.0"),
        )

        data.forEach {
            assertFalse(it.second.toVersion() satisfies it.first.toConstraint())
        }
    }

    @Test
    fun testParse() {
        val data: List<Pair<String, String>> = listOf(
            Pair("1.2.3 - 2.3.4", ">=1.2.3 <=2.3.4"),
            Pair("1.2.3 - 2.3.4 || 3.0.0 - 4.0.0", ">=1.2.3 <=2.3.4 || >=3.0.0 <=4.0.0"),
            Pair("1.2 - 2.3.4", ">=1.2.0 <=2.3.4"),
            Pair("1.2.3 - 2.3", ">=1.2.3 <2.4.0-0"),
            Pair("1.2.3 - 2", ">=1.2.3 <3.0.0-0"),
            Pair("~1.2.3", ">=1.2.3 <1.3.0-0"),
            Pair("~1.2", ">=1.2.0 <1.3.0-0"),
            Pair("~1", ">=1.0.0 <2.0.0-0"),
            Pair("~0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("~0.2", ">=0.2.0 <0.3.0-0"),
            Pair("~0", ">=0.0.0 <1.0.0-0"),
            Pair("~0.0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~1.2.3-alpha.1", ">=1.2.3-alpha.1 <1.3.0-0"),
            Pair("", ">=0.0.0"),
            Pair("*", ">=0.0.0"),
            Pair("x", ">=0.0.0"),
            Pair("X", ">=0.0.0"),
            Pair("1.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1", ">=1.0.0 <2.0.0-0"),
            Pair("1.*", ">=1.0.0 <2.0.0-0"),
            Pair("1.*.*", ">=1.0.0 <2.0.0-0"),
            Pair("1.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.x.x", ">=1.0.0 <2.0.0-0"),
            Pair("1.X", ">=1.0.0 <2.0.0-0"),
            Pair("1.X.X", ">=1.0.0 <2.0.0-0"),
            Pair("1.2", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.X", ">=1.2.0 <1.3.0-0"),
            Pair("^1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("^0.0.3", ">=0.0.3 <0.0.4-0"),
            Pair("^0", ">=0.0.0 <1.0.0-0"),
            Pair("^0.0", ">=0.0.0 <0.1.0-0"),
            Pair("^0.0.0", ">=0.0.0 <0.0.1-0"),
            Pair("^1.2.3-alpha.1", ">=1.2.3-alpha.1 <2.0.0-0"),
            Pair("^0.0.1-alpha", ">=0.0.1-alpha <0.0.2-0"),
            Pair("^0.0.*", ">=0.0.0 <0.1.0-0"),
            Pair("^1.2.*", ">=1.2.0 <2.0.0-0"),
            Pair("^1.*", ">=1.0.0 <2.0.0-0"),
            Pair("^0.*", ">=0.0.0 <1.0.0-0"),
            Pair("1.0.0 - 2.0.0", ">=1.0.0 <=2.0.0"),
            Pair("1 - 2", ">=1.0.0 <3.0.0-0"),
            Pair("1.0 - 2.0", ">=1.0.0 <2.1.0-0"),
            Pair("1.0.0", "=1.0.0"),
            Pair(">=*", ">=0.0.0"),
            Pair(">=1.0.0", ">=1.0.0"),
            Pair(">1.0.0", ">1.0.0"),
            Pair("<=2.0.0", "<=2.0.0"),
            Pair("<=2.0.0", "<=2.0.0"),
            Pair("<2.0.0", "<2.0.0"),
            Pair("<\t2.0.0", "<2.0.0"),
            Pair("<= 2.0.0", "<=2.0.0"),
            Pair("<=  2.0.0", "<=2.0.0"),
            Pair("<    2.0.0", "<2.0.0"),
            Pair("<    2.0", "<2.0.0"),
            Pair("<=    2.0", "<2.1.0-0"),
            Pair(">= 1.0.0", ">=1.0.0"),
            Pair(">=  1.0.0", ">=1.0.0"),
            Pair(">=   1.0.0", ">=1.0.0"),
            Pair("> 1.0.0", ">1.0.0"),
            Pair(">  1.0.0", ">1.0.0"),
            Pair("<=   2.0.0", "<=2.0.0"),
            Pair("0.1.0 || 1.2.3", "=0.1.0 || =1.2.3"),
            Pair(">=0.1.0 || <0.0.1", ">=0.1.0 || <0.0.1"),
            Pair("2.x.x", ">=2.0.0 <3.0.0-0"),
            Pair("1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("x", ">=0.0.0"),
            Pair("2.*.*", ">=2.0.0 <3.0.0-0"),
            Pair("1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("1.2.* || 2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("*", ">=0.0.0"),
            Pair("2", ">=2.0.0 <3.0.0-0"),
            Pair("2.3", ">=2.3.0 <2.4.0-0"),
            Pair("~2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~>3.2.1", ">=3.2.1 <3.3.0-0"),
            Pair("~1", ">=1.0.0 <2.0.0-0"),
            Pair("~>1", ">=1.0.0 <2.0.0-0"),
            Pair("~> 1", ">=1.0.0 <2.0.0-0"),
            Pair("~1.0", ">=1.0.0 <1.1.0-0"),
            Pair("~ 1.0", ">=1.0.0 <1.1.0-0"),
            Pair("^0", ">=0.0.0 <1.0.0-0"),
            Pair("^ 1", ">=1.0.0 <2.0.0-0"),
            Pair("^0.1", ">=0.1.0 <0.2.0-0"),
            Pair("^1.0", ">=1.0.0 <2.0.0-0"),
            Pair("^1.2", ">=1.2.0 <2.0.0-0"),
            Pair("^0.0.1", ">=0.0.1 <0.0.2-0"),
            Pair("^0.0.1-beta", ">=0.0.1-beta <0.0.2-0"),
            Pair("^0.1.2", ">=0.1.2 <0.2.0-0"),
            Pair("^1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^1.2.3-beta.4", ">=1.2.3-beta.4 <2.0.0-0"),
            Pair("<1", "<1.0.0"),
            Pair("< 1", "<1.0.0"),
            Pair("= 1", ">=1.0.0 <2.0.0-0"),
            Pair("!= 1", "<1.0.0 || >=2.0.0-0"),
            Pair(">=1", ">=1.0.0"),
            Pair(">= 1", ">=1.0.0"),
            Pair("<1.2", "<1.2.0"),
            Pair("< 1.2", "<1.2.0"),
            Pair("1", ">=1.0.0 <2.0.0-0"),
            Pair("^ 1.2 ^ 1", ">=1.2.0 <2.0.0-0 >=1.0.0 <2.0.0-0"),
            Pair("1.2 - 3.4.5", ">=1.2.0 <=3.4.5"),
            Pair("1.2.3 - 3.4", ">=1.2.3 <3.5.0-0"),
            Pair("1.2 - 3.4", ">=1.2.0 <3.5.0-0"),
            Pair(">1", ">=2.0.0-0"),
            Pair(">1.2", ">=1.3.0-0"),
            Pair("<*", "<0.0.0-0"),
            Pair(">*", "<0.0.0-0"),
            Pair("!=*", "<0.0.0-0"),
            Pair(">=*", ">=0.0.0"),
            Pair("<=*", ">=0.0.0"),
            Pair("=*", ">=0.0.0"),

            Pair("v1.2.3 - v2.3.4", ">=1.2.3 <=2.3.4"),
            Pair("v1.2.3 - v2.3.4 || 3.0.0 - 4.0.0", ">=1.2.3 <=2.3.4 || >=3.0.0 <=4.0.0"),
            Pair("v1.2 - v2.3.4", ">=1.2.0 <=2.3.4"),
            Pair("v1.2.3 - v2.3", ">=1.2.3 <2.4.0-0"),
            Pair("v1.2.3 - v2", ">=1.2.3 <3.0.0-0"),
            Pair("~v1.2.3", ">=1.2.3 <1.3.0-0"),
            Pair("~v1.2", ">=1.2.0 <1.3.0-0"),
            Pair("~v1", ">=1.0.0 <2.0.0-0"),
            Pair("~v0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("~v0.2", ">=0.2.0 <0.3.0-0"),
            Pair("~v0", ">=0.0.0 <1.0.0-0"),
            Pair("~v0.0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~v0.0", ">=0.0.0 <0.1.0-0"),
            Pair("~v1.2.3-alpha.1", ">=1.2.3-alpha.1 <1.3.0-0"),
            Pair("v*", ">=0.0.0"),
            Pair("vx", ">=0.0.0"),
            Pair("vX", ">=0.0.0"),
            Pair("v1.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1", ">=1.0.0 <2.0.0-0"),
            Pair("v1.*", ">=1.0.0 <2.0.0-0"),
            Pair("v1.*.*", ">=1.0.0 <2.0.0-0"),
            Pair("v1.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.x.x", ">=1.0.0 <2.0.0-0"),
            Pair("v1.X", ">=1.0.0 <2.0.0-0"),
            Pair("v1.X.X", ">=1.0.0 <2.0.0-0"),
            Pair("v1.2", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.X", ">=1.2.0 <1.3.0-0"),
            Pair("^v1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^v0.2.3", ">=0.2.3 <0.3.0-0"),
            Pair("^v0.0.3", ">=0.0.3 <0.0.4-0"),
            Pair("^v0", ">=0.0.0 <1.0.0-0"),
            Pair("^v0.0", ">=0.0.0 <0.1.0-0"),
            Pair("^v0.0.0", ">=0.0.0 <0.0.1-0"),
            Pair("^v1.2.3-alpha.1", ">=1.2.3-alpha.1 <2.0.0-0"),
            Pair("^v0.0.1-alpha", ">=0.0.1-alpha <0.0.2-0"),
            Pair("^v0.0.*", ">=0.0.0 <0.1.0-0"),
            Pair("^v1.2.*", ">=1.2.0 <2.0.0-0"),
            Pair("^v1.*", ">=1.0.0 <2.0.0-0"),
            Pair("^v0.*", ">=0.0.0 <1.0.0-0"),
            Pair("v1.0.0 - 2.0.0", ">=1.0.0 <=2.0.0"),
            Pair("v1 - v2", ">=1.0.0 <3.0.0-0"),
            Pair("v1.0 - v2.0", ">=1.0.0 <2.1.0-0"),
            Pair("v1.0.0", "=1.0.0"),
            Pair(">=v*", ">=0.0.0"),
            Pair(">=v1.0.0", ">=1.0.0"),
            Pair(">v1.0.0", ">1.0.0"),
            Pair("<=v2.0.0", "<=2.0.0"),
            Pair("<=v2.0.0", "<=2.0.0"),
            Pair("<v2.0.0", "<2.0.0"),
            Pair("<\tv2.0.0", "<2.0.0"),
            Pair("<= v2.0.0", "<=2.0.0"),
            Pair("<=  v2.0.0", "<=2.0.0"),
            Pair("<    v2.0.0", "<2.0.0"),
            Pair("<    v2.0", "<2.0.0"),
            Pair("<=    v2.0", "<2.1.0-0"),
            Pair(">= v1.0.0", ">=1.0.0"),
            Pair(">=  v1.0.0", ">=1.0.0"),
            Pair(">=   v1.0.0", ">=1.0.0"),
            Pair("> v1.0.0", ">1.0.0"),
            Pair(">  v1.0.0", ">1.0.0"),
            Pair("<=   v2.0.0", "<=2.0.0"),
            Pair("v0.1.0 || v1.2.3", "=0.1.0 || =1.2.3"),
            Pair(">=v0.1.0 || <v0.0.1", ">=0.1.0 || <0.0.1"),
            Pair("v2.x.x", ">=2.0.0 <3.0.0-0"),
            Pair("v1.2.x", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.x || v2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("v1.2.x || v2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("vx", ">=0.0.0"),
            Pair("v2.*.*", ">=2.0.0 <3.0.0-0"),
            Pair("v1.2.*", ">=1.2.0 <1.3.0-0"),
            Pair("v1.2.* || 2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
            Pair("v*", ">=0.0.0"),
            Pair("v2", ">=2.0.0 <3.0.0-0"),
            Pair("v2.3", ">=2.3.0 <2.4.0-0"),
            Pair("~v2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~v2.4", ">=2.4.0 <2.5.0-0"),
            Pair("~>v3.2.1", ">=3.2.1 <3.3.0-0"),
            Pair("~v1", ">=1.0.0 <2.0.0-0"),
            Pair("~>v1", ">=1.0.0 <2.0.0-0"),
            Pair("~> v1", ">=1.0.0 <2.0.0-0"),
            Pair("~v1.0", ">=1.0.0 <1.1.0-0"),
            Pair("~ v1.0", ">=1.0.0 <1.1.0-0"),
            Pair("^v0", ">=0.0.0 <1.0.0-0"),
            Pair("^ v1", ">=1.0.0 <2.0.0-0"),
            Pair("^v0.1", ">=0.1.0 <0.2.0-0"),
            Pair("^v1.0", ">=1.0.0 <2.0.0-0"),
            Pair("^v1.2", ">=1.2.0 <2.0.0-0"),
            Pair("^v0.0.1", ">=0.0.1 <0.0.2-0"),
            Pair("^v0.0.1-beta", ">=0.0.1-beta <0.0.2-0"),
            Pair("^v0.1.2", ">=0.1.2 <0.2.0-0"),
            Pair("^v1.2.3", ">=1.2.3 <2.0.0-0"),
            Pair("^v1.2.3-beta.4", ">=1.2.3-beta.4 <2.0.0-0"),
            Pair("<v1", "<1.0.0"),
            Pair("< v1", "<1.0.0"),
            Pair("= v1", ">=1.0.0 <2.0.0-0"),
            Pair("!= v1", "<1.0.0 || >=2.0.0-0"),
            Pair(">=v1", ">=1.0.0"),
            Pair(">= v1", ">=1.0.0"),
            Pair("<v1.2", "<1.2.0"),
            Pair("< v1.2", "<1.2.0"),
            Pair("v1", ">=1.0.0 <2.0.0-0"),
            Pair("^ v1.2 ^ v1", ">=1.2.0 <2.0.0-0 >=1.0.0 <2.0.0-0"),
            Pair("v1.2 - v3.4.5", ">=1.2.0 <=3.4.5"),
            Pair("v1.2.3 - v3.4", ">=1.2.3 <3.5.0-0"),
            Pair("v1.2 - v3.4", ">=1.2.0 <3.5.0-0"),
            Pair(">v1", ">=2.0.0-0"),
            Pair(">v1.2", ">=1.3.0-0"),
            Pair("<v*", "<0.0.0-0"),
            Pair(">v*", "<0.0.0-0"),
            Pair("!=v*", "<0.0.0-0"),
            Pair(">=v*", ">=0.0.0"),
            Pair("<=v*", ">=0.0.0"),
            Pair("=v*", ">=0.0.0"),
        )

        data.forEach {
            assertEquals(it.second, it.first.toConstraint().toString())
        }
    }
}
