# AEGIS ONE — A3 Bounded Implementation Plan

**ID:** AEGIS-A3-PLAN-001  
**Version:** 1.0  
**Date:** 2026-11-25  
**Status:** PLAN ONLY — A3 IMPLEMENTATION NOT AUTHORIZED  
**Controlling baseline:** AEGIS-A1-BASE-001 v1.0 / PMSS Architecture v1.2  
**A2 closure:** AEGIS-A2-CLOSE-001  
**Repository:** `akefadaka-git/aegis_one`  
**Planning branch:** `docs/a1-baseline-a2-reconciliation`

## 1. Purpose and authorization boundary

This document translates the Owner-approved A1/A2 baseline into a bounded, evidence-driven A3 implementation sequence. It is a plan, not implementation authorization. No application source, build configuration, dependencies, CI workflows, tests, handset state or deployment configuration are changed by this plan.

A3 begins only after a separate explicit Owner stage-transition authorization. Once authorized, work remains on the Owner-designated existing branch; do not create a new branch unless separately requested. Do not modify `main`, merge, release, install on the reference handset, enroll a device, generate production keys, provision live services or deploy under this plan.

## 2. Binding invariants

1. Preserve A1 v1.0 as frozen baseline; changes require controlled amendments.
2. Preserve OD-01 through OD-06 without dilution.
3. Modes C (consumer), F (family) and E (fully managed enterprise) remain distinct.
4. Deny by default. Unknown, stale, unsupported or unverified capabilities never become PASS or enforced.
5. AEGIS local enforcement remains authoritative; IROKA/AI can recommend but cannot grant itself authority or approve P3/P4.
6. P4 remains disabled in v1. P3 requires explicit scoped human authorization and local verification.
7. Personal vault, enterprise data and family roles remain cryptographically and logically separated.
8. Direct Boot is limited to documented Android behavior; no root, OEM authority, bootloader modification or security bypass.
9. Synthetic identities/data and mock services only until their respective real-world gates are met.
10. Every security-sensitive test must assert both expected denial and absence of side effects.

## 3. A3 workstream sequence

| Order | Workstream | Scope | Exit evidence | Gate |
|---|---|---|---|---|
| A3.0 | Repository and build foundation | Native Kotlin Android project, Gradle conventions, module boundaries, reproducible local build, baseline CI/static checks | Build evidence, toolchain pinning, clean source tree, CI results | First tranche |
| A3.1 | Security state and capability manifest | Typed states for VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED; capability discovery interfaces | Unit tests prove unknown/unsupported never render as active/enforced | First tranche |
| A3.2 | Mode and identity boundary | Mode C/F/E types, principal/role/scope abstractions, no implicit privilege inheritance | Cross-mode, cross-role and cross-tenant negative tests | First tranche |
| A3.3 | Local policy evaluator | Deterministic deny-by-default decisions; consent, role, scope, freshness, capability and policy checks | ALLOW/DENY/REQUIRE_APPROVAL/ESCALATE tests; no-side-effect assertions | First tranche |
| A3.4 | Offline policy state machine | Signed-policy interface, version/freshness/time-confidence model, expiry and rollback behavior; no production trust roots | Expiry, rollback, uncertain-clock and stale-policy tests | First tranche |
| A3.5 | Local audit foundation | Minimized typed event schema, protected-storage interface, sequence/integrity failure states | Secret redaction, gap/tamper detection tests; no absolute-immutability claim | First tranche |
| A3.6 | Remote command parser only | Strict typed envelope/parser and allowlist; no dispatch or live execution | Malformed, oversized, duplicate, wrong-scope and unknown-action rejection | Conditional; separate feature approval before execution |
| A3.7 | Vault scaffolding only | Non-sensitive vault domain interfaces, synthetic fixtures and storage abstraction | No plaintext/secret persistence; crypto engine inaccessible from scaffold | Conditional; cryptographic design gate before engine |
| A3.8 | Direct Boot boundary | Minimal bootstrap contract and test abstraction, no vault/recovery secrets | Device-protected storage boundary tests | Conditional; device validation before enforcement claim |
| A3.9 | Family and enterprise scaffolding | Synthetic policy models and adapter interfaces only | Guardian cannot access private vault; work profile != Device Owner; unsupported controls fail closed | Conditional; legal/device gates before activation |

