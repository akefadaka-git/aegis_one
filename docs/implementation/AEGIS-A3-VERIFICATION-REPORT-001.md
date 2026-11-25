# AEGIS ONE — A3 First-Tranche Verification Report

**ID:** AEGIS-A3-VER-001  
**Date:** 2026-11-25  
**Repository:** `akefadaka-git/aegis_one`  
**Implementation branch:** `docs/a1-baseline-a2-reconciliation`  
**Verified source commit:** `4f8ff12297a9c92d0676a89d95b88920e6fb8c98`  
**Controlling plan:** `docs/implementation/AEGIS-A3-BOUNDED-IMPLEMENTATION-PLAN.md` (AEGIS-A3-PLAN-001)  
**Authorization:** Owner authorization for A3.0–A3.5; no new branch, no main changes, no merge, no deployment or handset mutation.

## 1. Executive result

**A3.0–A3.5: IMPLEMENTED; VERIFICATION PARTIAL.** The latest branch-head CI run completed successfully. The build, unit-test task, Android lint, debug APK assembly and full-history Gitleaks scan passed. A CycloneDX SBOM was generated and retained, but the build log reports that the SBOM generator could not resolve the app-to-`:platform:android-capabilities` project dependency in `implementationDependenciesMetadata`. Therefore SBOM completeness is not established and A3.0 remains PARTIAL. The other first-tranche workstreams are accepted as bounded local scaffolding only, not as production security controls.

**Production readiness: NOT ESTABLISHED.** This report does not authorize A4, A5, release, deployment, installation, enterprise enrollment, production keys or activation of deferred features.

## 2. Repository and branch integrity

- Branch `docs/a1-baseline-a2-reconciliation`: `4f8ff12297a9c92d0676a89d95b88920e6fb8c98`
- `main`: `570b6fe5abfcf0d7b369af63e211ab937bfe4024` (unchanged)
- Branch inventory at verification: only the existing implementation branch and `main`; no new branch created.
- No merge, release, deployment, device installation, enrollment or handset/security-setting changes occurred.
- Latest source commit fetch-back and branch refs were verified through GitHub.

## 3. CI evidence

