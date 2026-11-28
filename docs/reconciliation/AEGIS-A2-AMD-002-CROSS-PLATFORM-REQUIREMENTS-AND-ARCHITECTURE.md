# AEGIS ONE — A2 Cross-Platform Product Requirements and Architecture Amendment

**Amendment ID:** AEGIS-A2-AMD-002  
**Version:** 1.0  
**Date:** 2026-11-26  
**Status:** OWNER-DIRECTED REVISION — A2 OPEN / RECONCILIATION IN PROGRESS  
**Controlling history:** AEGIS-A1-BASE-001 v1.0; AEGIS-A2-CLOSE-001; AEGIS-A1-AMD-001  
**Architecture designation:** PMSS v1.2, supplemented by this cross-platform architecture amendment  
**Repository:** `akefadaka-git/aegis_one`

## 1. Executive decision

AEGIS ONE is redefined as **one cross-platform device-security product with Personal and Enterprise modes**.

- Personal device security is the primary product focus.
- Enterprise endpoint/device security is the secondary focus.
- Cross-platform architecture is a first-order design requirement from inception.
- Target device families are Android, iOS/iPadOS, Windows, macOS and Linux.
- IROKA remains the governed intelligence/security backbone; AEGIS retains local enforcement and platform-specific authority boundaries.

This amendment records product requirements and a proposed target architecture. It is not implementation evidence, platform certification, a claim of feature parity, A2 closure, or authorization to implement.

## 2. Product requirements

### 2.1 Product identity and modes

There is one AEGIS ONE product identity, account/device model and product family. It may have native clients and platform-specific packages, but those clients are not separate Personal and Enterprise products.

| Mode | Intended user | Authority and boundary |
|---|---|---|
| Personal | Individual device owner | User-controlled local protection, privacy, vault, network safety and recovery |
| Family | Family group / guardian and member | A governed Personal sub-context with transparent, age-appropriate and least-privilege controls; never an Enterprise tenant |
| Enterprise | Organization and authorized IT/security administrators | Explicitly enrolled or otherwise platform-authorized corporate/BYOD management, tenant-scoped policies and enterprise telemetry |

Personal and Enterprise are the two top-level product modes. Family is a distinct relationship/policy context within Personal, not a third enterprise-like administrator mode.

A user may have Personal and Enterprise contexts under the same product identity, but data, keys, roles, policies, audit visibility and administrator authority must remain separated. A device must not silently switch authority modes. A Personal vault must not become enterprise-readable when the device is enrolled or a work context is added.

### 2.2 Personal security — primary requirements

**PERS-01 — Device security posture**
- Assess OS version, patch state, screen-lock configuration, encryption/security settings and supported integrity signals where the platform exposes them.
- Show evidence provenance and freshness: VERIFIED, OWNER-REPORTED, UNKNOWN, UNSUPPORTED or DEGRADED as appropriate.
- A posture score must disclose its evidence coverage and must not convert missing platform signals into a claim of safety.

**PERS-02 — App and privacy safety**
- Provide app inventory and permission/privacy review only to the extent permitted by each OS.
- Identify known risky configuration, suspicious links/domains and supported threat indicators.
- Do not promise unrestricted scanning of other applications or private app data where platform sandboxing prohibits it.
- No covert surveillance, hidden recording or collection unrelated to a disclosed security purpose.

**PERS-03 — Personal data protection**
- Provide an encrypted personal vault for supported files/secrets.
- Retain user-controlled high-entropy recovery and the approved no-provider-recovery policy.
- Use per-installation/device-bound cryptographic identity and protected key storage where supported.
- Keep personal vault keys and data cryptographically separate from Enterprise data and keys.

**PERS-04 — Network and web protection**
- Offer phishing/malicious-domain protection and network-risk warnings through supported browser, DNS, VPN or network APIs.
- VPN use requires the platform's user consent and any required entitlements/management authorization.
- Network lockdown is never silently enabled; explain loss-of-connectivity consequences and provide a safe recovery path.
- Do not claim universal packet inspection or device-wide network interception on platforms that do not permit it.

**PERS-05 — Lost device and recovery**
- Provide authenticated lost-device reporting, device-session revocation and recovery guidance.
- Location, remote lock, erase and similar actions are conditional on OS APIs, user consent, enrollment and jurisdiction.
- Remote wipe, key destruction and other P4 actions remain disabled by default in v1.
- Reinstall/replacement creates a new installation identity; revoked credentials are not resurrected.

**PERS-06 — Family protection**
- Preserve OD-03: transparent, age-appropriate, least-privilege guardian controls.
- Guardian authority does not include unrestricted access to vaults, passwords, recovery secrets, personal files or communications.
- Family roles never inherit Enterprise administrator privileges.
- Real-user activation requires applicable legal/privacy review.

