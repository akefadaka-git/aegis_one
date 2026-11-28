# AEGIS ONE — A3.06 Cross-Platform Technology Architecture Decision Record

**ID:** AEGIS-A3-ADR-006  
**Version:** 1.0  
**Status:** ACCEPTED ARCHITECTURE DECISION — implementation gates remain  
**Date:** 2026-11-28  
**Repository:** `akefadaka-git/aegis_one`  
**Implementation branch:** `docs/a1-baseline-a2-reconciliation`  
**Scope:** Android, iOS/iPadOS, Windows, macOS and Linux  
**Controlling contracts:** A3.01 security state/capability contracts; A3.02 identity and mode contracts; A3.03 deterministic policy evaluator; A3.04 offline policy validator; A3.05 typed audit ledger  
**Decision boundary:** Architecture and technology selection only. No platform adapter, KMP migration, native helper, VPN dependency, key store, production service, merge or deployment is authorized by this ADR.

## 1. Decision summary

AEGIS ONE will use a **Kotlin Multiplatform (KMP) shared domain core with platform-native security adapters and platform-appropriate application shells**. UI sharing is selective, not mandatory. Security-sensitive operations remain behind explicit, least-privilege platform adapters; shared code never pretends that OS capabilities are equivalent.

The existing Kotlin/JVM modules remain the accepted reference implementation until a separate, bounded portability migration proves that the contracts and deterministic test vectors behave identically on each target. This ADR does not convert Gradle plugins, source sets, dependencies or application modules.

### Selected application technology

| Platform | Application shell / UI | Shared domain runtime | Native security and OS integration |
|---|---|---|---|
| Android | Kotlin, Android SDK, Jetpack Compose | KMP Android/JVM target | Android Keystore/KeyMint, BiometricPrompt, Direct Boot APIs, DevicePolicyManager and VpnService, each capability-gated |
| iOS | Swift, SwiftUI | KMP iOS framework | Keychain, Secure Enclave when present, LocalAuthentication, CryptoKit and NetworkExtension entitlements |
| iPadOS | Swift, SwiftUI | KMP iOS framework | iOS-family APIs subject to iPadOS-specific availability, supervision, entitlement and OS restrictions |
| Windows | Kotlin/JVM desktop shell; Compose Multiplatform Desktop | KMP JVM target | Narrow Windows service/helper using DPAPI, CNG, Windows Hello and Windows Filtering Platform where justified |
| macOS | Kotlin/JVM desktop shell; Compose Multiplatform Desktop | KMP JVM target | Narrow signed native helper/system extension using Keychain, LocalAuthentication, NetworkExtension and App Sandbox/entitlements |
| Linux | Kotlin/JVM desktop shell; Compose Multiplatform Desktop | KMP JVM target | Narrow Rust helper using least privilege; kernel keyring/Secret Service only when available; Linux capabilities, namespaces and LSM controls where deployed |

The desktop shell is not a security boundary. Native helpers are separate, small, auditable processes with a typed IPC contract. They do not accept arbitrary shell commands, scripts, policy overrides or unscoped privileged requests.

## 2. Why this architecture

1. The accepted policy, identity, offline-policy and audit modules are pure Kotlin domain logic with no Android framework imports. They are the strongest existing basis for selective sharing.
2. KMP supports incremental sharing of business logic while retaining native UI and native platform APIs. Android, iOS, JVM desktop and Kotlin/Native targets are documented. iOS simulator/device builds require a macOS host with Xcode.
3. OS security APIs, entitlements, management roles, VPN lifecycle and secure storage differ materially. A shared UI or a generic cross-platform permission abstraction cannot manufacture these powers.
4. Rewriting the accepted Kotlin policy engine in Rust, C++, C# or Swift would create avoidable security-sensitive duplication and migration risk. No rewrite is selected.
5. A single KMP common domain core gives all clients the same typed policy and identity semantics; native adapters supply evidence and enforcement capabilities without changing those semantics.

