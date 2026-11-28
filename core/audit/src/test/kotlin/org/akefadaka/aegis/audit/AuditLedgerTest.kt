package org.akefadaka.aegis.audit

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditLedgerTest {
    private class Store : ProtectedAuditStorage {
        val rows = mutableListOf<ChainedAuditRecord>()
        var unavailable = false
        var rejectNextAppend = false

        override fun readSnapshot(): List<ChainedAuditRecord> {
            if (unavailable) throw IllegalStateException("storage unavailable")
            return rows.toList()
        }

        override fun appendIfSequence(
            expectedLastSequence: Long,
            record: ChainedAuditRecord
        ): Boolean {
            if (unavailable) throw IllegalStateException("storage unavailable")
            if (rejectNextAppend) {
                rejectNextAppend = false
                return false
            }
            val actualLast = rows.lastOrNull()?.sequence ?: 0L
            if (actualLast != expectedLastSequence) return false
            rows.add(record)
            return true
        }
    }

    private fun event(
        id: String = "00000000-0000-4000-8000-000000000001",
        outcome: AuditOutcome = AuditOutcome.DENY,
        reason: AuditReason = AuditReason.AUTHORIZATION_DENIED,
        time: Long = 10
    ) = AuditEvent(
        eventId = id,
        eventType = AuditEventType.POLICY_DECISION,
        actorRef = "a".repeat(64),
        outcome = outcome,
        occurredAtEpochMillis = time,
        reason = reason,
        subjectRef = "b".repeat(64),
        correlationRef = "c".repeat(64)
    )

    @Test
    fun appendsTypedEventsAndVerifiesMultiRecordChain() {
        val store = Store()
        val ledger = AuditLedger(store)

        val first = ledger.append(event())
        val second = ledger.append(
            event(
                id = "00000000-0000-4000-8000-000000000002",
                outcome = AuditOutcome.ALLOW,
                reason = AuditReason.NONE,
                time = 11
            )
        )
        val third = ledger.append(
            event(
                id = "00000000-0000-4000-8000-000000000003",
                outcome = AuditOutcome.ESCALATE,
                reason = AuditReason.CAPABILITY_UNAVAILABLE,
                time = 12
            )
        )

        assertTrue(first.appended)
        assertTrue(second.appended)
        assertTrue(third.appended)
        assertEquals(listOf(1L, 2L, 3L), store.rows.map { it.sequence })
        assertEquals(AuditFailureState.INTACT, ledger.verify().state)
        assertEquals(3L, ledger.verify().lastVerifiedSequence)
    }

    @Test
    fun detectsEventTamperingAndRefusesFurtherAppend() {
        val store = Store()
        val ledger = AuditLedger(store)
        assertTrue(ledger.append(event()).appended)

        store.rows[0] = store.rows[0].copy(
            event = event(outcome = AuditOutcome.ALLOW)
        )

        assertEquals(AuditFailureState.TAMPERED, ledger.verify().state)
        val rejected = ledger.append(event(id = "00000000-0000-4000-8000-000000000002"))
        assertFalse(rejected.appended)
        assertEquals(AuditFailureState.TAMPERED, rejected.state)
        assertEquals(1, store.rows.size)
    }

    @Test
    fun detectsDigestAndPreviousLinkTampering() {
        val store = Store()
        val ledger = AuditLedger(store)
        assertTrue(ledger.append(event()).appended)
        assertTrue(
            ledger.append(event(id = "00000000-0000-4000-8000-000000000002", time = 11)).appended
        )

        store.rows[1] = store.rows[1].copy(previousDigest = "0".repeat(64))
        assertEquals(AuditFailureState.TAMPERED, ledger.verify().state)
    }

    @Test
    fun detectsMissingAndOutOfOrderSequencesAsGap() {
        val store = Store()
        val ledger = AuditLedger(store)
        repeat(3) { index ->
            assertTrue(
                ledger.append(
                    event(
                        id = "00000000-0000-4000-8000-${(index + 1).toString().padStart(12, '0')}",
                        time = 10L + index
                    )
                ).appended
            )
        }

        store.rows.removeAt(1)
        assertEquals(AuditFailureState.SEQUENCE_GAP, ledger.verify().state)
    }

    @Test
    fun refusesAppendWhenStorageReportsSequenceGap() {
        val store = Store()
        val ledger = AuditLedger(store)
        assertTrue(ledger.append(event()).appended)
        store.rows[0] = store.rows[0].copy(sequence = 2)

        val result = ledger.append(event(id = "00000000-0000-4000-8000-000000000002"))
        assertEquals(AuditFailureState.SEQUENCE_GAP, result.state)
        assertEquals(1, store.rows.size)
    }

    @Test
    fun failsClosedWhenStorageIsUnavailableForReadOrAppend() {
        val store = Store()
        val ledger = AuditLedger(store)
        store.unavailable = true

        assertEquals(AuditFailureState.STORAGE_UNAVAILABLE, ledger.verify().state)
        val result = ledger.append(event())
        assertEquals(AuditFailureState.STORAGE_UNAVAILABLE, result.state)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun detectsAtomicAppendConflictWithoutClaimingSuccess() {
        val store = Store()
        val ledger = AuditLedger(store)
        store.rejectNextAppend = true

        val result = ledger.append(event())
        assertEquals(AuditFailureState.APPEND_CONFLICT, result.state)
        assertFalse(result.appended)
        assertNull(result.record)
        assertTrue(store.rows.isEmpty())
    }

    @Test
    fun eventSchemaRejectsRawIdentifiersAndInvalidTimes() {
        try {
            event().copy(actorRef = "user@example.com")
            org.junit.Assert.fail("raw actor identifier accepted")
        } catch (_: IllegalArgumentException) {
            // Expected: actor references must be opaque SHA-256-shaped identifiers.
        }

        try {
            event().copy(subjectRef = "password=hunter2")
            org.junit.Assert.fail("free-form subject accepted")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }

        try {
            event().copy(occurredAtEpochMillis = -1)
            org.junit.Assert.fail("negative event time accepted")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    @Test
    fun redactsCredentialAssignmentsBearerTokensJwtsAndPrivateKeys() {
        val jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIn0.signature"
        val privateKey = "-----BEGIN PRIVATE KEY-----\nprivate-material\n-----END PRIVATE KEY-----"
        val input = "password=hunter2 api_key=key-123 Authorization=Bearer abc.def.ghi " +
            "token=tok-456 otp=123456 jwt=$jwt $privateKey"

        val redacted = AuditSecretRedactor.redact(input)

        assertTrue(redacted.contains("password=[REDACTED]"))
        assertTrue(redacted.contains("api_key=[REDACTED]"))
        assertTrue(redacted.contains("Bearer [REDACTED]"))
        assertTrue(redacted.contains("token=[REDACTED]"))
        assertTrue(redacted.contains("otp=[REDACTED]"))
        assertTrue(redacted.contains("[REDACTED_JWT]"))
        assertTrue(redacted.contains("[REDACTED_PRIVATE_KEY]"))
        listOf("hunter2", "key-123", "abc.def.ghi", "tok-456", "123456", jwt, "private-material")
            .forEach { assertFalse("secret leaked: $it", redacted.contains(it)) }
    }

    @Test
    fun redactorLeavesNonSecretDiagnosticTextReadable() {
        val input = "policy decision denied: POLICY_STALE; capability unavailable"
        assertEquals(input, AuditSecretRedactor.redact(input))
    }
}
