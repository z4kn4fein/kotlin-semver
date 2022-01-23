# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.2.0] - 2022-01-23
### Added
- New `withoutSuffixes()` extension for `Version` which makes a copy without the pre-release and build metadata identities.
- Support for parsing loose versions.
- Constraints are now recognising the `v` prefix in conditions like: `>=v1.2`.

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

[1.1.0]: https://github.com/z4kn4fein/kotlin-semver/compare/1.0.0...1.1.0