### 2.3 Enterprise security — secondary requirements

**ENT-01 — Enrollment and management**
- Support a platform-specific enterprise enrollment adapter for each operating system.
- Management authority exists only after a platform-valid enrollment or documented authorization flow.
- Distinguish fully managed corporate-owned devices, work/profile/container contexts and BYOD management.
- Never describe an ordinary app installation as Device Owner, MDM-supervised or otherwise managed.

**ENT-02 — Policy and compliance**
- Define tenant-scoped device inventory, security posture, policy assignment and compliance evaluation.
- Policies are capability-aware: unsupported controls are reported as unsupported, not enforced.
- Administrators can manage only assigned tenants/devices and authorized capabilities.

**ENT-03 — Enterprise workspace and data**
- Separate enterprise app/work data, policy and keys from personal data.
- No implicit access to Personal vault contents.
- Enterprise vault escrow remains disabled in v1 under OD-02.
- Support access is time-limited, purpose-bound, least-privilege and auditable.

**ENT-04 — Threat and incident management**
- Provide enterprise security events, alert triage, incident timelines and recommended response.
- IROKA may analyze and propose; it cannot self-authorize high-impact actions.
- P3 actions require the approved human authorization, signed/time-limited scope and local policy check.
- P4 remains disabled by default and separately gated.

**ENT-05 — Administration and accountability**
- Use tenant-scoped roles, explicit delegations, audit trails and revocation.
- Enterprise administrators cannot cross tenant boundaries or expand their own permissions.
- Legal/privacy review and named operational ownership are required before real tenant activation.

## 3. Cross-platform capability and assurance model

The same feature name does not imply the same operating-system capability. Every platform adapter must publish a machine-readable capability manifest containing:

- platform and OS/API version;
- hardware/feature prerequisites;
- permissions, entitlements and enrollment state;
- capability status and evidence source;
- supported actions and explicit exclusions;
- policy version, evidence timestamp and freshness;
- recovery/failure behavior.

Allowed capability states are: **VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED, REQUIRES_PERMISSION, REQUIRES_ENROLLMENT**. A user-facing dashboard must not display a capability as active/enforced unless current evidence supports that state.

### 3.1 Platform capability matrix

| Platform | Personal-mode design | Enterprise-mode design | Important boundary |
|---|---|---|---|
| Android phones/tablets | Native Kotlin client; Android public APIs; app/vault protection; user-mediated VPN; supported device-integrity signals | Android Enterprise managed configurations, work profile and fully managed Device Owner where correctly provisioned; Knox only if a concrete requirement and support are verified | Ordinary app has no Device Owner authority; Galaxy A16 SM-A165F remains the Android reference device, not a universal target |
| iOS/iPadOS | Native Apple client; app sandbox; secure local storage; supported privacy/network features; phishing/web protection | Apple MDM and supported managed app/configuration controls; supervised/managed state where valid | No unrestricted device-wide antivirus scanning or arbitrary access to other apps; Network Extension capabilities depend on entitlement and platform rules |
| Windows | Native client/agent; supported Windows security interfaces; posture, firewall and Defender integration where available; local protection | Windows MDM/management APIs and authorized endpoint agent; tenant policy and compliance | Avoid kernel drivers and invasive system hooks in initial scope; use documented APIs and explicit service privilege |
| macOS | Native client/agent; app sandbox and supported endpoint/network security APIs | Apple MDM and eligible Endpoint Security / Network Extension capabilities | Entitlements and MDM authorization are prerequisites; do not claim access to protected system events without them |
| Linux | Supported distribution-specific client/agent; local posture, service and firewall checks | Explicitly installed and authorized management service/daemon; policy integration and audit | Define supported distributions and privilege boundaries; no assumed root access or universal distribution compatibility |

The matrix is a target design, not proof that every capability has been implemented or that every API/entitlement is approved or available. Detailed OS/API versions and vendor-specific support must be verified before feature acceptance.

## 4. Target architecture

### 4.1 Architectural layers

1. **Product experience layer:** one AEGIS ONE product identity with Personal and Enterprise mode selection, accessible platform-appropriate UX, device inventory and clear evidence status.
2. **Shared domain and contract layer:** platform-neutral models for accounts, installation identity, device records, roles, modes, security posture, capability manifests, policy, approvals, audit events, command envelopes and error states.
3. **Deterministic security/policy layer:** deny-by-default authorization, scope/tenant checks, consent, freshness, revocation, risk-class and capability checks. AI is not a policy root.
4. **Platform adapters:** separate native adapters for Android, iOS/iPadOS, Windows, macOS and Linux. Each adapter maps the common contract to actual platform APIs and reports limitations.
5. **Local protection services:** protected storage, cryptographic operations, local audit, offline policy cache, network controls and platform-specific event collection. Local protection must remain useful without cloud/IROKA connectivity.
6. **Backend and identity services:** account authentication, device registration, key/attestation verification, policy distribution, revocation, event receipt and enterprise tenancy. Service owners and production trust roots remain unassigned until separately established.
7. **IROKA governed intelligence:** threat analysis, correlation, risk assessment and signed/scoped authorization artifacts. AEGIS independently validates all artifacts and enforces local policy and platform permissions.

