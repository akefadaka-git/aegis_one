# AEGIS ONE — A2 Blocker Resolution Plan and Owner Decision Register

**ID:** AEGIS-A2-BLOCKERS-001 | **Version:** 1.0 | **Status:** OPEN — proposed dispositions, not Owner-approved

## 1. Purpose and disposition

This register separates blockers that can be reduced through repository documentation from blockers that require physical-device evidence, an independent reviewer, an external service, or explicit Owner decisions. No unknown is represented as verified and no proposed default is represented as approved.

## 2. Recommended safe defaults for Owner decision

| Decision | Recommended baseline | Rationale | State |
|---|---|---|---|
| Vault recovery | User-controlled high-entropy recovery secret; no provider/operator recovery; disclose permanent-loss risk | Minimizes unilateral recovery authority and server-side exposure | OWNER-APPROVED 2026-11-25 |
| Enterprise escrow | Disabled in v1; consider only in a later separately approved enterprise profile with tenant-held keys and dual control | Avoids silent employer access to personal vault data | OWNER APPROVED — 2026-11-25; see A2-OD-02 |
| Family model | Transparent, age-appropriate, least-privilege guardian controls; no default vault/content surveillance | Limits coercive or excessive access | OWNER-APPROVED 2026-11-25; legal review remains required |
| P3 actions | Require scoped human authority, valid signed/time-limited approval, and local policy check; user notice where feasible; AI cannot approve | Prevents AI/service-only device-impacting actions | OWNER-APPROVED 2026-11-25; see A2-OD-04 |
| P4 actions | Disabled by default in v1; future capability requires separate approval, strong human authorization, recovery analysis and independent review | Reduces catastrophic and irreversible misuse | OWNER-APPROVED 2026-11-25; see A2-OD-04 |
| Offline policy | Signed, scoped, unexpired policy; P0/P1 up to 7 days and P2 up to 24 hours maximum; P3 remote actions denied absent a separately approved offline protocol; P4 disabled | Preserves local safety without extending remote authority | OWNER-APPROVED 2026-11-25; see A2-OD-05 |
| Attestation failure | Unknown/unsupported → restricted trust state, not PASS; define blocking tiers after device/service capability validation | Avoids false assurance and avoids assuming every device supports identical evidence | PROPOSED TECHNICAL DEFAULT |
| Consumer anti-removal | Do not promise prevention of uninstall; provide user-mediated account/device recovery and local tamper signals where available | Ordinary apps do not possess Device Owner authority | PLATFORM CONSTRAINT |
| Enterprise management | Opt-in fully managed Android Enterprise Device Owner profile; maximum documented supported APIs subject to tenant policy, transparency, least privilege, audit, and personal-vault separation | Separates enterprise privileges from consumer mode | OWNER-APPROVED 2026-11-25; see A2-OD-06 |
| Command execution | Strict typed allowlist; no arbitrary shell/code/intent; P2–P4 cannot be approved by model output | Constrains remote execution and AI authority | PROPOSED TECHNICAL DEFAULT |

## 3. Physical-device evidence — cannot be inferred remotely

Target: Samsung Galaxy A16 SM-A165F.

Required read-only evidence record:
- exact model/product code and build fingerprint;
- Android release, API level, One UI version and security patch date;
- CSC/region and firmware build;
- Knox version and Knox warranty/attestation signals where legitimately available;
- Key Attestation security level and hardware-backed status, if supported;
- StrongBox availability, if supported;
- verified boot state, device lock state and bootloader state;
- available Android Enterprise management capabilities without enrolling the device.

Evidence must be collected from the actual handset using read-only Settings/About information and, if the Owner chooses, non-mutating diagnostic commands. Do not factory reset, unlock bootloader, enroll, install AEGIS, change security settings, or expose IMEI/serial numbers in public repository evidence. Redact personal identifiers.

**Status: BLOCKED — no handset evidence supplied or collected.** This is not a software defect and cannot be closed by guessing a firmware build.

## 4. Independent security review — cannot self-certify