The initial implementation tranche is A3.0–A3.5. A3.6–A3.9 require separate workstream-level readiness checks and may not be pulled into the first tranche merely because the scaffolding is described here.

## 4. Initial module boundaries (proposed)

Use a small modular structure; avoid premature service decomposition. Exact package/module names are confirmed against the actual repository tree after A3 authorization, before any source changes.

- `app`: Android application shell and composition root.
- `core:security-state`: security posture and evidence provenance types.
- `core:policy`: pure deterministic policy evaluation.
- `core:identity`: principal, role, mode and scope value types; no authentication backend assumption.
- `core:audit`: event schema and audit interfaces; no secrets in events.
- `platform:android-capabilities`: Android API/runtime capability discovery; no claim beyond observed support.
- `platform:storage`: storage interfaces and test doubles; no vault key implementation in first tranche.
- `test-support`: synthetic fixtures, fake clock and deterministic test utilities.

Keep pure domain logic independent of Android framework classes wherever practical. Introduce additional modules only when a real dependency boundary is demonstrated.

## 5. Engineering controls required from first commit

- Pin Gradle wrapper, Android Gradle Plugin, Kotlin and JDK versions to mutually compatible supported versions after checking the repository and current Android toolchain constraints.
- Use least-privilege CI permissions; no secrets in public workflow logs or source control.
- Enable formatting/lint, Kotlin static analysis where compatible, unit tests and secret scanning.
- Record dependency coordinates, versions, licenses and known vulnerabilities when dependencies are added; generate an SBOM at first dependency introduction and refresh before release.
- No unreviewed crypto, custom cryptographic primitives or guessed algorithm parameters.
- No live API endpoints, production credentials, signing keys, real user data or real child/employee data in development fixtures.
- Review security-sensitive diffs; tests must include negative cases and no-side-effect assertions.
- Preserve unrelated repository content and existing workflows; inspect before modifying.

## 6. First-tranche acceptance gates

A3.0–A3.5 are accepted individually only when:

1. Changes are limited to the approved workstream and documented paths.
2. Build and relevant tests execute successfully; unrun checks are explicitly NOT RUN.
3. Security-state provenance is preserved; no false VERIFIED status.
4. Policy decisions are deterministic and deny by default.
5. Cross-mode/role/scope and stale-policy negative tests pass.
6. Audit records exclude credentials, recovery material, vault keys and private payloads.
7. CI evidence and exact commit SHA are recorded.
8. No handset or production environment is touched.

A workstream cannot claim VERIFIED-CLOSED solely because code compiles. Acceptance requires its test and evidence criteria.

## 7. Deferred feature gates

- **Vault cryptographic engine:** approve exact standard/library, KDF, AEAD, nonce strategy, key hierarchy, backup/recovery and invalidation behavior in a feature-specific design review before implementation. OD-01/02 remain binding.
- **Remote command execution:** establish issuer trust, canonical serialization, key custody/rotation, revocation, replay/idempotency, local authorization and backend owner before live execution. Parsing-only scaffolding does not authorize dispatch.
- **Hardware attestation and StrongBox:** verify device support and verifier ownership before relying on these signals.
- **Device Owner / VPN / anti-theft:** validate Android Enterprise provisioning, API/version/device support and safe recovery on a dedicated test device before acceptance; no Owner handset enrollment or mutation under this plan.
- **Family / enterprise activation:** complete jurisdiction-specific legal/privacy review, consent/transparency and operational accountability before real users or tenants.
- **Independent implementation assessment:** mandatory A4/pre-release against a frozen candidate; critical findings remediated and retested before A5.

## 8. Reporting requirements

For each authorized A3 increment, report repository, branch, base/head SHA, changed paths, blob SHAs, build/test/CI evidence, dependency/SBOM state, test counts and failures, security findings, status labels, phase progress, readiness, blockers and immediate next step. Distinguish PASS, VERIFIED-CLOSED, PARTIAL, MISSING, UNRESOLVED, UNKNOWN and NOT RUN. Never infer success from a write response without fetch-back verification.

## 9. Stage status