### 4.2 Shared-core technology decision

The architecture must share security semantics and conformance contracts, not force identical code onto incompatible operating systems.

- Retain the existing Android client and Kotlin implementation as the Android native path.
- Keep domain logic independent of Android framework classes wherever practical.
- Evaluate a portable shared core (for example, Kotlin Multiplatform or a narrowly scoped Rust core) against security reviewability, native API integration, testability, build reproducibility, maintainability and platform support.
- Use native platform adapters for privileged OS capabilities and user-consent flows.
- Do not select a cross-platform UI framework or rewrite existing Android work before a comparative ADR and prototype establish a net benefit.
- Use versioned, platform-neutral contracts and a shared conformance test suite even if implementation languages differ.

**Technology selection status: OPEN — architecture decision record and feasibility evidence required.**

### 4.3 Trust boundaries

- OS/hardware security controls are authoritative within documented platform behavior.
- AEGIS local policy and enforcement cannot grant itself OS privileges.
- Platform management adapters receive only authority conferred by valid enrollment/entitlements.
- Backend/IROKA services can issue signed, scoped artifacts but cannot bypass local validation.
- AI models and agents can analyze and propose; they cannot sign approvals or execute P2–P4 actions.
- Personal, Family and Enterprise data/keys remain isolated.
- Transport delivery alone never establishes authority.

## 5. Preserved Owner decisions OD-01 through OD-06

This amendment preserves all prior approved Owner decisions. Cross-platform expansion does not dilute them.

| Decision | Preserved rule | Cross-platform application |
|---|---|---|
| OD-01 — Vault recovery | User-controlled high-entropy recovery secret; no provider/operator recovery; permanent-loss risk disclosed | Same recovery authority model on every client; platform-specific secure key storage does not change who controls recovery |
| OD-02 — Enterprise escrow | Enterprise escrow disabled in v1; Personal and Enterprise vault/key domains separated | No MDM/administrator access to Personal vault; any later escrow needs a separate versioned Owner approval |
| OD-03 — Family protection | Transparent, age-appropriate, least-privilege guardian controls; no unrestricted private-content access | Family remains separate from Enterprise on every platform; supported controls vary by OS |
| OD-04 — P3/P4 actions | P3 requires scoped human authority, signed/time-limited approval and local policy check; P4 disabled by default | Same risk policy on every platform; native APIs and enrollment must also permit the action |
| OD-05 — Offline authority | Signed/scoped/unexpired policy; P0/P1 max 7 days; P2 max 24 hours; P3 remote actions denied offline absent separately approved protocol; P4 disabled | Common policy semantics, platform-protected expiry/time confidence and local fail-closed enforcement |
| OD-06 — Enterprise management | Explicit opt-in and platform-valid managed-device authority; Android Device Owner only after valid Android Enterprise provisioning | Each OS uses its own supported enrollment/MDM/management model; no cross-platform equivalence is assumed |

Other preserved controls:
- Per-installation cryptographic identity; no IMEI/serial/MAC-based identity.
- Revocation is monotonic; revoked identities are never reactivated.
- Family guardian access excludes personal vaults and private content.
- Enterprise tenant and role scope is explicit; no cross-tenant authority.
- IROKA commands must be signed, scoped, fresh, replay-protected, locally authorized and auditable.
- Offline authority expires; local audit is tamper-evident with stated limitations.
- Destructive actions require separate authorization and recovery analysis.
- No root, bootloader modification, OEM firmware integration, custom OS, silent surveillance or unrestricted remote control.

## 6. Threat model additions and required controls

| Threat | Required control | Acceptance evidence |
|---|---|---|
| Cross-platform feature overclaim | Capability manifest and evidence state per OS/version | Unsupported/unknown never appears as enforced |
| Personal/Enterprise mode confusion | Explicit mode, tenant, role and data-domain binding | Cross-mode and cross-tenant negative tests |
| MDM or enrollment privilege escalation | Validate platform enrollment state and authority | Unmanaged device cannot invoke managed-only action |
| Personal data exposure to employer | Separate keys, stores, APIs and audit visibility | Enterprise role cannot decrypt/read Personal vault |
| Inconsistent policy across clients | Versioned shared contracts and conformance suite | Same policy vectors yield equivalent decisions |
| Platform API/entitlement unavailable | Capability gating and safe degraded mode | No unsupported side effect; clear limitation |
| Offline replay or time rollback | OD-05 expiry, replay ledger and time-confidence handling | Expired/replayed commands denied |
| AI/remote command abuse | Signed envelope, strict allowlist, local authorization and P3/P4 gates | Invalid issuer/scope/approval never causes side effects |
| Agent/service compromise | Least privilege, signed updates, protected service boundary and audit | Privilege and tamper tests per OS |
| Privacy-invasive family/enterprise monitoring | Purpose limitation, transparency, consent/legal review | Guardian/admin cannot access excluded content |

