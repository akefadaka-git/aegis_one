# AEGIS ONE — A2 Android and Device Reconciliation

**Document ID:** AEGIS-A2-REC-001  
**Version:** 1.0  
**Status:** PARTIAL — OPEN; NOT AUTHORIZATION TO IMPLEMENT  
**Repository:** `akefadaka-git/aegis_one`  
**Reconciliation date:** 2026-11-25  
**Target:** Samsung Galaxy A16 SM-A165F; Android/Kotlin

## 1. Executive result

A2 confirms that a substantial subset of AEGIS ONE's intended protections can be built using public Android APIs. Several high-impact capabilities are conditional on Android Enterprise management, user consent, hardware support, server-side contracts, and exact device firmware.

**A2 is not closed.** The most important blocker is the absence of a verified AEGIS-to-IROKA mobile execution and authorization contract. Other blockers include unverified physical-device state, Device Owner provisioning constraints, family/enterprise authority models, device attestation validation, and security acceptance tests.

No app source code, installation, enrollment, factory reset, or deployment is performed by this reconciliation.

## 2. Evidence and provenance

### 2.1 AEGIS repository

- Repository: `akefadaka-git/aegis_one`
- Initial branch: `main`
- Initial commit: `570b6fe5abfcf0d7b369af63e211ab937bfe4024`
- Initial tracked file: `README.md`
- README blob: `8ccafd205d71e37bf8b602c896aa0c74daf14797`
- No application source, Gradle build, tests, CI workflow, integration contract, or release artifact was present at the inspected baseline.

### 2.2 IROKA source evidence

Read-only inspection of `akefadaka-git/iroka`:

- README at main: `a85ee805e35481b20087e8f32e451eb1bd4eb4a7`
- Implementation strategy: `docs/IMPLEMENTATION_STRATEGY.md`, blob `85d3e69ef160f53acdc4aa2fcf2783581f639d24`

The IROKA README defines twelve logical chromosomes G01–G12 and the Governed Kernel as the trusted authority boundary. Protected operations resolve to `ALLOW`, `DENY`, `REQUIRE_APPROVAL`, or `ESCALATE`. Models and agents cannot self-authorize. Its implementation strategy requires explicit identity, policy, approval, scope, expiry, audit, revocation, and evidence contracts.

These are IROKA-level architecture sources, not proof that an AEGIS mobile integration contract or deployed IROKA endpoint exists. They are therefore architectural inputs requiring adaptation, not an already-operational dependency.

## 3. Device and platform reconciliation

### 3.1 Android version and API-level correction

The earlier working assumption of Android 16 / API 40 is not valid. Android's official SDK version table maps Android 16 to API 36 and Android 17 to API 37. The Galaxy A16 SM-A165F Samsung update feed surfaced for one regional software channel lists an Android 17 / One UI 9 release dated 2026-11-02. This is evidence that an Android 17 firmware exists for this model, not proof that the actual target handset has installed it.

**A2 correction:** do not encode API 40. The app's minimum and target SDK levels must be selected during A3 planning against supported Android SDK releases, policy, and test coverage. Runtime compatibility must be detected from the device, not inferred from model name.

### 3.2 Knox and Android Enterprise

Samsung Knox's supported-device information lists Galaxy A16 variants including SM-A165F, Knox Platform 3.13, and Android Enterprise Recommended status. This establishes a vendor-listed capability profile for the model family. It does not prove the actual device's current Knox state, firmware CSC, security patch, enrollment eligibility, or availability of every Knox commercial API.

No Knox SDK dependency is approved by this A2. Prefer public Android APIs unless a specific Knox-only requirement is demonstrated, licensed, and separately approved.

### 3.3 Capability matrix

