# AEGIS ONE — A2 Blocker Resolution Plan and Owner Decision Register

**ID:** AEGIS-A2-BLOCKERS-001 | **Version:** 1.0 | **Status:** OPEN — proposed dispositions, not Owner-approved

## 1. Purpose and disposition

This register separates blockers that can be reduced through repository documentation from blockers that require physical-device evidence, an independent reviewer, an external service, or explicit Owner decisions. No unknown is represented as verified and no proposed default is represented as approved.

## 2. Recommended safe defaults for Owner decision

| Decision | Recommended baseline | Rationale | State |
|---|---|---|---|
| Vault recovery | User-controlled high-entropy recovery secret; no provider/operator recovery; disclose permanent-loss risk | Minimizes unilateral recovery authority and server-side exposure | OWNER-APPROVED 2026-11-25 |
| Enterprise escrow | Disabled in v1; consider only in a later separately approved enterprise profile with tenant-held keys and dual control | Avoids silent employer access to personal vault data | OWNER APPROVAL REQUIRED |
| Family model | Transparent, age-appropriate, least-privilege guardian controls; no default vault/content surveillance | Limits coercive or excessive access | OWNER APPROVAL REQUIRED; legal review required |
| P3 actions | Require scoped admin authority, valid signed approval, and local policy check; user notice where feasible | Prevents AI/service-only device-impacting actions | OWNER APPROVAL REQUIRED |
| P4 actions | Disabled in v1; no remote wipe, key destruction, or irreversible account closure until separately approved and reviewed | Reduces catastrophic and irreversible misuse | OWNER APPROVAL REQUIRED |
| Offline policy | Permit only low-risk local protection under a signed, unexpired policy; deny sensitive export/recovery/destructive actions offline | Preserves local safety without extending remote authority | OWNER APPROVAL REQUIRED |
| Attestation failure | Unknown/unsupported → restricted trust state, not PASS; define blocking tiers after device/service capability validation | Avoids false assurance and avoids assuming every device supports identical evidence | PROPOSED TECHNICAL DEFAULT |
| Consumer anti-removal | Do not promise prevention of uninstall; provide user-mediated account/device recovery and local tamper signals where available | Ordinary apps do not possess Device Owner authority | PLATFORM CONSTRAINT |
| Enterprise management | Separate opt-in fully managed deployment profile; only use capabilities confirmed by actual provisioning | Separates enterprise privileges from consumer mode | OWNER APPROVAL REQUIRED |
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
