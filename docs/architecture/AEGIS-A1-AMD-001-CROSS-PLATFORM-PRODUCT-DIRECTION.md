# AEGIS ONE — Controlled A1 Product-Direction Amendment

**Amendment ID:** AEGIS-A1-AMD-001  
**Version:** 1.0  
**Date:** 2026-11-26  
**Status:** OWNER-DIRECTED PRODUCT DECISION RECORDED; A2 RECONCILIATION OPEN  
**Amends:** AEGIS-A1-BASE-001 v1.0 / PMSS Architecture v1.2  
**Repository:** `akefadaka-git/aegis_one`

## 1. Purpose and authority

This controlled amendment records the Owner's 2026-11-26 decisions that materially expand the original Android-only product direction. The original A1 v1.0 document remains preserved, unchanged, and historically authoritative for the decisions it contained. This amendment does not silently rewrite or erase that baseline.

The Owner explicitly selected:

1. **Product model — BOTH:** one AEGIS ONE product with Personal and Enterprise modes, rather than separate Personal and Enterprise products.
2. **Platform strategy — ALL:** cross-platform architecture from the beginning, covering personal phones, tablets and computers, with enterprise device security as a secondary product focus.

These are approved product-direction decisions. The detailed cross-platform architecture and capability boundaries are recorded in AEGIS-A2-AMD-002 and remain subject to A2 reconciliation and acceptance.

## 2. Revised product identity

**Product:** AEGIS ONE  
**Category:** Cross-platform personal and enterprise device security  
**Primary focus:** Personal device security for phones, tablets and computers  
**Secondary focus:** Enterprise endpoint and managed-device security  
**Intelligence backbone:** IROKA, operating under governed authority and not as an independent device-control authority.

**Revised product statement:** AEGIS ONE is one cross-platform device-security product that protects personal devices and digital privacy, with a separately governed Enterprise mode for authorized organizational device protection and management.

The product is not an ARNA/AGE security application. AEGIS ONE remains a distinct product and repository. IROKA remains its intelligence/security backbone; the product and the backbone are not the same system.

## 3. Controlled changes to A1 v1.0

| A1 v1.0 subject | Amendment disposition |
|---|---|
| Product identity and product statement | Expand from Android security product to cross-platform device security product |
| Target platform profile | Expand from Android-only to Android, iOS/iPadOS, Windows, macOS and Linux architecture |
| Initial reference device | Retain Samsung Galaxy A16 SM-A165F as the Android reference device; it is not the sole product target |
| Application technology | Retain Kotlin/native Android for the existing Android client; add native platform adapters/clients as required |
| Product modes | Retain distinct consumer, family and fully managed enterprise authority domains; formalize one product with Personal and Enterprise modes |
| IROKA boundary | Preserve governed intelligence; models/agents cannot self-authorize or bypass local/platform enforcement |
| Security, privacy and stage gates | Preserve all compatible A1 principles, constraints and A2 Owner decisions |

## 4. Platform scope

The target architecture must account for:

- Android phones and tablets.
- iPhone and iPad.
- Windows PCs and tablets.
- macOS computers.
- Linux computers.

"All platforms from the beginning" means that platform differences, capability contracts, security boundaries, identity, policy, testing and product navigation are designed into the architecture from inception. It does **not** mean every security feature is available on every operating system, or that all native clients must ship simultaneously.

Each platform must report only capabilities it can actually exercise under its current OS version, hardware, permissions, entitlements, enrollment state and policy. Unsupported or unverified capabilities remain explicitly unsupported/unknown and must never be displayed as protected or enforced.

## 5. Architecture direction

Adopt a platform-adapter architecture:

1. A common product/domain and policy contract for identity, modes, roles, device records, security state, authorization, audit events, IROKA command envelopes and cross-platform conformance.
2. Native platform clients and narrowly scoped platform adapters for operating-system security APIs and user experience.
3. A local enforcement boundary on every device. Core local protections must not depend on IROKA availability.
4. Optional backend and IROKA services for authenticated device registration, signed policy, threat intelligence, revocation and audit synchronization.
5. Separate Personal, Family and Enterprise data/authority domains, including separate cryptographic key hierarchies where required.

The exact shared-core technology and each desktop client technology are not selected by this amendment. A comparative architecture decision record and small feasibility prototypes are required before those choices are frozen. The existing Kotlin Android implementation is retained; it must not be treated as proof that a Kotlin-only application can safely provide all platform capabilities.

## 6. Preserved binding decisions and constraints

This amendment does not revoke or weaken:

- AEGIS-A1-BASE-001 v1.0 as a preserved historical baseline and its compatible security principles.
- PMSS Architecture v1.2 designation; no expansion of the acronym is invented.
- Local-first protection, least privilege, platform truth, fail-safe behavior, privacy by design and evidence-based assurance.
- Per-installation cryptographic device identity; hardware identifiers are not account identity.
- Monotonic revocation: a revoked installation identity is not reactivated.
- User-controlled high-entropy vault recovery; no provider/operator recovery.
- Enterprise vault escrow disabled in v1; no enterprise administrator or operator gains implicit access to a Personal vault.
- Transparent, age-appropriate, least-privilege Family controls; guardians do not gain unrestricted access to private vaults, passwords, files or communications.
- P3 device-impacting actions require scoped human authority, signed/time-limited approval and local policy verification; AI cannot approve them.
- P4 irreversible actions remain disabled by default in v1 and require a separate future approval.
- Offline policy authority remains signed, scoped and time-limited: P0/P1 maximum seven days, P2 maximum 24 hours; P3 remote actions denied offline absent a separately approved protocol; P4 disabled.
- Enterprise management requires explicit, platform-valid enrollment and role-scoped authority. Android Device Owner remains conditional on Android Enterprise provisioning; other platforms require their own supported management/enrollment mechanism.
- IROKA commands remain signed, scoped, fresh, replay-protected, locally authorized and auditable. IROKA/AI cannot override platform controls or local policy.
- No root, bootloader unlock, OEM/firmware modification, custom OS, silent surveillance, unrestricted remote control or false anti-uninstall claims.
- No factory reset, enrollment, installation on the Owner's handset, production key generation, live service provisioning, merge, release or deployment without the required separate authorization.
- A5 remains a separate Owner approval gate.

## 7. Stage and change-control effect

A2 is reopened only for the scoped cross-platform product and architecture reconciliation under AEGIS-A2-AMD-002. The prior A2 closure record remains in the repository as the historical record of the 2026-11-25 closure; this amendment does not falsify or delete it.

The existing A3 plan was written against the Android-only A1 baseline. It must be reconciled to the approved product direction before it can govern implementation of the expanded product. This amendment does not authorize cross-platform implementation, platform enrollment, device modification, release or deployment.

**Current status:** Product-direction decisions recorded; detailed A2 architecture and evidence reconciliation OPEN; A3 scope re-baselining required; A4 not started; A5 not authorized.

---
**End of controlled A1 product-direction amendment.**
