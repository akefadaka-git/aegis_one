# AEGIS ONE — A2 Traceability and Exit Evidence Register

**ID:** AEGIS-A2-TRACE-001 | **Version:** 1.0 | **Status:** PARTIAL — OPEN

## 1. Purpose
Map A1 requirements and material threats to a contract, control boundary, and future verification evidence. This is design traceability; no test is represented as executed.

## 2. Traceability matrix

| Requirement / threat | Artifact | Control owner | Required verification | Current status |
|---|---|---|---|---|
| Account/device identity and revocation | Device Identity and Recovery Contract | AEGIS identity/backend verifier | Challenge replay, key mismatch, revocation, reinstall | CONTRACT DRAFTED; tests NOT RUN |
| Family/enterprise privilege separation | Roles, Consent and Policy Matrix | Governance/tenant policy | Cross-role and cross-tenant denial | CONTRACT DRAFTED; legal decisions OPEN |
| Forged/replayed remote command | IROKA Mobile Command Contract | IROKA issuer + AEGIS local gate | Signature, nonce, audience, expiry, replay | CONTRACT DRAFTED; trust ceremony OPEN |
| AI self-authorization | Command Contract + A1 threat model | AEGIS local gate / IROKA Kernel | Model output cannot trigger protected side effect | DESIGN REQUIREMENT |
| Vault theft/tampering | Vault Cryptography and Recovery Contract | Local vault module | Ciphertext tamper, key invalidation, recovery | CONTRACT DRAFTED; algorithm review OPEN |
| Offline expiry/revocation | Offline Protection and Audit Contract | Local policy engine | Expired policy, clock rollback, reconnect | CONTRACT DRAFTED; TTL OPEN |
| Audit alteration/gaps | Offline Protection and Audit Contract | Local audit/backend receipt | Chain tamper, sequence gap, receipt verification | CONTRACT DRAFTED; tests NOT RUN |
| Device Owner overclaim | A2 Android/device reconciliation | Android management adapter | Consumer mode lacks Device Owner-only controls | PLATFORM CONSTRAINT CONFIRMED |
| VPN failure/lockdown outage | A2 Android/device reconciliation | VPN adapter/admin policy | Tunnel failure, lockdown recovery and notice | DESIGN REQUIRED; tests NOT RUN |
| Compromised OS | A1 threat model | Integrity/risk subsystem | Compromised-device scenarios and limitation checks | RESIDUAL RISK; test plan OPEN |
| Supply-chain compromise | A2 dependency gate | Build/release governance | SBOM, license, vulnerability, provenance | NOT RUN; no app dependencies |

## 3. A2 artifact inventory

| Artifact | Status |
|---|---|
| A1 product and architecture baseline | DOCUMENTED |
| A1 initial threat model | DOCUMENTED |
| Android/device capability reconciliation | DOCUMENTED; physical device facts UNKNOWN |
| Device identity and recovery contract | DRAFTED |
| Roles, consent and policy matrix | DRAFTED |
| IROKA mobile command contract | DRAFTED; critical |
| Vault cryptography and recovery contract | DRAFTED |
| Offline protection and audit contract | DRAFTED |
| Threat-to-control-to-test traceability | INITIAL VERSION DOCUMENTED |

## 4. Remaining A2 blockers
1. **Physical-device facts:** actual SM-A165F build fingerprint, Android/API, patch level, CSC/region, Knox status, StrongBox, hardware-backed Keystore, and boot integrity are UNKNOWN. No device inspection has occurred.
2. **Owner decisions:** recovery model, family age/guardian/legal requirements, enterprise dual control, P3/P4 approval, offline TTL, vault recovery/escrow, and action allowlist remain undecided.
3. **Independent technical review:** command signing, key lifecycle, vault cryptography, and local authorization contracts require security review before implementation.
4. **Dependency/SBOM review:** NOT RUN because no app dependency graph exists.
5. **Test environment:** contracts specify negative cases, but no implementation or tests exist.
6. **Backend availability:** no AEGIS backend endpoints, IROKA mobile API, signing infrastructure, or attestation verifier has been verified.

## 5. A2 exit decision
A2 is **not eligible for closure**. Core design contracts are drafted, but the critical command/authorization contract is not independently reviewed or approved; physical-device facts remain unknown; and material Owner policy decisions remain open.

A3 remains blocked until critical contracts are approved, device capability is recorded or a test-device strategy is accepted, unresolved decisions are closed or explicitly accepted as risks, the threat-control-test matrix is approved, and the Owner separately authorizes A3.

This register does not authorize code, installation, enrollment, merge, or deployment.