| Capability | Android/platform evidence | AEGIS disposition | Status |
|---|---|---|---|
| App-level vault encryption | Android Keystore and authenticated cryptographic keys | Feasible; use per-install/per-vault key hierarchy, authenticated encryption, safe recovery and key rotation design | PARTIAL — device hardware backing not tested |
| Biometric unlock | BiometricPrompt and auth-bound Keystore keys | Feasible where enrolled strong biometrics and supported authenticators exist; include device credential fallback policy | PARTIAL — hardware/user enrollment unknown |
| StrongBox | Optional device feature, discoverable at runtime | Treat as preferred optional hardening, not a baseline guarantee; handle unavailable/unsupported cases | UNRESOLVED — handset feature not probed |
| Hardware key attestation | Android Key Attestation supports remote validation; server must validate chain, root, challenge, revocation and claims | Define server-side verifier and nonce protocol before relying on device trust assertions | MISSING — AEGIS verifier contract |
| App lock / protected workspace | App can gate its own protected UI/data; Android app cannot generally lock arbitrary third-party apps | Implement own-app and vault protection; third-party app lock requires explicit supported management controls and must not be promised in consumer mode | PARTIAL |
| Device Owner / full management | DevicePolicyManager privileged policies are available to a provisioned Device Owner / Profile Owner | Separate enterprise-managed mode; no assumption of authority in unmanaged mode | CONDITIONAL |
| Anti-removal | Device Owner is established during managed provisioning and is protected from ordinary user modification; ordinary apps cannot prevent their own uninstall | Reword as managed-device policy/recovery capability only; no consumer-mode anti-uninstall promise | CONDITIONAL / PARTIAL |
| Always-on VPN | Android supports always-on VPN from API 24; DevicePolicyManager can configure it for a Device Owner/Profile Owner | Consumer mode requires user-mediated VPN setup; enterprise lockdown requires correct management role and tested recovery from VPN failure | CONDITIONAL |
| VPN lockdown | Android can block non-VPN traffic; a failed VPN can interrupt network access | Require explicit consent/policy, escape/recovery path, health monitoring, and test cases | PARTIAL |
| Device integrity | Boot state, patch, app signing, and attestation signals can contribute to risk assessment | Signals are evidence inputs, not absolute proof of safety; server-side verification required | PARTIAL |
| Anti-tracking | App permissions, privacy indicators, network filtering and user-controlled settings | Define exact threat model; no claim to suppress all OS-level or third-party tracking | PARTIAL |
| Anti-theft / recovery | App-level workflows and conditional managed-device controls | Define lost-device state machine, account recovery, data protection and authorization; avoid remote wipe or lock without separate authorization | MISSING contract |
| Tamper-evident logs | Local append-only/hash-chained records can expose alteration; server receipts can strengthen evidence | Specify monotonic sequence, key custody, batching, offline queues, retention and server verification | PARTIAL |
| Remote management | Requires identity, authorization, signed commands, replay protection, expiry, audit and revocation | No command execution until mobile contract and local policy gate exist | MISSING |
| Offline protection | Local cryptographic controls and cached policy can function without network | Define safe cached-policy expiry, local emergency mode, replay protection and reconciliation on reconnect | PARTIAL |
| Family protection | Not equivalent to Android Enterprise Device Owner | Separate guardian/child roles, consent, transparency, legal/privacy and account-recovery contract | MISSING |
| Multi-device | Requires device registration, identity binding, key lifecycle and revocation | Define device enrollment/account linking independently of Android Enterprise provisioning | MISSING |
| On-device AI | Depends on model availability, RAM/storage/thermal budgets, runtime, and model license | Optional replaceable local inference adapter; no security authority or mandatory dependency | UNRESOLVED |

## 4. Critical management-mode finding

Android's Device Owner role is not a permission that an ordinary app can request for itself. It is established through Android's managed-device provisioning flow. Android documents that managed provisioning sets a device admin app as Device Owner and that the provisioning action is restricted to an unprovisioned device. Fully managed Android Enterprise provisioning is intended for company-owned devices.

The Owner's current constraints prohibit factory reset and enrollment. Therefore:

1. Device Owner implementation may be designed and contract-tested in a non-device test environment, but cannot be activated on the current target handset under the present authorization.
2. No claim of Device Owner behavior may be made from a normal app install.
3. Consumer-mode behavior must remain independently useful without Device Owner.
4. A future enterprise provisioning action requires a new, explicit Owner authorization and a separate operational plan.

## 5. Required AEGIS operating modes

### Mode C — Unmanaged / consumer

