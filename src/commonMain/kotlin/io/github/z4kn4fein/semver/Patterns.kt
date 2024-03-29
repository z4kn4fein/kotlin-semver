package io.github.z4kn4fein.semver

internal object Patterns {
    // Numeric identifier pattern. (used for parsing major, minor, and patch)
    private const val NUMERIC = "0|[1-9]\\d*"

    // Alphanumeric or hyphen pattern.
    private const val ALPHANUMERIC_OR_HYPHEN = "[0-9a-zA-Z-]"

    // Letter or hyphen pattern.
    private const val LETTER_OR_HYPHEN = "[a-zA-Z-]"

    // Non-numeric identifier pattern. (used for parsing pre-release)
    private const val NON_NUMERIC = "\\d*$LETTER_OR_HYPHEN$ALPHANUMERIC_OR_HYPHEN*"

    // Dot-separated numeric identifier pattern. (<major>.<minor>.<patch>)
    private const val CORE_VERSION = "($NUMERIC)\\.($NUMERIC)\\.($NUMERIC)"

    // Dot-separated loose numeric identifier pattern. (<major>(.<minor>)?(.<patch>)?)
    private const val LOOSE_CORE_VERSION = "($NUMERIC)(?:\\.($NUMERIC))?(?:\\.($NUMERIC))?"

    // Numeric or non-numeric pre-release part pattern.
    private const val PRE_RELEASE_PART = "(?:$NUMERIC|$NON_NUMERIC)"

    // Pre-release identifier pattern. A hyphen followed by dot-separated
    // numeric or non-numeric pre-release parts.
    private const val PRE_RELEASE = "(?:-($PRE_RELEASE_PART(?:\\.$PRE_RELEASE_PART)*))"

    // Build-metadata identifier pattern. A + sign followed by dot-separated
    // alphanumeric build-metadata parts.
    private const val BUILD = "(?:\\+($ALPHANUMERIC_OR_HYPHEN+(?:\\.$ALPHANUMERIC_OR_HYPHEN+)*))"

    // List of allowed operations in a condition.
    private const val ALLOWED_OPERATORS = "||=|!=|<|<=|=<|>|>=|=>|\\^|~>|~"

    // Numeric identifier pattern for parsing conditions.
    private const val X_RANGE_NUMERIC = "$NUMERIC|x|X|\\*"

    // X-RANGE version: 1.x | 1.2.* | 1.1.X
    private const val X_RANGE_VERSION =
        "($X_RANGE_NUMERIC)(?:\\.($X_RANGE_NUMERIC)(?:\\.($X_RANGE_NUMERIC)(?:$PRE_RELEASE)?$BUILD?)?)?"

    // Pattern that only matches numbers.
    internal const val ONLY_NUMBER_REGEX: String = "^[0-9]+$"

    // Pattern that only matches alphanumeric or hyphen characters.
    internal const val ONLY_ALPHANUMERIC_OR_HYPHEN_REGEX: String = "^$ALPHANUMERIC_OR_HYPHEN+$"

    // Version parsing pattern: 1.2.3-alpha+build
    internal const val VERSION_REGEX: String = "^$CORE_VERSION$PRE_RELEASE?$BUILD?\$"

    // Prefixed version parsing pattern: v1.2-alpha+build
    internal const val LOOSE_VERSION_REGEX: String = "^v?$LOOSE_CORE_VERSION$PRE_RELEASE?$BUILD?\$"

    // Operator condition: >=1.2.*
    internal const val OPERATOR_CONDITION_REGEX = "($ALLOWED_OPERATORS)\\s*v?(?:$X_RANGE_VERSION)"

    // Hyphen range condition: 1.2.* - 2.0.0
    internal const val HYPHEN_CONDITION_REGEX = "\\s*v?(?:$X_RANGE_VERSION)\\s+-\\s+v?(?:$X_RANGE_VERSION)\\s*"
}
