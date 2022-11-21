# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.0] - 2022-11-21
### Added
- Support of `|` as OR constraint operator.

### Changed
- Kotlin version to `1.7.21`.

## [1.3.3] - 2022-05-22
### Added
- Missing JS IR target.

## [1.3.2] - 2022-05-21
### Fixed
- Incorrect multi-target generation.

## [1.3.1] - 2022-05-20
### Fixed
- Mysterious missing target issue. [#4](https://github.com/z4kn4fein/kotlin-semver/issues/4)

## [1.3.0] - 2022-05-18
### Added
- `kotlinx.serialization` support with predefined serializers.
- Equality operator for `Constraint`.

## [1.2.1] - 2022-01-24
### Fixed
- There was a case where the `ConstraintFormatException` did not contain the invalid Constraint's details.

## [1.2.0] - 2022-01-23
### Added
- New `withoutSuffixes()` extension for `Version` which makes a copy without the pre-release and build metadata identities.
- Support for parsing loose versions.
- Constraints are now recognizing the `v` prefix in conditions like: `>=v1.2`.

### Fixed
- During version incrementation the `preRelease` argument was not validated.

## [1.1.0] - 2022-01-16
### Added
- New incrementation method `inc()`.
- Optional `preRelease` argument for `next{Major|Minor|Patch|PreRelease}()` methods.
- [Constraints](https://github.com/z4kn4fein/kotlin-semver#constraints).
- Live code samples in [API documentation](https://z4kn4fein.github.io/kotlin-semver/).

## [1.0.0] - 2022-01-09
- First stable release

[1.4.0]: https://github.com/z4kn4fein/kotlin-semver/compare/1.3.3...1.4.0
[1.3.3]: https://github.com/z4kn4fein/kotlin-semver/compare/1.3.2...1.3.3
[1.3.2]: https://github.com/z4kn4fein/kotlin-semver/compare/1.3.1...1.3.2
[1.3.1]: https://github.com/z4kn4fein/kotlin-semver/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/z4kn4fein/kotlin-semver/compare/1.2.1...1.3.0
[1.2.1]: https://github.com/z4kn4fein/kotlin-semver/compare/1.2.0...1.2.1
[1.2.0]: https://github.com/z4kn4fein/kotlin-semver/compare/1.1.0...1.2.0
[1.1.0]: https://github.com/z4kn4fein/kotlin-semver/compare/1.0.0...1.1.0