## 3. Shared-core boundary

### 3.1 Modules to become portable incrementally

The intended KMP shared source boundary is:

- `core:security-state`: capability provenance, platform descriptor, identity/mode/scope types and pure identity validation.
- `core:policy`: deterministic P0–P4 policy evaluation and decision types.
- `core:offline-policy`: signed-envelope data contracts, freshness/expiry/scope/version state machine and verifier/store interfaces.
- `core:audit`: typed event schema, canonical event encoding contract, chain verification and protected-storage interface.
- A future explicit `core:platform-contracts` boundary only if real adapter duplication demonstrates the need; do not create a module speculatively.

Identity currently resides in `core:security-state`; do not create a second identity authority or split the package merely to match a diagram.

### 3.2 Common code must not contain

- Android, Apple, Windows or Linux framework types.
- Direct filesystem, database, keychain, Keystore, DPAPI, Keychain Services, Secret Service or kernel-keyring calls.
- Platform-specific wall-clock or secure-clock acquisition.
- Device enrollment, attestation-chain validation, OS entitlement discovery or management-role inference.
- Private-key generation/storage or key export.
- Network/VPN interface control, firewall rules, lockdown or privileged process management.
- UI, lifecycle, notification or background-execution assumptions.
- Hidden fallback behavior that promotes UNKNOWN/UNSUPPORTED/OWNER_REPORTED evidence to VERIFIED.

### 3.3 Explicit adapter ports

The shared core consumes narrow injected interfaces, including:

- `CapabilityEvidenceProvider`: reports capability, provenance, observed time, validity, prerequisites, permissions/entitlements, enrollment requirements, supported actions and exclusions.
- `IdentityAuthoritySnapshotProvider`: supplies a current versioned membership/role snapshot; unavailable or stale authority remains DENY.
- `TrustedTimeProvider`: reports time and confidence; wall-clock-only evidence cannot become TRUSTED.
- `PolicySignatureVerifier` and `PolicyIssuerTrust`: verify a canonical payload against an explicitly configured issuer; default implementation denies.
- `PolicyVersionStore`: durable, scope-specific atomic compare-and-set; no in-memory production substitute.
- `ProtectedAuditStorage`: encrypted/authenticated-at-rest, access-restricted, durable atomic compare-and-append; no plaintext fallback.
- `AuditDigestProvider`: vetted SHA-256 implementation over the exact versioned canonical byte sequence.
- `LocalAuthenticationProvider` and `ProtectedKeyProvider`: platform-mediated authentication and key operations; no key bytes cross into logs, UI state, IPC diagnostics or AI context.
- `NetworkProtectionAdapter`: typed VPN/network-protection state and capability evidence only; policy authorization remains in the shared core.

Adapters return explicit typed states: VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED, REQUIRES_PERMISSION or REQUIRES_ENROLLMENT. Adapter errors and missing evidence fail closed.

## 4. Contract reconciliation

| Accepted contract | Shared-core responsibility | Platform adapter responsibility | Required invariant |
|---|---|---|---|
| A3.01 security state | Provenance, freshness and capability semantics | Discover actual OS/API/entitlement/enrollment support | No unverified capability is enforced |
| A3.02 identity | Installation identity references, mode, principal, membership, exact role and scope validation | Generate and protect installation keys; obtain current identity evidence; report enrollment state | No cross-mode/tenant privilege; no implicit role |
| A3.03 policy | Deterministic ALLOW/DENY/REQUIRE_APPROVAL/ESCALATE decision | Execute only a separately authorized typed action after rechecking local prerequisites | AI/IROKA cannot self-authorize; P4 disabled; P3 human approval |
| A3.04 offline policy | Canonical envelope contract, issuer/scope/signature result, freshness, rollback and version state machine | Trusted-time evidence, issuer trust material, signature verification and durable monotonic version store | Offline does not extend authority; P3/P4 unavailable offline |
| A3.05 audit | Minimized typed event, stable canonical encoding, chain verification and failure states | Protected durable storage, access control, key protection, atomic commit and optional remote receipts | Detect tampering/gaps; never claim immutable local logs |