Required independent review scope:
1. Command envelope canonicalization, signature algorithm, key hierarchy, rotation and revocation.
2. Replay resistance, nonce/idempotency, time uncertainty and offline command rules.
3. Local AEGIS authorization and Android management-mode enforcement.
4. Vault key hierarchy, AEAD usage, nonce management, biometric binding and recovery.
5. Family/enterprise separation, support access, privacy and destructive-action controls.
6. Threat model coverage, negative test sufficiency and residual risks.

Reviewer must be independent of the author, record scope/method, findings with severity, evidence, remediation, and retest status. A self-review or AI-generated assessment is not independent assurance.

**Status: BLOCKED — no independent reviewer or signed review report identified.**

## 5. Backend and IROKA integration

No live endpoint or signing service is required to finish the A2 interface contract, but implementation readiness requires an agreed ownership and interface plan:
- AEGIS backend service owner and hosting/security boundary;
- IROKA issuer identity, signing-key custody, trust-root ceremony and rotation;
- device attestation verifier and current root/revocation source;
- registration, policy, command, revocation and receipt API contracts;
- data residency, retention, privacy and incident response;
- offline behavior and service-outage guarantees.

**Status: PARTIAL — interface requirements drafted; operational services and owners not verified.** Do not connect production endpoints or create signing keys as part of A2.

## 6. Dependency, SBOM and build pipeline

There is no Android application dependency graph to scan yet. This is a sequencing dependency, not an A2 documentation failure. At A3 inception, pin plugin/dependency versions, generate an SBOM, review licenses and vulnerabilities, configure reproducible build/provenance where feasible, and ensure workflows do not require paid runners or paid secrets. Do not add speculative application dependencies during A2.

**Status: NOT APPLICABLE YET / A3 ENTRY CONTROL.**

## 7. A2 exit gates

A2 may be proposed for closure only when all are satisfied:
- critical contracts reviewed and accepted by Owner;
- physical-device profile recorded, or Owner signs a scoped waiver and approves an alternative supported test-device matrix;
- Owner decisions in section 2 accepted or explicitly deferred with impact and scope;
- independent security review completed with no unresolved critical/high findings, or explicit risk disposition approved by Owner;
- backend/IROKA interface ownership and non-production integration plan agreed;
- threat-to-control-to-test matrix complete, with every test marked planned (not run) until A3/A4;
- no unsupported device capability or management privilege is presented as guaranteed;
- Owner explicitly accepts A2 closure and separately authorizes A3.

A2 closure does not imply application build, security certification, production readiness, or deployment authorization.

## 8. Current truthful status

- Documentation contracts: drafted and committed.
- Owner policy decisions: pending.
- Actual handset profile: unknown.
- Independent security review: not performed.
- Operational backend/IROKA endpoints: not verified.
- Android app dependencies/build: not present or not verified.
- A3: blocked pending A2 exit and separate authorization.
- A4/A5: not run / not authorized.

This plan is an A2 planning artifact only. It does not authorize device changes, installation, enrollment, key creation, production service setup, merge, release, or deployment.


## 9. Evidence collection pack

The [A2 Evidence Collection Pack](A2-EVIDENCE-COLLECTION-PACK.md) defines the read-only handset information to collect, prohibited personal identifiers, independent security review deliverables, and interface ownership worksheet. It reduces process ambiguity but does not itself resolve physical-device, reviewer, or backend-owner blockers.


## 10. Owner decision record

### A2-OD-01 — Vault recovery policy

**Decision:** APPROVED by the Owner on 2026-11-25.

**Approved baseline:**
- Vault recovery uses a user-controlled, high-entropy recovery secret.
- No provider or service operator recovery is permitted.
- The permanent-loss risk must be disclosed clearly: loss of the recovery secret together with the relevant device/key material may make vault data permanently unrecoverable.

**Scope and limits:** This approval settles the v1 recovery authority model only. It does not select cryptographic algorithms or parameters, authorize implementation or key generation, approve enterprise escrow, or close the remaining vault design decisions. Recovery-secret handling, rate limiting, phishing resistance, backup behavior, and independent cryptographic review remain design/review requirements.

**Evidence:** Owner instruction in the AEGIS ONE project conversation dated 2026-11-25. Repository record updated on the documentation branch; no application code or device state changed.


### A2-OD-01A — Recommended technical design (recommendation; not implementation authorization)

The recommended design is a two-path envelope-encryption model:

