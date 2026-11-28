package org.akefadaka.aegis.vault

/**
 * Non-sensitive identifier for a Personal vault. The value must be an opaque,
 * caller-generated digest-shaped reference; it is not a key or recovery secret.
 */
@JvmInline
value class VaultId(val value: String) {
    init {
        require(value.matches(Regex("[a-f0-9]{64}"))) { "VaultId must be an opaque 64-character lowercase hex reference" }
    }
}

/** Opaque reference to the owning installation; never a user identifier. */
@JvmInline
value class VaultOwnerRef(val value: String) {
    init {
        require(value.matches(Regex("[a-f0-9]{64}"))) { "VaultOwnerRef must be an opaque 64-character lowercase hex reference" }
    }
}

enum class VaultDomain {
    PERSONAL
}

enum class VaultScaffoldState {
    UNINITIALIZED,
    LOCKED,
    UNAVAILABLE
}

/**
 * Recovery authority is intentionally fixed in this scaffold.
 * No operator reset, recovery, substitution, or enterprise escrow path exists.
 */
data class VaultRecoveryPolicy(
    val recoverySecretOwner: RecoverySecretOwner = RecoverySecretOwner.USER,
    val operatorRecoveryAllowed: Boolean = false,
    val enterpriseEscrowAllowed: Boolean = false
) {
    init {
        require(recoverySecretOwner == RecoverySecretOwner.USER)
        require(!operatorRecoveryAllowed)
        require(!enterpriseEscrowAllowed)
    }
}

enum class RecoverySecretOwner {
    USER
}

data class PersonalVaultDescriptor(
    val id: VaultId,
    val owner: VaultOwnerRef,
    val domain: VaultDomain = VaultDomain.PERSONAL,
    val state: VaultScaffoldState = VaultScaffoldState.UNINITIALIZED,
    val recoveryPolicy: VaultRecoveryPolicy = VaultRecoveryPolicy()
) {
    init {
        require(domain == VaultDomain.PERSONAL) { "Only Personal vault scaffolding is defined" }
    }
}

enum class NativeCustodyCapability {
    UNKNOWN,
    UNSUPPORTED,
    AVAILABLE_UNVERIFIED
}

enum class VaultStorageCapability {
    UNKNOWN,
    UNSUPPORTED,
    AVAILABLE_UNVERIFIED
}

/**
 * Platform boundary only. Implementations must keep key material inside the
 * platform-native security service and must never return raw key bytes.
 *
 * A3.08 deliberately defines no key-generation, unwrap, export, or crypto API.
 */
interface NativeKeyCustodyBoundary {
    fun capability(): NativeCustodyCapability
}

/**
 * Storage boundary only. A3.08 does not persist vault data or recovery material.
 * Future implementations may accept only authenticated ciphertext records.
 */
interface VaultStorageBoundary {
    fun capability(): VaultStorageCapability
}

/** Safe, non-secret readiness view. This scaffold can never report READY. */
data class VaultScaffoldStatus(
    val vault: PersonalVaultDescriptor,
    val nativeCustody: NativeCustodyCapability,
    val storage: VaultStorageCapability,
    val cryptographicEnginePresent: Boolean = false,
    val persistentVaultStorageEnabled: Boolean = false
) {
    init {
        require(!cryptographicEnginePresent)
        require(!persistentVaultStorageEnabled)
    }

    val usable: Boolean
        get() = false
}

class VaultScaffold(
    private val nativeCustody: NativeKeyCustodyBoundary,
    private val storage: VaultStorageBoundary
) {
    fun inspect(vault: PersonalVaultDescriptor): VaultScaffoldStatus =
        VaultScaffoldStatus(
            vault = vault,
            nativeCustody = nativeCustody.capability(),
            storage = storage.capability()
        )
}