**Important:** the current A3.05 hash chain is tamper-evident, not immutable. A compromised OS or privileged attacker may alter both data and local integrity material. Platform storage hardening and server checkpoints are future controls, not claims of this ADR.

## 5. Platform decisions and capability limits

### 5.1 Android

**Selected:** Kotlin application and platform adapters; Jetpack Compose for app UI; Android Keystore/KeyMint and BiometricPrompt for local cryptographic/authentication integration.

- Hardware-backed Keystore/KeyMint and StrongBox are optional device capabilities. Query and validate the actual key security level; never infer StrongBox, Knox, verified boot or attestation from model name or OS version.
- Direct Boot support must be a small device-protected bootstrap boundary. Credential-encrypted vault data and authentication-bound keys remain unavailable until user unlock.
- DevicePolicyManager controls are conditional on eligible Android Enterprise provisioning and the actual Device Owner/Profile Owner role. A normal install is not Device Owner.
- VpnService requires user-mediated consent and is distinct from Device Owner policy. Always-on/lockdown behavior is conditional on OS version, provisioning role and tested recovery.
- Keep the existing `platform:android-capabilities` module as the Android-specific evidence adapter. It currently reports only the runtime API as VERIFIED and leaves StrongBox, Device Owner, verified boot and Knox UNKNOWN; do not broaden it without runtime evidence.

### 5.2 iOS and iPadOS

**Selected:** Swift/SwiftUI application and extension shells; KMP shared domain framework; Apple Security/Keychain, LocalAuthentication and CryptoKit for native cryptographic boundaries.

- Use Keychain protection classes appropriate to the data lifecycle. Use Secure Enclave-backed keys only when the hardware and API operation actually support them; absence is UNKNOWN/UNSUPPORTED, not a software-equivalent VERIFIED result.
- NetworkExtension is the supported framework for VPN configurations, custom VPN protocols, DNS proxy and content filtering. Availability varies by OS, entitlement, supervision and feature.
- Per-app VPN and Always On VPN are not general consumer-app capabilities; management/supervision prerequisites must be represented in the capability manifest.
- iOS/iPadOS background execution, app sandbox and extension lifecycle constrain continuous monitoring. Do not promise always-running protection or device-wide control beyond documented entitlements.
- The iOS app must not be represented as an Android-style Device Owner equivalent.

### 5.3 Windows

**Selected:** Kotlin/JVM + Compose Multiplatform Desktop for the user-facing desktop shell; narrow signed Windows service/helper only for capabilities that cannot be safely performed in the user process.

- Use DPAPI for user- or machine-bound protected data where its documented semantics fit; use CNG for cryptographic key operations and crypto-provider selection. Prefer hardware-backed/TPM protection only after checking actual provider/key properties.
- Windows Hello is an authentication signal, not a substitute for AEGIS membership, role, scope or approval validation.
- Windows Filtering Platform is the native filtering enforcement surface. Any callout driver, service or filter installation requires separate design, signing, privilege and recovery gates.
- Privileged IPC must use authenticated local named pipes, restrictive ACLs, request size limits, versioned typed messages, replay protection and a strict allowlist. The service revalidates authorization; it does not trust a desktop UI decision.
- WireGuard Windows integration is a separate dependency/license/provenance gate. Do not embed a driver or install a service in A3.06.

### 5.4 macOS

**Selected:** Kotlin/JVM + Compose Multiplatform Desktop shell; native Swift helper/extension where Apple frameworks require it.