**Workflow:** [Android foundation CI — run #45555662296](https://github.com/akefadaka-git/aegis_one/actions/runs/45555662296)  
**Run conclusion:** `success`  
**Job:** `verify`, job ID `139006973899`, conclusion `success`  
**Source SHA:** `4f8ff12297a9c92d0676a89d95b88920e6fb8c98`

| Check | Result | Evidence |
|---|---|---|
| Checkout full Git history | PASS | `fetch-depth: 0`; checked out exact branch-head SHA |
| Gitleaks | PASS | 70 commits scanned; 256,699 bytes scanned; no leaks found |
| Gradle build | PASS | `BUILD SUCCESSFUL`; 166 actionable tasks |
| Unit-test task | PASS | `test` task completed successfully across applicable modules |
| Android lint | PASS | `:platform:android-capabilities:lintDebug` and `:app:lintDebug` completed |
| Debug APK assembly | PASS | `:app:assembleDebug` completed |
| CycloneDX SBOM generation | PARTIAL | Root task completed and file exists; one project dependency could not be resolved in app metadata |
| CI evidence upload | PASS | Three files uploaded as artifact ID `14994878584` |

### Secret scan details

Gitleaks v8.24.3 scanned all 70 commits using the checked-in `.gitleaks.toml`. The retained SARIF is version 2.1.0 with zero results. The historical exception is limited to the `generic-api-key` rule, the `key/revocation refresh` phrase, the single offline-protection contract path, and historical commit `fe00cb773c63eb2c983a6e2e30bc620808eb8073`. It is not a repository-wide or rule-wide suppression.

### Retained artifact

[Download CI evidence artifact](https://github.com/akefadaka-git/aegis_one/actions/runs/45555662296/artifacts/14994878584)

- Artifact: `aegis-one-ci-evidence-4f8ff12297a9c92d0676a89d95b88920e6fb8c98`
- Artifact ZIP SHA-256: `9265d4baa10d9b20c8545eb6f4e4d70fabb13aa113d36b84c29f0c268b928e2e`
- Debug APK: 881,906 bytes; SHA-256 `4366c71bfb344bbc659afd33aed277654351fc42165971d91e1f89fb39b9a28b`
- CycloneDX JSON: 385,626 bytes; CycloneDX 1.6; 167 components and 168 dependency entries; SHA-256 `789e38415d7a804d67ad0ee3de02af4af6152d2615b7eddf465a145805fa0664`
- Gitleaks SARIF: 45,825 bytes; zero findings; SHA-256 `f1cc1fc5bf34d5e9643655ac6480ff4681e27aa9c2c639f03067fc7ea8595e3d`

**SBOM limitation:** CycloneDX logged: `The SBOM cannot describe a dependency of implementationDependenciesMetadata: Could not resolve project :platform:android-capabilities`; Gradle could not choose between the Android library's debug and release variants for that metadata configuration. The BOM file is syntactically valid and retained, but its project dependency graph must be corrected and regenerated before treating the inventory as complete. The build also reported Kotlin `jvmTarget` deprecation and Gradle configuration-time dependency-resolution warnings; these did not fail CI.

## 4. First-tranche workstream assessment

| Workstream | Status | Verified scope and limitation |
|---|---|---|
| A3.0 Build foundation | PARTIAL | Native Kotlin/Android shell, Gradle 8.13/JDK 17, modular build and branch-scoped CI build successfully. SBOM dependency graph has the unresolved project dependency described above. |
| A3.1 Security state | VERIFIED-CLOSED (scaffold) | Typed capability status/provenance; unknown, unsupported, owner-reported and test-fixture evidence cannot assert runtime enforcement. Three unit tests pass. |
| A3.2 Mode and identity boundary | VERIFIED-CLOSED (scaffold) | Consumer/family/enterprise modes, principal kinds, tenant/resource scope and consent types; cross-mode and tenant mismatch denial covered by policy tests. No live identity provider or authentication integration. |
| A3.3 Local policy evaluator | VERIFIED-CLOSED (scaffold) | Deterministic deny-by-default evaluator; AI and P4 denied; P3 requires online state, scoped/unexpired/non-replayed human approval and verifier success. Seven policy tests pass. The production signature verifier is not implemented; default verifier fails closed. No P3 action execution is enabled. |
| A3.4 Offline policy | VERIFIED-CLOSED (scaffold) | Policy validator enforces clock confidence, scope, version, signature interface and P0/P1 seven-day / P2 24-hour maximum freshness; P3/P4 are rejected offline. Four tests pass. Production signature verification and trust roots are not implemented. |
| A3.5 Audit foundation | VERIFIED-CLOSED (scaffold) | Typed/minimized event schema, chained SHA-256 records, sequence-gap/tamper detection and refusal to append to an invalid chain. Four tests pass. No production protected/transactional store; this is not immutable storage. |

The four reviewed unit-test suites contain 18 test methods in total (3 security-state, 7 policy, 4 offline-policy, 4 audit). The CI `test` task completed successfully. The tests use deterministic fixtures; they do not establish production cryptographic verification, protected persistence or device-level enforcement.

## 5. Source evidence at the verified commit

| File | Blob SHA |
|---|---|
| `core/security-state/src/main/kotlin/org/akefadaka/aegis/security/SecurityState.kt` | `2119d88916bc35ba8eb16b72d9620ba15c60165f` |
| `core/policy/src/main/kotlin/org/akefadaka/aegis/policy/PolicyEvaluator.kt` | `24249ae2efcd0b2f56446db1a1e6d4704551f678` |
| `core/offline-policy/src/main/kotlin/org/akefadaka/aegis/offline/OfflinePolicy.kt` | `cf22bf89a608b339004f7f855b94dfa8c05e73ef` |
| `core/audit/src/main/kotlin/org/akefadaka/aegis/audit/AuditLedger.kt` | `8a8999c77fc0c0b68b7fc7a63fe647b34e731327` |
| `build.gradle.kts` | `ee303b6021005887e2abe1c1bd4daebf208d32d3` |
| `.gitleaks.toml` | `2cdadd9b0d67a983e82ed7d504fb6f0340147b20` |

## 6. Explicitly deferred / not implemented

- Production vault cryptography, recovery, key hierarchy, key rotation or production keys.
- Production approval-signature and cached-policy signature verification, trust roots, key custody or revocation.
- Remote command dispatch/execution and live IROKA/backend integration.
- Production protected audit storage or multi-process transactional writer.
- Direct Boot implementation, hardware attestation enforcement, StrongBox/KeyMint/Knox verification.
- Device Owner provisioning, VPN/lockdown, anti-theft enforcement or OEM/system privileges.
- Real family or enterprise activation; real identities or user data.
- P3 remote execution and all P4 actions (P4 remains disabled).
- Independent implementation security assessment (mandatory at A4/pre-release).

## 7. Required next actions

1. Resolve the CycloneDX app-to-platform project dependency variant ambiguity; regenerate the SBOM and verify the project dependency graph and component inventory.
2. Refresh this verification record with the corrected SBOM artifact and exact hashes. Do not mark A3.0 fully closed until this is verified.
3. Preserve A3.1–A3.5 as bounded scaffold closures; do not reinterpret them as production security enforcement.
4. Continue only within the already-authorized A3.0–A3.5 scope. A3.6–A3.9 remain outside this authorization.
5. A4 verification and independent implementation security assessment remain a separate stage; A5 requires separate Owner authorization.

## 8. Stage status

- A1: VERIFIED-CLOSED / FROZEN.
- A2: VERIFIED-CLOSED.
- A3.0: PARTIAL.
- A3.1–A3.5: VERIFIED-CLOSED as bounded scaffolding only.
- Overall A3 first tranche: PARTIAL; not fully closed.
- A4: NOT STARTED.
- A5: NOT AUTHORIZED.
- Production readiness: NOT ESTABLISHED.


## 9. Verification addendum — SBOM graph review and follow-up correction (2026-11-25)

This addendum supersedes the earlier assumption that the only SBOM defect was the Android metadata-configuration variant ambiguity. The first regenerated SBOM from run #45556424411 was inspected directly; its graph was structurally referentially consistent but too broad to serve as a precise debug-APK dependency inventory.

### 9.1 Run #45556424411 — build passed, SBOM scope review PARTIAL

**Workflow:** [run #45556424411](https://github.com/akefadaka-git/aegis_one/actions/runs/45556424411)  
**Source SHA:** `0755b77b4271f9302bccfc4d61ca22e57c56a1c7`  
**Job:** `verify`, job ID `139009456178`, conclusion `success`  
**Artifact:** [CI evidence artifact 14995753427](https://github.com/akefadaka-git/aegis_one/actions/runs/45556424411/artifacts/14995753427)  
**Artifact ZIP SHA-256:** `eff40b6117ebf09693b9cb53a85e3b6c9829bc98da9c2014f6e1c1706b9eba18`

The build/test/lint/debug APK/SBOM task and full-history Gitleaks scan completed successfully. Gitleaks scanned 72 commits and reported no leaks. The evidence ZIP contained:
- CycloneDX JSON: 385,626 bytes; SHA-256 `a6fb054f5381eb05bbbec560fbf014759625e053a07c07dccf3fbfa97dfc64bf`.
- Debug APK: 881,906 bytes; SHA-256 `20948098456ba4dd147e3bddc9a800574ad9a486020b57cc6536e3d84b2e8b65`.
- Gitleaks SARIF: 45,825 bytes; SARIF 2.1.0, zero results; SHA-256 `f1cc1fc5bf34d5e9643655ac6480ff4681e27aa9c2c639f03067fc7ea8595e3d`.

### 9.2 Direct inspection of the generated CycloneDX document

The JSON parsed successfully as CycloneDX 1.6. Structural graph checks found:
- 167 components and 168 dependency nodes (including the metadata/root component).
- 562 dependency edges.
- Zero missing dependency target references.
- Zero dependency cycles.
- Zero components marked with `cdx:maven:package:metadata-unresolved`.
- 153 components with hashes and 159 with license data.
- The metadata root points to eight project-level nodes; all component references are reachable from the metadata root.

However, the project-level graph contained nine AEGIS project nodes: the root, app, five actual included feature/platform modules, and synthetic `:core` / `:platform` parent nodes. The latter two have no outgoing dependencies and generated CycloneDX warnings that they were constructed as disconnected roots. The app node contained 26 direct dependency edges, including Android test-provider/tooling and Kotlin compiler/build-tool components, because the plugin's default empty `includeConfigs` scans every resolvable configuration. No components were marked as test-only by the default test-configuration pattern. Thus, structural referential integrity passed, but the component boundary was over-inclusive and not sufficiently precise for the debug APK. This is a scope/quality issue, not evidence of an unresolved library or a missing graph reference.

### 9.3 Corrective configuration submitted

Commit `9e089b361a628ef0ac216b5b49a69c8d6adb5ab4` changes root `build.gradle.kts` (blob `939bc8ee6f2fdadc49608caa6d15accc9ea5b892`) to restrict each Direct SBOM to:
- Android app and Android capabilities module: `debugCompileClasspath` and `debugRuntimeClasspath`, matching the debug APK built by CI.
- Kotlin/JVM modules: `compileClasspath` and `runtimeClasspath`.
- `failOnUnresolvedDependencies = true` remains applied to every Direct SBOM task.

This removes test/build-tool-only configurations from the intended inventory boundary while preserving the actual debug app/module classpaths. It also avoids silently accepting unresolved dependencies. The CycloneDX plugin's documented `includeConfigs` and `failOnUnresolvedDependencies` controls support this approach.

**Follow-up workflow:** [run #45556857300](https://github.com/akefadaka-git/aegis_one/actions/runs/45556857300)  
**Source SHA:** `9e089b361a628ef0ac216b5b49a69c8d6adb5ab4`  
**Job ID:** `139010882253`  
**Status at the time this addendum was written:** `in_progress`; Gitleaks step passed, Gradle build/test/lint/APK/SBOM step still in progress; artifact not yet available. Therefore the corrected SBOM graph has not yet been independently inspected and is not accepted in this addendum.

### 9.4 Updated stage decision

- A3.0: **PARTIAL** — build foundation passes; corrected SBOM scope awaits run #45556857300 completion and artifact-level graph inspection.
- A3.1–A3.5: **VERIFIED-CLOSED as bounded scaffolds only**, subject to the functional/security limitations in this report.
- Overall A3 first tranche: **PARTIAL / NOT CLOSED**.
- Production readiness: **NOT ESTABLISHED**.
- No A4/A5, merge, deployment, release, device installation or enrollment authorization is created by this addendum.


## 10. Verification addendum — narrowed SBOM inspection (2026-11-25)

### 10.1 Run #45556857300 — CI PASS, aggregate SBOM NOT ACCEPTED

**Workflow:** [Android foundation CI — run #45556857300](https://github.com/akefadaka-git/aegis_one/actions/runs/45556857300)  
**Source SHA:** `9e089b361a628ef0ac216b5b49a69c8d6adb5ab4`  
**Branch:** `docs/a1-baseline-a2-reconciliation`  
**Job:** `verify`, job ID `139010882253`, conclusion `success`  
**Artifact:** [CI evidence artifact 14996495428](https://github.com/akefadaka-git/aegis_one/actions/runs/45556857300/artifacts/14996495428)  
**Artifact ZIP SHA-256:** `baafb4f11dc92d6b88f32e5e5c4c87df67b685a3d444c4f978e7daa079899a06`

CI completed the following successfully:
- Gitleaks v8.24.3 scanned 73 commits / approximately 266,810 bytes; no leaks found.
- `./gradlew --no-daemon clean test lint assembleDebug cyclonedxBom` completed with `BUILD SUCCESSFUL`.
- Unit-test tasks, Android lint, debug APK assembly and SBOM generation passed.
- Evidence artifact upload passed.

Artifact file evidence:
- `build/reports/cyclonedx/bom.json`: 11,722 bytes; CycloneDX 1.6; SHA-256 `a86c0403d190a21364ab706ccafec4cc58abb343cb785361f9623fa5134002b5`.
- Debug APK: 881,906 bytes; SHA-256 `b7417fdedafd2dd5c9ce333f9f7dfde6c913e94ffe3f3901b796f20cbbcc9031`.
- Gitleaks SARIF: 45,825 bytes; SARIF 2.1.0; zero results; SHA-256 `f1cc1fc5bf34d5e9643655ac6480ff4681e27aa9c2c639f03067fc7ea8595e3d`.

### 10.2 Aggregate SBOM graph findings

The CycloneDX JSON parsed successfully. Direct graph inspection found:
- 10 components; 6 dependency nodes; 7 dependency edges.
- Zero missing dependency target references and zero cycles.
- The metadata root is `pkg:maven/org.akefadaka.aegis/AEGIS-ONE@0.1.0?project_path=%3A`.
- Only one dependency node was reachable from the metadata root; six dependency nodes were disconnected from it.
- The component inventory contains the root, app, parent `:core` / `:platform`, five actual feature/platform modules, and two external libraries (`kotlin-stdlib`, `annotations`).
- The app component is present but the aggregate dependency graph does not connect it to its declared project dependencies. The checked-in `app/build.gradle.kts` declares `:core:security-state`, `:core:policy`, `:core:offline-policy`, `:core:audit`, and `:platform:android-capabilities` as implementation project dependencies.
- No unresolved dependency metadata markers were found. The narrower inventory no longer contains the earlier test-provider/compiler/build-tool over-inventory, but the missing app-to-project edges mean the aggregate BOM is under-inclusive and disconnected.
- The document has two components with hash/license data; the eight project/module components do not carry external package hashes/licenses, which is expected for local project modules but reinforces that this aggregate is not a complete packaged-library inventory by itself.

The CI log confirms CycloneDX warnings that root, app, `:core`, `:platform`, and `:platform:android-capabilities` were not found in the graph and were constructed as disconnected roots. The app's declared project dependency edges are therefore not represented in the aggregate BOM.

### 10.3 Decision and corrective direction

The regex-limited configuration removed the prior broad test/build-tool inventory, but did not yield a connected aggregate dependency graph. Do not treat the absence of unresolved markers or a green Gradle task as proof of SBOM completeness.

Next bounded A3.0 correction: preserve each subproject's Direct CycloneDX BOM as separate CI evidence alongside the aggregate BOM, then inspect the app module's own debug BOM to determine whether it records the actual app-to-feature-module and external runtime/compile dependency relationships. If module-level BOMs are still disconnected or omit declared project dependencies, correct the generation/aggregation method rather than accepting the artifact. The CI evidence upload currently retains only the root aggregate JSON, APK and SARIF, so it is insufficient to perform that module-level reconciliation.

### 10.4 Updated stage decision

- A3.0: **PARTIAL** — build/test/lint/APK/Gitleaks pass; aggregate SBOM generated but disconnected and under-inclusive; module-level dependency reconciliation remains outstanding.
- A3.1–A3.5: **VERIFIED-CLOSED as bounded scaffolds only**, with the implementation limits already recorded above.
- Overall A3 first tranche: **PARTIAL / NOT CLOSED**.
- A4: **NOT STARTED**.
- A5: **NOT AUTHORIZED**.
- Production readiness: **NOT ESTABLISHED**.

Repository refs rechecked after run #45556857300: implementation branch remained at `d218df37618e9afdf7bb6b0e27aafaa751ac2f47` at the time of this report fetch; `main` remained `570b6fe5abfcf0d7b369af63e211ab937bfe4024`. No new branch, main modification, merge or deployment occurred.
