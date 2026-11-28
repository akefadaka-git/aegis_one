package org.akefadaka.aegis.audit

enum class AuditEventType {
    POLICY_DECISION,
    CAPABILITY_CHANGE,
    OFFLINE_POLICY_ACCEPTED,
    OFFLINE_POLICY_REJECTED,
    IDENTITY_STATE_CHANGE,
    CONSENT_CHANGE,
    SECURITY_ALERT
}

enum class AuditOutcome { ALLOW, DENY, REQUIRE_APPROVAL, ESCALATE, SUCCESS, FAILURE }

enum class AuditReason {
    NONE,
    POLICY_MISSING,
    POLICY_STALE,
    POLICY_EXPIRED,
    POLICY_SIGNATURE_INVALID,
    POLICY_ISSUER_UNTRUSTED,
    POLICY_ROLLBACK,
    POLICY_SCOPE_MISMATCH,
    CAPABILITY_UNAVAILABLE,
    CAPABILITY_STALE,
    IDENTITY_UNAVAILABLE,
    IDENTITY_REVOKED,
    IDENTITY_RESTRICTED,
    AUTHORIZATION_DENIED,
    CONSENT_REQUIRED,
    INTEGRITY_FAILURE,
    STORAGE_FAILURE,
    OTHER_CODED_REASON
}

/**
 * Minimal, typed event contract. References are one-way pseudonymous identifiers;
 * arbitrary messages, request bodies, credentials, device serials and network content
 * are deliberately absent from the schema.
 */
data class AuditEvent(
    val eventId: String,
    val eventType: AuditEventType,
    val actorRef: String,
    val outcome: AuditOutcome,
    val occurredAtEpochMillis: Long,
    val reason: AuditReason = AuditReason.NONE,
    val subjectRef: String? = null,
    val correlationRef: String? = null,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION
) {
    init {
        require(UUID_PATTERN.matches(eventId))
        require(OPAQUE_REF_PATTERN.matches(actorRef))
        require(subjectRef == null || OPAQUE_REF_PATTERN.matches(subjectRef))
        require(correlationRef == null || OPAQUE_REF_PATTERN.matches(correlationRef))
        require(occurredAtEpochMillis >= 0)
        require(schemaVersion == CURRENT_SCHEMA_VERSION)
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        private val UUID_PATTERN = Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
        private val OPAQUE_REF_PATTERN = Regex("[a-f0-9]{64}")
    }
}

data class ChainedAuditRecord(
    val sequence: Long,
    val event: AuditEvent,
    val previousDigest: String,
    val digest: String
) {
    init {
        require(sequence > 0)
        require(previousDigest.isNotBlank() && digest.matches(Regex("[a-f0-9]{64}")))
    }
}

/**
 * Protected persistence boundary.
 *
 * Production adapters must encrypt/authenticate records at rest, restrict reads and
 * writes to the security service, durably commit before returning, and implement the
 * sequence compare-and-append as one atomic transaction. No plaintext fallback is allowed.
 */
interface ProtectedAuditStorage {
    /** Returns the committed ordered snapshot or throws when storage is unavailable/corrupt. */
    fun readSnapshot(): List<ChainedAuditRecord>

    /**
     * Atomically append only when the committed last sequence equals expectedLastSequence.
     * Return false on concurrent modification; do not partially commit.
     */
    fun appendIfSequence(
        expectedLastSequence: Long,
        record: ChainedAuditRecord
    ): Boolean
}

enum class AuditFailureState {
    INTACT,
    SEQUENCE_GAP,
    TAMPERED,
    STORAGE_UNAVAILABLE,
    APPEND_CONFLICT
}

data class AuditVerification(
    val state: AuditFailureState,
    val lastVerifiedSequence: Long
) {
    val isIntact: Boolean get() = state == AuditFailureState.INTACT
}

data class AuditAppendResult(
    val state: AuditFailureState,
    val record: ChainedAuditRecord? = null
) {
    val appended: Boolean get() = state == AuditFailureState.INTACT && record != null
}

/**
 * Hash-chained local ledger. Integrity verification is performed before every append.
 * Atomicity across processes/devices is the storage adapter's responsibility.
 */
