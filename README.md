# kotlin-semver

[![Maven Central](https://img.shields.io/maven-central/v/io.github.z4kn4fein/semver?label=maven%20central)](https://search.maven.org/artifact/io.github.z4kn4fein/semver/)
[![Snapshot](https://img.shields.io/nexus/s/io.github.z4kn4fein/semver?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/z4kn4fein/semver/)
[![Build](https://img.shields.io/github/workflow/status/z4kn4fein/kotlin-semver/Semver%20CI?logo=GitHub)](https://github.com/z4kn4fein/kotlin-semver/actions/workflows/semver-ci.yml)
[![Quality Gate Status](https://img.shields.io/sonar/quality_gate/z4kn4fein_kotlin-semver?logo=SonarCloud&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=z4kn4fein_kotlin-semver)
[![SonarCloud Coverage](https://img.shields.io/sonar/coverage/z4kn4fein_kotlin-semver?logo=SonarCloud&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=z4kn4fein_kotlin-semver)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6-blueviolet.svg?logo=kotlin)](http://kotlinlang.org)

Semantic Versioning library for [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html).
It implements the full [semantic version 2.0.0](https://semver.org/spec/v2.0.0.html) specification and 
provides the ability to **parse**, **compare**, and **increment** semantic versions along with validation against constraints.

The API Documentation is available [here](https://z4kn4fein.github.io/kotlin-semver/).

## Install with Gradle
This library is available in Maven Central, so you have to add it to your repositories.
```kotlin
repositories {
    mavenCentral()
}
```
Then, you can add the package to your dependencies.
```kotlin
dependencies {
    implementation("io.github.z4kn4fein:semver:1.1.0")
}
```
In case of a multiplatform project, you can simply reference the package in your `commonMain` source set.
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies { 
                implementation("io.github.z4kn4fein:semver:1.1.0")
            }
        }
    }
}
```
You can also use the platform specific packages, [here](https://search.maven.org/search?q=g:io.github.z4kn4fein%20AND%20a:semver*) you can find them for each supported platform.

## Usage
The following options are available to construct a `Version`:
1. Building part by part.

   ```kotlin
   Version(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")
   ```
   
2. Parsing from a string with `Version.parse()`.

   ```kotlin
   Version.parse("3.5.2-alpha+build")
   ```
   
3. Using the `toVersion()` or `toVersionOrNull()` extension methods on a string.

   ```kotlin
   "3.5.2-alpha+build".toVersion()
   ```

The following information is accessible on a constructed `Version` object:
```kotlin
val version = "3.5.2-alpha.2+build".toVersion()
version.major           // 3
version.minor           // 5
version.patch           // 2
version.preRelease      // "alpha.2"
version.buildMetadata   // "build"

version.isPreRelease    // true
version.isStable        // false
version.toString()      // "3.5.2-alpha.2+build"
```

`Version` also supports destructuring.
```kotlin
val version = "2.3.1-alpha.2+build".toVersion()
val (major, minor, patch, preRelease, buildMetadata) = version 

// major: 2
// minor: 3
// patch: 1
// preRelease: "alpha.2"
// buildMetadata: "build"
```

## Compare

It is possible to compare two `Version` objects with [comparison operators](https://kotlinlang.org/docs/operator-overloading.html#comparison-operators) or with `.compareTo()`.
```kotlin
"0.1.0".toVersion() < "0.1.1".toVersion()                   // true
"0.1.1".toVersion() <= "0.1.1".toVersion()                  // true
"0.1.0-alpha.3".toVersion() < "0.1.0-alpha.4".toVersion()   // true

"0.1.1".toVersion().compareTo("0.1.0".toVersion())          //  1
"0.1.0".toVersion().compareTo("0.1.1".toVersion())          // -1
"0.1.1".toVersion().compareTo("0.1.1".toVersion())          //  0
```

The equality of two `Version` objects can be determined with [equality operators](https://kotlinlang.org/docs/operator-overloading.html#equality-and-inequality-operators) or with `equals()`.
```kotlin
"0.1.1".toVersion() == "0.1.1".toVersion()       // true
"0.1.1".toVersion() != "0.1.1".toVersion()       // false

"0.1.1".toVersion().equals("0.1.1".toVersion())  // true
"0.1.0".toVersion().equals("0.1.1".toVersion())  // false
```

### Sort
As `Version` objects are comparable, a collection of them can be sorted easily like in the following example.
```kotlin
val list: List<Version> = listOf(
    "1.0.1".toVersion(),
    "1.0.1-alpha".toVersion(),
    "1.0.1-alpha.beta".toVersion(),
    "1.0.1-alpha.3".toVersion(),
    "1.0.1-alpha.2".toVersion(),
    "1.1.0".toVersion(),
    "1.1.0+build".toVersion(),
).sorted()

// The result:
//   "1.0.1-alpha"
//   "1.0.1-alpha.2"
//   "1.0.1-alpha.3"
//   "1.0.1-alpha.beta"
//   "1.0.1"
//   "1.1.0"
//   "1.1.0+build"
```

### Range
Having an order provides the ability to determine whether a `Version` is in the range between two given versions.
```kotlin
val range = "1.0.0".toVersion().."1.1.0".toVersion()

"1.0.1".toVersion() in range     // true
"1.1.1".toVersion() in range     // false
```

## Constraints
With constraints, it's possible to validate whether a version satisfies a set of rules or not.
A constraint can be described as one or more conditions combined with logical `OR` and `AND` operators.

### Conditions
Conditions are usually composed of a comparison operator and a version like `>=1.2.0`.
The condition `>=1.2.0` would be met by any version that greater than or equal to `1.2.0` for example `1.3.0` or `1.2.1`.

Available comparison operators:
- `=` Equal (equivalent to no operator)
- `!=` Not equal
- `<` Less than
- `<=` Less than or equal
- `>` Greater than
- `>=` Greater than or equal

These conditions can be joined together with whitespace, which represents the `AND` logical operator between them.
The `OR` operation can be expressed with `||` between condition sets combined with whitespaces.

The constraint `>=1.2.0 <3.0.0 || >4.0.0` translates to: *Only those versions are allowed that are either greater than or
equal to `1.2.0` {**AND**} less than `3.0.0` {**OR**} greater than `4.0.0`*.

We can notice that with the first part of the previous constraint (`>=1.2.0 <3.0.0`) we simply defined a semantic version range.
There are additional options to express version ranges, these are described in the following section.

### Range Conditions
There are special range indicators that in fact only sugars for longer range expressions.

- **X-Range**: The `x`, `X`, and `*` characters can be used as wildcard for the numeric parts of a version.
   - `1.2.x` translates to `>=1.2.0 <1.3.0-0`
   - `1.x` translates to `>=1.0.0 <2.0.0-0`
   - `*` translates to `>=0.0.0`

  In partial version expressions the missing numbers are treated as wildcards.
   - `1.2` means `1.2.x` which finally translates to `>=1.2.0 <1.3.0-0`
   - `1` means `1.x` or `1.x.x` which finally translates to `>=1.0.0 <2.0.0-0`

- **Hyphen Range**: Describes an inclusive version range. Wildcards are evaluated and taken into account in the final range.
   - `1.0.0 - 1.2.0` translates to `>=1.0.0 <=1.2.0`
   - `1.1 - 1.4.0` means `>=(>=1.1.0 <1.2.0-0) <=1.4.0` which finally translates to `>=1.1.0 <=1.4.0`
   - `1.1.0 - 2` means `>=1.1.0 <=(>=2.0.0 <3.0.0-0)` which finally translates to `>=1.1.0 <3.0.0-0`

- **Tilde Range (`~`)**: Describes a patch level range when the minor version is specified or a minor level range, when it's not.
   - `~1.0.1` translates to `>=1.0.1 <1.1.0-0`
   - `~1.0` translates to `>=1.0.0 <1.1.0-0`
   - `~1` translates to `>=1.0.0 <2.0.0-0`
   - `~1.0.0-alpha.1` translates to `>=1.0.1-alpha.1 <1.1.0-0`

- **Caret Range (`^`)**: Describes a range with regard to the most left non-zero part of the version.
   - `^1.1.2` translates to `>=1.1.2 <2.0.0-0`
   - `^0.1.2` translates to `>=0.1.2 <0.2.0-0`
   - `^0.0.2` translates to `>=0.0.2 <0.0.3-0`
   - `^1.2` translates to `>=1.2.0 <2.0.0-0`
   - `^1` translates to `>=1.0.0 <2.0.0-0`
   - `^0.1.2-alpha.1` translates to `>=0.1.2-alpha.1 <0.2.0-0`

### Validation
The following options are available to construct a `Constraint`:
- Parsing from a string with `Constraint.parse()`.
   ```kotlin
   Constraint.parse(">=1.2.0")
   ```

- Using the `toConstraint()` or `toConstraintOrNull()` extension methods on a string.
   ```kotlin
   ">=1.2.0".toConstraint()
   ```

Let's see how we can determine whether a version satisfies a constraint or not.
```kotlin
val constraint = ">=1.2.0".toConstraint()
val version = "1.2.1".toVersion()

version satisfies constraint        // true
constraint satisfiedBy version      // true
```

It's also possible to validate against a collection of constraints.
```kotlin
val constraints = listOf(">=1.2.0", "<2.0.0").map { it.toConstraint() }
val version = "1.2.1".toVersion()

version satisfiesAll constraints       // true
version satisfiesAny constraints       // true
```
> With `satisfiesAll` the version must satisfy each constraint within the collection. With `satisfiesAny` it must satisfy at least one constraint to pass the validation.

Or to validate a collection of versions.
```kotlin
val constraint = ">=1.2.0".toConstraint()
val versions = listOf("1.2.1", "1.1.0").map { it.toVersion() }

constraint satisfiedByAll versions       // false
constraint satisfiedByAny versions       // true
```
> With `satisfiedByAll` the constraint must be satisfied by each version within the collection. With `satisfiedByAny` it must be satisfied by at least one version to pass the validation.

## Increment
`Version` objects can produce incremented versions of themselves with the `next{Major|Minor|Patch|PreRelease}()` and `inc()` methods.
These methods can be used to determine the next version in order by increasing the appropriate identifier.
`Version` objects are **immutable**, so each incrementing function creates a new `Version`.

This example shows how the incrementation works on a stable version:
```kotlin
val stableVersion = "1.0.0".toVersion()

val nextMajor = stableVersion.nextMajor()                               // 2.0.0
val nextMinor = stableVersion.nextMinor()                               // 1.1.0
val nextPatch = stableVersion.nextPatch()                               // 1.0.1
val nextPreRelease = stableVersion.nextPreRelease()                     // 1.0.1-0

// or with the inc() method:
val incrementedByMajor = stableVersion.inc(by = Inc.MAJOR)              // 2.0.0
val incrementedByMinor = stableVersion.inc(by = Inc.MINOR)              // 1.1.0
val incrementedByPatch = stableVersion.inc(by = Inc.PATCH)              // 1.0.1
val incrementedByPreRelease = stableVersion.inc(by = Inc.PRE_RELEASE)   // 1.0.1-0
```

In case of an unstable version:
```kotlin
val unstableVersion = "1.0.0-alpha.2+build.1".toVersion()

val nextMajor = unstableVersion.nextMajor()                               // 2.0.0
val nextMinor = unstableVersion.nextMinor()                               // 1.1.0
val nextPatch = unstableVersion.nextPatch()                               // 1.0.0
val nextPreRelease = unstableVersion.nextPreRelease()                     // 1.0.0-alpha.3

// or with the inc() method:
val incrementedByMajor = unstableVersion.inc(by = Inc.MAJOR)              // 2.0.0
val incrementedByMinor = unstableVersion.inc(by = Inc.MINOR)              // 1.1.0
val incrementedByPatch = unstableVersion.inc(by = Inc.PATCH)              // 1.0.0
val incrementedByPreRelease = unstableVersion.inc(by = Inc.PRE_RELEASE)   // 1.0.0-alpha.3
```

Each incrementing function provides the option to set a pre-release identity on the incremented version.
```kotlin
val version = "1.0.0-alpha.1".toVersion()

val nextPreMajor = version.nextMajor(preRelease = "beta")           // 2.0.0-beta.0
val nextPreMinor = version.nextMinor(preRelease = "")               // 1.1.0-0
val nextPrePatch = version.nextPatch(preRelease = "alpha")          // 1.0.1-alpha.0
val nextPreRelease = version.nextPreRelease(preRelease = "alpha")   // 1.0.0-alpha.2

// or with the inc() method:
val incrementedByMajor = version.inc(by = Inc.MAJOR, preRelease = "beta")               // 2.0.0-beta.0
val incrementedByMinor = version.inc(by = Inc.MINOR, preRelease = "")                   // 1.1.0-0
val incrementedByPatch = version.inc(by = Inc.PATCH, preRelease = "alpha")              // 1.0.1-alpha.0
val incrementedByPreRelease = version.inc(by = Inc.PRE_RELEASE, preRelease = "alpha")   // 1.0.0-alpha.2
```

## Copy
It's possible to make a copy of a version with the `copy()` method.
It allows to alter the copied version's properties with optional parameters.
```kotlin
val version = "1.0.0-alpha.2+build.1".toVersion()

val exactCopy = version.copy()                                            // 1.0.0-alpha.2+build.1
val withDifferentMajor = version.copy(major = 3)                          // 3.0.0-alpha.2+build.1
val withDifferentMinor = version.copy(minor = 4)                          // 1.4.0-alpha.2+build.1
val withDifferentPatch = version.copy(patch = 5)                          // 1.0.5-alpha.2+build.1
val withDifferentPreRelease = version.copy(preRelease = "alpha.4")        // 1.0.0-alpha.4+build.1
val withDifferentBuildMetadata = version.copy(buildMetadata = "build.3")  // 1.0.0-alpha.2+build.3
val withDifferentNumbers = version.copy(major = 3, minor = 4, patch = 5)  // 3.4.5-alpha.2+build.1
```
> Without setting any optional parameter, the `copy()` method will produce an exact copy of the original version.

## Exceptions
When the version parsing fails due to an invalid format, the library throws a specific `VersionFormatException`.
Similarly, when the constraint parsing fails the library throws a `ConstraintFormatException`.
> The `toVersionOrNull()` and `toConstraintOrNull()` methods can be used for exception-less conversions as they return `null` when the parsing fails.