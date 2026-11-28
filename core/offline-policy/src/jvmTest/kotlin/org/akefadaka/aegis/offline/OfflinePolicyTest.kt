package org.akefadaka.aegis.offline

import org.akefadaka.aegis.security.SecurityTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflinePolicyTest {
    private class TestVersionStore : PolicyVersionStore {
        private val records = mutableMapOf<String, PolicyVersionRecord>()
        var rejectNextWrite = false

        override fun read(scope: String): PolicyVersionRecord? = records[scope]

        override fun compareAndSet(
            scope: String,
            expected: PolicyVersionRecord?,
            replacement: PolicyVersionRecord
        ): Boolean {
            if (rejectNextWrite) {
                rejectNextWrite = false
                return false
            }
            if (records[scope] != expected) return false
            records[scope] = replacement
            return true
        }

        fun seed(scope: String, record: PolicyVersionRecord) {
            records[scope] = record
        }
    }

    private val store = TestVersionStore()
    private val trustedIssuer = PolicyIssuerTrust { issuer, scope ->
        issuer == "issuer" && scope == "device"
    }
    private val validator = OfflinePolicyValidator(
        verifier = PolicySignatureVerifier { it.signature.contentEquals(byteArrayOf(1, 2, 3)) },
        issuerTrust = trustedIssuer,
        versionStore = store
    )

    private fun policy(
        id: String = "policy-1",
        version: Long = 2,
        issuedAt: Long = 1_000,
        expiresAt: Long = 999_999_999,
        tier: SecurityTier = SecurityTier.P1,
        scope: String = "device",
        issuer: String = "issuer",
        digest: String = "sha256:payload-1",
        signature: ByteArray = byteArrayOf(1, 2, 3)
    ) = CachedPolicy(id, version, issuedAt, expiresAt, tier, scope, issuer, digest, signature)

    @Test fun acceptsTrustedSignatureAndPersistsVersionAtomically() {
        val result = validator.validate(policy(), 2_000, TimeConfidence.TRUSTED, "device")

        assertEquals(PolicyState.ACCEPTED, result.state)
        assertEquals(1_000 + OfflinePolicyValidator.DEFAULT_P0_P1_MAX_AGE_MILLIS, result.effectiveUntilEpochMillis)
        assertEquals(PolicyVersionRecord("policy-1", 2, 1_000, "sha256:payload-1"), store.read("device"))
    }

    @Test fun capsP0AndP1AtSevenDaysAndP2AtTwentyFourHours() {
        val p1 = validator.validate(policy(), 2_000, TimeConfidence.TRUSTED, "device")
        val p2 = validator.validate(
            policy(id = "policy-2", version = 3, tier = SecurityTier.P2),
            2_000,
            TimeConfidence.TRUSTED,
            "device"
        )

        assertEquals(1_000 + 7L * 24 * 60 * 60 * 1000, p1.effectiveUntilEpochMillis)
        assertEquals(1_000 + 24L * 60 * 60 * 1000, p2.effectiveUntilEpochMillis)
    }

    @Test fun rejectsUncertainOrRolledBackClockAndNegativeTime() {
        assertEquals(
            PolicyState.REJECTED_CLOCK,
            validator.validate(policy(), 2_000, TimeConfidence.UNCERTAIN, "device").state
        )
        assertEquals(
            PolicyState.REJECTED_CLOCK,
            validator.validate(policy(), 2_000, TimeConfidence.ROLLBACK_DETECTED, "device").state
        )
        assertEquals(
            PolicyState.REJECTED_CLOCK,
            validator.validate(policy(), -1, TimeConfidence.TRUSTED, "device").state
        )
        assertNull(store.read("device"))
    }

    @Test fun rejectsWrongScopeAndUntrustedIssuerBeforeAcceptingPolicy() {
        assertEquals(
            PolicyState.REJECTED_SCOPE,
            validator.validate(policy(), 2_000, TimeConfidence.TRUSTED, "another-device").state
        )
        assertEquals(
            PolicyState.REJECTED_ISSUER,
            validator.validate(policy(issuer = "unknown-issuer"), 2_000, TimeConfidence.TRUSTED, "device").state
        )
        assertNull(store.read("device"))
    }

    @Test fun rejectsInvalidSignatureWithoutAdvancingVersion() {
        val result = validator.validate(
            policy(signature = byteArrayOf(9, 9, 9)),
            2_000,
            TimeConfidence.TRUSTED,
            "device"
        )

        assertEquals(PolicyState.REJECTED_SIGNATURE, result.state)
        assertNull(store.read("device"))
    }

    @Test fun rejectsLowerVersionAndOlderIssuedAtEvenWhenVersionIncreases() {
        store.seed("device", PolicyVersionRecord("policy-5", 5, 5_000, "sha256:five"))

        assertEquals(
            PolicyState.REJECTED_ROLLBACK,
            validator.validate(
                policy(version = 4, issuedAt = 6_000),
                7_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
        assertEquals(
            PolicyState.REJECTED_ROLLBACK,
            validator.validate(
                policy(version = 6, issuedAt = 4_000),
                7_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
    }

    @Test fun acceptsExactSameVersionReplayButRejectsSameVersionPayloadConflict() {
        store.seed("device", PolicyVersionRecord("policy-1", 2, 1_000, "sha256:payload-1"))

        val replay = validator.validate(policy(), 2_000, TimeConfidence.TRUSTED, "device")
        val conflict = validator.validate(
            policy(digest = "sha256:substituted-payload"),
            2_000,
            TimeConfidence.TRUSTED,
            "device"
        )

        assertEquals(PolicyState.ACCEPTED, replay.state)
        assertEquals(PolicyState.REJECTED_VERSION_CONFLICT, conflict.state)
        assertEquals("sha256:payload-1", store.read("device")?.contentDigest)
    }

    @Test fun rejectsConcurrentVersionStoreUpdateFailClosed() {
        store.rejectNextWrite = true

        val result = validator.validate(policy(), 2_000, TimeConfidence.TRUSTED, "device")

        assertEquals(PolicyState.REJECTED_CONCURRENT_UPDATE, result.state)
        assertFalse(result.isAccepted)
        assertNull(store.read("device"))
    }

    @Test fun rejectsExpiredAndNotYetIssuedPolicies() {
        assertEquals(
            PolicyState.EXPIRED,
            validator.validate(
                policy(expiresAt = 1_500),
                2_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
        assertEquals(
            PolicyState.EXPIRED,
            validator.validate(
                policy(issuedAt = 3_000, expiresAt = 9_000),
                2_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
        assertNull(store.read("device"))
    }

    @Test fun rejectsP3AndP4ForOfflineCaching() {
        assertEquals(
            PolicyState.REJECTED_TIER,
            validator.validate(
                policy(tier = SecurityTier.P3),
                2_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
        assertEquals(
            PolicyState.REJECTED_TIER,
            validator.validate(
                policy(tier = SecurityTier.P4),
                2_000,
                TimeConfidence.TRUSTED,
                "device"
            ).state
        )
        assertNull(store.read("device"))
    }

    @Test fun capsExpiryAtSignedExpiryWhenItIsEarlierThanTierMaximum() {
        val result = validator.validate(
            policy(expiresAt = 5_000),
            2_000,
            TimeConfidence.TRUSTED,
            "device"
        )

        assertEquals(PolicyState.ACCEPTED, result.state)
        assertEquals(5_000L, result.effectiveUntilEpochMillis)
        assertNotNull(store.read("device"))
    }

    @Test fun protectsExpiryArithmeticFromLongOverflow() {
        val issuedAt = Long.MAX_VALUE - 100
        val result = validator.validate(
            policy(issuedAt = issuedAt, expiresAt = Long.MAX_VALUE),
            issuedAt,
            TimeConfidence.TRUSTED,
            "device"
        )

        assertEquals(PolicyState.ACCEPTED, result.state)
        assertEquals(Long.MAX_VALUE, result.effectiveUntilEpochMillis)
    }
}
