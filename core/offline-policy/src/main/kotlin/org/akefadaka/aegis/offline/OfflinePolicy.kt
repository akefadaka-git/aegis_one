package org.akefadaka.aegis.offline

import org.akefadaka.aegis.security.SecurityTier

/**
 * Clock confidence is supplied by the platform boundary. A wall clock alone must never
 * be promoted to TRUSTED by this module.
 */
enum class TimeConfidence { TRUSTED, UNCERTAIN, ROLLBACK_DETECTED }

enum class PolicyState {
    ACCEPTED,
    EXPIRED,
    REJECTED_SIGNATURE,
    REJECTED_ISSUER,
    REJECTED_ROLLBACK,
    REJECTED_VERSION_CONFLICT,
    REJECTED_CONCURRENT_UPDATE,
    REJECTED_CLOCK,
    REJECTED_SCOPE,
    REJECTED_TIER
}

/**
 * Signed policy envelope. contentDigest identifies the canonical policy payload produced
 * by the trusted serialization boundary; the verifier must verify the signature over
 * that same canonical payload, not over ad-hoc field concatenation.
 *
 * No production issuer keys, signature algorithm, or trust roots are embedded here.
 */
data class CachedPolicy(
    val policyId: String,
    val version: Long,
    val issuedAtEpochMillis: Long,
    val expiresAtEpochMillis: Long,
    val tier: SecurityTier,
    val scope: String,
    val issuerId: String,
    val contentDigest: String,
    val signature: ByteArray
) {
    init {
        require(policyId.isNotBlank() && issuerId.isNotBlank() && scope.isNotBlank())
        require(contentDigest.isNotBlank())
        require(version > 0)
        require(issuedAtEpochMillis >= 0 && expiresAtEpochMillis > issuedAtEpochMillis)
        require(signature.isNotEmpty())
    }
}

fun interface PolicySignatureVerifier {
    /** Verifies the canonical signed policy payload against the supplied issuer signature. */
    fun verify(policy: CachedPolicy): Boolean
}

fun interface PolicyIssuerTrust {
    /** Must resolve to an explicitly trusted issuer for this exact policy scope. */
    fun isTrusted(issuerId: String, scope: String): Boolean
}

/** Durable adapter contract. Implementations must provide atomic compare-and-set semantics. */
data class PolicyVersionRecord(
    val policyId: String,
    val version: Long,
    val issuedAtEpochMillis: Long,
    val contentDigest: String
) {
    init {
        require(policyId.isNotBlank() && version > 0 && issuedAtEpochMillis >= 0)
        require(contentDigest.isNotBlank())
    }
}

interface PolicyVersionStore {
    fun read(scope: String): PolicyVersionRecord?

    /**
     * Atomically replace the observed record. A null expected value means no record existed.
     * Return false if another writer changed the record after it was read.
     */
    fun compareAndSet(
        scope: String,
        expected: PolicyVersionRecord?,
        replacement: PolicyVersionRecord
    ): Boolean
}

data class PolicyCheck(
    val state: PolicyState,
    val effectiveUntilEpochMillis: Long? = null
) {
    val isAccepted: Boolean get() = state == PolicyState.ACCEPTED
}

class OfflinePolicyValidator(
    private val verifier: PolicySignatureVerifier,
    private val issuerTrust: PolicyIssuerTrust,
    private val versionStore: PolicyVersionStore,
    private val p0P1MaxAgeMillis: Long = DEFAULT_P0_P1_MAX_AGE_MILLIS,
    private val p2MaxAgeMillis: Long = DEFAULT_P2_MAX_AGE_MILLIS
) {
    init {
        require(p0P1MaxAgeMillis > 0 && p2MaxAgeMillis > 0)
    }

    fun validate(
        policy: CachedPolicy,
        nowEpochMillis: Long,
        timeConfidence: TimeConfidence,
        requestedScope: String
    ): PolicyCheck {
        if (nowEpochMillis < 0 || timeConfidence != TimeConfidence.TRUSTED) {
            return PolicyCheck(PolicyState.REJECTED_CLOCK)
        }
        if (requestedScope.isBlank() || policy.scope != requestedScope) {
            return PolicyCheck(PolicyState.REJECTED_SCOPE)
        }

        val maxAge = when (policy.tier) {
            SecurityTier.P0, SecurityTier.P1 -> p0P1MaxAgeMillis
            SecurityTier.P2 -> p2MaxAgeMillis
            SecurityTier.P3, SecurityTier.P4 -> return PolicyCheck(PolicyState.REJECTED_TIER)
        }
        val ageBound = if (policy.issuedAtEpochMillis > Long.MAX_VALUE - maxAge) {
            Long.MAX_VALUE
        } else {
            policy.issuedAtEpochMillis + maxAge
        }
        val effectiveUntil = minOf(policy.expiresAtEpochMillis, ageBound)
        if (nowEpochMillis < policy.issuedAtEpochMillis || nowEpochMillis >= effectiveUntil) {
            return PolicyCheck(PolicyState.EXPIRED)
        }

        if (!issuerTrust.isTrusted(policy.issuerId, policy.scope)) {
            return PolicyCheck(PolicyState.REJECTED_ISSUER)
        }
        if (!verifier.verify(policy)) {
            return PolicyCheck(PolicyState.REJECTED_SIGNATURE)
        }

        val observed = versionStore.read(policy.scope)
        if (observed != null) {
            if (policy.version < observed.version ||
                (policy.version == observed.version && policy.issuedAtEpochMillis < observed.issuedAtEpochMillis)
            ) {
                return PolicyCheck(PolicyState.REJECTED_ROLLBACK)
            }
            if (policy.version == observed.version) {
                val sameAcceptedPolicy = policy.policyId == observed.policyId &&
                    policy.contentDigest == observed.contentDigest &&
                    policy.issuedAtEpochMillis == observed.issuedAtEpochMillis
                return if (sameAcceptedPolicy) {
                    PolicyCheck(PolicyState.ACCEPTED, effectiveUntil)
                } else {
                    PolicyCheck(PolicyState.REJECTED_VERSION_CONFLICT)
                }
            }
            if (policy.issuedAtEpochMillis < observed.issuedAtEpochMillis) {
                return PolicyCheck(PolicyState.REJECTED_ROLLBACK)
            }
        }

        val replacement = PolicyVersionRecord(
            policyId = policy.policyId,
            version = policy.version,
            issuedAtEpochMillis = policy.issuedAtEpochMillis,
            contentDigest = policy.contentDigest
        )
        return if (versionStore.compareAndSet(policy.scope, observed, replacement)) {
            PolicyCheck(PolicyState.ACCEPTED, effectiveUntil)
        } else {
            PolicyCheck(PolicyState.REJECTED_CONCURRENT_UPDATE)
        }
    }

    companion object {
        const val DEFAULT_P0_P1_MAX_AGE_MILLIS: Long = 7L * 24 * 60 * 60 * 1000
        const val DEFAULT_P2_MAX_AGE_MILLIS: Long = 24L * 60 * 60 * 1000
    }
}
