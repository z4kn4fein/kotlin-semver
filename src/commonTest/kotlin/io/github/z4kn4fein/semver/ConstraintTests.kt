package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.toConstraint
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConstraintTests {

    @Test
    fun testSatisfies() {
        forAll(
            table(
                headers("constraint", "version"),
                row("<\t1.0.0", "0.1.2"),
                row("1.2.3", "1.2.3"),
                row("=1.2.3", "1.2.3"),
                row("!=1.2.3", "1.2.4"),
                row("1.0.0 - 2.0.0", "1.2.3"),
                row("^1.2.3+build", "1.2.3"),
                row("^1.2.3+build", "1.3.0"),
                row("x - 1.0.0", "0.9.7"),
                row("x - 1.x", "0.9.7"),
                row("1.0.0 - x", "1.9.7"),
                row("1.x - x", "1.9.7"),
                row("1.1 - 2", "1.1.1"),
                row("1 - 2", "2.0.0-alpha"),
                row("1 - 2", "1.0.0"),
                row("1.0 - 2", "1.0.0"),
                row("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3"),
                row("1.2.3-alpha+beta - 2.4.5-alpha+beta", "1.2.3-alpha.2"),
                row("1.2.3-alpha+beta - 2.4.5-alpha+beta", "2.4.5-alpha"),
                row("1.2.3+beta - 2.4.3+beta", "1.2.3"),
                row("1.0.0", "1.0.0"),
                row(">=1.0.0", "1.0.0"),
                row(">=1.0.0", "1.0.1"),
                row(">=1.0.0", "1.1.0"),
                row(">1.0.0", "1.0.1"),
                row(">1.0.0", "1.1.0"),
                row("<=2.0.0", "2.0.0"),
                row("<=2.0.0", "1.9.9"),
                row("<=2.0.0", "0.1.2"),
                row("<2.0.0", "1.9.9"),
                row("<2.0.0", "0.1.2"),
                row(">= 1.0.0", "1.0.0"),
                row(">=  1.0.0", "1.0.1"),
                row(">=   1.0.0", "1.1.0"),
                row("> 1.0.0", "1.0.1"),
                row(">  1.0.0", "1.1.0"),
                row("<=   2.0.0", "2.0.0"),
                row("<= 2.0.0", "1.9.9"),
                row("<=  2.0.0", "0.1.2"),
                row("<    2.0.0", "1.9.9"),
                row(">=0.1.2", "0.1.2"),
                row(">1.1 <2", "1.2.1"),
                row("0.1.2 || 1.2.4", "1.2.4"),
                row(">=0.1.2 || <0.0.1", "0.0.0"),
                row(">=0.1.2 || <0.0.1", "0.1.2"),
                row(">=0.1.2 || <0.0.1", "0.1.3"),
                row(">=1.1 <2 !=1.2.3 || > 3", "4.1.2"),
                row(">=1.1 <2 !=1.2.3 || >= 3", "3.0.0"),
                row(">=1", "1.0.0"),
                row(">= 1", "1.0.0"),
                row("<1.2", "1.1.1"),
                row("< 1.2", "1.1.1"),
                row("=0.7.x", "0.7.2"),
                row("<=0.7.x", "0.7.2"),
                row(">=0.7.x", "0.7.2"),
                row("<=0.7.x", "0.6.2"),
                row("||", "1.3.4"),
                row("2.x.x", "2.1.3"),
                row("1.2.x", "1.2.3"),
                row("1.2.x || 2.x", "2.1.3"),
                row("1.2.x || 2.x", "1.2.3"),
                row("4.1", "4.1.0"),
                row("4.1.x", "4.1.3"),
                row("1.x", "1.4.0"),
                row("x", "1.2.3"),
                row("2.*.*", "2.1.3"),
                row("1.2.*", "1.2.3"),
                row("1.2.* || 2.*", "2.1.3"),
                row("1.2.* || 2.*", "1.2.3"),
                row("*", "1.2.3"),
                row(">=*", "0.2.4"),
                row("*", "1.0.0-beta"),
                row("2", "2.1.2"),
                row("2.3", "2.3.1"),
                row("~0.0.1", "0.0.1"),
                row("~0.0.1", "0.0.2"),
                row("~x", "0.0.9"),
                row("~2", "2.0.9"),
                row("~2.4", "2.4.0"),
                row("~2.4", "2.4.5"),
                row("~>3.2.1", "3.2.2"),
                row("~1", "1.2.3"),
                row("~>1", "1.2.3"),
                row("~> 1", "1.2.3"),
                row("~1.0", "1.0.2"),
                row("~ 1.0", "1.0.2"),
                row("~ 1.0.3", "1.0.12"),
                row("~ 1.0.3-alpha", "1.0.12"),
                row("~0.5.4-alpha", "0.5.5"),
                row("~0.5.4-alpha", "0.5.4"),
                row("~1.2.1 >=1.2.3", "1.2.3"),
                row("~1.2.1 =1.2.3", "1.2.3"),
                row("~1.2.1 1.2.3", "1.2.3"),
                row("~1.2.1 >=1.2.3 1.2.3", "1.2.3"),
                row("~1.2.1 1.2.3 >=1.2.3", "1.2.3"),
                row("~1.2.1 1.2.3", "1.2.3"),
                row("~*", "2.1.1"),
                row("~1", "1.3.5"),
                row("~1.x", "1.3.5"),
                row("~1.3.5-alpha", "1.3.5-beta"),
                row("~1.x", "1.2.3"),
                row("~1.1", "1.1.1"),
                row("~1.2.3", "1.2.5"),
                row("~0.0.0", "0.0.1"),
                row("~1.2.3-beta.2", "1.2.4-beta.2"),
                row(">=1.2.1 1.2.3", "1.2.3"),
                row("1.2.3 >=1.2.1", "1.2.3"),
                row(">=1.2.3 >=1.2.1", "1.2.3"),
                row(">=1.2.1 >=1.2.3", "1.2.3"),
                row(">=1.2", "1.2.8"),
                row("^1.2.3", "1.8.1"),
                row("^0.1.2", "0.1.2"),
                row("^0.1", "0.1.2"),
                row("^0.0.1", "0.0.1"),
                row("^1.2", "1.4.2"),
                row("^1.2 ^1", "1.4.2"),
                row("^1.2.3-alpha", "1.2.3-alpha"),
                row("^1.2.0-alpha", "1.2.0-alpha"),
                row("^0.0.1-alpha", "0.0.1-beta"),
                row("^0.0.1-alpha", "0.0.1"),
                row("^0.1.1-alpha", "0.1.1-beta"),
                row("^x", "1.2.3"),
                row("<=7.x", "7.9.9"),
                row("2.x", "2.0.0"),
                row("2.x", "2.1.0-alpha.0"),
                row("1.1.x", "1.1.0"),
                row("1.1.x", "1.1.1-a"),
                row("^1.0.0-0", "1.0.1-beta"),
                row("^1.0.0-beta", "1.0.1-beta"),
                row("^1.0.0", "1.0.1-beta"),
                row("^1.0.0", "1.1.0-beta"),
                row("^1.2.3", "1.8.9"),
                row("^1.2.0-alpha.0", "1.2.1-alpha.0"),
                row("^1.2.0-alpha.0", "1.2.1-alpha.1"),
                row("^1.2", "1.8.9"),
                row("^1", "1.8.9"),
                row("^0.2.3", "0.2.5"),
                row("^0.2", "0.2.5"),
                row("^0.0.3", "0.0.3"),
                row("^0.0", "0.0.3"),
                row("^0", "0.2.3"),
                row("^0.2.3-beta.2", "0.2.3-beta.4"),
                row("^1.1", "1.1.1"),
                row("^1.x", "1.1.1"),
                row("^1.1.0", "1.1.1-alpha.1"),
                row("^1.1.1-alpha", "1.1.1-beta"),
                row("^0.1.2-alpha.1", "0.1.2-alpha.1"),
                row("^0.1.2-alpha.1", "0.1.3-alpha.1"),
                row("^0.0.1", "0.0.1"),
                row("=0.7.x", "0.7.0"),
                row(">=0.7.x", "0.7.0"),
                row("<=0.7.x", "0.7.0"),
                row(">=1.0.0 <=1.1.0", "1.1.0-alpha"),
                row("= 2.0", "2.0.0"),
                row("!=1.1", "1.0.0"),
                row("!=1.1", "1.2.3"),
                row("!=1.x", "2.1.0"),
                row("!=1.x", "1.0.0-alpha"),
                row("!=1.1.x", "1.0.0"),
                row("!=1.1.x", "1.2.3"),
                row(">=1.1", "4.1.0"),
                row("<=1.1", "1.1.0"),
                row("<=1.1", "0.1.0"),
                row(">=1.1", "1.1.0"),
                row("<=1.1", "1.1.1"),
                row("<=1.x", "1.1.0"),
                row(">1.1", "4.1.0"),
                row("<1.1", "0.1.0"),
                row("<2.x", "1.1.1"),
                row("<1.2.x", "1.1.1"),
            )
        ) { constraint: String, version: String ->
            version.toVersion() satisfies constraint.toConstraint() shouldBe true
        }
    }

    @Test
    fun testNotSatisfies() {
        forAll(
            table(
                headers("constraint", "version"),
                row("~1.2.3-alpha.2", "1.3.4-alpha.2"),
                row("^1.2.3", "2.8.9"),
                row("^1.2.3", "1.2.1"),
                row("^1.1.0", "2.1.0"),
                row("^1.2.0", "2.2.1"),
                row("^1.2.0-alpha.2", "1.2.0-alpha.1"),
                row("^1.2", "2.8.9"),
                row("^1", "2.8.9"),
                row("^0.2.3", "0.5.6"),
                row("^0.2", "0.5.6"),
                row("^0.0.3", "0.0.4"),
                row("^0.0", "0.1.4"),
                row("^0.0", "1.0.4"),
                row("^0", "1.1.4"),
                row("^0.0.1", "0.0.2-alpha"),
                row("^0.0.1", "0.0.2"),
                row("^1.2.3", "2.0.0-alpha"),
                row("^1.2.3", "1.2.2"),
                row("^1.2", "1.1.9"),
                row("^1.0.0", "1.0.0-alpha"),
                row("^1.0.0", "2.0.0-alpha"),
                row("^1.2.3-beta", "2.0.0"),
                row("^1.0.0", "2.0.0-alpha"),
                row("^1.0.0", "2.0.0-alpha"),
                row("^1.2.3+build", "2.0.0"),
                row("^1.2.3+build", "1.2.0"),
                row("^1.2.3", "1.2.3-beta"),
                row("^1.2", "1.2.0-beta"),
                row("^1.1", "1.1.0-alpha"),
                row("^1.1.1-beta", "1.1.1-alpha"),
                row("^1.1", "3.0.0"),
                row("^2.x", "1.1.1"),
                row("^1.x", "2.1.1"),
                row("^0.0.1", "0.1.3"),
                row("1 - 2", "3.0.0-alpha"),
                row("1 - 2", "1.0.0-alpha"),
                row("1.0 - 2", "1.0.0-alpha"),
                row("1.0.0 - 2.0.0", "2.2.3"),
                row("1.2.3+alpha - 2.4.3+alpha", "1.2.3-alpha.1"),
                row("1.2.3+alpha - 2.4.3-alpha", "2.4.3-alpha.1"),
                row("1.1.x", "1.0.0-alpha"),
                row("1.1.x", "1.1.0-alpha"),
                row("1.1.x", "1.2.0-alpha"),
                row("1.1.x", "1.2.0-alpha"),
                row("1.1.x", "1.0.0-alpha"),
                row("1.x", "1.0.0-alpha"),
                row("1.x", "0.0.0-alpha"),
                row("1.x", "2.0.0-alpha"),
                row(">1.1", "1.1.0"),
                row("<1.1", "1.1.0"),
                row("<1.1", "1.1.1"),
                row("<1.x", "1.1.1"),
                row("<1.x", "2.1.1"),
                row("<1.1.x", "1.2.1"),
                row("<1.1.x", "1.1.1"),
                row(">=1.1", "0.0.9"),
                row("<=2.x", "3.1.0"),
                row("<=1.1.x", "1.2.1"),
                row(">1.1 <2", "1.1.1"),
                row(">1.1 <3", "4.3.2"),
                row(">=1.1 <2 !=1.1.1", "1.1.1"),
                row(">=1.1 <2 !=1.1.1 || > 3", "1.1.1"),
                row(">=1.1 <2 !=1.1.1 || > 3", "3.1.2"),
                row(">=1.1 <2 !=1.1.1 || > 3", "3.0.0"),
                row("1.1-3", "4.3.2"),
                row("~1", "2.1.1"),
                row("~1", "2.0.0-alpha"),
                row("~1.x", "2.1.1"),
                row("~1.x", "2.0.0-alpha"),
                row("~1.3.6-alpha", "1.3.5-beta"),
                row("~1.3.5-beta", "1.3.5-alpha"),
                row("~1.2.3", "1.2.2"),
                row("~1.2.3", "1.3.2"),
                row("~1.1", "1.2.3"),
                row("~1.3", "2.4.5"),
                row(">1.2", "1.2.0"),
                row("<=1.2.3", "1.2.4-beta"),
                row("^1.2.3", "1.2.3-beta"),
                row("=0.7.x", "0.7.0-alpha"),
                row(">=0.7.x", "0.7.0-alpha"),
                row("<=0.7.x", "0.8.0-alpha"),
                row("1", "1.0.0-beta"),
                row("<1", "1.0.1"),
                row("< 1", "1.0.1-beta"),
                row("1.0.0", "1.0.1"),
                row(">=1.0.0", "0.0.0"),
                row(">=1.0.0", "0.0.1"),
                row(">=1.0.0", "0.1.0"),
                row(">1.0.0", "0.0.1"),
                row(">1.0.0", "0.1.0"),
                row("<=2.0.0", "3.0.0"),
                row("=<2.0.0", "2.1.0"),
                row("=<2.0.0", "2.0.1"),
                row("<2.0.0", "2.0.0"),
                row("<2.0.0", "2.0.1"),
                row(">=0.1.2", "0.1.1"),
                row("0.1.1 || 1.2.4", "1.2.3"),
                row(">=0.1.0 || <0.0.1", "0.0.1"),
                row(">=0.1.1 || <0.0.1", "0.1.0"),
                row("2.x.x", "1.1.3",),
                row("2.x.x", "3.1.3"),
                row("1.2.X", "1.3.3"),
                row("1.2.X || 2.x", "3.1.3"),
                row("1.2.X || 2.x", "1.1.3"),
                row("2.*.*", "1.1.3"),
                row("2.*.*", "3.1.3"),
                row("1.2.*", "1.3.3"),
                row("2", "1.1.3"),
                row("2", "3.1.3"),
                row("1.2", "1.3.3"),
                row("1.2.* || 2.*", "3.1.3"),
                row("1.2.* || 2.*", "1.1.3"),
                row("2", "1.1.2"),
                row("2.3", "2.4.1"),
                row("<1", "1.0.0"),
                row("=>1.2", "1.1.1"),
                row("1", "2.0.0-beta"),
                row("~0.1.1-alpha.2", "0.1.1-alpha.1"),
                row("=0.1.x", "0.2.0"),
                row(">=0.1.x", "0.0.1"),
                row("<0.1.x", "0.1.1"),
                row("<1.2.3", "1.2.4-beta"),
                row("=1.2.3", "1.2.3-beta"),
                row(">1.2", "1.2.8"),
                row("2.x", "3.0.0-beta.0"),
                row(">=1.0.0 <1.1.0", "1.1.0"),
                row(">=1.0.0 <1.1.0", "1.1.0"),
                row(">=1.0.0 <1.1.0-beta", "1.1.0-beta"),
                row("=2.0.0", "1.2.3"),
                row("=2.0", "1.2.3"),
                row("= 2.0", "1.2.3"),
                row("!=4.1", "4.1.0"),
                row("!=4.x", "4.1.0"),
                row("!=4.2.x", "4.2.3"),
                row("!=1.1.0", "1.1.0"),
                row("!=1.1", "1.1.0"),
                row("!=1.1", "1.1.1"),
                row("!=1.1", "1.1.1-alpha"),
                row("<1", "1.1.0"),
                row("<1.1", "1.1.0"),
                row("<1.1", "1.1.1"),
                row("<=1", "2.0.0"),
                row("<=1.1", "1.2.3"),
                row(">1.1", "1.1.0"),
                row(">0", "0.0.0"),
                row(">0", "0.0.1-alpha"),
                row(">0.0", "0.0.1-alpha"),
                row(">0-0", "0.0.1-alpha"),
                row(">0.0-0", "0.0.1-alpha"),
                row(">0", "0.0.0-alpha"),
                row(">0-0", "0.0.0-alpha"),
                row(">1", "1.1.0"),
                row(">1.1", "1.1.0"),
                row(">1.1", "1.1.1"),
                row(">=1.1", "1.0.2"),
                row(">=1.1", "0.0.9"),
                row(">=0", "0.0.0-alpha"),
                row(">=0.0", "0.0.0-alpha"),
                row("<0", "0.0.0"),
                row("=0", "1.0.0"),
                row("2.*", "3.0.0"),
                row("2", "2.0.0-alpha"),
                row("2.1.*", "2.2.1"),
                row("2", "3.0.0"),
                row("2.1", "2.2.1"),
                row("~1.2.3", "1.3.0"),
                row("~1.2", "1.3.0"),
                row("~1", "2.0.0"),
                row("~0.1.2", "0.2.0"),
                row("~0.0.1", "0.1.0-alpha"),
                row("~0.0.1", "0.1.0"),
                row("~2.4", "2.5.0"),
                row("~2.4", "2.3.9"),
                row("~>3.2.1", "3.3.2"),
                row("~>3.2.1", "3.2.0"),
                row("~1", "0.2.3"),
                row("~>1", "2.2.3"),
                row("~1.0", "1.1.0"),
            )
        ) { constraint: String, version: String ->
            version.toVersion() satisfies constraint.toConstraint() shouldBe false
        }
    }

    @Test
    fun testParse() {
        forAll(
            table(
                headers("constraint", "expected"),
                row("1.2.3 - 2.3.4", ">=1.2.3 <=2.3.4"),
                row("1.2 - 2.3.4", ">=1.2.0 <=2.3.4"),
                row("1.2.3 - 2.3", ">=1.2.3 <2.4.0-0"),
                row("1.2.3 - 2", ">=1.2.3 <3.0.0-0"),
                row("~1.2.3", ">=1.2.3 <1.3.0-0"),
                row("~1.2", ">=1.2.0 <1.3.0-0"),
                row("~1", ">=1.0.0 <2.0.0-0"),
                row("~0.2.3", ">=0.2.3 <0.3.0-0"),
                row("~0.2", ">=0.2.0 <0.3.0-0"),
                row("~0", ">=0.0.0 <1.0.0-0"),
                row("~0.0.0", ">=0.0.0 <0.1.0-0"),
                row("~0.0", ">=0.0.0 <0.1.0-0"),
                row("~1.2.3-alpha.1", ">=1.2.3-alpha.1 <1.3.0-0"),
                row("", ">=0.0.0"),
                row("*", ">=0.0.0"),
                row("x", ">=0.0.0"),
                row("X", ">=0.0.0"),
                row("1.x", ">=1.0.0 <2.0.0-0"),
                row("1.2.x", ">=1.2.0 <1.3.0-0"),
                row("1", ">=1.0.0 <2.0.0-0"),
                row("1.*", ">=1.0.0 <2.0.0-0"),
                row("1.*.*", ">=1.0.0 <2.0.0-0"),
                row("1.x", ">=1.0.0 <2.0.0-0"),
                row("1.x.x", ">=1.0.0 <2.0.0-0"),
                row("1.X", ">=1.0.0 <2.0.0-0"),
                row("1.X.X", ">=1.0.0 <2.0.0-0"),
                row("1.2", ">=1.2.0 <1.3.0-0"),
                row("1.2.*", ">=1.2.0 <1.3.0-0"),
                row("1.2.x", ">=1.2.0 <1.3.0-0"),
                row("1.2.X", ">=1.2.0 <1.3.0-0"),
                row("^1.2.3", ">=1.2.3 <2.0.0-0"),
                row("^0.2.3", ">=0.2.3 <0.3.0-0"),
                row("^0.0.3", ">=0.0.3 <0.0.4-0"),
                row("^0", ">=0.0.0 <1.0.0-0"),
                row("^0.0", ">=0.0.0 <0.1.0-0"),
                row("^0.0.0", ">=0.0.0 <0.0.1-0"),
                row("^1.2.3-alpha.1", ">=1.2.3-alpha.1 <2.0.0-0"),
                row("^0.0.1-alpha", ">=0.0.1-alpha <0.0.2-0"),
                row("^0.0.*", ">=0.0.0 <0.1.0-0"),
                row("^1.2.*", ">=1.2.0 <2.0.0-0"),
                row("^1.*", ">=1.0.0 <2.0.0-0"),
                row("^0.*", ">=0.0.0 <1.0.0-0"),
                row("1.0.0 - 2.0.0", ">=1.0.0 <=2.0.0"),
                row("1 - 2", ">=1.0.0 <3.0.0-0"),
                row("1.0 - 2.0", ">=1.0.0 <2.1.0-0"),
                row("1.0.0", "=1.0.0"),
                row(">=*", ">=0.0.0"),
                row(">=1.0.0", ">=1.0.0"),
                row(">1.0.0", ">1.0.0"),
                row("<=2.0.0", "<=2.0.0"),
                row("<=2.0.0", "<=2.0.0"),
                row("<2.0.0", "<2.0.0"),
                row("<\t2.0.0", "<2.0.0"),
                row("<= 2.0.0", "<=2.0.0"),
                row("<=  2.0.0", "<=2.0.0"),
                row("<    2.0.0", "<2.0.0"),
                row(">= 1.0.0", ">=1.0.0"),
                row(">=  1.0.0", ">=1.0.0"),
                row(">=   1.0.0", ">=1.0.0"),
                row("> 1.0.0", ">1.0.0"),
                row(">  1.0.0", ">1.0.0"),
                row("<=   2.0.0", "<=2.0.0"),
                row("0.1.0 || 1.2.3", "=0.1.0 || =1.2.3"),
                row(">=0.1.0 || <0.0.1", ">=0.1.0 || <0.0.1"),
                row("||", ">=0.0.0"),
                row("2.x.x", ">=2.0.0 <3.0.0-0"),
                row("1.2.x", ">=1.2.0 <1.3.0-0"),
                row("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
                row("1.2.x || 2.x", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
                row("x", ">=0.0.0"),
                row("2.*.*", ">=2.0.0 <3.0.0-0"),
                row("1.2.*", ">=1.2.0 <1.3.0-0"),
                row("1.2.* || 2.*", ">=1.2.0 <1.3.0-0 || >=2.0.0 <3.0.0-0"),
                row("*", ">=0.0.0"),
                row("2", ">=2.0.0 <3.0.0-0"),
                row("2.3", ">=2.3.0 <2.4.0-0"),
                row("~2.4", ">=2.4.0 <2.5.0-0"),
                row("~2.4", ">=2.4.0 <2.5.0-0"),
                row("~>3.2.1", ">=3.2.1 <3.3.0-0"),
                row("~1", ">=1.0.0 <2.0.0-0"),
                row("~>1", ">=1.0.0 <2.0.0-0"),
                row("~> 1", ">=1.0.0 <2.0.0-0"),
                row("~1.0", ">=1.0.0 <1.1.0-0"),
                row("~ 1.0", ">=1.0.0 <1.1.0-0"),
                row("^0", ">=0.0.0 <1.0.0-0"),
                row("^ 1", ">=1.0.0 <2.0.0-0"),
                row("^0.1", ">=0.1.0 <0.2.0-0"),
                row("^1.0", ">=1.0.0 <2.0.0-0"),
                row("^1.2", ">=1.2.0 <2.0.0-0"),
                row("^0.0.1", ">=0.0.1 <0.0.2-0"),
                row("^0.0.1-beta", ">=0.0.1-beta <0.0.2-0"),
                row("^0.1.2", ">=0.1.2 <0.2.0-0"),
                row("^1.2.3", ">=1.2.3 <2.0.0-0"),
                row("^1.2.3-beta.4", ">=1.2.3-beta.4 <2.0.0-0"),
                row("<1", "<1.0.0"),
                row("< 1", "<1.0.0"),
                row(">=1", ">=1.0.0"),
                row(">= 1", ">=1.0.0"),
                row("<1.2", "<1.2.0"),
                row("< 1.2", "<1.2.0"),
                row("1", ">=1.0.0 <2.0.0-0"),
                row("^ 1.2 ^ 1", ">=1.2.0 <2.0.0-0 >=1.0.0 <2.0.0-0"),
                row("1.2 - 3.4.5", ">=1.2.0 <=3.4.5"),
                row("1.2.3 - 3.4", ">=1.2.3 <3.5.0-0"),
                row("1.2 - 3.4", ">=1.2.0 <3.5.0-0"),
                row(">1", ">=2.0.0-0"),
                row(">1.2", ">=1.3.0-0"),
            )
        ) { constraint: String, expected: String ->
            constraint.toConstraint().toString() shouldBe expected
        }
    }
}
