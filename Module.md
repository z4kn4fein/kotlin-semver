# Module semver
Semantic Versioning library for [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html).
It fully supports the [semver 2.0.0](https://semver.org/spec/v2.0.0.html) standards and
provides ability to **parse**, **compare**, and **increment** semantic versions.

## Install with Gradle
The library is available in Maven Central, so you have to add it to your repositories.
```kotlin
repositories {
    mavenCentral()
}
```
<br/>

Then, you can add the package to the dependencies list.
```kotlin
dependencies {
    implementation("io.github.z4kn4fein:semver:1.0.0")
}
```

## Usage
The following options are supported to construct a `Version`:
- Building part by part.
   ```kotlin
   Version(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")
   ```  
<br/>

- Parsing from a string with `Version.parse()`.
   ```kotlin
   Version.parse("3.5.2-alpha+build")
   ```  
<br/>

- Using the `toVersion()` or `toVersionOrNull()` extension methods on a string.
   ```kotlin
   "3.5.2-alpha+build".toVersion()
   ```  
<br/>

The following information is accessible on a constructed `Version` object:
```kotlin
val version = "3.5.2-alpha.2+build".toVersion()
version.major           // 3
version.minor           // 5
version.patch           // 2
version.preRelease      // "alpha.2"
version.buildMetadata   // "build"

version.isPreRelease    // true
version.toString()      // "3.5.2-alpha.2+build"
```
<br/>

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

## Compare / Sort / Range

It is possible to compare two `Version` objects with [comparison operators](https://kotlinlang.org/docs/operator-overloading.html#comparison-operators) or with `.compareTo()`.
```kotlin
"0.1.0".toVersion() < "0.1.1".toVersion()                   // true
"0.1.1".toVersion() <= "0.1.1".toVersion()                  // true
"0.1.0-alpha.3".toVersion() < "0.1.0-alpha.4".toVersion()   // true

"0.1.1".toVersion().compareTo("0.1.0".toVersion())          //  1
"0.1.0".toVersion().compareTo("0.1.1".toVersion())          // -1
"0.1.1".toVersion().compareTo("0.1.1".toVersion())          //  0
```
<br/>

The equality of two `Version` objects can be determined with [equality operators](https://kotlinlang.org/docs/operator-overloading.html#equality-and-inequality-operators) or with `equals()`.
```kotlin
"0.1.1".toVersion() == "0.1.1".toVersion()       // true
"0.1.1".toVersion() != "0.1.1".toVersion()       // false

"0.1.1".toVersion().equals("0.1.1".toVersion())  // true
"0.1.0".toVersion().equals("0.1.1".toVersion())  // false
```
<br/>

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

// The content will be:
//   "1.0.1-alpha"
//   "1.0.1-alpha.2"
//   "1.0.1-alpha.3"
//   "1.0.1-alpha.beta"
//   "1.0.1"
//   "1.1.0"
//   "1.1.0+build"
```
<br/>

Having an order provides the ability to determine whether a `Version` is in the range between two given versions.
```kotlin
val range = "1.0.0".toVersion().."1.1.0".toVersion()

"1.0.1".toVersion() in range          // true
"1.1.1".toVersion() in range          // false
```

## Increment
`Version` objects can produce incremented versions of themselves with the `next{Major|Minor|Patch|PreRelease}` methods.
These methods can be used to determine the next version in order incremented by the according part.
```kotlin
val stableVersion = "1.0.0".toVersion()

val nextStableMajor = stableVersion.nextMajor()                 // 2.0.0
val nextStableMinor = stableVersion.nextMinor()                 // 1.1.0
val nextStablePatch = stableVersion.nextPatch()                 // 1.0.1
val nextStablePreRelease = stableVersion.nextPreRelease()       // 1.0.1-0

val unstableVersion = "1.0.0-alpha.2+build.1".toVersion()

val nextUnstableMajor = unstableVersion.nextMajor()             // 2.0.0
val nextUnstableMinor = unstableVersion.nextMinor()             // 1.1.0
val nextUnstablePatch = unstableVersion.nextPatch()             // 1.0.0
val nextUnstablePreRelease = unstableVersion.nextPreRelease()   // 1.0.0-alpha.3
```
> `Version` objects are immutable, so each incrementing function creates a new `Version`.

## Copy
It's possible to make a copy of a particular version with the `copy()` method.
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
> Without setting any optional parameter, the `copy()` method will produce an exact copy of the original version.

## Invalid Versions
When the version parsing fails due to an invalid format, the library throws a specific `VersionFormatException`.
> The `toVersionOrNull()` method can be used for exception-less conversions as it returns `null` when the version parsing fails.