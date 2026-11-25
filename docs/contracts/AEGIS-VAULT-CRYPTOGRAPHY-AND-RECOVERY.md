# AEGIS ONE — Vault Cryptography and Recovery Contract

**ID:** AEGIS-CONTRACT-VAULT-001 | **Version:** 1.0 | **Status:** A2 contract; recovery authority policy Owner-approved 2026-11-25; remaining design approval required

## 1. Security objectives
Protect vault confidentiality and integrity at rest; prevent unauthorized key use; minimize plaintext lifetime; make recovery trade-offs explicit; avoid silent unrecoverable data loss.

## 2. Cryptographic requirements
- Use Android platform cryptographic APIs and maintained, reviewed primitives; never implement custom cryptography.
- Use authenticated encryption with unique nonces/IVs per encryption under a key. Select algorithm and parameters during implementation review.
- Separate data-encryption keys (DEKs) from key-encryption keys (KEKs). Use envelope encryption where appropriate.
- Protect KEKs/wrapping keys with Android Keystore. Prefer hardware-backed security where available; detect and record security level. StrongBox is optional, not assumed.
- Bind ciphertext to context with authenticated associated data: vault ID, object ID, format version, and stable ownership metadata.
- Never store plaintext keys in preferences, logs, backups, analytics, crash reports, or source control.
- Never release plaintext after authentication-tag failure.
- Minimize plaintext copies/lifetime; exclude plaintext from logs, notifications, clipboard, thumbnails, and untrusted exports by default.
- Version ciphertext formats and define migration/rotation procedures.

## 3. Logical key hierarchy
1. Hardware/OS-protected Keystore key or wrapping key.
2. Vault-specific KEK/wrapping layer.
3. Random DEK per vault or object group, per selected design.
4. Authenticated ciphertext objects and integrity metadata.

Exact algorithms, key sizes, nonce construction, and key-per-object strategy require current Android guidance and independent review. This document does not prescribe a novel cryptographic scheme.

## 4. Authentication
BiometricPrompt is an authentication interface, not an encryption algorithm. Where policy requires biometric-bound key use, configure Keystore key authorization and test invalidation/enrollment-change behavior. Define whether device-credential fallback is allowed by vault sensitivity. Biometric success does not prove remote account/device trust.

## 5. Backup and recovery
- Backup policy for vault ciphertext and key material must be explicit; no implicit cloud backup of wrapping keys.
- **Owner-approved v1 recovery authority policy (2026-11-25):** use a user-controlled, high-entropy recovery secret; prohibit provider/operator recovery; disclose the permanent-loss risk clearly. Loss of both the recovery secret and relevant device/key material may make vault data permanently unrecoverable.
- Recovery secrets must be high entropy, rate-limited, protected from server operators, and tested for phishing/brute force.
- Enterprise escrow requires tenant-controlled ownership, dual control, auditable access, and employee disclosure.
- Device replacement does not silently clone a hardware-bound private key.
- Loss of a non-exportable key may make data unrecoverable; disclose this before vault use.
- Cryptographic erasure requires destroying all usable wrapping paths and assessing backups/replicas/escrow; deleting a database row alone is not erasure.

## 6. File and storage handling
Use app-private storage by default. External import/export is explicit and user-mediated. Minimize temporary plaintext. Validate file type/size; do not trust extensions or MIME metadata. Protect against path traversal, symlink surprises, decompression bombs, malformed media, and parser vulnerabilities. Share through scoped content URIs and short-lived grants, not raw paths.

## 7. Required tests
Ciphertext tampering/tag failure; nonce uniqueness; key invalidation; biometric enrollment change; authentication cancellation; process death during writes; interrupted rotation; backup/restore; device replacement; storage full; rollback; malformed/oversized imports; recovery abuse.

## 8. Open decisions
Vault granularity/file format; exact recovery-secret derivation, storage, rate limiting and abuse controls; enterprise escrow (not approved); backup/export/retention; biometric strength and credential fallback; data classification tiers.

No cryptographic implementation or key generation is authorized by this contract.


## 9. Advanced vault and early-boot requirements

The vault subsystem is expected to provide an integrated, defense-in-depth capability set. Subject to A3 design review and independent security review, the design must cover:
- encrypted vault objects and metadata; authenticated integrity; versioned formats; safe import/export; app-private storage; secure temporary-file handling; and explicit backup/restore behavior;
- separation of personal vault and enterprise-managed data key domains, consistent with Owner-approved OD-02; enterprise enrollment must never silently transfer access to a personal vault;
- per-install/per-vault key separation, device-bound Android Keystore protection, hardware-backed security detection, key rotation, invalidation handling, and user-controlled recovery under OD-01;
- biometric and device-credential policy, session locking, inactivity/foreground/background transitions, clipboard/share protections, screen-capture policy where supported, and clear user-visible recovery semantics;
- tamper-resistant handling of vault metadata, audit events, export authorization, deletion semantics, and recovery abuse controls;
- offline operation under OD-05, including signed-policy expiry and explicit restrictions on sensitive export/recovery/destructive actions;
- Direct Boot feasibility: a minimal Direct-Boot-aware bootstrap may enforce only a narrow pre-unlock policy using device-protected storage. It must not store plaintext vault data, recovery secrets, or credential-encrypted vault keys there. Full vault availability remains gated on user unlock and the platform's Keystore/authentication rules.

**Early-boot platform boundary:** AEGIS cannot start before Android's own boot chain or obtain unrestricted OS/root privileges as a normal application. It can activate only supported early-start behavior after Android reaches the relevant application lifecycle stage. The design must show a distinct pre-unlock limited-protection state and post-unlock full-vault state. No lock-screen bypass or pre-unlock vault decryption is permitted.

**Administration boundary:** Device-wide configuration is conditional on supported Android APIs and a valid management role. Consumer mode is limited to app-scoped/user-consented capabilities. Fully managed enterprise Device Owner controls require eligible Android Enterprise provisioning and remain within documented policy APIs; Device Owner is not unrestricted root access. Family authority is separate.

**Status:** REQUIREMENTS RECORDED; no crypto algorithm/library, Direct Boot component, device admin enrollment, production key generation, or implementation is approved by this contract alone. All cryptographic choices require current platform guidance, dependency/SBOM review, negative tests, and independent security review.
