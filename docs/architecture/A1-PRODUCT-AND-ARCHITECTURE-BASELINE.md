# AEGIS ONE — A1 Product and Architecture Baseline

**Document ID:** AEGIS-A1-BASE-001  
**Version:** 1.0  
**Status:** OWNER-AUTHORIZED BASELINE — FROZEN FOR A2 RECONCILIATION  
**Product:** AEGIS ONE — Powered by IROKA  
**Repository:** `akefadaka-git/aegis_one`  
**Architecture designation:** PMSS Architecture v1.2  
**Platform:** Native Android application (Kotlin)  
**Baseline date:** 2026-11-25

## 1. Purpose and authority

This document establishes the initial product and architecture baseline for AEGIS ONE. It records the project identity, intended outcomes, scope boundaries, architecture principles, constraints, and stage gates that subsequent work must follow.

This baseline is grounded in the Owner's prior project decisions and the explicit instruction to establish A1 and conduct A2 before implementation. It does not assert that the proposed capabilities have been implemented, tested, or verified on a physical device.

The PMSS v1.2 designation is retained as the Owner's selected architecture baseline. This document does not invent an expansion for the acronym PMSS.

## 2. Product identity

AEGIS ONE is a dedicated Android security and protection product. IROKA is its intelligence and governed-security backbone; it is not the Android product itself.

**Product statement:** AEGIS ONE provides layered Android device, application, data, privacy, and management protections, using local Android security controls and governed IROKA intelligence where available.

**Product tagline:** Intelligent Android Security. Powered by IROKA.

AEGIS ONE is a distinct project. It must not be merged conceptually, architecturally, or organizationally with ARNA, MASES, or the broader IROKA cognitive operating system.

## 3. Target platform profile

| Attribute | Baseline |
|---|---|
| Initial reference device | Samsung Galaxy A16, model SM-A165F |
| App implementation | Kotlin / native Android |
| Platform integration | Public Android SDK and supported Android Enterprise APIs |
| OEM integration | Out of scope |
| Custom operating system | Out of scope |
| Reference architecture | PMSS v1.2 |
| Device OS/API state | To be read from the actual device before compatibility claims |
| Knox state | To be verified against device firmware and Knox diagnostics |

The target device and historical platform assumptions are not evidence of the device's current installed firmware, patch level, boot state, Knox configuration, or hardware-backed security features.

## 4. Product outcomes and capability scope

The following are product objectives, not claims of current availability or guaranteed support on every device.

### 4.1 Local protection

- Encrypted file and folder vault.
- App-level access controls and protected workspace.
- Biometric and device-credential authentication through supported Android system APIs.
- Device and application integrity signals, with clear confidence and limitations.
- Privacy and anti-tracking controls within Android's supported permission and platform model.
- Anti-theft assistance and recovery workflows that do not claim powers unavailable to an ordinary application.
- Tamper-evident security events and local audit records.
- Essential local protection while disconnected from IROKA or remote services.

### 4.2 Network protection

- VPN-based network protection.
- Optional always-on VPN and lockdown where Android role, ownership, API, and user/administrator authorization requirements are met.
- Explicit failure behavior, including the availability impact of lockdown.

### 4.3 Managed-device protection

- Device Owner / fully managed controls for eligible, explicitly provisioned enterprise-owned devices.
- Enterprise policy, protected-workspace, and remote-management capabilities under an approved management contract.
- No assumption that Device Owner powers are available in consumer or unmanaged mode.

### 4.4 Multi-device, family, and enterprise

- Multiple-device account model.
- Family-protection capabilities with explicit role, consent, privacy, and guardian authority boundaries.
- Enterprise management with tenant isolation and role-scoped administrator authority.
- Offline emergency protection and emergency-contact workflows.

Family protection and enterprise management are separate policy contexts. Family membership must not silently confer enterprise administrator powers, and enterprise administration must not silently expose personal data outside the approved management scope.

### 4.5 IROKA intelligence and governance

IROKA is intended to provide, through separately specified and authenticated contracts:

- Threat analysis and threat-intelligence assistance.
- Trust, identity, and device-identity verification.
- Policy orchestration and risk assessment.
- Incident correlation and recovery recommendations.
- Signed and authenticated remote-management command proposals.

IROKA models and agents are not authority roots. A model response, remote message, or agent plan is not an authorization token.

## 5. Architecture principles