| Stage | Status |
|---|---|
| A1 | VERIFIED-CLOSED / FROZEN |
| A2 | VERIFIED-CLOSED |
| A3 plan | DOCUMENTED; awaiting separate Owner stage authorization |
| A3 implementation | NOT AUTHORIZED / NOT STARTED |
| A4 | NOT STARTED |
| Independent implementation assessment | A4 / pre-release; NOT PERFORMED |
| A5 | NOT AUTHORIZED |

**Production readiness:** NOT ESTABLISHED.

**Immediate next gate:** Owner explicitly authorizes A3 transition and identifies/affirms the existing implementation branch. Until then, this plan remains documentation only.


---

## 10. Controlled implementation-roadmap addendum — 2026-11-28

**Addendum:** AEGIS-A3-ADD-001  
**Controlling product direction:** AEGIS-A1-AMD-001 and AEGIS-A2-AMD-002  
**VPN architecture:** AEGIS-A2-AMD-003  
**Status:** ROADMAP UPDATED; A2 FEATURE DESIGN OPEN; A3 TRANSITION STILL REQUIRES SEPARATE OWNER AUTHORIZATION  
**Planning branch:** docs/a2-cross-platform-product-revision  
**Preserved main baseline:** 570b6fe5abfcf0d7b369af63e211ab937bfe4024

This addendum supplements the original plan without deleting its historical sequence or changing the frozen A1 baseline. The current cross-platform documentation branch contains an Android application/core foundation; reconcile and preserve that work before adding VPN modules. Do not infer that VPN code, a tunnel, a gateway, or cross-platform clients exist.

### 10.1 Binding VPN implementation decisions

- One AEGIS ONE product with Personal VPN and Enterprise VPN.
- Hybrid gateway architecture: AEGIS-operated, approved partner-operated and customer-operated gateway classes, with explicit provider trust and policy selection.
- WireGuard first, using maintained upstream/platform implementations; no custom VPN protocol or cryptographic primitives.
- Independent VPN subsystem and native platform adapters within the integrated product.
- Separate Personal, Family and Enterprise identities, credentials, gateway eligibility, routes, DNS policy, audit visibility and retention.
- Endpoint-generated unique private keys; private keys remain endpoint-side and are never sent to the control plane.
- IROKA may analyze/recommend only; local AEGIS policy and explicit user/admin authority govern every protected action.
- No browsing-history/content logging by default; accurate disclosure of endpoint, control-plane and gateway-visible metadata.
- No paid gateway, provider, subscription, server, top-up or production capacity commitment without separate Owner authorization.
- No source implementation, production key issuance, live provisioning, merge, release or deployment is authorized by this planning addendum.

### 10.2 Required A2 design gates before VPN implementation

1. Complete platform-by-platform WireGuard implementation/library comparison, including maintenance, supported OS versions, license, provenance, vulnerability history and reproducible-build requirements.
2. Define the provider-neutral gateway registry, gateway identity/trust evidence, region/operator metadata and partner/customer onboarding and revocation.
3. Define control-plane contracts for device registration, peer provisioning, expiry, revocation, key rotation and server-side peer cleanup.
4. Define endpoint key generation/storage and lost-device/replacement behavior without private-key upload or Personal/Enterprise key crossover.
5. Define Personal full-tunnel/optional split-tunnel behavior separately from Enterprise private-resource routes and BYOD/corporate-owned policy.
6. Define DNS, IPv4/IPv6, MTU, captive-portal, roaming, sleep/resume and tunnel-failure semantics per platform.
7. Define kill-switch/always-on consent, OS capability gates and a tested recovery path. Unsupported OS behavior must remain visibly unsupported.
8. Define gateway operational security, abuse handling, incident response, privacy disclosure, log minimization and retention before any live service.
9. Complete VPN-TH-01 through VPN-TH-12 threat/control/test mapping and a cross-platform conformance plan.
10. Cost and approve any production gateway capacity separately. Local/test gateways and synthetic fixtures are permitted for development planning; production provisioning is not.

### 10.3 Revised A3 roadmap and dependency order

