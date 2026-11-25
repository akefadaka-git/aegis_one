# AEGIS ONE — IROKA Mobile Command and Execution Contract

**ID:** AEGIS-CONTRACT-G08-G09-001 | **Version:** 1.0 | **Status:** A2 proposed contract; critical approval required

## 1. Contract boundary
IROKA is the governed intelligence and policy authority service. AEGIS ONE is the Android execution boundary. IROKA may send a signed, scoped command authorization artifact; AEGIS independently validates and enforces it. A model output, push notification, workflow event, or transport-authenticated message is never sufficient authority.

## 2. Command envelope
Every command carries a canonical, versioned envelope:
- schema_version; command_id; cryptographically random single-use nonce;
- issuer_id and signing key_id;
- exact audience, target account, tenant, and device identity;
- subject principal; allowlisted action; strict typed parameters;
- resource scope; capability and capability version;
- policy_version; issued_at, not_before, expires_at;
- approval reference; idempotency key; risk class P0–P4;
- signature over canonical envelope excluding signature field.

## 3. Verification sequence
1. Parse with strict size, depth, type, and schema limits.
2. Verify canonical serialization and supported schema version.
3. Verify signature using a pinned/rotatable trusted issuer key.
4. Verify issuer status, key validity, purpose, and revocation.
5. Verify audience, account, tenant, and device binding.
6. Verify validity interval with bounded clock uncertainty.
7. Verify nonce and command ID are unused; enforce idempotency.
8. Verify capability is locally registered and permitted in current Android management mode.
9. Verify policy version and approval evidence.
10. Apply local hard policy, consent, user-presence, and platform permission requirements.
11. Execute through a typed allowlisted adapter; record result and audit receipt.

Any failure or indeterminate result rejects protected execution. Signature validity does not imply consent, Android authority, or policy approval.

## 4. Action classes and prohibited payloads
Actions are enumerated and versioned. No arbitrary shell, JavaScript, reflection, dynamic code loading, unrestricted Android Intent, or arbitrary file path is accepted. Lock, wipe, key destruction, policy lockdown, credential reset, or disabling recovery remain disabled until their own threat, authorization, rollback, and acceptance contracts are approved.

## 5. Offline and replay behavior
Commands requiring online revocation or current policy are not executable offline. Offline commands require short validity, pre-authorized scope, and explicit offline eligibility. A monotonic local replay ledger records command IDs/nonces and idempotency outcomes. If secure time cannot be established within policy tolerance, time-sensitive commands fail closed. On reconnect, AEGIS reconciles receipts and revocation before processing queued commands. A lost receipt never causes blind re-execution; query idempotency state first.

## 6. Key lifecycle
Signing keys are held by the trusted issuer, not a model or agent. Rotation, overlap, revocation, compromise response, and root recovery are versioned. Trust-store updates require authenticated integrity-protected metadata and rollback protection. No single remote command may replace the trust root that authorizes that same command.

## 7. Result receipt
Receipt binds command ID, device identity, action, policy version, decision, execution status, timestamps/clock quality, error class, and audit sequence. It excludes vault plaintext and secrets. Sign with device key where supported.

## 8. Required negative tests
Wrong/revoked signer; malformed envelope; schema downgrade; wrong audience/device/tenant; replay/duplicate; expiry/not-before; clock rollback; missing approval; policy mismatch; local denial; unsupported management mode; process death; partial execution; lost receipt; trust-root rotation race.

## 9. Open decisions
Signature algorithm and canonical encoding; issuer hierarchy and trust-root ceremony; offline allowlist and TTL by risk class; secure time strategy; approval evidence and dual control; transport and receipt endpoint.

No command execution or IROKA endpoint connection is authorized by this contract.