The existing FT-01–FT-12 design traceability is retained for the original first tranche. It is not evidence of cross-platform coverage. The above cross-platform threat set must be mapped to platform-specific negative and acceptance tests before the affected feature is implemented.

## 7. Delivery and validation strategy

Cross-platform architecture is established now; implementation and release can be staged.

**Architecture-first work:**
1. Freeze platform-neutral product/domain contracts.
2. Complete a native capability and API evidence matrix for each target OS.
3. Complete the shared-core technology ADR and small non-production feasibility prototypes.
4. Define Personal/Enterprise mode separation, enrollment and data/key boundaries.
5. Establish a cross-platform security conformance suite and platform-specific test matrix.
6. Reconcile the A3 plan into common-core, Android, Apple, Windows and Linux workstreams.

**Implementation sequencing:** After revised A2 closure and separate A3 authorization, implementation may be sequenced by risk and evidence. Android can remain the first device-validation/reference path, but Android-only architecture or Android-only security assumptions must not constrain the product. The exact client release order is not approved by this document.

## 8. A2 acceptance and open decisions

A2 remains OPEN until the following are completed or explicitly deferred by the Owner with scope and impact:

- Confirm detailed product requirements and Personal/Enterprise mode boundaries.
- Complete OS-specific capability/API/entitlement/enrollment evidence for Android, iOS/iPadOS, Windows, macOS and Linux.
- Complete shared-core technology ADR and architecture feasibility evidence.
- Define account/device identity and tenant/family relationship model across platforms.
- Define enterprise BYOD, corporate-owned, work-profile/container and offboarding behavior per platform.
- Define cross-platform key portability, device replacement and recovery without exporting non-exportable device private keys.
- Define cross-platform policy signing, secure time, revocation and offline enforcement.
- Define privacy, data residency, retention and legal review requirements for each deployment jurisdiction.
- Complete cross-platform threat/control/test traceability.
- Reconcile README, architecture, contracts, A3 plan and status registers without rewriting historical approvals.

Physical-device and vendor-specific evidence remains necessary before accepting device-specific enforcement. Backend/IROKA operational owners and production trust roots remain necessary before live integration. Independent implementation security assessment remains mandatory at A4/pre-release; it is not replaced by this A2 document.

## 9. Stage and authorization status

| Stage | Status |
|---|---|
| A1 v1.0 | VERIFIED-CLOSED / FROZEN as historical baseline |
| A1-AMD-001 | Owner product-direction decision recorded; controlled amendment subject to A2 reconciliation |
| A2 original closure | Preserved historical record dated 2026-11-25 |
| A2 cross-platform amendment | OPEN — reconciliation in progress |
| Existing A3 plan | Requires re-baselining against cross-platform requirements before it can govern expanded scope |
| New cross-platform A3 work | NOT AUTHORIZED by this amendment |
| A4 | NOT STARTED |
| A5 | NOT AUTHORIZED |

This document does not authorize source-code changes, device installation, enrollment, factory reset, production key generation, live service provisioning, merge, release or deployment. Existing repository work is preserved; this is a documentation-only A2 revision.

**Production readiness: NOT ESTABLISHED.**

**Immediate next gate:** Complete the A2 open items and present the revised requirements, platform evidence, technology ADR and cross-platform test traceability for Owner acceptance. Only then should the A3 plan be re-baselined and separately authorized.

---
**End of A2 cross-platform product and architecture amendment.**


## 10. Controlled VPN architecture amendment — 2026-11-28

The Owner's VPN decisions are recorded in [AEGIS-A2-AMD-003 — VPN Requirements and Architecture](AEGIS-A2-AMD-003-VPN-REQUIREMENTS-AND-ARCHITECTURE.md): one integrated AEGIS ONE product with both Personal and Enterprise VPN; hybrid AEGIS-operated, approved partner-operated and customer-operated gateway classes; WireGuard as the initial protocol using maintained implementations.

AEGIS-A2-AMD-003 supplements this amendment. It does not rewrite the historical A1 baseline, revoke OD-01 through OD-06, authorize VPN implementation, provision gateways, authorize paid infrastructure, or authorize production service activation. VPN feature-specific A2 design and threat/test traceability remain open.

