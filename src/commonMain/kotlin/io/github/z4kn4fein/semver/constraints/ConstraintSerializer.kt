package io.github.z4kn4fein.semver.constraints

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes a [Constraint] as its string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.serialization
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.deserialization
 */
public object ConstraintSerializer : KSerializer<Constraint> {
    override fun deserialize(decoder: Decoder): Constraint = decoder.decodeString().toConstraint()

    override fun serialize(
        encoder: Encoder,
        value: Constraint,
    ): Unit = encoder.encodeString(value.toString())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Constraint", PrimitiveKind.STRING)
}

/**
 * Built-in [kotlinx.serialization] serializer that encodes and decodes a [Constraint] in Maven-style string representation.
 *
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.mavenSerialization
 * @sample io.github.z4kn4fein.semver.samples.ConstraintSamples.mavenDeserialization
 */
public object MavenConstraintSerializer : KSerializer<Constraint> {
    override fun deserialize(decoder: Decoder): Constraint = decoder.decodeString().toMavenConstraint()

    override fun serialize(
        encoder: Encoder,
        value: Constraint,
    ): Unit = encoder.encodeString(value.toMavenFormat())

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Constraint", PrimitiveKind.STRING)
}
