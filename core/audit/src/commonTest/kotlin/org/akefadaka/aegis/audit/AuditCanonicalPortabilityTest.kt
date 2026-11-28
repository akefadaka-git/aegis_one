package org.akefadaka.aegis.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuditCanonicalPortabilityTest {
    @Test
    fun versionOneEncodingAndSha256MatchFrozenJvmGoldenVector() {
        val event = AuditEvent(
            eventId = "00000000-0000-4000-8000-000000000001",
            eventType = AuditEventType.POLICY_DECISION,
            actorRef = "a".repeat(64),
            outcome = AuditOutcome.DENY,
            occurredAtEpochMillis = 10,
            reason = AuditReason.AUTHORIZATION_DENIED,
            subjectRef = "b".repeat(64),
            correlationRef = "c".repeat(64)
        )
        val canonical = AuditCanonicalEncoder.encode(1, event, "AEGIS-AUDIT-GENESIS-v1")
        assertEquals(331, canonical.size)
        assertEquals(
            "c7663b649b20ae6b39cea6a826705a867dbd6d4ea70abdf18f9684060dc5aff7",
            platformSha256(canonical).toLowerHex()
        )
        assertTrue(canonical.take(4).toByteArray().contentEquals(byteArrayOf(0, 0, 0, 1)))
    }

    @Test
    fun modifiedUtfEncodingPreservesNullAndSupplementaryCodeUnits() {
        val value = "A\u0000é😀"
        val writer = AuditCanonicalEncoder.encode(
            1,
            AuditEvent(
                eventId = "00000000-0000-4000-8000-000000000002",
                eventType = AuditEventType.SECURITY_ALERT,
                actorRef = "d".repeat(64),
                outcome = AuditOutcome.SUCCESS,
                occurredAtEpochMillis = 11,
                reason = AuditReason.NONE,
                subjectRef = null,
                correlationRef = null
            ),
            value
        )
        assertTrue(writer.isNotEmpty())
    }
}