- Use Keychain and LocalAuthentication; Secure Enclave support is conditional on hardware/API. Do not assume a Secure Enclave exists on every Mac.
- Use App Sandbox and explicit entitlements. NetworkExtension/system extensions are privileged system integration points and require the correct entitlement, approval and lifecycle handling.
- Prefer user-space System Extensions/NetworkExtension over kernel extensions. Avoid kernel extensions and broad root daemons.
- macOS NetworkExtension features are not interchangeable with iOS/iPadOS features. Each capability needs an OS-specific availability/entitlement record.
- macOS native helper installation, notarization, signing and update policy require a separate distribution gate.

### 5.5 Linux

**Selected:** Kotlin/JVM + Compose Multiplatform Desktop shell; a narrow Rust helper for operations requiring OS privilege or a stable native boundary.

- Treat Linux as a distribution/kernel/environment matrix, not a single uniform security API. Detect kernel, architecture, init system, desktop secret service, LSM, cgroup and networking capabilities at runtime.
- Linux kernel keyrings may be used where enabled and appropriate, but are not a universal desktop secret vault. Secret Service/libsecret or desktop wallet integration is optional; absence must not trigger plaintext storage.
- Use restrictive file permissions, process isolation and available LSM controls as defense in depth. Apply seccomp/namespaces/capability dropping to helpers where supported and tested.
- TUN and firewall/network operations require specific privileges such as CAP_NET_ADMIN or a narrowly authorized privileged helper. Do not run the entire UI or policy engine as root.
- A systemd service is an optional integration, not an assumption about every Linux distribution. Provide an explicit unsupported/restricted state when the required service manager or capability is absent.
- WireGuard may use the kernel implementation or a vetted userspace implementation; the exact implementation, version, license, provenance and privilege model remain a separate A3.12 gate.

## 6. UI and process architecture

- Android: single app process by default; isolate any future VPN or privileged service according to Android lifecycle and OS rules.
- iOS/iPadOS: SwiftUI host app plus only the entitled NetworkExtension/other extensions actually needed.
- Windows: Compose Desktop user process plus optional signed Windows service; named-pipe IPC.
- macOS: Compose Desktop user process plus optional signed Swift system extension/helper; use Apple authorization and entitlement flows.
- Linux: Compose Desktop user process plus optional Rust helper; Unix-domain socket IPC with peer-credential validation and restrictive socket permissions.

No privileged helper may:
- accept arbitrary command strings, scripts or executable paths;
- override policy decisions, P3 approval, P4 disabled state or tenant boundaries;
- receive private vault keys, recovery secrets or unrelated personal data;
- grant IROKA/AI authority;
- silently change routes, DNS, firewall, VPN, device management or user security settings.

## 7. Shared serialization, cryptography and deterministic behavior

1. Preserve the existing policy decision and identity semantics exactly during portability work.
2. Define versioned, language-neutral canonical encodings for policy envelopes, approval payloads and audit records. No ad-hoc JSON serialization or map-order-dependent signatures.
3. Use vetted platform cryptographic providers or an approved multiplatform library. Do not implement custom cryptographic primitives.
4. Hash algorithms and canonical bytes must produce identical golden test vectors on Android/JVM, iOS/Native and desktop/JVM.
5. Key generation, private-key storage, user-presence gating and attestation stay in native adapters. Private keys never cross the shared-core boundary.
6. Platform time providers must report source, confidence, monotonic/wall-clock relationship, rollback evidence and observation time. A wall clock alone cannot assert trusted time.
7. Policy, membership, role, capability and audit state must retain version, scope, expiry and provenance through every serialization boundary.
8. Audit writes and policy-version writes must be atomic and durable before success is reported. On unsupported storage semantics, return STORAGE_UNAVAILABLE/RESTRICTED; never silently downgrade.

### Current portability blockers discovered in the source

The accepted source is not yet KMP-common compatible in all modules:

- `core:audit` imports `java.io.ByteArrayOutputStream`, `java.io.DataOutputStream`, `java.nio.charset.StandardCharsets` and `java.security.MessageDigest`. Canonical byte encoding and digest calculation need portable interfaces/implementations and cross-target vectors.
- Audit event ID/ref validation currently uses Kotlin Regex (portable in principle) but must be checked under every selected compiler target.
- Identity uses `@JvmInline`; retain only after compiling and testing the exact KMP targets.
- Offline policy and most policy/state code use basic Kotlin data types and injected interfaces, but `ByteArray` equality/serialization and deterministic map/set handling require explicit canonicalization tests.
- The current repository has only Android application and Android capability modules plus Kotlin/JVM core modules. There is no iOS, Windows, macOS or Linux application, no KMP plugin configuration, no native helper, and no production protected store.
- Current CI has not yet built iOS targets or a desktop distribution. Android CI success is not cross-platform verification.

These are migration findings, not defects in the accepted A3.01–A3.05 Android/JVM scope.

## 8. Target and build strategy

### 8.1 Initial target matrix

| Target | Initial build target | Verification host |
|---|---|---|
| Android | KMP Android/JVM | Linux or Windows CI with Android SDK; device/API tests later |
| iOS device | `iosArm64` | macOS with Xcode |
| iOS simulator | `iosSimulatorArm64` | Apple Silicon macOS with Xcode |
| Windows desktop | KMP `jvm` + Compose Desktop | Windows CI |
| macOS desktop | KMP `jvm` + Compose Desktop | macOS CI |
| Linux desktop | KMP `jvm` + Compose Desktop | Linux CI |

Do not select Kotlin/Native `mingwX64` as a production Windows shared-core target in this phase: current Kotlin documentation classifies it as Tier 3. Linux Kotlin/Native targets are Tier 2; Apple iOS device and Apple Silicon simulator/macOS ARM64 targets have higher support tiers. The desktop JVM route is selected to avoid making a lower-tier native target a security-critical release dependency.

### 8.2 CI requirements before platform acceptance

- Build and run shared-core conformance tests on Android/JVM and desktop/JVM.
- Build and run iOS framework and simulator tests on macOS/Xcode; device signing/install tests are a separate gate.
- Build desktop packages on Windows, macOS and Linux independently.
- Run the same identity, policy, offline-policy and audit golden vectors on every target.
- Run platform-specific capability and negative tests, including missing entitlement, absent secure hardware, no trusted time, revoked membership, unavailable storage, append race, tampered chain and unsupported VPN controls.
- Generate per-target SBOMs and inspect dependency/license/provenance coverage before adding native dependencies.
- Sign and notarize platform artifacts only in a separately authorized release workflow; CI must not use production signing keys.

## 9. Dependency, license and cost policy

- Prefer platform SDKs and maintained upstream implementations over custom security code.
- No commercial cross-platform framework or paid cloud service is required by this ADR.
- No VPN engine is introduced here. WireGuard remains first choice only after A3.12 exact dependency, license, provenance, update and platform-integration review.
- Any third-party multiplatform crypto, storage, IPC, database or VPN library requires exact coordinates/version, license, maintenance status, CVE review, SBOM evidence and a replacement/upgrade plan before adoption.
- Do not bundle multiple crypto implementations without a justified compatibility requirement.
- Do not add a runtime, background daemon or native helper merely to claim feature parity.

## 10. Decisions and deferred gates

| Decision | Outcome |
|---|---|
| One shared UI across all five OS families | REJECTED as a requirement; platform-native UX and lifecycle take precedence |
| KMP shared domain logic | ACCEPTED target architecture; incremental migration only |
| Native security adapters | ACCEPTED; least-privilege and capability-evidence driven |
| Rewrite all existing core modules in Rust/C++ | REJECTED |
| Compose Multiplatform Desktop for Windows/macOS/Linux shell | ACCEPTED target; package and accessibility proof still required |
| SwiftUI for iOS/iPadOS | ACCEPTED |
| Jetpack Compose for Android | ACCEPTED |
| Windows native privileged service | CONDITIONAL; only when a required feature cannot run safely in user mode |
| macOS system extension/helper | CONDITIONAL; entitlement and signing proof required |
| Linux Rust privileged helper | CONDITIONAL; capability/LSM/IPC proof required |
| Secure Enclave/StrongBox/TPM/attestation | CONDITIONAL per actual device evidence |
| Production audit storage and policy-version store | DEFERRED to platform storage workstreams |
| VPN adapters and WireGuard engine | DEFERRED to A3.11 onward and A3.12 dependency gate |
| Device Owner, MDM/supervision and system-wide enforcement | CONDITIONAL on legal, enrollment, entitlement and OS gates |

