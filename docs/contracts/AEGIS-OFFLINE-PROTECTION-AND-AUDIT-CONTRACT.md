# AEGIS ONE — Offline Protection and Audit Contract

**ID:** AEGIS-CONTRACT-G08-G11-001 | **Version:** 1.0 | **Status:** A2 proposed contract; approval required

## 1. Offline principle
AEGIS continues essential local protection without network or IROKA availability. Offline operation does not grant new authority, extend expired approvals indefinitely, or bypass Android permissions.

## 2. Offline policy snapshot
A signed snapshot contains policy ID/version, tenant/device scope, issue time, expiry, permitted offline actions, risk limits, issuer/key ID, and signature. Verify signature, scope, version, and validity before caching. Maximum offline lifetime is risk-dependent and requires Owner approval.

- Low-risk local protections may continue under last valid policy.
- Sensitive export, account recovery, remote management, destructive actions, and operations requiring current revocation are online-only unless separately approved.
- Expired or unverifiable policy transitions to a restricted safe state.
- Reject policy rollback using protected version state where supported.
- Emergency mode exposes predefined local functions and emergency contacts; it does not enable broad remote access.

## 3. Connectivity state machine
ONLINE → DEGRADED → OFFLINE → RECONNECTING → ONLINE.

Record transitions. Network loss must not expose partial plaintext or corrupt committed vault writes. Reconnection performs authentication, key/revocation refresh, policy reconciliation, audit receipt synchronization, and queued-event upload before accepting new remote commands.

## 4. Audit event schema
Every security event records schema version, event ID, installation identity, tenant scope where applicable, event type/outcome, actor/service identity, authorization reference, policy version, capability/action, sequence number, event time and clock quality, previous-event integrity reference, minimal context/reason, sync state, and server receipt reference.

Never log secrets, credentials, private keys, vault plaintext, or unnecessary personal data.

## 5. Tamper evidence and limitations
Use append-oriented, cryptographically linked events with integrity keys protected by Keystore where possible. A hash chain can reveal alteration of retained records but cannot prove completeness if an attacker controls process, storage, and keys. Server-signed receipts and independent checkpoints strengthen evidence after sync. Do not claim immutable local logs against a fully compromised OS or privileged attacker.

## 6. Queue and synchronization
Unique event IDs and idempotency keys; preserve per-device order and record gaps; bounded retry/backoff and storage limits; preserve critical security events; disclose dropped low-priority telemetry; never silently discard critical events. Acknowledge receipt only after durable server acceptance. Duplicate uploads are idempotent. Retention/deletion follows approved privacy policy.

## 7. Required tests
Network loss at command/write phases; policy expiry offline; clock rollback; stale/revoked key on reconnect; duplicate upload; sequence gap; queue exhaustion; storage full; process death; corrupted audit chain; server outage.

## 8. Open decisions
Offline TTL by risk; secure time and monotonic counter strategy; event retention/export/deletion; server checkpoint cadence and receipt format; critical-event queue reservation and overflow policy.

No production telemetry or backend integration is authorized by this contract.