- No Device Owner assumptions.
- Use runtime permissions and user-visible consent.
- App-level vault and protected-workspace enforcement.
- BiometricPrompt and Android Keystore subject to actual device capability.
- User-mediated VPN setup; no claim of silent global lockdown.
- Local threat assessment and emergency protection.
- Remote actions limited to authenticated, signed, scoped actions that pass local authorization and user/administrator policy.

### Mode E — Fully managed enterprise

- Only after eligible device ownership and successful Android Enterprise provisioning.
- Device Owner policies exposed through a dedicated policy adapter.
- Always-on VPN/lockdown only after explicit administrator policy and failure-recovery tests.
- Anti-removal and device-wide policy enforcement described only within documented Device Owner authority.
- Tenant, administrator, employee, and personal-data boundaries enforced.
- This mode is designed but not activated or enrolled in the present work.

### Mode F — Family protection

- Separate from enterprise mode and Device Owner assumptions.
- Guardian authority, child transparency, age-appropriate controls, consent, data minimization, emergency access, and recovery must be specified.
- No silent surveillance or unrestricted remote access.

## 6. IROKA integration reconciliation

IROKA's documented G01/G02/G08/G09/G11 concepts provide relevant architecture, but the AEGIS-specific adaptation remains incomplete.

| IROKA domain | Required AEGIS adaptation | Current result |
|---|---|---|
| G01 Identity | Device identity lifecycle, app instance identity, account binding, key rotation, revocation, attestation and recovery | MISSING AEGIS contract |
| G02 Governance | Owner/guardian/enterprise roles, approval lifecycle, local policy precedence, consent, emergency exceptions | PARTIAL architecture input; AEGIS role matrix missing |
| G08 Orchestration | Offline command queue, command status, idempotency, expiry, retry, cancellation and recovery | MISSING mobile workflow contract |
| G09 Execution | Android capability manifest, local policy check, user-presence requirements, safe execution adapter and result receipts | MISSING AEGIS mobile execution contract |
| G11 Assurance | Security event schema, integrity evidence, local/server audit correlation, test attestations and incident export | PARTIAL architecture input; implementation contract missing |

### Required remote-command envelope (contract to be designed before implementation)

At minimum, every protected remote command must bind:

- command ID and unique nonce;
- issuer identity and signing-key ID;
- target account and device identity;
- action and typed parameters;
- exact capability and resource scope;
- issued-at, not-before, expiry and policy version;
- approval evidence and required user-presence class;
- replay/idempotency key;
- signature and canonical serialization version.

The local app must independently verify issuer trust, signature, target, scope, expiry, replay state, policy version, and local authorization. It must reject unknown, expired, revoked, malformed, or out-of-scope commands. A remote signature proves origin/integrity; it does not replace local authorization. IROKA cannot override Android permissions, the user, or the device's actual management role.

## 7. Security and privacy blockers

Before A2 closure, the project must define:

1. Device/account identity and recovery, including lost-device and reinstallation behavior.
2. Vault key hierarchy, backup/recovery trade-offs, key rotation and cryptographic erasure.
3. Family/enterprise role matrix, consent and tenant isolation.
4. Remote command signing, replay protection, revocation and local policy precedence.
5. Offline policy expiry and emergency-mode restrictions.
6. Audit integrity, clock rollback handling, log export and retention.
7. Threat model for rooted/compromised OS, malicious accessibility services, overlay attacks, phishing, app tampering, stolen credentials, malicious administrators, network interception, server compromise and supply-chain attacks.
8. Privacy data inventory, purpose limitation, retention and user-visible disclosures.
9. Device compatibility test plan for actual SM-A165F firmware and all optional hardware features.
10. Third-party dependency/license review and public-repository secret hygiene.

## 8. A2 decision register

| ID | Decision / finding | Result |
|---|---|---|
| A2-D01 | Retain PMSS v1.2 as architecture designation | ACCEPTED |
| A2-D02 | Correct historical Android 16 / API 40 assumption | CORRECTED: Android 16 = API 36; Android 17 = API 37 |
| A2-D03 | Treat Galaxy A16 SM-A165F as initial reference device, not a verified live device profile | ACCEPTED |
| A2-D04 | Separate unmanaged consumer mode from fully managed Device Owner mode | REQUIRED |
| A2-D05 | Device Owner and lockdown features are conditional on valid provisioning/role | CONFIRMED |
| A2-D06 | StrongBox and hardware-backed properties must be probed/attested | REQUIRED |
| A2-D07 | IROKA integration must use a dedicated AEGIS mobile contract | REQUIRED; MISSING |
| A2-D08 | Local protection must continue without IROKA connectivity | REQUIRED |
| A2-D09 | Family and enterprise management must have separate role/policy boundaries | REQUIRED |
| A2-D10 | No factory reset, enrollment, installation, deployment, or release during this stage | PRESERVED |

