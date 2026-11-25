# AEGIS ONE — A2 Security Enhancement Register

**ID:** AEGIS-A2-SEC-ENH-001  
**Version:** 1.0  
**Status:** DESIGN RECOMMENDATIONS — NOT IMPLEMENTED  
**Stage:** A2 read-only reconciliation  
**Applies to:** Consumer (Mode C), Family (Mode F), and Fully Managed Enterprise (Mode E), with strict separation.

## 1. Purpose and decision rule

This register identifies security controls that should be included in the AEGIS ONE design before implementation. It prioritizes prevention, containment, recoverability, verifiability, and safe failure. Controls are recommendations unless explicitly marked as binding from approved Owner decisions OD-01 through OD-06.

No item in this document authorizes code, device configuration, enrollment, production keys, service provisioning, merge, release, or deployment. Features must use documented Android APIs and must report unsupported capabilities honestly.

Priority:
- **P0 — A3 entry/design blocker:** must be specified and testable before the affected feature is implemented.
- **P1 — v1 security baseline:** include in the first implementation where the feature is in scope.
- **P2 — staged enhancement:** valuable, but can follow the secure v1 baseline.
- **Conditional:** only in the relevant management mode and only where the OS/device/service supports it.

## 2. Highest-priority security enhancements

| Priority | Enhancement | Required design/control | Acceptance evidence |
|---|---|---|---|
| P0 | Threat-driven security architecture | Maintain assets, trust boundaries, abuse cases, attack trees, mitigations, residual risks, and threat-to-test links. Reassess after material feature/API changes. | Reviewed threat model; every high-risk threat maps to a control and negative test. |
| P0 | Typed remote-command protocol | Canonical serialization; strict schema/version; issuer key ID; audience; device/account/tenant binding; action/resource scope; issued/not-before/expiry; nonce; idempotency; policy version; approval reference; signature. Reject unknown fields/actions and ambiguous encodings. | Golden test vectors; malformed/canonicalization/signature/audience/expiry/replay/duplicate tests. |
| P0 | Local authorization gate | Verify trust chain, signature, target, capability, scope, freshness, revocation snapshot, policy, mode, user-presence requirement, and current Android authority locally before any side effect. Remote signature is not authorization. | Deny-by-default matrix; no side effect on any failed check; auditable decision reason. |
| P0 | Key lifecycle and trust-root governance | Separate keys by purpose and environment; document generation, custody, rotation, revocation, compromise response, backup, recovery, and trust-root update. Pin trust roots safely; support overlapping rotation windows and emergency revocation without accepting unsigned updates. | Key lifecycle state machine; rotation/revocation tests; named accountable custodians and recovery procedure. |
| P0 | Vault cryptographic boundary | Use reviewed, standard cryptographic libraries; envelope encryption; authenticated encryption; independent device and recovery wrapping paths; unique nonce discipline; domain separation; versioned key metadata; no plaintext key persistence. Exact algorithms/parameters require independent review. | Independent crypto design review; tamper, wrong-key, rollback, key-invalidation, recovery and rotation tests. |
| P0 | Personal/enterprise/family isolation | Separate data namespaces, keys, policies, identity scopes, audit access and lifecycle. No enterprise or guardian role silently grants personal vault access. | Cross-mode, cross-tenant and cross-role denial tests; data-flow review. |
| P0 | Android capability truthfulness | Runtime capability manifest for API level, management role, biometric class, hardware-backed Keystore, StrongBox, attestation, VPN and Direct Boot. Distinguish VERIFIED, OWNER-REPORTED, UNSUPPORTED and UNKNOWN. | Device/API matrix; unsupported-feature negative tests; no UI claim of enforcement without evidence. |
| P0 | Offline authorization | Enforce OD-05 TTLs: P0/P1 max 7 days; P2 max 24 hours; P3 remote commands denied offline absent separately approved protocol; P4 disabled. Handle clock rollback, uncertain time, stale revocation, queued actions and reconnect. | Expiry/clock rollback/offline/reconnect/revocation tests; no privilege extension under time uncertainty. |
| P0 | P3/P4 safety | P3 requires authorized human approval, signed scope/time limit, local policy verification and audit. P4 remains disabled in v1. AI cannot approve P3/P4. | Approval provenance tests; replay/expiry/scope tests; explicit P4 deny tests. |
| P0 | Independent security review | Independent qualified review of command signing, key lifecycle, vault cryptography, local authorization, Android assumptions, family/enterprise separation, threat model and negative tests. | Report identifies exact commit, reviewer qualifications, method, findings/severity, fixes and retest. AI/self-review does not satisfy this gate. |