| Order | Workstream | Scope | Exit evidence / gate |
|---|---|---|---|
| A3.0 | Existing foundation reconciliation | Reconcile current app/core modules, build, CI, dependencies and exact SHA against cross-platform A2; preserve current Android work | File-level inventory, baseline SHA, build/test evidence, SBOM state |
| A3.1 | Shared domain and capability contracts | Common identity, mode, device, capability, error and conformance contracts | Platform-neutral tests; unknown/unsupported never treated as enforced |
| A3.2 | Authority and policy boundary | Personal/Family/Enterprise separation, tenant scope, consent and deny-by-default policy | Cross-mode/tenant negative tests; no-side-effect assertions |
| A3.3 | Offline policy and audit | Signed policy, expiry/revocation/time confidence, minimized audit and evidence provenance | Expiry, rollback, revocation, redaction and tamper tests |
| A3.4 | Cross-platform technology ADR | Compare shared-core options and native client/adapter strategy; preserve Kotlin Android foundation | Decision record, prototype evidence, license/security review |
| A3.5 | VPN domain contract | Tunnel desired/current state, gateway registry model, route/DNS policy, errors, health, key/peer lifecycle abstractions | Contract tests and VPN threat/test traceability |
| A3.6 | WireGuard dependency and supply-chain gate | Select maintained implementation per platform; pin versions, licenses, provenance and SBOM | Approved dependency record, SBOM, vulnerability/license scan |
| A3.7 | Android VPN adapter | VpnService integration, consent, foreground lifecycle, tunnel start/stop and supported always-on/lockdown reporting | Android unit/instrumentation tests, no false capability claims |
| A3.8 | VPN control plane — synthetic first | Gateway discovery, device registration and peer provisioning contracts with mock/local endpoints | Auth, scope, replay, expiry and revocation tests; no production keys |
| A3.9 | Gateway reference environment | Local/test WireGuard gateways and provider-neutral operations interface | Reproducible setup, peer lifecycle, health, isolation and teardown evidence |
| A3.10 | Network safety controls | DNS routing, leak prevention, IPv4/IPv6, reconnect, optional kill switch, split tunneling where supported | Packet-capture/leak tests, fail/recovery matrix per platform |
| A3.11 | Personal VPN policies | User consent, gateway selection, privacy UX, optional Wi-Fi auto-connect and usage diagnostics | Personal-mode consent/privacy and routing tests |
| A3.12 | Enterprise VPN policies | Tenant gateway profiles, private routes, enrollment, BYOD boundaries and admin audit | Cross-tenant denial, personal/corporate route separation and authorization tests |
| A3.13 | Apple VPN adapters | iOS/iPadOS Network Extension and supported WireGuard integration | Entitlement/signing feasibility, platform tests and capability manifest |
| A3.14 | Windows VPN adapter | Supported WireGuard integration and least-privilege service lifecycle | Install/update/uninstall, privilege and tunnel tests |
| A3.15 | macOS and Linux adapters | Native supported integration and OS-specific routing/privilege boundaries | Per-OS compatibility and conformance evidence |
| A3.16 | Hybrid provider operations | AEGIS/partner/customer gateway registry, trust, onboarding, revocation and incident workflows | Provider assessment, isolation, operational drills; production requires separate authorization |
| A3.17 | VPN integration hardening | IROKA advisory-only integration, privacy/audit integration, full regression and cross-platform conformance | AI cannot self-authorize; all VPN threat tests and regressions pass |

**Dependency rule:** A3.5–A3.17 cannot bypass their A2 design gates. Platform VPN clients must not be implemented against an unapproved or unpinned engine. Live provisioning and production gateways remain outside A3 until their operational and authorization gates are satisfied. The existing Android security foundation remains intact; VPN work is additive and must respect current module ownership.

### 10.4 A4 acceptance expansion

A4 must include, on exact frozen candidate SHA(s):

- Independent security assessment and remediation/retest.
- WireGuard integration and dependency/provenance/SBOM review.
- Key lifecycle, peer revocation, gateway trust and control-plane authorization testing.
- Packet-capture verification for DNS/IP leakage, IPv4/IPv6 routing, tunnel drops, reconnect, roaming and reboot.
- Kill-switch/lockdown recovery tests on each supported OS where the feature is claimed.
- Personal/Enterprise route, identity, key, tenant and audit isolation tests.
- Gateway operational security, logging minimization, privacy disclosure and incident-response evidence.
- Platform capability/entitlement/permission tests and explicit unsupported-state verification.
- Full regression of existing Android security foundations and OD-01 through OD-06.

No A4 pass may be inferred from a successful build alone. No production readiness claim is permitted until the complete applicable evidence is reviewed.

### 10.5 Current stage and repository status

