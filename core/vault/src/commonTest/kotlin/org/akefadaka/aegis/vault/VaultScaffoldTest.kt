package org.akefadaka.aegis.vault

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith

class VaultScaffoldTest {
    private class FakeCustody(
        private val result: NativeCustodyCapability
    ) : NativeKeyCustodyBoundary {
        override fun capability() = result
    }

    private class FakeStorage(
        private val result: VaultStorageCapability
    ) : VaultStorageBoundary {
        override fun capability() = result
    }

    private fun descriptor() = PersonalVaultDescriptor(
        id = VaultId("a".repeat(64)),
        owner = VaultOwnerRef("b".repeat(64))
    )

    @Test
    fun recoveryAuthorityIsUserOnlyAndCannotBeRelaxed() {
        val policy = VaultRecoveryPolicy()
        assertEquals(RecoverySecretOwner.USER, policy.recoverySecretOwner)
        assertFalse(policy.operatorRecoveryAllowed)
        assertFalse(policy.enterpriseEscrowAllowed)
        assertFailsWith<IllegalArgumentException> {
            VaultRecoveryPolicy(operatorRecoveryAllowed = true)
        }
        assertFailsWith<IllegalArgumentException> {
            VaultRecoveryPolicy(enterpriseEscrowAllowed = true)
        }
    }

    @Test
    fun scaffoldNeverReportsVaultUsableEvenWhenAdaptersClaimUnverifiedAvailability() {
        val scaffold = VaultScaffold(
            FakeCustody(NativeCustodyCapability.AVAILABLE_UNVERIFIED),
            FakeStorage(VaultStorageCapability.AVAILABLE_UNVERIFIED)
        )

        val status = scaffold.inspect(descriptor())

        assertEquals(NativeCustodyCapability.AVAILABLE_UNVERIFIED, status.nativeCustody)
        assertEquals(VaultStorageCapability.AVAILABLE_UNVERIFIED, status.storage)
        assertFalse(status.cryptographicEnginePresent)
        assertFalse(status.persistentVaultStorageEnabled)
        assertFalse(status.usable)
    }

    @Test
    fun unknownOrUnsupportedPlatformCapabilitiesFailClosed() {
        for (custody in NativeCustodyCapability.values()) {
            for (storage in VaultStorageCapability.values()) {
                val status = VaultScaffold(
                    FakeCustody(custody),
                    FakeStorage(storage)
                ).inspect(descriptor())
                assertFalse(status.usable)
            }
        }
    }

    @Test
    fun identifiersRejectNonOpaqueOrMalformedValues() {
        assertFailsWith<IllegalArgumentException> { VaultId("user@example.com") }
        assertFailsWith<IllegalArgumentException> { VaultOwnerRef("installation-123") }
    }

    @Test
    fun personalVaultDefaultsToLockedDownUninitializedState() {
        val vault = descriptor()
        assertEquals(VaultDomain.PERSONAL, vault.domain)
        assertEquals(VaultScaffoldState.UNINITIALIZED, vault.state)
        assertEquals(RecoverySecretOwner.USER, vault.recoveryPolicy.recoverySecretOwner)
    }
}