## 3. Additional v1 security baseline

### 3.1 Identity, sessions and device trust

- Use a random installation identity distinct from hardware identifiers, account identity and user identity.
- Prefer hardware-backed Android Keystore keys when available; detect and record security level rather than assuming StrongBox.
- Treat attestation as evidence with freshness, challenge binding, certificate-chain validation, revocation and verifier policy. Never trust a client-reported Boolean.
- Do not use IMEI, serial number, advertising ID or phone number as a cryptographic identity.
- Rotate/revoke device credentials after loss, reinstall, account recovery, key compromise or ownership transition.
- Bind session tokens to narrowly scoped server-side sessions; short-lived access credentials, refresh-token rotation, reuse detection, revocation and secure logout.
- Require step-up authentication for recovery, key changes, exporting sensitive data, administrative policy changes and high-risk account operations.
- Apply rate limits, progressive delays and abuse detection to authentication and recovery without creating account-enumeration leaks.

### 3.2 Vault and protected data

- Keep vault encryption independent from server account authentication.
- Separate personal vault, enterprise workspace and family policy metadata cryptographically and logically.
- Encrypt metadata where practical; minimize filenames, thumbnails, search indexes and previews exposed outside the vault boundary.
- Use secure temporary-file handling; avoid plaintext cache, logs, crash reports, clipboard and external share targets.
- Define safe import/export, file-type validation, size limits, archive traversal protection, malware scanning boundaries and atomic writes.
- Use authenticated backup formats with versioning, integrity verification and restore-before-replace safeguards.
- Make deletion semantics explicit: logical deletion, backup retention, key destruction and remote deletion are different operations.
- Require explicit user confirmation and re-authentication for sensitive export and recovery.
- Detect key invalidation and provide the OD-01 recovery path; never silently downgrade cryptographic protection.

### 3.3 Android application hardening

- Minimize exported components; explicitly protect exported activities, services, receivers and providers with permissions and caller validation.
- Use immutable PendingIntents unless mutability is required and justified; validate deep links and intent parameters.
- Protect against overlay/tapjacking, task hijacking, insecure WebView use, unsafe deserialization, path traversal and content-provider leakage.
- Do not expose secrets in notifications, recent-app previews, screenshots, clipboard or accessibility descriptions; use platform protections where supported and explain limitations.
- Enforce network security configuration: HTTPS, modern TLS, cleartext disabled by default, strict hostname validation. Certificate pinning, if used, must have a safe rotation/recovery design and must not become a self-inflicted outage.
- Keep debug flags, test endpoints, verbose security logs and test credentials out of production builds.
- Do not use dynamic code loading, arbitrary shell execution, hidden APIs, reflection-based privilege escalation or remotely supplied executable content.
- Use Play Integrity or hardware attestation only as a risk signal with documented availability/limitations; never treat it as a complete proof that a device is uncompromised.

### 3.4 Direct Boot and early-start behavior

- Start only the minimum Direct-Boot-aware bootstrap at the earliest documented Android lifecycle point.
- Use device-protected storage only for non-secret bootstrap state, signed policy metadata within the OD-05 limits, and integrity-protected event queue metadata.
- Never place plaintext vault content, recovery phrase, raw vault key, credential-bound key material or long-lived bearer secrets in device-protected storage.
- Make startup idempotent and resilient to process death, force-stop, OS update, reboot loops, storage exhaustion and unavailable network.
- Display distinct states: limited pre-first-unlock protection, full post-unlock protection, degraded/unsupported, and error.
- Do not claim protection is active until the relevant OS control is actually applied and verified.
- Android initializes its boot chain before third-party apps. AEGIS cannot execute before Android or become an unrestricted system process.

### 3.5 Device Owner, VPN and family boundaries

- Mode C must remain useful without Device Owner; no anti-uninstall or device-wide policy promises.
- Mode E may use documented DevicePolicyManager capabilities only after eligible fully managed provisioning; work profile is not equivalent to Device Owner.
- Mode F must have guardian/member roles, age-appropriate notice, least privilege, child privacy, and lawful consent. No covert recording or default private-vault access.
- For always-on VPN/lockdown, include DNS behavior, captive portals, network transitions, tunnel failure, battery optimization, emergency recovery and clear user/admin notice.
- Never silently convert a VPN failure into unrestricted traffic if policy requires lockdown; define a deliberate fail-closed or recovery state per mode and test it.
- Emergency contact and location features must be opt-in, purpose-limited, visible, minimized and revocable; no background collection by default.