class AuditLedger(private val storage: ProtectedAuditStorage) {
    fun append(event: AuditEvent): AuditAppendResult {
        val verification = verify()
        if (!verification.isIntact) return AuditAppendResult(verification.state)

        val rows = try {
            storage.readSnapshot()
        } catch (_: RuntimeException) {
            return AuditAppendResult(AuditFailureState.STORAGE_UNAVAILABLE)
        }

        if (rows.any { it.event.eventId == event.eventId }) {
            return AuditAppendResult(AuditFailureState.APPEND_CONFLICT)
        }
        val lastSequence = rows.lastOrNull()?.sequence ?: 0L
        if (lastSequence != verification.lastVerifiedSequence) {
            return AuditAppendResult(AuditFailureState.APPEND_CONFLICT)
        }
        if (lastSequence == Long.MAX_VALUE) {
            return AuditAppendResult(AuditFailureState.APPEND_CONFLICT)
        }

        val previousDigest = rows.lastOrNull()?.digest ?: GENESIS_DIGEST
        val sequence = lastSequence + 1
        val record = ChainedAuditRecord(
            sequence = sequence,
            event = event,
            previousDigest = previousDigest,
            digest = digest(sequence, event, previousDigest)
        )

        return try {
            if (storage.appendIfSequence(lastSequence, record)) {
                AuditAppendResult(AuditFailureState.INTACT, record)
            } else {
                AuditAppendResult(AuditFailureState.APPEND_CONFLICT)
            }
        } catch (_: RuntimeException) {
            AuditAppendResult(AuditFailureState.STORAGE_UNAVAILABLE)
        }
    }

    fun verify(): AuditVerification {
        val rows = try {
            storage.readSnapshot()
        } catch (_: RuntimeException) {
            return AuditVerification(AuditFailureState.STORAGE_UNAVAILABLE, 0)
        }

        var expectedSequence = 1L
        var previousDigest = GENESIS_DIGEST
        for (record in rows) {
            if (record.sequence != expectedSequence) {
                return AuditVerification(AuditFailureState.SEQUENCE_GAP, expectedSequence - 1)
            }
            if (record.previousDigest != previousDigest ||
                record.digest != digest(record.sequence, record.event, previousDigest)
            ) {
                return AuditVerification(AuditFailureState.TAMPERED, expectedSequence - 1)
            }
            previousDigest = record.digest
            if (expectedSequence == Long.MAX_VALUE) {
                return AuditVerification(AuditFailureState.SEQUENCE_GAP, expectedSequence)
            }
            expectedSequence++
        }
        return AuditVerification(AuditFailureState.INTACT, expectedSequence - 1)
    }

    private fun digest(sequence: Long, event: AuditEvent, previousDigest: String): String {
        val canonical = AuditCanonicalEncoder.encode(sequence, event, previousDigest)
        val result = platformSha256(canonical)
        require(result.size == 32) { "SHA-256 provider returned an invalid digest length" }
        return result.toLowerHex()
    }

    companion object {
        private const val DIGEST_FORMAT_VERSION = 1
        private const val GENESIS_DIGEST = "AEGIS-AUDIT-GENESIS-v1"
    }
}

/**
 * Defense-in-depth redactor for diagnostics at integration boundaries. AuditEvent itself
 * never accepts diagnostic strings; callers should persist only the resulting typed codes.
 */
object AuditSecretRedactor {
    private val assignment = Regex(
        "(?i)(password|passwd|pwd|token|access[_-]?token|refresh[_-]?token|secret|api[_-]?key|authorization|otp|pin)\\s*[:=]\\s*([^\\s,;]+)"
    )
    private val bearer = Regex("(?i)\\bBearer\\s+[A-Za-z0-9._~+/=-]+")
    private val jwt = Regex("\\beyJ[A-Za-z0-9_-]+\\.eyJ[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\b")
    private val privateKeyBlock = Regex(
        "-----BEGIN [A-Z ]*PRIVATE KEY-----[\\s\\S]*?-----END [A-Z ]*PRIVATE KEY-----"
    )

    fun redact(input: String): String {
        var result = bearer.replace(input, "Bearer [REDACTED]")
        result = assignment.replace(result) { match -> "${match.groupValues[1]}=[REDACTED]" }
        result = jwt.replace(result, "[REDACTED_JWT]")
        result = privateKeyBlock.replace(result, "[REDACTED_PRIVATE_KEY]")
        return result
    }
}