1. **Recovery secret generation:** Generate 256 bits of uniformly random entropy with the platform cryptographically secure random generator. Do not use a user-created password as the recovery secret.
2. **Human-readable encoding:** Encode the entropy as a 24-word mnemonic using a vetted, checksum-protected standard implementation. The exact standard and library require dependency review and independent security review before adoption.
3. **Vault key:** Generate the vault root/data-encryption key independently and randomly. Protect it through separate wrapping paths: one using a device-bound key protected by Android Keystore, and one using a key derived from the recovery secret.
4. **Cryptographic construction:** Use a reviewed KDF with explicit domain separation and a reviewed authenticated-encryption construction with unique nonces for each wrapping operation. Exact algorithms, parameters, nonce lifecycle, and libraries remain subject to A3 design review and independent security review; no novel cryptography.
5. **Phrase handling:** Show the phrase only during setup or deliberate recovery enrollment; require confirmation; advise an offline physical backup. Never transmit it to AEGIS/IROKA services or place it in logs, analytics, crash reports, or automatic clipboard contents.
6. **Recovery flow:** Require the phrase plus the encrypted vault data/backup. After successful recovery, provision a new device-bound key, re-wrap the vault key, and revoke the lost device's registration where applicable. The phrase must not provide a server operator with plaintext access.
7. **Loss semantics:** If the phrase and all devices/key material capable of decrypting the vault are lost, recovery is intentionally impossible. Explain this clearly before enrollment and remind users to maintain a safe offline copy.
8. **Rotation:** Rotate the phrase only through an authenticated existing device or the current recovery phrase. Re-wrap the same vault key under a newly generated recovery secret and invalidate the old phrase.

**Important qualification:** This is the recommended architecture, not approval of a particular mnemonic standard, cryptographic algorithm/library, implementation, backup provider, or production key operation. Those require subsequent technical review and separate A3 authorization.


### A2-OD-02 — Enterprise vault escrow policy

**Decision:** APPROVED by the Owner on 2026-11-25.

**Approved v1 baseline:**
- Enterprise vault escrow is disabled in v1.
- Personal vault data and enterprise-managed data must remain cryptographically separate, with distinct key hierarchies and authorization boundaries.
- Enterprise enrollment or management must not silently transfer access to, decrypt, or change the recovery authority for an existing personal vault.
- Enterprise administrators and service operators receive no implicit personal-vault recovery or decryption capability.

**Future consideration:** Any later enterprise escrow capability requires a separate versioned proposal and explicit Owner approval. It must define tenant-held key custody, dual-control authorization, employee disclosure/consent, purpose limitation, auditability, revocation, and independent security review. This future possibility does not weaken the v1 prohibition.

**Scope and limits:** This decision settles the v1 enterprise escrow policy only. It does not authorize enterprise enrollment, implementation, key generation, or production services. It does not itself define the complete technical separation architecture; detailed key-domain, storage, backup, and migration contracts remain subject to design review before implementation.

**Evidence:** Owner instruction in the AEGIS ONE project conversation dated 2026-11-25. Recorded on the documentation branch; no application code, device state, or `main` branch was changed.


### A2-OD-03 — Family protection policy

**Decision:** APPROVED by the Owner on 2026-11-25.

**Approved v1 baseline:**
- Family protection must use transparent, age-appropriate, least-privilege guardian controls.
- Guardian authority is limited to lawful, disclosed safety policies and device-use controls; it does not grant unrestricted access to a child's private vault, passwords, recovery secrets, personal files, or communications.
- No covert surveillance, hidden recording, or undisclosed collection of private communications is permitted.
- Children receive age-appropriate notice of monitoring, restrictions, and guardian controls.
- Emergency protections must have defined purposes, authorization, safeguards, and auditability.
- Account setup and guardian authority must account for applicable age requirements, custody arrangements, local law, and child assent where appropriate.
- Family roles and permissions remain separate from enterprise administrator privileges.

**Implementation authorization and limits:** The Owner's instruction on 2026-11-25 authorizes the necessary A3 implementation work for the approved family-protection model. This authorization is subject to the A2 exit gates and does not waive unresolved A2 blockers, independent security review, legal review, or verification requirements. It does not authorize merge, release, deployment, production enrollment, or changes to the Owner's handset; those remain separate gates. Other pending Owner decisions (including P3/P4 action policy and offline policy) are not approved by this decision.