### 3.6 Audit, privacy and incident response

- Use structured security events with actor, device, mode, action, target, policy version, approval reference, timestamp source, sequence, result and reason.
- Protect local audit records against casual tampering; use hash chaining/signatures where appropriate, while documenting that a compromised OS can still alter local evidence.
- Distinguish device time from trusted server time; preserve monotonic sequence and flag gaps, clock rollback and uncertain timestamps.
- Minimize sensitive event payloads; never log secrets, recovery material, access tokens, plaintext vault data or unnecessary personal content.
- Define retention, access, export, deletion and legal-hold policies before backend telemetry is enabled.
- Establish key-compromise, lost-device, suspected malware, account takeover, data exposure and service outage runbooks.
- Provide a user-visible security status and incident history without exposing sensitive internal data to unauthorized roles.

## 4. Supply-chain and release controls

These are mandatory before any production release, even though dependency scanning is currently NOT RUN because no app dependency graph exists.

1. Generate an SBOM for every release candidate; record direct and transitive dependencies, versions, licenses and provenance.
2. Pin dependency versions; review updates and avoid abandoned/unmaintained libraries for security-critical functions.
3. Run static analysis, secret scanning, dependency vulnerability scanning, license checks and Android manifest/exported-component checks.
4. Verify build provenance and artifact hashes; protect signing keys outside source control and separate debug/release keys.
5. Use reproducible or independently verifiable builds where feasible; document build environment and toolchain.
6. Require protected branch review, signed release tags, least-privilege CI tokens and no secrets in public workflow logs.
7. Do not make paid CI or services a prerequisite for A2 documentation; use available public/free workflows where feasible.
8. No production signing key generation or release signing is authorized in A2.

## 5. Security test catalogue — required negative cases

| Domain | Minimum negative tests |
|---|---|
| Command protocol | Wrong issuer, unknown key, revoked key, altered payload, non-canonical encoding, wrong audience/device/tenant, expired/not-yet-valid, duplicate nonce, replay, unknown action, oversized parameters, missing approval. |
| Authorization | AI-only approval, wrong role, stale consent, cross-tenant request, wrong mode, user-presence absent, revoked admin, policy rollback, unsupported capability. |
| Vault | Ciphertext bit flip, metadata substitution, nonce reuse detection, wrong recovery phrase, phrase rotation, lost device, Keystore invalidation, backup rollback, malicious archive/path traversal, plaintext temp-file residue. |
| Offline | TTL exceeded, wall-clock rollback, no trusted time, stale revocation, duplicate queued event, reconnect storm, partial sync, policy downgrade, emergency mode attempting P4. |
| Android | Exported component abuse, malicious intent/deep link, overlay/tapjacking, notification leakage, screenshot/task preview, force-stop, reboot, Direct Boot, process death, storage full, OS upgrade. |
| VPN | Tunnel unavailable, DNS leak, captive portal, network switch, battery restriction, lockdown recovery, user cancellation, policy revoked. |
| Family/enterprise | Guardian escalation, child account transition, employee personal-vault access, work-profile/Device Owner confusion, tenant crossover, admin compromise, de-enrollment/reassignment. |
| Supply chain | Malicious dependency update, compromised build input, secret in repository/log, unsigned artifact, altered SBOM, debug configuration in release. |

Tests must assert both the expected denial and the absence of side effects. A test plan is not a test result; all unexecuted tests remain **NOT RUN**.

## 6. Deferred or separately authorized enhancements

- Hardware-backed remote attestation enforcement: conditional on actual device support, verifier ownership and privacy review.
- Enterprise escrow: disabled in v1 under OD-02; any future proposal requires separate Owner approval and tenant-held keys/dual control.
- Remote wipe, irreversible key destruction and permanent account closure: P4 disabled under OD-04.
- Root, bootloader unlock, custom OS, OEM signing, hidden system privileges and security bypass: out of scope.
- Covert monitoring, hidden recording, indiscriminate location tracking or private-communications surveillance: prohibited.
- External paid threat-intelligence, analytics, MDM or cloud dependencies: require cost, data-protection and ownership assessment; not assumed.
- AI-driven autonomous response: AI may classify/recommend; protected actions remain governed by local policy and human approval requirements.

