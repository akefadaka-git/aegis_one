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

## 10. Owner-authorized A3 roadmap addendum — A3.06 cross-platform technology (2026-11-28)

This addendum supersedes conflicting A3 numbering and authorization-status statements in Sections 1, 3, 7 and 9 of the original 2026-11-25 plan. It does not alter A1/A2 security invariants or authorize production operations.

**Owner instruction:** Execute A3.06 cross-platform technology ADR, reconciling Android, iOS/iPadOS, Windows, macOS and Linux technology choices against the accepted security-state, identity, policy, offline-policy and audit contracts. Preserve the existing implementation branch and keep `main` unchanged.

**A3.06 result:** Architecture decision recorded in `docs/architecture/AEGIS-A3-ADR-006-CROSS-PLATFORM-TECHNOLOGY.md`, version 1.0. The selected target is Kotlin Multiplatform for shared pure domain logic, native security adapters, Jetpack Compose on Android, SwiftUI on iOS/iPadOS, and Compose Multiplatform Desktop on Windows/macOS/Linux. Native helpers and privileged integrations remain conditional and separately gated.

**Renumbered sequence:**

| Workstream | Scope | Status / gate |
|---|---|---|
| A3.00–A3.05 | Existing bounded foundation, security state, identity, policy, offline policy and audit | Accepted individually as bounded scaffolds on exact-SHA evidence |
| A3.06 | Cross-platform technology ADR | ARCHITECTURE DECISION ACCEPTED; no migration or adapter implementation |
| A3.07 | Vault scaffolding | Conditional; no live cryptography/recovery until feature-specific design gate |
| A3.08 | Direct Boot boundary | Conditional; Android-specific feasibility and device/API validation |
| A3.09 | Family and enterprise scaffolding | Conditional; synthetic data only until legal, consent and enrollment gates |
| A3.10 | Remote command parser only | Conditional; no dispatch/execution; issuer trust and execution remain separately gated |

The subsequent VPN, gateway, cross-platform adapter, IROKA and release-candidate workstreams require their own approved scope, dependency gates and evidence. The original plan's A3.6 parser numbering is retired; it is now A3.10.

**Authorization status:** A3 implementation has been authorized incrementally. A3.06 is complete as an architecture decision. This does not authorize KMP source migration, platform application creation, privileged helpers, native key stores, VPN integration, device enrollment, production keys/services, merge, release, deployment, A4 or A5.

**Repository controls:** Continue on `docs/a1-baseline-a2-reconciliation`; do not create a branch. Keep `main` at `570b6fe5abfcf0d7b369af63e211ab937bfe4024`. No production mutation or device interaction.

**Immediate next gate:** A3.07 is the next numbered workstream in the superseding roadmap, but it remains conditional and must not be inferred as authorized by completion of A3.06.
