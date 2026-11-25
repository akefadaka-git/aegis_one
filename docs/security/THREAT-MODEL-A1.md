# AEGIS ONE — Initial Threat Model

**Document ID:** AEGIS-SEC-TM-001  
**Version:** 1.0  
**Status:** A1 baseline artifact; A2 validation required  
**Date:** 2026-11-25

## 1. Security objectives

Protect the confidentiality, integrity, availability, and authorized use of:

- Vault files and encryption keys.
- User, account, and device identities.
- Local security policies and protected application state.
- Remote-management commands and approvals.
- Security events, evidence, and incident records.
- Family and enterprise management data.

The system must preserve useful local protection when disconnected from IROKA. It must not convert remote intelligence into unrestricted device authority.

## 2. Adversaries and misuse cases

| Threat actor / condition | Example threat | Required defense direction |
|---|---|---|
| Opportunistic thief | Attempts to open vault or use unlocked device | Auth-bound keys, app lock, rate limits, recovery and data minimization |
| Malicious app | Reads shared files, abuses intents, overlays or accessibility | Least privilege, explicit exported-component policy, secure IPC, anti-overlay protections where supported |
| Phishing operator | Steals account credentials or tricks user into approving a command | Phishing-resistant authentication options, transaction context, signed command display and explicit approval |
| Compromised OS / rooted device | Hooks app process or extracts plaintext while in use | Detect and report integrity signals; reduce exposed data; do not claim app-only controls can fully defend a compromised kernel |
| Malicious administrator / guardian | Abuses legitimate remote-management privileges | Scoped roles, dual control for high-impact actions, transparency, immutable audit and revocation |
| Compromised IROKA/model provider | Returns malicious instructions or forged recommendations | Treat outputs as untrusted proposals; local authorization; signed command verification; no model authority |
| Network attacker | Intercepts, replays or modifies commands | TLS, application-layer signatures, nonces, expiry, replay cache and idempotency |
| Backend compromise | Alters account/device state or issues unauthorized commands | Key separation, signing isolation, independent audit, revocation and recovery |
| Supply-chain attacker | Introduces malicious dependency or build artifact | Dependency pinning, license/SBOM review, reproducible build, signed release provenance |
| Insider / data overreach | Uses family or enterprise access to inspect unrelated personal data | Purpose limitation, role boundaries, tenant isolation, access logging and disclosure |

## 3. Trust boundaries

1. Android hardware and operating system.
2. AEGIS application process and local storage.
3. Android Keystore / biometric authentication subsystem.
4. Optional Android Enterprise Device Owner control plane.
5. IROKA service and Governed Kernel.
6. Remote management API and signing infrastructure.
7. Family or enterprise administrators and other enrolled devices.
8. External networks and third-party dependencies.

No boundary is assumed trustworthy merely because it is within the same app, account, network, or organization.

## 4. Security invariants

- Keys are never hardcoded, committed, logged, or exported in plaintext.
- Vault data uses authenticated encryption; unauthenticated plaintext is never released.
- Authentication failure, stale approval, revoked authority, signature failure, replay, and policy uncertainty fail closed for protected operations.
- Local app policy is checked before every protected action.
- A remote command cannot widen the app's local capability or Android management role.
- Models, agent outputs, push messages, and retrieved content are untrusted data.
- Destructive actions (wipe, lockout, credential reset, key destruction, device policy change) require an explicit high-assurance authorization path and recoverability analysis.
- Logs exclude secrets and sensitive plaintext; integrity metadata and clock uncertainty are recorded.
- User/guardian/enterprise roles are distinct, scoped, visible, and revocable.
- Offline mode does not extend a revoked or expired authorization indefinitely.

## 5. Initial abuse cases

- Replaying an old signed lock command after the user recovered the device.
- Reusing a valid command against another device or tenant.
- Downgrading policy version or accepting a command after its expiry.
- Using a compromised model prompt to request a destructive action.
- Forcing VPN lockdown without a recovery route, leaving the device offline.
- Triggering vault key rotation or deletion to cause irreversible data loss.
- Exploiting device clock rollback to revive expired offline policy.
- A family guardian accessing unrelated personal files or communications.
- A tenant administrator targeting a device belonging to another tenant.
- Forging local audit events or deleting evidence before synchronization.

## 6. Required mitigations and verification

A2 must map each abuse case to a specific control, owner, test, and evidence artifact. A4 must execute negative tests for invalid signatures, replay, cross-device targeting, cross-tenant access, stale policy, clock rollback, revoked keys, offline transitions, corrupted vault ciphertext, authentication cancellation, process death, and VPN failure.

## 7. Known limitations

- App-level protections cannot guarantee security against a fully compromised Android OS or privileged attacker.
- Ordinary apps cannot prevent their own uninstall or obtain Device Owner authority without the required managed provisioning.
- Biometric, StrongBox, hardware attestation, and device-integrity capabilities vary by device and must be detected and tested.
- VPN lockdown can interrupt network access when the VPN is unavailable.
- Remote recovery and anti-theft features depend on authenticated backend services and carefully bounded authority.

---
**This threat model is an initial baseline, not a completed security assessment.**