## 9. A2 closure criteria and remaining work

A2 remains OPEN until all of the following are evidenced:

- [ ] AEGIS device/account identity and recovery contract.
- [ ] AEGIS role, approval, consent, and policy-precedence matrix.
- [ ] Versioned IROKA mobile integration and remote-command contract.
- [ ] Android capability manifest separating consumer and managed-device features.
- [ ] Vault cryptographic/key lifecycle and recovery specification.
- [ ] Offline policy and emergency-protection specification.
- [ ] Threat-to-control-to-test traceability matrix.
- [ ] Actual SM-A165F firmware/API/Knox/hardware capability evidence, obtained without installation or enrollment.
- [ ] Dependency and licensing policy; no dependency adoption without review.
- [ ] Owner review and explicit A2 closure decision.

**A3 is blocked.** This document records A2 findings and does not authorize Android application implementation.

## 10. Sources

Official platform/vendor references consulted on 2026-11-25:

1. Android `DevicePolicyManager`: https://developer.android.com/reference/android/app/admin/DevicePolicyManager
2. Android Enterprise device provisioning: https://developers.google.com/android/management/provision-device
3. Android `VpnService`: https://developer.android.com/reference/android/net/VpnService
4. Android VPN guide: https://developer.android.com/develop/connectivity/vpn
5. Android biometric authentication: https://developer.android.com/identity/sign-in/biometric-auth
6. Android hardware key attestation: https://developer.android.com/privacy-and-security/security-key-attestation
7. Android SDK/API levels: https://developer.android.com/guide/topics/manifest/uses-sdk-element
8. Samsung Galaxy A16 SM-A165F update feed (regional channel): https://doc.samsungmobile.com/SM-A165F/031857241225/roh.html
9. Samsung Knox supported devices: https://www.samsungknox.com/en/knox-platform/supported-devices/kme

Vendor and platform capability listings do not substitute for testing the actual handset, firmware, carrier/region profile, or account configuration.

---
**A2 result: PARTIAL / OPEN. No A3 authorization.**


## 11. Detailed A2 contract artifacts

The following versioned design contracts extend this reconciliation:

- [Device Identity and Recovery Contract](../contracts/AEGIS-DEVICE-IDENTITY-AND-RECOVERY-CONTRACT.md)
- [Roles, Consent and Policy Matrix](../contracts/AEGIS-ROLES-CONSENT-AND-POLICY-MATRIX.md)
- [IROKA Mobile Command and Execution Contract](../contracts/AEGIS-IROKA-MOBILE-COMMAND-CONTRACT.md)
- [Vault Cryptography and Recovery Contract](../contracts/AEGIS-VAULT-CRYPTOGRAPHY-AND-RECOVERY.md)
- [Offline Protection and Audit Contract](../contracts/AEGIS-OFFLINE-PROTECTION-AND-AUDIT-CONTRACT.md)
- [A2 Traceability and Exit Evidence Register](A2-TRACEABILITY-AND-EXIT-REGISTER.md)

These are proposed contracts, not implementation specifications approved for coding. The traceability register records remaining blockers and A2 exit conditions. Their creation does not close A2 or authorize A3.

## 12. Blocker disposition and Owner decisions

See [A2 Blocker Resolution and Owner Decision Register](A2-BLOCKER-RESOLUTION-AND-OWNER-DECISIONS.md). It separates proposed safe defaults from evidence that cannot be inferred remotely, including physical handset facts and independent security review. A2 remains open; the register is not an Owner approval.


## 13. Owner-reported handset software information received (2026-11-25)

The Owner supplied an initial device-information record:

