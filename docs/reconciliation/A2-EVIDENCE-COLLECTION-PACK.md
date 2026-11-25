# AEGIS ONE — A2 Evidence Collection Pack

**ID:** AEGIS-A2-EVIDENCE-001 | **Version:** 1.0 | **Status:** READY FOR EVIDENCE COLLECTION — NO DEVICE ACTION AUTHORIZED

## 1. Purpose

This pack turns the remaining A2 evidence gaps into a controlled, privacy-preserving collection procedure. It does not require installing AEGIS, enabling developer options or USB debugging, enrolling the handset, changing security settings, unlocking the bootloader, or resetting the device.

## 2. Reference handset evidence sheet

Target reference: Samsung Galaxy A16 **SM-A165F**. Record only what is visible in Settings. Menu wording can vary by One UI release.

| Evidence | Read-only collection path | Record |
|---|---|---|
| Model number | Settings → About phone | Model number; confirm SM-A165F |
| Software build | Settings → About phone → Software information | Build number; Android version; One UI version |
| Security patch | Settings → About phone → Software information | Android security patch level |
| Baseband | Settings → About phone → Software information | Baseband version (optional; redact if sharing publicly) |
| Service provider software / CSC | Settings → About phone → Software information | Region/CSC string; redact subscriber/carrier identifiers |
| Knox | Settings → Security and privacy / About phone (if displayed) | Only the Knox version/status actually shown; otherwise UNKNOWN |
| Biometrics | Settings → Security and privacy → Biometrics | Whether fingerprint is enrolled; do not disclose biometric data |
| Device lock | Settings → Lock screen and AOD / Screen lock type | Screen-lock category only; never disclose PIN/password |
| Bootloader / verified boot | No ordinary Settings screen reliably proves all required claims | UNKNOWN unless independently attested by a trusted diagnostic |
| StrongBox / hardware-backed key | Not reliably established by Settings | UNKNOWN until runtime capability probe and/or valid key attestation |
| Android Enterprise mode | Settings → Security and privacy / Device admin apps / Work profile (where present) | State whether a work profile or management is already present; do not enroll |

**Do not submit:** IMEI, serial number, phone number, SIM identifiers, account email, lock credentials, biometric material, QR enrollment tokens, or screenshots containing notifications/personal data.

If the actual handset is unavailable, record **NOT AVAILABLE** and propose an alternative test-device matrix. Do not infer its firmware from the model number or a vendor update page.

## 3. Evidence record template

Copy and complete only fields that are visible and safe to share:

- Collection date (local): 
- Collector role (Owner / delegated reviewer):
- Device model: 
- Android version / API level (API may be derived from Android version only after recording the version):
- One UI version:
- Build number (redacted if needed):
- Security patch date:
- CSC/region (redacted):
- Knox status: VERIFIED / NOT SHOWN / UNKNOWN
- Work profile or device management already active: YES / NO / UNKNOWN
- StrongBox: UNKNOWN unless technically probed
- Hardware-backed Keystore: UNKNOWN unless technically probed
- Verified boot / bootloader state: UNKNOWN unless technically attested
- Evidence source: Settings screen names / trusted diagnostic report
- Redactions applied:
- Reviewer and date:

## 4. Optional technical evidence — separate, explicit consent

A later diagnostic session may use Android's documented read-only capability queries to check feature declarations and key-attestation results. This is not required for the initial Settings record. Do not enable USB debugging or install diagnostic software solely to complete this pack without the Owner's separate consent. Any diagnostic must avoid extracting private keys or personal identifiers and must document tool version, commands, output, and limitations.

StrongBox availability, KeyMint security level, verified boot state, and attestation trust must be established through supported platform APIs and server-side certificate-chain validation; a screenshot or model-level product listing is not sufficient.

## 5. Independent security review package

Provide the reviewer with the following exact versioned artifacts from the A2 branch:

1. `docs/security/THREAT-MODEL-A1.md`
2. `docs/contracts/AEGIS-DEVICE-IDENTITY-AND-RECOVERY-CONTRACT.md`
3. `docs/contracts/AEGIS-ROLES-CONSENT-AND-POLICY-MATRIX.md`
4. `docs/contracts/AEGIS-IROKA-MOBILE-COMMAND-CONTRACT.md`
5. `docs/contracts/AEGIS-VAULT-CRYPTOGRAPHY-AND-RECOVERY.md`
6. `docs/contracts/AEGIS-OFFLINE-PROTECTION-AND-AUDIT-CONTRACT.md`
7. `docs/reconciliation/A2-TRACEABILITY-AND-EXIT-REGISTER.md`
8. `docs/reconciliation/A2-BLOCKER-RESOLUTION-AND-OWNER-DECISIONS.md`

Reviewer independence and deliverable requirements:
- Reviewer is not the author of these contracts and has no implementation delivery conflict.
- Review is scoped to cryptographic design, protocol correctness, Android enforcement assumptions, privacy/authority boundaries, abuse cases, and negative-test completeness.
- Report identifies reviewer qualifications, date, exact commit SHA reviewed, methodology, files reviewed, findings, severity, exploitability, remediation, and retest result.
- Critical/high findings must be fixed and retested or explicitly risk-accepted by the Owner before A2 closure.
- An AI-generated review or author self-review does not satisfy this gate.

No reviewer has been engaged by this document. Review status remains **NOT PERFORMED**.

## 6. Backend and IROKA interface ownership worksheet

These fields can be agreed at architecture level without provisioning production services or creating keys:

| Interface / control | Required accountable owner | Current evidence |
|---|---|---|
| AEGIS account/device registry | AEGIS backend owner | UNKNOWN |
| IROKA issuer identity and trust roots | IROKA security/governance owner | UNKNOWN |
| Signing-key custody and rotation | Designated key custodian / security owner | UNKNOWN |
| Attestation verification and root updates | Backend security owner | UNKNOWN |
| Policy publication and revocation | Policy authority | Contract drafted; operational owner UNKNOWN |
| Audit receipt and retention | Security/privacy owner | Contract drafted; retention decision OPEN |
| Incident response and key compromise | Security incident commander | UNKNOWN |

Do not place private keys, credentials, endpoint secrets, or production user data in this public repository. A2 requires named accountability and interface definitions, not production provisioning.

## 7. A2 evidence acceptance rules

- Every claim has a source, collection date, collector, and redaction note.
- Distinguish VERIFIED, OWNER-REPORTED, VENDOR-DOCUMENTED, INFERRED, UNKNOWN, and NOT AVAILABLE.
- Model-level support does not prove actual handset state.
- A device capability is not guaranteed until runtime detection and negative-path behavior are designed.
- Missing evidence stays UNKNOWN; do not convert it to PASS.
- Device collection does not authorize installation, enrollment, factory reset, policy changes, or remote management.

## 8. Current status

- Physical handset evidence: **NOT COLLECTED**.
- Independent security review: **NOT PERFORMED**.
- Backend/IROKA accountable owners: **NOT IDENTIFIED**.
- This pack: **DOCUMENTED**.
- A2: **OPEN**.
- A3: **BLOCKED** pending A2 exit and separate Owner authorization.
