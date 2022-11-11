# kotlin-semver

[![Maven Central](https://img.shields.io/maven-central/v/io.github.z4kn4fein/semver?label=maven%20central)](https://search.maven.org/artifact/io.github.z4kn4fein/semver/)
[![Snapshot](https://img.shields.io/nexus/s/io.github.z4kn4fein/semver?label=snapshot&server=https%3A%2F%2Fs01.oss.sonatype.org)](https://s01.oss.sonatype.org/content/repositories/snapshots/io/github/z4kn4fein/semver/)
[![Build](https://img.shields.io/github/workflow/status/z4kn4fein/kotlin-semver/Semver%20CI?logo=GitHub)](https://github.com/z4kn4fein/kotlin-semver/actions/workflows/semver-ci.yml)
[![Quality Gate Status](https://img.shields.io/sonar/quality_gate/z4kn4fein_kotlin-semver?logo=SonarCloud&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=z4kn4fein_kotlin-semver)
[![SonarCloud Coverage](https://img.shields.io/sonar/coverage/z4kn4fein_kotlin-semver?logo=SonarCloud&server=https%3A%2F%2Fsonarcloud.io)](https://sonarcloud.io/project/overview?id=z4kn4fein_kotlin-semver)
[![Kotlin](https://img.shields.io/badge/kotlin-1.6-blueviolet.svg?logo=kotlin)](http://kotlinlang.org)

Semantic Versioning library for [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html).
It implements the full [semantic version 2.0.0](https://semver.org/spec/v2.0.0.html) specification and 
provides the ability to **parse**, **compare**, and **increment** semantic versions along with validation against **constraints**.

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
    implementation("io.github.z4kn4fein:semver:1.3.3")
}
```
In case of a multiplatform project, you can simply reference the package in your `commonMain` source set.
```kotlin
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies { 
                implementation("io.github.z4kn4fein:semver:1.3.3")
            }
        }
    }
}
```
You can also use platform-specific packages that you can find [here](https://search.maven.org/search?q=g:io.github.z4kn4fein%20AND%20a:semver*) for each supported platform.

## Usage
The following options are available to construct a [`Version`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/-version/index.html):
1. Building part by part.

   ```kotlin
   Version(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")
   ```
   
2. Parsing from a string with [`Version.parse()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/-version/-companion/parse.html).

   ```kotlin
   Version.parse("3.5.2-alpha+build")
   ```
   
3. Using the [`toVersion()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/to-version.html) or [`toVersionOrNull()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/to-version-or-null.html) extension method on a string.

   ```kotlin
   "3.5.2-alpha+build".toVersion()
   ```

The constructed `Version` object provides the following information:
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

version.withoutSuffixes().toString()   // "3.5.2"
```

### Destructuring
`Version` supports destructuring by its public properties.
```kotlin
val version = "2.3.1-alpha.2+build".toVersion()
val (major, minor, patch, preRelease, buildMetadata) = version

// major: 2
// minor: 3
// patch: 1
// preRelease: "alpha.2"
// buildMetadata: "build"
```

### Strict vs. Loose Parsing
By default, the version parser considers partial versions like `1.0` and versions starting with the `v` prefix invalid.
This behaviour can be turned off by setting the `strict` parameter to `false`.
```kotlin
"v2.3-alpha".toVersion()                    // exception
"2.1".toVersion()                           // exception
"v3".toVersion()                            // exception

"v2.3-alpha".toVersion(strict = false)      // 2.3.0-alpha
"2.1".toVersion(strict = false)             // 2.1.0
"v3".toVersion(strict = false)              // 3.0.0
```

## Compare

It is possible to compare two `Version` objects with [comparison operators](https://kotlinlang.org/docs/operator-overloading.html#comparison-operators) or with the `.compareTo()` method.
```kotlin
"0.1.0".toVersion() < "0.1.1".toVersion()                   // true
"0.1.1".toVersion() <= "0.1.1".toVersion()                  // true
"0.1.0-alpha.3".toVersion() < "0.1.0-alpha.4".toVersion()   // true

"0.1.1".toVersion().compareTo("0.1.0".toVersion())          //  1
"0.1.0".toVersion().compareTo("0.1.1".toVersion())          // -1
"0.1.1".toVersion().compareTo("0.1.1".toVersion())          //  0
```

The equality of two `Version` objects can be determined with [equality operators](https://kotlinlang.org/docs/operator-overloading.html#equality-and-inequality-operators) or with the `equals()` method.
```kotlin
"0.1.1".toVersion() == "0.1.1".toVersion()       // true
"0.1.1".toVersion() != "0.1.1".toVersion()       // false

"0.1.1".toVersion().equals("0.1.1".toVersion())  // true
"0.1.0".toVersion().equals("0.1.1".toVersion())  // false
```

### Sort
As `Version` objects are comparable, you can get a sorted collection from them.
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
Having an order provides the ability to determine whether a `Version` is in the range between two other versions.
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
The condition `>=1.2.0` would be met by any version that greater than or equal to `1.2.0`.

Supported comparison operators:
- `=` Equal (equivalent to no operator: `1.2.0` means `=1.2.0`)
- `!=` Not equal
- `<` Less than
- `<=` Less than or equal
- `>` Greater than
- `>=` Greater than or equal

Conditions can be joined together with whitespace, representing the `AND` logical operator between them.
The `OR` operator can be expressed with `||` between condition sets.

For example, the constraint `>=1.2.0 <3.0.0 || >4.0.0` translates to: *Only those versions are allowed that are either greater than or 
equal to `1.2.0` {**AND**} less than `3.0.0` {**OR**} greater than `4.0.0`*.

We can notice that the first part of the previous constraint (`>=1.2.0 <3.0.0`) is a simple semantic version range.
There are more ways to express version ranges; the following section will go through all the available options.

### Range Conditions
There are particular range indicators which are sugars for more extended range expressions.

- **X-Range**: The `x`, `X`, and `*` characters can be used as a wildcard for the numeric parts of a version.
   - `1.2.x` translates to `>=1.2.0 <1.3.0-0`
   - `1.x` translates to `>=1.0.0 <2.0.0-0`
   - `*` translates to `>=0.0.0`

  In partial version expressions, the missing numbers are treated as wildcards.
   - `1.2` means `1.2.x` which finally translates to `>=1.2.0 <1.3.0-0`
   - `1` means `1.x` or `1.x.x` which finally translates to `>=1.0.0 <2.0.0-0`

- **Hyphen Range**: Describes an inclusive version range. Wildcards are evaluated and taken into account in the final range.
   - `1.0.0 - 1.2.0` translates to `>=1.0.0 <=1.2.0`
   - `1.1 - 1.4.0` means `>=(>=1.1.0 <1.2.0-0) <=1.4.0` which finally translates to `>=1.1.0 <=1.4.0`
   - `1.1.0 - 2` means `>=1.1.0 <=(>=2.0.0 <3.0.0-0)` which finally translates to `>=1.1.0 <3.0.0-0`

- **Tilde Range (`~`)**: Describes a patch level range when the minor version is specified or a minor level range when it's not.
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
The following options are available to construct a [`Constraint`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/-constraint/index.html):
- Parsing from a string with [`Constraint.parse()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/-constraint/-companion/parse.html).
   ```kotlin
   Constraint.parse(">=1.2.0")
   ```

- Using the [`toConstraint()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/to-constraint.html) or [`toConstraintOrNull()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/to-constraint-or-null.html) extension method on a string.
   ```kotlin
   ">=1.2.0".toConstraint()
   ```

Let's see how we can determine whether a version [`satisfies`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/satisfies.html) a constraint or not.
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
> **Note**:
> With [`satisfiesAll`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/satisfies-all.html) the version must satisfy each constraint within the collection. 
> With [`satisfiesAny`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/satisfies-any.html) it must satisfy at least one constraint to pass the validation.

Or to validate a collection of versions.
```kotlin
val constraint = ">=1.2.0".toConstraint()
val versions = listOf("1.2.1", "1.1.0").map { it.toVersion() }

constraint satisfiedByAll versions       // false
constraint satisfiedByAny versions       // true
```
> **Note**:
> With [`satisfiedByAll`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/satisfied-by-all.html) the constraint must be satisfied by each version within the collection. 
> With [`satisfiedByAny`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/satisfied-by-any.html) it must be satisfied by at least one version to pass the validation.

## Increment
`Version` objects can produce incremented versions of themselves with the [`nextMajor()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/next-major.html),
[`nextMinor()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/next-minor.html),
[`nextPatch()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/next-patch.html),
[`nextPreRelease()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/next-pre-release.html),
and [`inc()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/inc.html) methods.
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

val nextPreMajor = version.nextMajor(preRelease = "beta")           // 2.0.0-beta
val nextPreMinor = version.nextMinor(preRelease = "")               // 1.1.0-0
val nextPrePatch = version.nextPatch(preRelease = "alpha")          // 1.0.1-alpha
val nextPreRelease = version.nextPreRelease(preRelease = "alpha")   // 1.0.0-alpha.2

// or with the inc() method:
val incrementedByMajor = version.inc(by = Inc.MAJOR, preRelease = "beta")               // 2.0.0-beta
val incrementedByMinor = version.inc(by = Inc.MINOR, preRelease = "")                   // 1.1.0-0
val incrementedByPatch = version.inc(by = Inc.PATCH, preRelease = "alpha")              // 1.0.1-alpha
val incrementedByPreRelease = version.inc(by = Inc.PRE_RELEASE, preRelease = "alpha")   // 1.0.0-alpha.2
```

## Copy
It's possible to create a copy of a version with its [`copy()`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/-version/copy.html) method.
It allows altering the copied version's properties with optional parameters.
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
> **Note**:
> Without setting any optional parameter, the `copy()` method will produce an exact copy of the original version.

## Exceptions
When the version parsing fails due to an invalid format, the library throws a specific [`VersionFormatException`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver/-version-format-exception/index.html).
Similarly, when the constraint parsing fails, the library throws a [`ConstraintFormatException`](https://z4kn4fein.github.io/kotlin-semver/semver/io.github.z4kn4fein.semver.constraints/-constraint-format-exception/index.html).
> **Note**:
> The `toVersionOrNull()` and `toConstraintOrNull()` methods can be used for exception-less conversions as they return `null` when the parsing fails.

## Contact & Support
- Create an [issue](https://github.com/z4kn4fein/kotlin-semver/issues) for bug reports and feature requests.
- Start a [discussion](https://github.com/z4kn4fein/kotlin-semver/discussions) for your questions and ideas.
- Add a ⭐️ to support the project!