## 11. Acceptance criteria for A3.06

A3.06 is accepted as an architecture decision when:

- all five OS families have an explicit app-shell, shared-core and native security integration choice;
- accepted A3.01–A3.05 contracts map to shared responsibilities and native adapter responsibilities;
- unsupported/unknown capability behavior, P3/P4 rules, mode/tenant isolation and offline freshness are preserved;
- platform-specific entitlements, privilege boundaries, secure-storage limits and VPN constraints are explicit;
- current Kotlin/JVM portability blockers and CI host requirements are recorded;
- no unapproved dependencies, source migrations, native services, credentials, enrollment or production changes are introduced;
- exact branch and `main` SHAs are fetched back after documentation commit.

## 12. Implementation sequence after this ADR

1. Preserve current Kotlin/JVM modules as the conformance reference.
2. Add a KMP portability proof-of-concept in a bounded, separately tracked workstream; first compile the pure state/identity/policy contracts without changing their behavior.
3. Extract canonical serialization and digest provider interfaces for audit; freeze golden vectors before any implementation replacement.
4. Prove Android/JVM and iOS/Native equivalence, then add desktop/JVM builds on Windows, macOS and Linux.
5. Implement one platform adapter at a time, each with its own capability matrix and negative tests.
6. Only after the native storage adapters exist can protected audit persistence and offline policy version state be considered production-ready.
7. VPN and privileged network enforcement remain separately gated; this ADR does not authorize those implementations.

## 13. Source references

Official platform and toolchain references consulted on 2026-11-28:

- Kotlin Multiplatform overview and incremental sharing: https://kotlinlang.org/docs/multiplatform/faq.html and https://kotlinlang.org/multiplatform/
- Kotlin/Native target support tiers and host restrictions: https://kotlinlang.org/docs/native-target-support.html
- Kotlin Multiplatform targets and iOS device/simulator targets: https://kotlinlang.org/docs/multiplatform-set-up-targets.html
- Android security features and hardware-backed Keystore/KeyMint: https://source.android.com/docs/security/features and https://developer.android.com/privacy-and-security/keystore
- Apple NetworkExtension capabilities and restrictions: https://developer.apple.com/documentation/networkextension
- Apple entitlements: https://developer.apple.com/documentation/bundleresources/entitlements
- Apple App Sandbox: https://developer.apple.com/documentation/security/app-sandbox
- Microsoft DPAPI/CNG: https://learn.microsoft.com/en-us/windows/win32/seccng/cng-dpapi
- Microsoft Windows Filtering Platform: https://learn.microsoft.com/en-us/windows/win32/fwp/about-windows-filtering-platform
- Linux kernel key retention service: https://www.kernel.org/doc/html/latest/security/keys/core.html
- WireGuard official platform installation and embedding references: https://www.wireguard.com/install/ and https://www.wireguard.com/embedding/

## 14. Status

- A3.06: **ARCHITECTURE DECISION ACCEPTED**.
- KMP migration: NOT STARTED.
- Native platform adapters: NOT STARTED.
- Platform builds/device tests: NOT RUN.
- Production readiness: NOT ESTABLISHED.
- Merge/deployment: NOT AUTHORIZED by this ADR.

**Immediate next workstream:** A3.07 — bounded shared-core portability proof-of-concept and technology validation, with no change to the accepted policy semantics and no production platform integration.
