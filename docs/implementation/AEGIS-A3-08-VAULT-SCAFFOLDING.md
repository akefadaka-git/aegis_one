# AEGIS ONE — A3.08 Vault Scaffolding

**Status:** ACCEPTED — BOUNDED SCAFFOLD; PRODUCTION READINESS NOT ESTABLISHED  
**Repository:** `akefadaka-git/aegis_one`  
**Branch:** `docs/a1-baseline-a2-reconciliation`  
**Base:** `f80b24c83a59f3abc4b07a5329838c5b6d60ba14`

## 1. Scope

A3.08 adds a Kotlin Multiplatform, non-operational Personal-vault domain scaffold. It defines non-sensitive descriptors, immutable recovery-authority constraints, and platform-native custody/storage capability boundaries. It does not implement vault encryption, key generation, recovery, persistence, backup, unlock, biometric authentication, or user-facing vault flows.

## 2. Binding security invariants

- The user is the sole owner of the vault recovery secret.
- Operator/support recovery, reset, reconstruction, substitution, or override is forbidden.
- Enterprise escrow or access to Personal vault keys is forbidden.
- Personal vault keys are not shared with identity, audit, policy-signing, VPN, Family, or Enterprise domains.
- Key material must remain within the relevant platform-native security service. Shared code must never receive raw key bytes.
- No custom cryptography, KDF, AEAD, nonce, backup format, or recovery algorithm is introduced.
- No plaintext, recovery secret, key material, or vault payload is persisted by this scaffold.
- Unknown, unsupported, or unverified platform capability never enables vault use.

## 3. Implementation

- Added `:core:vault` as a Kotlin Multiplatform module with Android, JVM, iOS ARM64 and iOS Simulator ARM64 targets.
- Added `VaultId` and `VaultOwnerRef` opaque digest-shaped references; raw account/device identifiers are rejected.
- Added Personal-only `PersonalVaultDescriptor`, initialized in `UNINITIALIZED` state.
- Added `VaultRecoveryPolicy` whose constructor rejects operator recovery and enterprise escrow.
- Added `NativeKeyCustodyBoundary` and `VaultStorageBoundary` capability-only interfaces. No adapter implementation or key operation exists.
- Added `VaultScaffoldStatus`, which cannot report usable, cryptographic engine present, or persistent storage enabled.
- Added common tests for ownership constraints, malformed identifiers, unknown/unsupported/unverified capabilities, and no-usable-state behavior.

## 4. Explicit exclusions

No Android Keystore/StrongBox, Apple Keychain/Secure Enclave, Windows DPAPI/CNG, macOS Keychain, Linux Secret Service, biometric gate, hardware attestation, KDF, AEAD, ciphertext format, key hierarchy, key rotation, backup, recovery, secure deletion, or production storage adapter is implemented or claimed.

These require a separate cryptographic design gate and per-platform implementation/verification. This workstream does not authorize generation of real keys, device enrollment, installation on the reference handset, production data, merge, release, deployment, or A5.

## 5. Acceptance evidence

- Exact source SHA and CI run: PENDING.
- Android/JVM/iOS compilation and tests: PENDING.
- Formatting/lint, complete workspace tests, SBOM validation and secret scan: PENDING.
- Production readiness: NOT ESTABLISHED.

## 6. Next gate

Run formatting, all-target compilation, all module tests, Android lint/assembly, SBOM validation and secret scanning against the exact resulting SHA. Resolve all failures before A3.08 acceptance.