| Item | Status |
|---|---|
| Frozen historical A1 v1.0 | Preserved |
| Cross-platform A2 amendment | OPEN / reconciliation in progress |
| VPN A2-AMD-003 | Recorded; feature design and traceability OPEN |
| Existing Android foundation | Present on cross-platform documentation branch; reconcile and preserve |
| VPN implementation | NOT STARTED / NOT VERIFIED |
| VPN gateways or control plane | NOT PROVISIONED / NOT VERIFIED |
| A3 authorization | NOT GRANTED by this addendum |
| A4 | NOT STARTED |
| A5 | NOT AUTHORIZED |
| Production readiness | NOT ESTABLISHED |

**Immediate next gate:** complete VPN A2 technical selections and contracts, reconcile them with the cross-platform A2 evidence register, then present the revised complete A3 baseline for the Owner's separate A3 transition decision.

---
**End of A3 controlled roadmap addendum.**


---

## 11. Consolidated cross-platform A3 implementation baseline — 2026-11-28

**Baseline ID:** AEGIS-A3-CONSOLIDATED-001  
**Status:** CONSOLIDATED PLAN FOR SEPARATE OWNER A3 TRANSITION DECISION  
**Controlling architecture:** AEGIS-A2-AMD-002 + AEGIS-A2-AMD-003 + AEGIS-A2-VPN-ENG-001  
**Preserved main baseline:** 570b6fe5abfcf0d7b369af63e211ab937bfe4024  
**Planning branch:** docs/a2-cross-platform-product-revision

This section is the current integrated A3 plan. It supersedes earlier sequencing tables for forward planning only; it does not delete their historical record or change frozen A1/A2 approvals. It integrates the original Android security foundation, cross-platform architecture and the VPN workstreams into one dependency-ordered delivery plan. A3 implementation is still NOT AUTHORIZED until the Owner's separate stage-transition decision.

### 11.1 Binding delivery rules

1. Preserve the current main baseline and existing Android source foundation. A3.00 is read-only inventory/reconciliation before any code change.
2. Use the Owner-designated existing implementation branch. Do not create a branch, change main, merge or deploy without separate direction.
3. Keep all implementation synthetic/local by default. No production credentials, production keys, live tenant data, paid services, public gateway capacity, user enrollment or production provisioning.
4. Preserve OD-01 through OD-06, Personal/Family/Enterprise separation, monotonic revocation, deny-by-default and local enforcement authority.
5. A3 implements one bounded workstream at a time. Each workstream has exact base/head SHA, changed paths, build/test/CI evidence, dependency/SBOM status, security findings and explicit NOT RUN status for unexecuted checks.
6. Security-sensitive tests assert both the denial and absence of side effects. Build success alone is not acceptance.
7. IROKA remains advisory; no AI action may bypass current local authorization.
8. Platform parity is not assumed. Every adapter publishes current capability evidence and limitations.
9. No production gateway or service operation is implied by local WireGuard test infrastructure.
10. A4 exact-SHA security assessment and regression precede any A5 deployment decision. A5 remains separate.

### 11.2 Integrated ordered workstreams

