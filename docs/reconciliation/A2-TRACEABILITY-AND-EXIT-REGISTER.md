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
| Vault theft/tampering | Vault Cryptography and Recovery Contract | Local vault module | Ciphertext tamper, key invalidation, recovery | RECOVERY POLICY OWNER-APPROVED 2026-11-25; crypto/design review OPEN |
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
2. **Owner decisions:** vault recovery authority model approved (A2-OD-01, 2026-11-25); family age/guardian/legal requirements, enterprise dual control, P3/P4 approval, offline TTL, enterprise escrow, and action allowlist remain undecided.
3. **Independent technical review:** command signing, key lifecycle, vault cryptography, and local authorization contracts require security review before implementation.
4. **Dependency/SBOM review:** NOT RUN because no app dependency graph exists.
5. **Test environment:** contracts specify negative cases, but no implementation or tests exist.
6. **Backend availability:** no AEGIS backend endpoints, IROKA mobile API, signing infrastructure, or attestation verifier has been verified.

## 5. A2 exit decision
A2 is **not eligible for closure**. Core design contracts are drafted, but the critical command/authorization contract is not independently reviewed or approved; physical-device facts remain unknown; and material Owner policy decisions remain open.

A3 remains blocked until critical contracts are approved, device capability is recorded or a test-device strategy is accepted, unresolved decisions are closed or explicitly accepted as risks, the threat-control-test matrix is approved, and the Owner separately authorizes A3.

This register does not authorize code, installation, enrollment, merge, or deployment.


## 6. Evidence collection procedure

See [A2 Evidence Collection Pack](A2-EVIDENCE-COLLECTION-PACK.md) for privacy-safe handset evidence collection, independent-review package requirements, and backend/IROKA accountability worksheet. The pack is documented; evidence has not yet been collected and independent review has not been performed.


## 7. Owner decision evidence

- **A2-OD-01 — Vault recovery:** APPROVED 2026-11-25. User-controlled high-entropy recovery secret; no provider/operator recovery; permanent-loss risk must be disclosed. See the Owner decision record in [A2 Blocker Resolution and Owner Decision Register](A2-BLOCKER-RESOLUTION-AND-OWNER-DECISIONS.md).


## 8. Controlled reconciliation — Owner decisions and security enhancements (2026-11-25)

This section supersedes conflicting earlier status statements in Sections 2, 4, and 5 where they describe Owner decisions as undecided or the offline TTL as open. The historical entries are retained as audit history; the current controlling decisions are recorded in the Owner Decision Register.

### 8.1 Current Owner-decision state

- A2-OD-01 vault recovery authority: **APPROVED** — user-controlled high-entropy recovery secret; no provider/operator recovery; permanent-loss risk disclosed.
- A2-OD-02 enterprise vault escrow: **APPROVED** — disabled in v1; personal and enterprise cryptographic boundaries preserved.
- A2-OD-03 family protection: **APPROVED** — transparent, age-appropriate, least-privilege model; no default access to private vault contents or communications.
- A2-OD-04 P3/P4: **APPROVED** — P3 requires scoped human authorization and local verification; P4 disabled by default in v1.
- A2-OD-05 offline and early-boot: **APPROVED** — signed, scoped, expiring policy; P0/P1 maximum 7 days, P2 maximum 24 hours, P3 remote commands denied offline absent a separately approved protocol, P4 disabled; limited Direct Boot-aware bootstrap only within Android's documented boundaries.
- A2-OD-06 enterprise management: **APPROVED** — maximum documented Android Enterprise Device Owner capabilities only after eligible provisioning; no root or unrestricted system authority.

### 8.2 Security enhancement register

The new [A2 Security Enhancement Register](../security/AEGIS-SECURITY-ENHANCEMENT-REGISTER-A2.md) defines prioritized controls and acceptance evidence for command protocol hardening, key lifecycle, vault isolation, local authorization, Android lifecycle and management modes, offline safety, audit integrity, privacy, supply-chain integrity, recovery, and operational resilience. It is a design/reconciliation artifact, not implementation authorization.

### 8.3 Corrected current blockers

1. Physical handset evidence: **PARTIAL / OWNER-REPORTED**. Model SM-A165F/DS, Android 17/API 37, One UI 9.0, build, security patch, AP/CP/CSC were supplied by the Owner. They have not been independently inspected or attested. Knox, StrongBox, KeyMint security level, verified boot/bootloader, and Device Owner/work-profile state remain **UNKNOWN**.
2. Independent security review: **NOT PERFORMED**. A qualified independent reviewer must assess the exact frozen commit and report methodology, findings/severity, remediation, and retest.
3. Backend/IROKA operational ownership: **UNRESOLVED**. Named accountable owners, trust-root custody/rotation, attestation verification, policy publication/revocation, audit retention, and incident response are not evidenced.
4. Dependency/SBOM: **NOT RUN** because no application dependency graph exists; repeat at A3 dependency introduction and before release.
5. Threat-to-control-to-test traceability: **PARTIAL**. This register and the enhancement register define design coverage; implementation tests have not run.
6. Legal review: **NOT PERFORMED** for family age/guardian/custody and enterprise employee transparency/privacy requirements.
7. A1 formal freeze: **NOT CONFIRMED** by explicit Owner freeze decision.

### 8.4 Current stage decision

A2 remains **OPEN — NOT ELIGIBLE FOR CLOSURE**. The six Owner decisions are approved, but approval of policy does not substitute for physical-device evidence, independent review, accountable service ownership, legal review, or complete traceability. A3 remains **BLOCKED** pending A2 exit and explicit stage transition. A5 remains a separate authorization gate.