| Field | Value | Classification |
|---|---|---|
| Model number | Not supplied | UNKNOWN |
| Android version | 17 | OWNER-REPORTED |
| Android API level | 37 | DERIVED from Android 17 |
| One UI | 9.0 | OWNER-REPORTED |
| Build number | `CP2A.260605.A165FXXUBEZJ7` | OWNER-REPORTED |
| Android security patch | 5 October 2026 | OWNER-REPORTED |
| CSC / service provider software | Not supplied | UNKNOWN |

This updates the prior software-version uncertainty: the Owner reports Android 17 / API 37, One UI 9.0, build `CP2A.260605.A165FXXUBEZJ7`, and a 5 October 2026 security patch. It does **not** establish that the handset is SM-A165F because the model-number field was blank. Nor does it establish Knox state, StrongBox availability, hardware-backed Keystore/KeyMint security level, verified boot, bootloader state, or Device Owner provisioning.

The values have not been independently inspected or cryptographically attested. Treat them as Owner-reported evidence pending a complete model/CSC record and, where relevant, runtime capability probes. No device settings were changed and no diagnostic software was installed.


## 14. Owner-reported exact model confirmation (2026-11-25)

The Owner confirmed the model number as **SM-A165F/DS** (Samsung Galaxy A16 dual-SIM variant). This resolves the missing model-number field in the initial evidence record. Combined with the previously supplied software values, the reference handset profile is now:

| Field | Value | Classification |
|---|---|---|
| Model | Samsung Galaxy A16 SM-A165F/DS | OWNER-REPORTED |
| Android | 17 | OWNER-REPORTED |
| API level | 37 | DERIVED from Android 17 |
| One UI | 9.0 | OWNER-REPORTED |
| Build | `CP2A.260605.A165FXXUBEZJ7` | OWNER-REPORTED |
| Security patch | 5 October 2026 | OWNER-REPORTED |
| CSC / service provider software | Not supplied | UNKNOWN |

The reference model identity is now complete at the Owner-reported level. No independent handset inspection or cryptographic attestation has occurred. Knox state, StrongBox, hardware-backed Keystore/KeyMint security level, verified boot/bootloader state, and Device Owner/work-profile status remain UNKNOWN. This evidence does not authorize installation, enrollment, configuration changes, or factory reset.


## 15. Owner-reported firmware identifiers (2026-11-25)

After the service-provider-software field could not be located in Settings, the Owner used the handset's information screen (dialer code `*#1234#`) and supplied these values:

| Field | Value | Classification |
|---|---|---|
| AP | `A165FXXUBEZJ7` | OWNER-REPORTED |
| CP | `A165FXXUBEZJ7` | OWNER-REPORTED |
| CSC | `A165FOJMBEZJ7` | OWNER-REPORTED |

This resolves the previously missing CSC/firmware identifier at the Owner-reported level. The CSC string is recorded verbatim; no unsupported inference is made about the active carrier, country configuration, or service-provider profile. AP/CP/CSC values have not been independently inspected or cryptographically attested.

The combined reference profile remains Samsung Galaxy A16 SM-A165F/DS, Android 17 / API 37, One UI 9.0, build `CP2A.260605.A165FXXUBEZJ7`, security patch 5 October 2026, and the AP/CP/CSC values above. Knox state/version, StrongBox availability, hardware-backed Keystore/KeyMint security level, verified boot/bootloader state, and Device Owner/work-profile status remain UNKNOWN. No device settings were changed, no diagnostic software was installed, and no enrollment or reset was performed.


## 15. Owner requirement — early-boot protection and system administration

**Owner requirement (2026-11-25):** Security protections should become active with the operating system as early as Android permits, not wait for the complete AEGIS user interface to open. The Owner also requested device-wide administrative configuration capability.