| Order | Workstream | Scope | Exit evidence / dependency |
|---|---|---|---|
| A3.00 | Repository and baseline reconciliation | Read-only inventory of main and designated implementation branch; app/core modules, Gradle, CI, dependencies, tests, current Android foundation, prior evidence and open changes | Exact baseline SHA; file/module inventory; build/test baseline; dependency/SBOM state; no unrelated changes |
| A3.01 | Security state and capability contracts | Typed evidence states, provenance/freshness, capability manifest and common error model | Unit/conformance tests prove UNKNOWN/UNSUPPORTED/DEGRADED never count as enforced |
| A3.02 | Shared identity and mode contracts | Per-installation identity; Personal/Family/Enterprise context; principal, role, tenant, membership version and device binding | Cross-mode, cross-tenant, stale-membership and revoked-identity negative tests |
| A3.03 | Deterministic local policy engine | Deny-by-default decision model; consent, scope, policy freshness, revocation, capability and risk gates | ALLOW/DENY/REQUIRE_APPROVAL/ESCALATE vectors; no-side-effect tests; AI cannot self-authorize |
| A3.04 | Offline policy state machine | Signed/scoped policy, expiry, rollback, replay protection, time confidence and OD-05 limits | Stale/expired/replayed/uncertain-clock tests; P0/P1 7-day, P2 24-hour, P3/P4 rules retained |
| A3.05 | Local audit foundation | Minimized typed events, protected storage interface, sequence/integrity states and redaction | Secret/content exclusion, tamper/gap detection and audit failure tests |
| A3.06 | Cross-platform technology ADR | Compare KMP/Rust/narrow shared core/native options; retain Kotlin Android path; define platform build and conformance strategy | Comparative decision record, small non-production feasibility evidence, licenses/security/build reproducibility; no rewrite by assumption |
| A3.07 | Vault domain scaffolding (conditional) | Non-sensitive interfaces, synthetic fixtures and storage abstraction only | No secret/plaintext persistence; crypto engine remains gated by separate cryptographic design |
| A3.08 | Android Direct Boot boundary (conditional) | Minimal bootstrap contract and tests; no vault/recovery secrets | Device-protected storage tests; physical-device evidence before any enforcement claim |
| A3.09 | Family and Enterprise security scaffolding | Synthetic relationship/tenant policies, enrollment/capability adapters and offboarding boundaries | Guardian cannot access private vault; work profile != Device Owner; cross-tenant denial |
| A3.10 | Remote command parser only (conditional) | Strict typed envelope, canonical schema, allowlist and parser; no dispatch or live execution | Malformed, oversized, replayed, wrong-scope and unknown-action denial; execution remains separately gated |
| A3.11 | VPN domain and state contract integration | Implement the A2-ENG-001 domain types, AuthorityContext, VpnProfile, GatewayDescriptor, TunnelState, typed errors and capability evidence | Contract tests, valid/invalid transition tests and VPN-TH traceability |
| A3.12 | WireGuard engine/dependency gate | Select maintained integration per OS; evaluate license, maintenance, supported versions, provenance, vulnerability history and reproducible builds; pin exact versions | Approved per-platform ADR, SBOM, license/provenance/vulnerability evidence; no custom crypto |
| A3.13 | Android VPN adapter | VpnService, consent, foreground lifecycle, tunnel start/stop, state/health and supported always-on/lockdown reporting | Android unit/instrumentation tests, lifecycle/recovery and capability evidence; no false support claims |
| A3.14 | VPN control plane — synthetic first | Mock/local registration, gateway discovery, peer provisioning, rotation, revocation and health receipt API | Auth/scope/tenant/replay/idempotency/expiry tests; private key absent from all requests/logs; no production credentials |
| A3.15 | Local gateway reference environment | Reproducible local/test WireGuard gateways, registry fixtures, peer lifecycle, health and teardown | Synthetic/local integration tests, isolation and cleanup; no paid/production gateway |
| A3.16 | Network safety controls | DNS policy, IPv4/IPv6, MTU, route enforcement, reconnect, captive portal, split-tunnel where supported, kill-switch/lockdown only with consent | Packet-capture and DNS/IP leak matrix; fail-open/fail-closed recovery; OS-specific capability evidence |
| A3.17 | Personal VPN policy and UX | Consent, eligible gateway selection, full/split routing, public Wi-Fi rules, diagnostics and privacy notice | Personal-mode route/key/gateway isolation and privacy tests |
| A3.18 | Enterprise VPN policy and UX | Tenant profiles, private routes, enrollment, BYOD/corporate-owned boundaries, admin assignment and offboarding | Cross-tenant denial; Personal/Enterprise route/key/log separation; authorization and offboarding tests |
| A3.19 | Apple VPN adapters | iOS/iPadOS Network Extension and supported maintained engine | Entitlement/provisioning feasibility, signing/distribution constraints, OS tests and capability manifest |
| A3.20 | Windows VPN adapter | Supported WireGuard integration and least-privilege service lifecycle | Service privilege, signing/provenance, install/update/uninstall and tunnel/recovery tests |
| A3.21 | macOS and Linux VPN adapters | Native supported integration and per-OS routing/privilege boundaries | Supported OS/distribution matrix, entitlement/kernel tests, conformance and safe teardown |
| A3.22 | Hybrid gateway operations (non-production first) | Provider registry lifecycle, operator/region trust, customer tenant binding, partner due diligence, abuse/incident and retention controls | Test provider admission/revocation drills and audit evidence; production approval/cost/legal gates remain separate |
| A3.23 | IROKA and cross-domain integration | Advisory-only threat/risk recommendations; local authorization remains mandatory; integrate audit, policy, identity and status | Malicious/stale AI recommendation cannot cause protected side effect; regression of existing security domains |
| A3.24 | Integrated hardening and release candidate | Full regression, cross-platform conformance, privacy review, SBOM, dependency and operational evidence; freeze exact candidate | All applicable requirements trace to tests/evidence on exact SHA; zero unresolved critical/high security findings; A4 handoff package |

