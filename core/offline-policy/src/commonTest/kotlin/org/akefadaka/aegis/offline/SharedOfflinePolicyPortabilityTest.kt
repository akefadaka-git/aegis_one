package org.akefadaka.aegis.offline

import org.akefadaka.aegis.security.SecurityTier
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedOfflinePolicyPortabilityTest {
    @Test
    fun untrustedClockAndP3RemainDenied() {
        val store = object : PolicyVersionStore {
            override fun read(scope: String): PolicyVersionRecord? = null
            override fun compareAndSet(scope: String, expected: PolicyVersionRecord?, replacement: PolicyVersionRecord) = true
        }
        val validator = OfflinePolicyValidator(
            PolicySignatureVerifier { true },
            PolicyIssuerTrust { _, _ -> true },
            store
        )
        val policy = CachedPolicy("p1", 1, 100, 10_000, SecurityTier.P1,
            "device", "issuer", "digest", byteArrayOf(1))
        assertEquals(PolicyState.REJECTED_CLOCK,
            validator.validate(policy, 500, TimeConfidence.UNCERTAIN, "device").state)
        val p3 = policy.copy(tier = SecurityTier.P3)
        assertEquals(PolicyState.REJECTED_TIER,
            validator.validate(p3, 500, TimeConfidence.TRUSTED, "device").state)
    }
}
