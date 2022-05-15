package io.github.z4kn4fein.semver

import io.github.z4kn4fein.semver.constraints.Constraint
import io.github.z4kn4fein.semver.constraints.toConstraint
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes [Version] as its string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.serialization
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.deserialization
 */
public object VersionSerializer : KSerializer<Version> {
    override fun deserialize(decoder: Decoder): Version = decoder.decodeString().toVersion()
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Version", PrimitiveKind.STRING)
}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes non-strict [Version] as its string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.serialization
 * @sample io.github.z4kn4fein.semver.samples.VersionSamples.deserialization
 */
public object LooseVersionSerializer : KSerializer<Version> {
    override fun deserialize(decoder: Decoder): Version = decoder.decodeString().toVersion(strict = false)
    override fun serialize(encoder: Encoder, value: Version): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LooseVersion", PrimitiveKind.STRING)
}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes [Constraint] as its string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.serialization
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.deserialization
 */
public object VersionConstraintSerializer : KSerializer<Constraint> {
    override fun deserialize(decoder: Decoder): Constraint = decoder.decodeString().toConstraint()
    override fun serialize(encoder: Encoder, value: Constraint): Unit = encoder.encodeString(value.toString())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Constraint", PrimitiveKind.STRING)
}
