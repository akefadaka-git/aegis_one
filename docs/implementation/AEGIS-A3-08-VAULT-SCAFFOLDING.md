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

- Exact implementation SHA: `2f5da6691a5a798168774e37eb1906f858c5b6b6`.
- Shared-core portability run [#46168750737](https://github.com/akefadaka-git/aegis_one/actions/runs/46168750737): **SUCCESS**, all five jobs passed on the exact SHA — iOS, Android, Windows JVM, macOS JVM and Ubuntu JVM.
- iOS job #141052121627: **SUCCESS**. The decoded log confirms checkout of the exact SHA. iOS ARM64 compilation succeeded for security-state, policy, offline-policy, audit and vault. iOS Simulator ARM64 common tests succeeded for all five modules. Gradle reported `BUILD SUCCESSFUL` with 32 actionable tasks executed.
- Android foundation run [#46168750745](https://github.com/akefadaka-git/aegis_one/actions/runs/46168750745): **SUCCESS** on the same exact SHA. Android build, module tests, lint, APK assembly, SBOM generation/validation and full-history Gitleaks scan passed.
- Android evidence artifact ID `15273151015`, 946,207 bytes; SHA-256 `4e4ee081dba95e0d298b4bd0f63e70b32782d60aa28cfc16523117275da95f2d`. Artifact metadata binds it to run #46168750745 and the exact implementation SHA. Archive inspection found no unresolved SBOM references and zero Gitleaks findings.
- The portability run published no separate artifact; its run/job records and decoded iOS job log are the retained evidence.
- Verified implementation SHA: `2f5da6691a5a798168774e37eb1906f858c5b6b6`. Documentation record commits followed verification; `main` remains `570b6fe5abfcf0d7b369af63e211ab937bfe4024`.
- Production readiness: **NOT ESTABLISHED**. This workstream remains a non-operational scaffold only.

## 6. Acceptance boundary and next gate

A3.08 is **ACCEPTED — BOUNDED SCAFFOLD ONLY**. Acceptance proves the shared scaffold compiles and its declared tests pass on the configured targets; it does not prove cryptographic custody, durable storage, recovery, backup, biometric enforcement, device integration, or production security. Those remain separate design and implementation gates. No merge, release, deployment, production mutation, key generation, or handset interaction occurred.