No application code, handset state, enrollment, production key, merge, release, or deployment is authorized by this reconciliation.


---

## 9. Controlled amendment — risk-based traceability and A2 exit assessment (2026-11-25)

**Controlling amendment:** [AEGIS-A2-ADD-001 — Risk-Based Security Assurance and Exit Revision](AEGIS-A2-ADD-001-RISK-BASED-ASSURANCE-AND-EXIT-REVISION.md). This section supersedes earlier universal A3-blocker and independent-review timing statements. Historical entries remain visible for audit history.

### 9.1 First-tranche traceability readiness

| First A3 tranche requirement/threat | Planned component/control | Minimum A3 negative evidence | Entry disposition |
|---|---|---|---|
| False security posture / unsupported device capability | Capability manifest and security status model | UNKNOWN/UNSUPPORTED never rendered as VERIFIED or enforced | ELIGIBLE |
| Privilege confusion across consumer/family/enterprise | Mode and role policy model | Cross-mode, cross-role and cross-tenant denial | ELIGIBLE |
| Unauthorized local action | Deterministic local policy evaluator | Wrong role, missing consent, stale policy and unsupported capability produce DENY/REQUIRE_APPROVAL with no side effect | ELIGIBLE |
| Offline policy expiry / clock rollback | Offline policy state machine | Expired policy, rollback, uncertain time and stale revocation do not extend authority | ELIGIBLE |
| Audit leakage or false immutability claim | Local audit event schema/storage boundary | Secret redaction, sequence-gap detection, integrity failure state; no claim of absolute immutability | ELIGIBLE |
| Forged/replayed remote command | Typed schema/parser only in first tranche | Malformed, unknown, wrong-scope and duplicate input rejected before side effects | CONDITIONAL; no live command execution |
| Vault theft/tampering | Vault interfaces and synthetic test harness only | No real secret persistence or plaintext leakage in scaffolding | CONDITIONAL; cryptographic engine deferred |
| Direct Boot exposure | Minimal bootstrap interface only | No credential-bound secret or vault plaintext in device-protected storage | CONDITIONAL |
| Family/enterprise authority abuse | Synthetic policy and management adapter tests | Guardian cannot access private vault; work profile is not Device Owner; unsupported controls fail closed | CONDITIONAL |

The table defines planned acceptance evidence, not executed tests. Every test remains **NOT RUN** until implemented and executed.

### 9.2 Revised blocker mapping

- Independent implementation assessment: A4/pre-release gate, not A2 exit.
- SBOM/dependency scanning: trigger at first dependency introduction in A3 and repeat for release candidates.
- Device hardware/boot/management evidence: required before accepting affected device-specific controls, not for platform-neutral foundations.
- Backend/IROKA named operational owners and trust services: required before live integration.
- Family/enterprise legal/privacy review: required before real-user/tenant activation; synthetic scaffolding may proceed.
- A1 formal freeze: still NOT CONFIRMED; Owner must confirm the controlling baseline or record a scoped deferral before A2 closure.

### 9.3 Focused A2 exit result

**PARTIALLY EXIT-READY — NOT CLOSED.** A3 foundation workstreams are architecturally eligible after separate Owner stage authorization. Conditional and deferred workstreams are listed in AEGIS-A2-ADD-001. The first-tranche threat/control/test mapping and A1 baseline governance item remain outstanding. No implementation or test execution is claimed.


### 9.4 First-tranche mapping completion

The [A2 Controlled Amendment](AEGIS-A2-ADD-001-RISK-BASED-ASSURANCE-AND-EXIT-REVISION.md) Section 9 records the first-tranche threat-to-control-to-test matrix FT-01 through FT-12.

**Disposition: DESIGN-LEVEL FIRST-TRANCHE TRACEABILITY COMPLETE.** All associated tests remain **NOT RUN** because no application implementation exists. This closes the first-tranche design-mapping task only; later feature workstreams require additional threat/test mappings before implementation.


## 10. Owner-approved A2 closure — 2026-11-25

**A1 disposition:** The Owner explicitly accepts AEGIS-A1-BASE-001 v1.0 / PMSS Architecture v1.2 as the frozen controlling requirements and architecture baseline. Material changes require controlled amendments.

**A2 disposition:** The Owner explicitly approves closure of A2 under AEGIS-A2-ADD-001. The first-tranche design-level threat/control/test mapping FT-01 through FT-12 is complete. OD-01 through OD-06 remain approved and binding. Earlier “PARTIALLY EXIT-READY / NOT CLOSED” statements are historical and superseded by this closure record.

**Residual gates (not A2 blockers):** handset/Knox/StrongBox/KeyMint/Verified Boot/management evidence before affected device-specific acceptance; named backend/IROKA owners and trust operations before live integration; SBOM/dependency scanning when dependencies are introduced and before release; legal/privacy review before real family/enterprise activation; independent implementation security assessment at A4 against a frozen pre-release candidate. Feature-specific design decisions and threat/test mappings remain required before their respective implementation workstreams.

**A3 status:** NOT AUTHORIZED / NOT STARTED. This closure is not A3 stage authorization. No code, handset changes, enrollment, production keys, live service provisioning, merge, release or deployment is authorized. A5 remains a separate Owner gate.

**Evidence limitation:** first-tranche mapping is design-level only; all implementation tests remain NOT RUN. Production readiness is NOT ESTABLISHED.