## 7. A3 implementation sequencing recommendation

1. Security foundations: identity/session lifecycle, typed command parser, local authorization gate, policy/version engine, secure audit schema.
2. Vault foundation: key hierarchy, encrypted object store, recovery and rotation, safe import/export, lock/session behavior.
3. Android lifecycle: capability manifest, Direct Boot bootstrap, biometric/Keystore adapters, consumer-mode baseline.
4. Family controls: transparent guardian/member roles, consent, age-appropriate policy, no private-vault access.
5. Enterprise controls: managed configuration and DevicePolicyManager adapter, only in eligible test provisioning.
6. VPN and advanced threat defenses: staged behind capability checks and recovery tests.
7. Release assurance: SBOM, static analysis, dependency/license review, independent security review and signed artifact verification.

This sequence is a recommendation, not an A3 stage transition or authorization to implement.

## 8. A2 status and acceptance

- Security enhancement register: **DOCUMENTED**.
- Owner decisions OD-01 through OD-06: **APPROVED** in the Owner Decision Register.
- Independent security review: **NOT PERFORMED**.
- Physical handset security capability evidence: **PARTIAL / OWNER-REPORTED**; Knox, StrongBox, KeyMint security level, verified boot and management state remain **UNKNOWN**.
- Backend/IROKA accountable service owners: **UNRESOLVED**.
- Legal review: **NOT PERFORMED**.
- Application implementation and security tests: **NOT RUN**.
- A2: **OPEN**; A3: **BLOCKED** pending A2 exit and explicit stage transition; A5: **NOT AUTHORIZED**.

**No production-readiness claim is made.**


---

## 9. Controlled assurance timing revision — AEGIS-A2-ADD-001 (2026-11-25)

**Controlling amendment:** [AEGIS-A2-ADD-001 — Risk-Based Security Assurance and Exit Revision](../reconciliation/AEGIS-A2-ADD-001-RISK-BASED-ASSURANCE-AND-EXIT-REVISION.md).

This section supersedes the universal A3-entry interpretation of the “Independent security review” P0 row in Section 2 and the earlier A2 status/acceptance language in Section 8.

- The independent implementation security assessment is a mandatory **A4 / frozen pre-release** control before A5.
- A targeted independent architecture/cryptography/protocol consultation may occur earlier when a specific high-risk design question warrants it; this is optional and scoped.
- Security controls are implemented and verified incrementally in A3: code review, static analysis, dependency review, SBOM when dependencies exist, unit/negative tests, and feature-specific device tests.
- Exact cryptographic construction, protocol canonicalization, key lifecycle and recovery implementation decisions must be resolved before their affected components are implemented; this is a component-level design gate, not a reason to block unrelated foundations.
- Hardware-backed security and Android management capabilities remain UNKNOWN until established on supported devices. No unsupported capability may be claimed as enforced.
- Backend/IROKA operational ownership is required before live integration; synthetic/local interfaces may proceed earlier.
- Legal/privacy review is required before real family/enterprise activation; synthetic-only scaffolding remains bounded by OD-03/OD-06.
- The reviewer brief remains a handoff specification; review status is **NOT PERFORMED**, not a finding of security weakness or a completed assurance result.

A2 focused exit result: **PARTIALLY EXIT-READY — NOT CLOSED**. Component-level A3 eligibility and remaining conditions are controlled by AEGIS-A2-ADD-001.


## 12. Owner-approved A2 closure and assurance timing — 2026-11-25

The Owner accepted AEGIS-A1-BASE-001 v1.0 as the frozen controlling baseline and approved A2 closure under AEGIS-A2-ADD-001. The mandatory independent implementation assessment remains an A4/pre-release gate, not an A2 prerequisite. Dependency/SBOM assurance begins when the application dependency graph is introduced and is repeated for release candidates. Device-specific evidence, live backend/IROKA ownership and legal/privacy review remain gates for the affected feature, integration or real-user activation.

A2: **VERIFIED-CLOSED** (documentation/governance only). A3: **NOT AUTHORIZED / NOT STARTED**. All implementation tests remain NOT RUN; no production readiness or deployment is claimed. OD-01 through OD-06 remain binding.
