# kotlin-semver (work in progress)

Semantic Version utility library for [Kotlin Multiplatform](https://kotlinlang.org/docs/mpp-intro.html). 
This library fully supports the [semver 2.0.0](https://semver.org/spec/v2.0.0.html) standards. 
It provides ability to **parse**, **compare/sort**, and produce **incremented** semantic versions.

## Install with Gradle
This library is available in Maven Central, so you have to add it to your repositories.
```kotlin
repositories {
    mavenCentral()
}
```
Then, you can add it to the dependencies list.
```kotlin
dependencies {
    implementation("io.github.z4kn4fein:semver:$version")
}
```

## Usage
The following options are supported to construct a `Version`:
1. Building part by part. 
   ```kotlin
   Version(major = 3, minor = 5, patch = 2, preRelease = "alpha", buildMetadata = "build")
   ```
2. Parsing from a version string with `Version.parse()`.
   ```kotlin
   Version.parse("3.5.2-alpha+build")
   ```
3. Using the `toVersion()` extension method on a version string.
   ```kotlin
   "3.5.2-alpha+build".toVersion()
   ```
The following information is accessible on a constructed `Version` object:
```kotlin
val version = "3.5.2-alpha.2+build".toVersion()
version.major           // 3
version.minor           // 5
version.patch           // 2
version.preRelease      // 'alpha.2'
version.buildMetadata   // 'build'
version.isStable        // false
```

### Compare / Sort
It is possible to compare two `Version` objects with [comparison operators](https://kotlinlang.org/docs/operator-overloading.html#comparison-operators) or with `.compareTo()`.
```kotlin
"0.1.1".toVersion() > "0.1.0".toVersion()                   // true
"0.1.1".toVersion() <= "0.1.1".toVersion()                  // true
"0.1.0-alpha.3".toVersion() < "0.1.0-alpha.4".toVersion()   // true

"0.1.1".toVersion().compareTo("0.1.0".toVersion())  // 1
"0.1.0".toVersion().compareTo("0.1.1".toVersion())  // -1
"0.1.1".toVersion().compareTo("0.1.1".toVersion())  // 0
```
The equality of two `Version` objects can be determined with [equality operators](https://kotlinlang.org/docs/operator-overloading.html#equality-and-inequality-operators) or with `equals()`.
```kotlin
"0.1.1".toVersion() == "0.1.1".toVersion()  // true
"0.1.1".toVersion() != "0.1.1".toVersion()  // false

"0.1.1".toVersion().equals("0.1.1".toVersion())  // true
"0.1.0".toVersion().equals("0.1.1".toVersion())  // false
```
As `Version` objects are comparable, they can be sorted in a collection.
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

// The content of the list variable will be:
//   "1.0.1-alpha"
//   "1.0.1-alpha.2"
//   "1.0.1-alpha.3"
//   "1.0.1-alpha.beta"
//   "1.0.1"
//   "1.1.0"
//   "1.1.0+build"
```

### Increment
A `Version` is able to produce an incremented version of itself. 
```kotlin
val version = "1.0.0-alpha.2+build.1".toVersion()

val nextMajor = version.nextMajor()             // 2.0.0
val nextMinor = version.nextMinor()             // 1.1.0
val nextPatch = version.nextPatch()             // 1.0.1
val nextPreRelease = version.nextPreRelease()   // 1.0.0-alpha.3
```
> `Version` objects are immutable, so every incremented version creates a new `Version`.

### Clone
It is possible to clone a `Version` with optionally different parts.
```kotlin
val version = "1.0.0-alpha.2+build.1".toVersion()

val withDifferentMajor = version.clone(major = 3)                          // 3.0.0
val withDifferentMinor = version.clone(minor = 4)                          // 1.4.0
val withDifferentPatch = version.clone(patch = 5)                          // 1.0.5
val withDifferentPreRelease = version.clone(preRelease = "alpha.4")        // 1.0.0-alpha.4
val withDifferentBuildMetadata = version.clone(buildMetadata = "build.3")  // 1.0.0-alpha.2+build.3
```
> Without setting any optional parameters, the `clone()` will produce an exact copy of the original version.

### Invalid Versions
When the version parsing fails due to an invalid format, the library throws a specific `VersionFormatException`.