1. **Local-first protection:** core protection remains useful during loss of connectivity.
2. **Least privilege:** use the narrowest Android permission, role, and management authority required.
3. **Platform truth:** never claim OS-level enforcement from ordinary app privileges.
4. **Explicit management modes:** distinguish unmanaged consumer mode from fully managed Device Owner mode.
5. **Governed intelligence:** IROKA may analyze and propose; deterministic policy and authorized principals decide.
6. **Fail-safe behavior:** protected operations fail closed when authorization is absent, expired, revoked, malformed, or indeterminate.
7. **Cryptographic separation:** encryption keys are protected by Android Keystore where supported; hardware-backed and StrongBox properties are detected and verified, not assumed.
8. **Privacy by design:** data minimization, transparent permissions, purpose limitation, and explicit family/enterprise boundaries.
9. **Evidence-based assurance:** security claims are tied to test evidence, device facts, exact commits, and documented limitations.
10. **Reversible change:** no destructive device action or remote command without a separate authorization and recovery path.

## 6. Explicit non-goals and constraints

The following are excluded from this baseline:

- OEM-level integration, firmware modification, custom ROM, or custom Android OS.
- Rooting, bootloader unlocking, or bypassing Android security controls.
- Factory reset, device enrollment, or Device Owner provisioning as part of this work.
- Installing the application on a physical device as part of this work.
- Deployment, publication, release, or production activation.
- Unrestricted remote control or silent surveillance.
- Claims that an ordinary application can prevent its own uninstall, override user consent, or enforce system-wide policies without the required Android management role.
- Paid infrastructure or workflows that require a deposit/top-up. Prefer free and reproducible tooling.

The current work is repository documentation and read-only platform/source reconciliation only.

## 7. Security and trust boundaries

| Boundary | Trusted responsibility | Must not be trusted to |
|---|---|---|
| Android OS / hardware | Enforce documented platform controls and protect supported Keystore operations | Prove every device is uncompromised without independent signals |
| AEGIS local app | Enforce its own app-level authentication, encryption, policy, and audit logic | Escalate itself to Device Owner or bypass user/system controls |
| Device Owner DPC (managed mode only) | Apply policies permitted to the enrolled device owner | Exercise authority outside the enterprise enrollment and policy scope |
| IROKA service / Kernel | Authenticate principals, evaluate policy, authorize scoped commands, preserve evidence | Grant itself unrestricted control or bypass local device controls |
| AI models / agents | Produce analysis and structured proposals | Self-authorize, change trust roots, or execute protected actions |
| Remote transport | Deliver authenticated, integrity-protected messages | Establish authority merely because a message arrived |
| User / administrator | Exercise role-appropriate consent and approvals | Override immutable platform constraints or another tenant's boundary |

## 8. Required management modes

A2 must specify at least two separately tested operating modes:

- **Unmanaged / consumer mode:** no Device Owner assumption. Use ordinary application capabilities, runtime permissions, user-mediated VPN consent, app-level controls, and supported Android APIs.
- **Fully managed enterprise mode:** Device Owner features are conditional on eligible device ownership and successful provisioning. This mode is not activated, enrolled, or tested on a device during the present work.

A future work-profile mode may be evaluated separately. It is not implicitly included in this baseline.

## 9. Lifecycle and stage gates

| Stage | Objective | Exit condition |
|---|---|---|
| A1 | Product, architecture, scope, threat model, and acceptance baseline | Baseline documented and Owner-authorized |
| A2 | Reconcile Android/device capabilities, historical IROKA evidence, contracts, risks, and testability | Evidence-backed reconciliation; all critical gaps either closed or explicitly retained as blockers; Owner accepts A2 |
| A3 | Implement approved Android modules and integration contracts | Separate explicit Owner authorization after A2 |
| A4 | Verify build, security, compatibility, resilience, and release evidence | Reproducible evidence on exact commit(s) |
| A5 | Controlled release/deployment | Separate explicit release authorization |

No A3 implementation is authorized by this document. A5 remains a separate approval gate.

## 10. A1 acceptance criteria

- Product identity and separation from IROKA/other projects are explicit.
- Target device and platform assumptions are labeled as assumptions until device evidence exists.
- Consumer and managed-device capabilities are distinguished.
- Scope, non-goals, and prohibited actions are explicit.
- Trust boundaries and local-offline requirements are defined.
- Stage gates and A3/A5 authorization requirements are explicit.
- A2 findings are recorded separately and do not silently amend this frozen A1 baseline.

## 11. Controlled amendments

A1 v1.0 is preserved as the initial baseline. Material changes must be recorded as a versioned amendment or addendum, with rationale, impact, evidence, and Owner approval. A2 reconciliation findings do not silently rewrite this document.

---
**End of A1 baseline.**