**Evidence:** Owner instruction in the AEGIS ONE project conversation dated 2026-11-25: approval and authorization to implement the approved family-protection model and necessary scoped work. Recorded on the documentation branch; no application code, device state, or `main` branch was changed.


### A2-OD-04 — P3/P4 sensitive and irreversible action authorization

**Decision:** APPROVED by the Owner on 2026-11-25.

**Approved v1 policy — P3 high-impact actions:**
- Require an authenticated, explicitly authorized human administrator or account owner, according to the action and management mode.
- Require a signed, scoped, time-limited approval.
- Require local AEGIS policy verification before execution.
- Enforce least privilege, audit logging, replay protection, and idempotency.
- Notify affected users where legally and technically feasible.
- AI models and autonomous agents may recommend or prepare P3 actions but cannot independently approve them.

**Approved v1 policy — P4 irreversible/critical actions:**
- P4 capabilities are disabled by default in v1, including remote wipe, cryptographic key destruction, irreversible vault deletion, and permanent account closure.
- AI-only or service-operator-only authorization is prohibited.
- Any future P4 capability requires a separately approved policy, strong human authorization, explicit scope, independent security review, recovery analysis, and auditable safeguards.
- Local emergency protections must not silently become irreversible remote actions.

**Authority boundaries:**
- Family guardian authority does not automatically confer enterprise administrator authority.
- Enterprise administrators receive no access to personal vault keys.
- IROKA cannot override local authorization.
- Device Owner privileges, where legitimately provisioned, do not remove policy and authorization checks.
- Emergency actions must remain narrowly scoped and cannot become a general authorization bypass.

**Scope and limits:** This decision approves the policy baseline only. It does not authorize implementation or activation of P3/P4 capabilities, remote wipe, key destruction, production command signing, deployment, or changes to the Owner's handset. P4 remains disabled in v1. Any later P4 enablement requires a new explicit Owner decision.

**Evidence:** Owner approval in the AEGIS ONE project conversation dated 2026-11-25. Recorded on the documentation branch; no application code, device state, or `main` branch was changed.


### A2-OD-05 — Offline protection and authorization policy

**Decision:** APPROVED by the Owner on 2026-11-25, including the proposed maximum policy lifetimes.

**Approved v1 baseline:**
- Essential local protection continues during loss of connectivity; connectivity loss must not disable local vault encryption, app locking, or other already-authorized local protections.
- Cached policy must be issuer-signed and validated for issuer/trust root, device/account scope, version, integrity, and validity period.
- Maximum cached-policy lifetime: P0/P1 low-risk operations up to 7 days; P2 operations up to 24 hours. These are upper bounds, not guaranteed entitlements; individual operations may require shorter validity or online verification.
- P3 remote commands are denied offline in v1 unless a future, separately approved offline protocol explicitly defines authorization, freshness, revocation limitations, and safeguards.
- P4 remains disabled in v1.
- Offline mode must deny new administrator privileges, trust-root changes, sensitive export, and destructive operations unless a separately approved local procedure explicitly permits the specific operation.
- Detect and safely handle clock rollback/time uncertainty; do not extend policy validity silently.
- Apply known revocations immediately. Clearly represent that revocations not yet received cannot be known while disconnected.
- Queue security events in protected local storage with sequence/integrity controls and synchronize when connectivity returns; local tamper-evidence is not represented as absolute immutability.
- Provide a safe reconnect/recovery path without weakening local authentication or policy enforcement.

**Additional Owner requirement recorded with OD-05:** The vault subsystem is to be comprehensive and integrated with the security architecture. AEGIS should activate eligible early-boot protections as early as the Android platform permits, rather than waiting for the full user-facing app UI to open. Device-wide administrative control is conditional on Android Enterprise provisioning and supported Android APIs; this is not authority to bypass Android's OS security model or obtain unrestricted root/system access.

**Platform feasibility boundary:** Android starts and enforces its own boot chain before third-party apps. A normal app cannot execute before Android or obtain unrestricted system administration. A Direct-Boot-aware component may run during the limited pre-first-unlock phase using device-protected storage and supported APIs; credential-encrypted vault data and keys remain unavailable until the user unlocks, subject to Android/Keystore behavior. Full Device Owner policy authority requires eligible managed-device provisioning. These limits are mandatory design constraints, not optional caveats.

