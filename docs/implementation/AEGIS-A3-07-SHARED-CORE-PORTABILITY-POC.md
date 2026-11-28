# AEGIS ONE — A3.07 Shared-Core Portability Proof-of-Concept

**ID:** AEGIS-A3-POC-007  
**Version:** 1.0  
**Status:** ACCEPTED — bounded portability proof only  
**Implementation SHA:** `b6346906ea453ef1d75410e40893845d5c9d9913`  
**Portability CI:** [Run #46152135064](https://github.com/akefadaka-git/aegis_one/actions/runs/46152135064)  
**Android foundation CI:** [Run #46152134992](https://github.com/akefadaka-git/aegis_one/actions/runs/46152134992)  
**Scope:** Android/JVM, iOS/Native, Windows/macOS/Linux JVM  
**Branch:** `docs/a1-baseline-a2-reconciliation`

## 1. Objective and boundary

Prove that the accepted security-state, identity, policy and offline-policy contracts compile as Kotlin Multiplatform common code and produce identical deterministic results on Android/JVM, iOS/Native and desktop/JVM. Prove audit canonical serialization and platform SHA-256 provider equivalence.

This is a bounded engineering proof. It does not create production application shells, privileged native services, production key stores, device enrollment, VPN enforcement, or production audit persistence. It does not authorize merge or deployment.

## 2. KMP targets and source-set layout

The following modules now use Kotlin Multiplatform targets:

- `core:security-state`
- `core:policy`
- `core:offline-policy`
- `core:audit`

Each module configures Android, JVM, iOS ARM64 device and iOS Simulator ARM64 targets. Shared source files are in `src/commonMain`; shared conformance tests are in `src/commonTest`; existing JUnit regression suites are in `src/jvmTest`.

Platform digest providers are isolated by source set:

| Target | SHA-256 provider |
|---|---|
| Android | Android/JVM JCA `MessageDigest` |
| Desktop JVM | JVM JCA `MessageDigest` |
| iOS device and simulator | Apple CommonCrypto `CC_SHA256` through a typed C-interop wrapper |

The iOS CommonCrypto C interop is configured for both Apple targets and commonized for `iosMain`. No third-party cryptographic library or custom cryptographic primitive was introduced.

## 3. Contract portability

| Contract | Common implementation | Proof |
|---|---|---|
| A3.01 security state | Capability provenance and state contracts | iOS device compile, Android compile, JVM and iOS common tests |
| A3.02 identity | Installation identity, principal, membership, mode, scoped role and validation contracts | Shared exact-scope, tenant-isolation, unavailable-authority and AI-principal tests |
| A3.03 policy | Deterministic policy evaluator | Shared P4-denial and P3-approval-required regression |
| A3.04 offline policy | Freshness, time confidence, tier and version validation | Shared uncertain-clock and P3 offline rejection regression |
| A3.05 audit | Typed ledger, v1 canonical encoder, digest-provider boundary and chain validation | Frozen canonical SHA-256 golden vector executed on Android, JVM and iOS simulator |

The `InstallationId` representation changed from a JVM-inline value class to a Kotlin data class so it can compile in common code. Its value validation and equality semantics remain explicit; no authorization rule was changed.

## 4. Audit canonical encoding and digest portability

`AuditCanonicalEncoder` preserves the existing version-1 byte format:

- big-endian 32-bit and 64-bit integer encoding;
- one-byte boolean encoding;
- Java `DataOutputStream.writeUTF`-compatible modified UTF-8, including null and UTF-16 surrogate code-unit behavior;
- the existing field order and version-1 format identifier.

The encoder is common Kotlin. SHA-256 is provided by platform actuals:

- JVM and Android use a fresh JCA `MessageDigest.getInstance("SHA-256")` instance per call.
- iOS calls Apple's CommonCrypto `CC_SHA256` through the narrow C wrapper `aegis_sha256`.

The common test freezes the canonical sample at 331 bytes and verifies the digest:

`c7663b649b20ae6b39cea6a826705a867dbd6d4ea70abdf18f9684060dc5aff7`

The same common test executes against the JVM, Android unit-test and iOS Simulator actual providers. The audit storage interface and its atomic compare-and-append contract are unchanged. Removing the JVM-only `@Synchronized` annotation does not weaken the storage CAS requirement; cross-process/instance correctness remains the responsibility of the atomic storage implementation.

## 5. Common conformance coverage

New common tests cover:

- identity exact resource scope, tenant mismatch, unavailable membership authority and AI principal rejection;
- policy P4 unconditional denial and P3 requiring separate approval;
- offline policy rejection for uncertain trusted time and P3 offline use;
- canonical audit encoding and exact SHA-256 golden-vector equivalence, including modified UTF edge cases.

Existing JVM regression suites remain in `jvmTest` and continue to run. The Android unit-test tasks execute the shared common tests using the Android actual implementation. iOS Simulator tasks execute the same common tests against the CommonCrypto actual implementation.

## 6. Exact verification evidence

### Shared-core portability CI

Run #46152135064 completed with conclusion **success** on implementation SHA `b6346906ea453ef1d75410e40893845d5c9d9913`.

| Job | Result |
|---|---|
| Android compilation and all four modules' `testDebugUnitTest` | PASS |
| iOS ARM64 device compilation | PASS |
| iOS Simulator ARM64 common tests for all four modules | PASS |
| Ubuntu JVM regression suites | PASS |
| Windows JVM regression suites | PASS |
| macOS JVM regression suites | PASS |

The iOS log confirms successful `cinteropCommonCryptoIosArm64`, `compileKotlinIosArm64`, `cinteropCommonCryptoIosSimulatorArm64` and `iosSimulatorArm64Test` tasks. The complete iOS command ended with `BUILD SUCCESSFUL`.

### Android foundation CI and actual artifact inspection

Run #46152134992 completed with conclusion **success** on the same implementation SHA.

- Artifact ID: `15265774790`
- Archive: `aegis-a307-ci-evidence-b6346906ea453ef1d75410e40893845d5c9d9913.zip`
- Archive SHA-256: `03b10e3a159ca755a09cd05b7df6289bab1cdbbfb8d80e14026256c69f3e2607`
- Archive contents: 18 files, including aggregate and direct CycloneDX JSON/XML SBOMs, Android debug APK and Gitleaks SARIF.
- Six module direct SBOMs parsed successfully with zero unresolved dependency references.
- Aggregate SBOM: 8 components and 21 dependency edges.
- All eight SBOM XML files parsed successfully.
- APK: 911,778 bytes; Android manifest present; six DEX files present.
- Gitleaks SARIF findings: zero.

The archive was downloaded and inspected, not accepted solely from the green workflow status.

## 7. Known limitations and production gates

This proof does not establish production readiness. Still not implemented or verified:

- production encrypted/authenticated protected audit storage on each platform;
- durable atomic offline-policy version stores and trusted-time providers;
- native key custody, user-presence and attestation adapters;
- production identity/membership authority integrations;
- signed iOS application/framework distribution and physical-device installation/testing;
- complete Windows/macOS/Linux desktop application packaging, accessibility, privileged helper IPC or native enforcement;
- device-specific security capability verification;
- VPN adapters, gateways, provider/legal/privacy review and WireGuard dependency gate.

The current Android app and KMP core compilation are not a claim that the full AEGIS product is available on iOS or desktop.

## 8. Acceptance

A3.07 is accepted as a bounded shared-core portability proof because all five portability CI jobs passed on the exact implementation SHA, Android foundation CI passed on the same SHA, the actual Android artifact was inspected, and the common audit golden vector executed across Android/JVM, desktop/JVM and iOS/Native.

**A3.07 status:** ACCEPTED — PROOF-OF-CONCEPT ONLY.  
**Production readiness:** NOT ESTABLISHED.  
**Merge/deployment:** NOT AUTHORIZED.  
**Immediate next workstream:** A3.08 vault scaffolding remains conditional and requires its own bounded scope and security gate.
