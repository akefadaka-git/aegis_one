package org.akefadaka.aegis.audit

/**
 * Version-1 canonical encoder. This intentionally reproduces Java DataOutputStream's
 * big-endian primitives and modified-UTF writeUTF representation byte-for-byte.
 * Keep this format frozen: changing it requires a new digest format version and migration.
 */
object AuditCanonicalEncoder {
    fun encode(sequence: Long, event: AuditEvent, previousDigest: String): ByteArray {
        require(sequence > 0)
        require(previousDigest.isNotBlank())
        val writer = CanonicalWriter()
        writer.writeInt(DIGEST_FORMAT_VERSION)
        writer.writeLong(sequence)
        writer.writeModifiedUtf(event.eventId)
        writer.writeInt(event.schemaVersion)
        writer.writeModifiedUtf(event.eventType.name)
        writer.writeModifiedUtf(event.actorRef)
        writer.writeBoolean(event.subjectRef != null)
        event.subjectRef?.let(writer::writeModifiedUtf)
        writer.writeBoolean(event.correlationRef != null)
        event.correlationRef?.let(writer::writeModifiedUtf)
        writer.writeModifiedUtf(event.outcome.name)
        writer.writeModifiedUtf(event.reason.name)
        writer.writeLong(event.occurredAtEpochMillis)
        writer.writeModifiedUtf(previousDigest)
        return writer.toByteArray()
    }

    private const val DIGEST_FORMAT_VERSION = 1

    private class CanonicalWriter {
        private val output = ArrayList<Byte>()

        fun writeBoolean(value: Boolean) {
            output.add(if (value) 1 else 0)
        }

        fun writeInt(value: Int) {
            output.add((value ushr 24).toByte())
            output.add((value ushr 16).toByte())
            output.add((value ushr 8).toByte())
            output.add(value.toByte())
        }

        fun writeLong(value: Long) {
            for (shift in 56 downTo 0 step 8) output.add((value ushr shift).toByte())
        }

        fun writeModifiedUtf(value: String) {
            var encodedLength = 0
            for (character in value) {
                val code = character.code
                encodedLength += when {
                    code in 1..0x7f -> 1
                    code <= 0x07ff -> 2
                    else -> 3
                }
            }
            require(encodedLength <= 0xffff) { "Canonical UTF field exceeds the version-1 limit" }
            output.add((encodedLength ushr 8).toByte())
            output.add(encodedLength.toByte())
            for (character in value) {
                val code = character.code
                when {
                    code in 1..0x7f -> output.add(code.toByte())
                    code <= 0x07ff -> {
                        output.add((0xc0 or ((code shr 6) and 0x1f)).toByte())
                        output.add((0x80 or (code and 0x3f)).toByte())
                    }
                    else -> {
                        output.add((0xe0 or ((code shr 12) and 0x0f)).toByte())
                        output.add((0x80 or ((code shr 6) and 0x3f)).toByte())
                        output.add((0x80 or (code and 0x3f)).toByte())
                    }
                }
            }
        }

        fun toByteArray(): ByteArray = output.toByteArray()
    }
}

/** Platform implementation must return exactly 32 bytes (SHA-256). */
expect fun platformSha256(input: ByteArray): ByteArray

internal fun ByteArray.toLowerHex(): String {
    val alphabet = "0123456789abcdef"
    val result = StringBuilder(size * 2)
    for (byte in this) {
        val value = byte.toInt() and 0xff
        result.append(alphabet[value ushr 4])
        result.append(alphabet[value and 0x0f])
    }
    return result.toString()
}