### 11.3 Dependencies and tranche boundaries

- **Foundation tranche:** A3.00–A3.06. No feature work proceeds until the existing repository baseline is reconciled, security authority contracts are testable and the shared-core/native strategy is recorded.
- **Conditional security scaffolding:** A3.07–A3.10. These are not permission to implement vault cryptography, remote command execution, destructive anti-theft, P3 or P4 actions. Each remains behind its own approved design/authorization gate.
- **VPN domain and Android reference tranche:** A3.11–A3.16. A3.12 must pass before any platform VPN adapter consumes a WireGuard engine. Control-plane and gateway work stays mock/local/synthetic. Packet/leak evidence is mandatory before a feature is represented as protective.
- **Product and remaining platform tranche:** A3.17–A3.22. Personal and Enterprise remain separate; Apple/Windows/macOS/Linux adapters follow their platform feasibility and entitlement gates. Production provider onboarding is not included.
- **Integration/hardening tranche:** A3.23–A3.24. IROKA cannot authorize actions. Full regression includes OD-01–OD-06 and existing Android foundation. A4 is not folded into A3.

A later workstream may be developed in parallel only after its upstream contracts and dependency selection are frozen and the exact file/module ownership is conflict-free. Do not parallelize edits to the same files or bypass a gate for schedule convenience.

### 11.4 Cross-platform acceptance and A4 evidence

For each OS, record OS/API version, engine/library version, permission/entitlement/enrollment state, key-storage capability, tunnel lifecycle, routing semantics, DNS/IPv4/IPv6 behavior, failure/recovery behavior and unsupported features. Required A4 evidence on exact frozen candidate SHA includes:

- Independent security assessment, findings, remediation and retest.
- Dependency/license/provenance/vulnerability/SBOM review.
- Key generation/storage/rotation/revocation and peer cleanup.
- Forged/stale gateway and cross-tenant/mode authorization denial.
- Packet-capture DNS/IP leak, IPv4/IPv6, tunnel-drop, reconnect, roaming, sleep/resume and reboot evidence.
- Kill-switch/lockdown recovery only on platforms where it is claimed and supported.
- Personal/Family/Enterprise route, key, data and audit separation.
- Gateway operational security, privacy/retention, abuse and incident response evidence.
- Platform permissions, entitlements, enrollment and unsupported-state verification.
- Full regression of the existing Android foundation and OD-01 through OD-06.

### 11.5 Consolidated status and decision request

| Item | Status |
|---|---|
| A1 v1.0 / OD-01–OD-06 | FROZEN / PRESERVED |
| A2 cross-platform product direction | RECORDED |
| A2 VPN service/protocol/infrastructure decisions | OWNER-DECIDED |
| A2 VPN engineering contracts | DOCUMENTED in AEGIS-A2-VPN-ENG-001 |
| Exact engine/library and OS capability ADRs | OPEN / gated before platform integration |
| Repository implementation baseline | A3.00 READ-ONLY RECONCILIATION REQUIRED |
| A3 plan | CONSOLIDATED; presented for separate Owner decision |
| A3 implementation | NOT AUTHORIZED / NOT STARTED by this document |
| A4 | NOT STARTED |
| A5 | NOT AUTHORIZED |
| Production VPN / gateway readiness | NOT ESTABLISHED |

**Separate decision requested:** whether to authorize A3 transition under AEGIS-A3-CONSOLIDATED-001, beginning with read-only A3.00 reconciliation on the existing implementation branch. Authorization to begin A3.00 does not authorize production services, live keys, user enrollment, merge, release or deployment; those remain separately gated.

**Immediate next step:** Owner's separate A3 transition decision. If authorized, execute A3.00 first and report its exact baseline SHA and inventory before source changes.

---
**End of consolidated A3 baseline.**