**Android feasibility and security boundary:**
- Android's boot chain, verified boot, encryption and system services initialize before third-party applications. A normal third-party app cannot execute before Android itself or replace the OS security authority.
- AEGIS may evaluate a Direct-Boot-aware, minimal early-start component using supported Android lifecycle entry points (including the locked-boot phase where supported), device-protected storage, and a narrowly defined allowlist of operations. It must not assume credential-encrypted storage, user-authenticated vault keys, or the full vault are available before first unlock.
- The early-start component must be minimal, resilient to process death, idempotent, observable, and fail safely. It must not unlock the vault, bypass the lock screen, access credential-protected user data, or perform P3/P4 actions before the applicable authorization conditions are met.
- After user unlock, the normal application may initialize credential-protected vault components, authenticate the user, and reconcile queued events/policy.
- Device-wide policy administration is available only to the extent granted by Android roles and supported APIs. A fully managed Device Owner can apply documented DevicePolicyManager policies after eligible Android Enterprise provisioning. It is not root, unrestricted system UID, firmware authority, or permission to override platform security.
- Consumer mode remains app-scoped and user-consented. Enterprise Device Owner mode requires separate eligible provisioning and explicit authorization. Family mode remains distinct and does not inherit enterprise powers.
- Root access, bootloader unlocking, custom firmware/OS, OEM signing, hidden privilege escalation, and bypassing Android security controls are outside scope.

**Vault implications:** Keep vault data encrypted at rest using the approved key hierarchy. Direct Boot support, if implemented, must use a separate minimal device-protected bootstrap state and must never cache plaintext vault keys or recovery secrets in device-protected storage. Vault content and credential-bound keys remain gated until successful user authentication/unlock. Early-boot security status must clearly distinguish protection active, limited pre-unlock mode, and full post-unlock vault availability.

**Status:** REQUIREMENT RECORDED; implementation feasibility requires A3 design and device/API validation. Current target handset capability, Direct Boot behavior, KeyMint/StrongBox details, and Device Owner eligibility remain UNKNOWN until verified. This requirement does not change the no-factory-reset/no-enrollment/no-device-modification constraints.


## 16. Approved enterprise management model and recommended advancements

**Owner decision:** A2-OD-06 APPROVED on 2026-11-25.

### 16.1 Enterprise management authority

AEGIS enterprise mode may use the maximum documented Android Enterprise Device Owner capabilities supported by the actual enrolled device and Android version. This authority is conditional on eligible fully managed provisioning and is limited to documented Android Enterprise/DevicePolicyManager APIs. It is not root, unrestricted system access, OEM authority, bootloader control, firmware modification, or permission to bypass Android security.

Consumer, family, work-profile, and fully managed enterprise modes must be represented distinctly. A work profile is not equivalent to a fully managed Device Owner device. No enterprise authority is inherited by family or consumer mode.

### 16.2 Recommended enterprise capabilities

Subject to device/API support and A3 design review:
- signed, versioned, tenant-scoped managed configurations;
- capability discovery and enforcement-state reporting before policy activation;
- policy validation, configuration diffs, staged rollout rings, last-known-good rollback, and safe-disable procedures;
- managed application configuration and per-capability policy adapters;
- device compliance posture with evidence freshness and explicit UNKNOWN/UNSUPPORTED states;
- tenant isolation, administrator MFA, least-privilege roles, separation of duties, and dual approval for high-impact administrative policy changes;
- enrollment, reassignment, de-enrollment, ownership disclosure, employee notice, and data-boundary lifecycle handling;
- always-on VPN/lockdown only where documented APIs and provisioning role permit, with connectivity recovery and failure tests;
- audit export, security event monitoring, privacy-minimized retention, revocation propagation, and incident response integration;
- OS/API compatibility matrix and negative tests for unsupported or partially supported capabilities.

### 16.3 Binding safety constraints

- Personal vault and enterprise data remain cryptographically separate; enterprise enrollment cannot silently transfer access or recovery authority.
- P3 operations require OD-04 human authorization, signed scoped approvals, and local policy checks. P4 remains disabled in v1.
- Offline behavior follows OD-05; no cached policy may silently outlive its approved maximum lifetime.
- Unsupported management capabilities must be surfaced as unsupported, not claimed as active.
- Emergency disable/rollback must not become a route to bypass local authorization or vault protections.
- All policy changes and administrative actions require attributable audit evidence, subject to documented limits of local/offline logging.

### 16.4 Status and authorization boundary

**Status:** REQUIREMENT AND OWNER POLICY APPROVED; implementation not started. A3 implementation remains gated by A2 exit requirements and separate stage transition. Enrollment, production tenant activation, device modification, root/system access, firmware changes, merge, release, and deployment are not authorized by OD-06.