**Scope and limits:** This decision approves offline policy and records the additional vault/early-boot/management requirements for A2 feasibility reconciliation. It does not authorize bootloader changes, root, firmware modification, custom OS, factory reset, enterprise enrollment, privileged system access, production key generation, merge, release, or deployment. Exact vault cryptography and implementation remain subject to technical design and independent security review. Existing OD-01 recovery and OD-02 no-escrow rules remain in force.

**Evidence:** Owner approval and additional requirements in the AEGIS ONE project conversation dated 2026-11-25. Recorded on the documentation branch; no application code, device state, or `main` branch was changed.


### A2-OD-06 — Enterprise management authority and capability policy

**Decision:** APPROVED by the Owner on 2026-11-25.

**Approved v1 enterprise baseline:**
- AEGIS may use the maximum documented Android Enterprise Device Owner capabilities that are actually supported by the enrolled device, Android release, provisioning mode, and applicable APIs.
- Enterprise management is a distinct, opt-in fully managed profile and may only be activated after eligible Android Enterprise provisioning and explicit organizational/device-owner authorization.
- Device-wide configuration must be mediated by documented Android Enterprise/DevicePolicyManager APIs and a typed capability allowlist. No root, unrestricted system UID, bootloader access, firmware modification, privilege escalation, hidden control, or Android security bypass.
- Enforce tenant-scoped roles, least privilege, separation of duties, explicit policy approval, employee transparency, audit records, revocation, and safe failure behavior.
- Device capability discovery must precede policy activation. Unsupported controls must be reported as unsupported, not simulated or represented as enforced.
- Enterprise policy must not silently change ownership of, decrypt, escrow, or gain access to an existing personal vault. OD-02 personal/enterprise cryptographic separation remains binding.
- Family mode and consumer mode remain separate and do not inherit enterprise Device Owner privileges.
- Remote management remains subject to OD-04 P3 human authorization and P4 disabled-in-v1 rules, and OD-05 offline policy.

**Additional advancement recommendations included within this authorization:**
1. Signed, versioned, tenant-scoped managed configuration with staged rollout, policy validation, rollback to last-known-good non-expired policy, and emergency safe-disable controls.
2. Device compliance posture reporting with explicit evidence freshness and UNKNOWN/UNSUPPORTED states; no false PASS for unavailable attestation or management signals.
3. Managed app configuration and per-capability policy adapters with runtime feature detection and OS-version compatibility tests.
4. Work-profile/personal-profile boundary awareness where applicable; do not treat a work profile as equivalent to fully managed Device Owner.
5. Administrator MFA/strong authentication, role separation, dual approval for high-impact administrative policy changes, and immutable-to-ordinary-admin audit export where technically supported.
6. Enrollment/de-enrollment lifecycle, ownership disclosure, device reassignment/wipe semantics, and employee notice before any action that affects personal or enterprise data. Destructive P4 functions remain disabled in v1.
7. Network controls such as always-on VPN and lockdown only where documented APIs, provisioning role, explicit tenant policy, user/legal notice, connectivity recovery, and failure testing support them.
8. Central policy templates with safe defaults, drift detection, configuration diff/approval, staged deployment rings, and rollback safeguards.
9. Security event monitoring, incident response hooks, key/certificate rotation planning, revocation propagation, and privacy-minimized retention.
10. A capability matrix and automated negative tests for each supported Android/API/device management mode.

**Scope and limits:** Owner authorization covers architecture, documentation, and subsequent A3 implementation planning/implementation of the approved enterprise management model and the listed recommendations, after A2 exit requirements are met. It does not authorize enrollment or changes to the Owner's handset, production tenant activation, root/system privileges, production key generation, merge, release, or deployment. Any feature requiring a new external service, paid dependency, OEM-only capability, or materially broader data collection must be separately assessed; it is not automatically authorized by the phrase "all advancements." A5 remains a separate gate.

**Evidence:** Owner approval in the AEGIS ONE project conversation dated 2026-11-25, including request to include all recommended advancements. Recorded on the documentation branch; no application code, device state, or `main` branch was changed.
