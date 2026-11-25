# AEGIS ONE — A2 Controlled Amendment: Risk-Based Security Assurance and Exit Revision

**ID:** AEGIS-A2-ADD-001  
**Version:** 1.0  
**Date:** 2026-11-25  
**Status:** DOCUMENTATION AMENDMENT — A2 EXIT ASSESSMENT RECORDED; OWNER CLOSURE / A3 TRANSITION NOT GRANTED  
**Repository:** `akefadaka-git/aegis_one`  
**Branch:** `docs/a1-baseline-a2-reconciliation`

## 1. Purpose and precedence

This controlled amendment revises the timing and scope of AEGIS ONE security assurance and A2 exit criteria. It corrects the prior sequencing assumption that a full independent security review, complete reference-handset attestation, a live backend/IROKA service, and an application SBOM must all exist before any A3 implementation can begin.

The governing principle is **security assurance throughout the lifecycle, with assurance depth matched to implementation maturity and risk**:
- A1/A2 establish architecture, policy, threat coverage, platform constraints and testable acceptance criteria.
- A3 implements in controlled increments with code review, automated tests, dependency controls and feature-specific gates.
- A4 performs system/device verification, penetration testing and remediation.
- An independent implementation assessment is required against a frozen release candidate before A5 authorization.
- A5 remains a separate Owner authorization gate.

This amendment supersedes conflicting A2 timing and exit statements in the A2 Blocker Resolution and Owner Decision Register, A2 Traceability and Exit Register, A2 Evidence Collection Pack, A2 Independent Security Review Brief, and A2 Security Enhancement Register. Historical entries remain preserved as audit history.

All Owner decisions A2-OD-01 through A2-OD-06 remain APPROVED and binding. This amendment does not amend their substance.

## 2. Assurance stages and required evidence

| Assurance activity | Required stage | Entry/exit expectation |
|---|---|---|
| Threat model, assets, trust boundaries, abuse cases | A1/A2; maintained in A3 | Material threats map to controls and negative tests |
| Architecture and Android platform constraint review | A2 | No unresolved critical architecture contradiction; unsupported capabilities explicitly bounded |
| Targeted specialist design consultation | A2/A3, risk-triggered | Recommended for unresolved high-risk design questions; not a substitute for independent release assessment |
| Component code review | A3 | Review each security-sensitive change before integration |
| Unit, negative, policy and regression tests | A3 onward | Tests assert denial and absence of side effects; unrun tests remain NOT RUN |
| Dependency pinning, license/vulnerability scan and SBOM | At first application dependency introduction in A3; refreshed for release | No dependency graph means NOT APPLICABLE YET, not an A2 failure |
| Device capability discovery and hardware/boot evidence | A3 feature bring-up / A4 | Required before claiming or accepting device-specific enforcement |
| Backend/IROKA operational ownership and live integration | Before the relevant integration work | Mock interfaces may be developed earlier; no production keys/endpoints without separate authorization |
| Family/enterprise legal and privacy review | Before real-user/tenant deployment and before enabling jurisdiction-dependent controls | Synthetic-only development may proceed within approved boundaries |
| Integration, device, recovery and resilience verification | A4 | Evidence against supported device/API/mode matrix |
| Penetration test and independent implementation security assessment | A4 / frozen pre-release candidate | Independent qualified reviewer; exact source/artifact SHA; findings and retests |
| Final release security disposition | Before A5 | Critical findings resolved; high findings resolved or explicitly risk-accepted by Owner with rationale and compensating controls |
| Deployment | A5 | Separate explicit Owner authorization |

A documentation-only architecture review may be commissioned earlier if a specific unresolved design risk warrants it. It is not a universal A2 exit prerequisite and cannot be represented as a source-code audit, penetration test, device attestation or cryptographic verification.

## 3. Revised A2 exit criteria

A2 may be recommended for closure when all of the following are true:

1. **Owner policy decisions:** OD-01 through OD-06 are recorded accurately and no material policy decision required for the proposed A3 scope remains silently unresolved.
2. **Architecture baseline:** identity/device trust, local authorization, command boundary, vault/recovery, offline behavior, Direct Boot, family/enterprise separation and Android management-mode constraints are documented sufficiently to define safe implementation boundaries.
3. **Threat traceability:** each material high-risk threat in the approved A3 scope maps to a preventive/detective control, owner or responsible module, and planned negative test. Test execution is not required at A2.
4. **Unresolved design choices:** cryptographic algorithms, libraries, protocol canonicalization details, trust-root lifecycle and other security-critical choices are explicitly marked as design decisions required before the affected component is implemented; they are not guessed in code.
5. **Capability truthfulness:** unknown handset facts and unsupported Android capabilities are represented as UNKNOWN/UNVERIFIED, with a feature-specific evidence plan. A reference handset need not be fully attested to begin platform-neutral work.
6. **Service boundary:** backend and IROKA interfaces, trust responsibilities and data boundaries are specified sufficiently for local interfaces and mocks. Named operational owners and production services are required before live integration, not before isolated local foundations.
7. **Legal boundary:** family and enterprise legal/privacy review is tracked as a mandatory gate before real-user or tenant activation. It does not block synthetic-only scaffolding that cannot collect or expose real personal data.
8. **Supply-chain sequencing:** dependency/SBOM work is scheduled at dependency introduction and before release; no application dependency graph is falsely reported as scanned.
9. **Stage governance:** A1 formal freeze status is accurately recorded. If formal freeze is still unconfirmed, the Owner must explicitly confirm the A1 baseline as the controlling A3 requirements reference or record a scoped deferral before overall A2 closure.
10. **Owner gate:** the Owner explicitly accepts A2 closure and separately authorizes the A3 stage transition. This amendment and its assessment do not grant either approval.

A2 closure is a design/governance milestone. It does not certify the implementation, device, cryptography, backend, legal compliance, production readiness or deployment.

## 4. Focused A2 exit assessment — 2026-11-25

### 4.1 Overall disposition

**A2: PARTIALLY EXIT-READY; NOT CLOSED.**

The documented architecture and six approved Owner decisions provide a sufficient basis to define a bounded A3 work plan. The previous blanket A3 block is too broad. Several implementation foundations can safely be prepared once the Owner authorizes the A3 transition.

However, the repository evidence does not establish formal A1 freeze, complete threat-to-control-to-test coverage, or all feature-specific technical decisions. Therefore this assessment does not claim unconditional A2 closure or authorize code changes.

### 4.2 Component-level readiness

| Component/workstream | Assessment | Conditions and limits |
|---|---|---|
| Kotlin/Android project skeleton, module boundaries, build conventions | **ELIGIBLE FOR A3** | Keep build reproducible where feasible; no production credentials or signing keys |
| CI, formatting, static-analysis and test harness foundation | **ELIGIBLE FOR A3** | Use available public/free workflows; do not require paid runners or secrets |
| Security status UI and app shell | **ELIGIBLE FOR A3** | Must distinguish VERIFIED, OWNER-REPORTED, UNKNOWN, UNSUPPORTED and DEGRADED; no false protection claims |
| Mode C/F/E domain model and role/consent policy representation | **ELIGIBLE FOR A3** | Implement approved boundaries; family/enterprise roles must never imply OS privileges or private-vault access |
| Local policy evaluator and deny-by-default decision model | **ELIGIBLE FOR A3** | Implement deterministic local decisions and tests; P4 remains disabled; no remote side effects |
| Capability manifest and Android API feature detection | **ELIGIBLE FOR A3** | Report UNKNOWN/UNSUPPORTED truthfully; actual device enforcement claims await device evidence |
| Local audit event schema and protected-storage abstraction | **ELIGIBLE FOR A3** | Synthetic/local data first; no claim of immutable audit; secrets and vault plaintext excluded |
| Typed command schema/parser and validation harness | **CONDITIONAL A3** | Schema-only, no executable remote side effects; cryptographic canonicalization, signer trust, replay and authorization design must be resolved before live command acceptance |
| Vault UI, object model and storage interfaces | **CONDITIONAL A3** | Use synthetic test data and non-sensitive scaffolding only; no production vault encryption or persisted user secrets until cryptographic design/library decisions are reviewed |
| Vault cryptographic engine, recovery and key rotation | **DEFERRED** | Exact reviewed algorithms, parameters, KDF, AEAD, nonce lifecycle, mnemonic standard/library, key invalidation and recovery tests must be settled before implementation |
| Direct Boot bootstrap | **CONDITIONAL A3** | Minimal non-secret bootstrap only; device-protected storage cannot hold vault plaintext, recovery phrase, raw vault keys or credential-bound secrets; platform/device verification required before claims |
| Family policy engine and guardian/member UI | **CONDITIONAL A3** | Synthetic identities and no real child data; approved OD-03 boundaries mandatory; jurisdictional legal review before real-user activation |
| Enterprise policy model and management adapter interfaces | **CONDITIONAL A3** | Mocks and runtime capability adapters may be developed; no Device Owner enrollment, tenant activation or policy enforcement claim before eligible provisioning and device tests |
| Offline policy state machine and clock/revocation test harness | **ELIGIBLE FOR A3** | Enforce OD-05 limits; simulated clock and revocation states; no silent validity extension |
| Backend/IROKA API client and live command integration | **DEFERRED for live integration** | Mock/interface work may proceed; named service owners, API contract, trust-root/key custody, verifier, privacy/retention and incident response required before live integration |
| Device attestation enforcement | **DEFERRED** | Verifier ownership, trust roots, supported device evidence, privacy and server-side validation required |
| Always-on VPN/lockdown and advanced anti-theft/anti-removal | **DEFERRED / DEVICE-CONDITIONAL** | Require documented API capability, mode eligibility, connectivity recovery design, legal/user notice and device verification |
| Real family deployment and enterprise rollout | **DEFERRED** | Legal/privacy review, eligible provisioning, operational ownership, acceptance tests and explicit activation authorization |
| P3 remote action activation | **DEFERRED** | OD-04 policy is approved, but capability implementation/activation needs scoped design, human approval flow, local verification and tests |
| P4 destructive actions | **PROHIBITED IN V1 BY OD-04** | Remain disabled; any future enablement requires a new explicit Owner decision |
| Production signing, production keys, release signing and deployment | **NOT AUTHORIZED** | Separate authorization; A5 remains separate |

“Eligible for A3” means the component has sufficient architectural boundaries to enter controlled implementation after the formal A3 gate. It is not a claim that the code has been implemented or verified.

### 4.3 Remaining A2 / governance items

| Item | Current evidence | Revised treatment |
|---|---|---|
| OD-01 through OD-06 | APPROVED | Closed as Owner policy decisions; preserve scope limits |
| Reference handset software profile | OWNER-REPORTED | Adequate for planning only; independently verify before device-specific acceptance |
| Knox, StrongBox, KeyMint, Verified Boot, Device Owner/work-profile | UNKNOWN / NOT COLLECTED | Feature-specific A3/A4 evidence; not a universal A3 entry blocker |
| Independent security review | NOT PERFORMED; brief prepared | Move mandatory full implementation review to A4/pre-release; targeted design advice optional earlier |
| Backend/IROKA operational owners | UNRESOLVED | Block live integration, not local interfaces, mocks or independent modules |
| SBOM/dependency scan | NOT RUN; no dependency graph | Trigger at first dependency introduction and repeat before release |
| Legal review | NOT PERFORMED | Block real family/enterprise activation and jurisdiction-specific collection, not synthetic scaffolding |
| Threat-to-control-to-test matrix | PARTIAL | Complete A2-scope mapping before closure; execution belongs to A3/A4 |
| A1 formal freeze | NOT CONFIRMED | Owner must confirm controlling baseline or explicitly defer with scope before A2 closure |
| Application tests | NOT RUN; no implementation | Correct status; tests begin with A3 |

## 5. Mandatory A3 engineering controls

Once A3 is separately authorized:

- Every security-sensitive change must have a scoped design note, code review and negative tests.
- Tests must assert both expected denial and absence of side effects.
- No unknown Android capability may be represented as PASS.
- No real child, employee, customer or production data in scaffolding/test environments.
- No production keys, credentials or live endpoints in source control or public CI logs.
- Pin dependencies when introduced; review licenses and known vulnerabilities; generate and retain an SBOM.
- Use synthetic fixtures and mock service interfaces until operational owners and integration controls are approved.
- Security-sensitive cryptographic and command components cannot proceed on guessed algorithms or incomplete trust assumptions.
- Preserve OD-01 through OD-06, including P4 disabled, no provider/operator vault recovery, no enterprise escrow in v1, and no AI approval of P3/P4.
- Maintain a component-level evidence ledger with PASS / PARTIAL / MISSING / UNRESOLVED / UNKNOWN / NOT RUN labels.

## 6. Independent security assessment — revised timing

The existing A2 Independent Security Review Brief is retained as a useful scope and methodology source, but its timing is revised.

**Mandatory independent assessment:** A4, against a frozen implementation/release candidate before A5. The assessment should include source code, architecture, cryptographic and protocol implementation, Android enforcement assumptions, privacy/authority boundaries, dependency/build evidence, negative tests and relevant device/API behavior.

**Optional earlier review:** A targeted independent specialist consultation may be commissioned during A2/A3 for a specific high-risk design question, especially cryptography, command protocol or Android management assumptions. It is not a full release audit and is not a blanket A2 gate.

The independent report must record reviewer qualifications and conflicts, exact repository and commit/artifact hashes, scope, methodology, findings and severity, evidence, remediation, retest and residual risks. Critical findings must be resolved and independently retested before release. High findings must be resolved or explicitly risk-accepted by the Owner with rationale, compensating controls, scope and review date. No finding may be silently omitted.

An AI-generated assessment or author self-review is not independent assurance.

## 7. A2 exit decision and next gate

**Focused assessment result:** The previous universal A3 blockers are reclassified. The A3 workstream eligibility matrix above identifies safe foundations and conditional/deferred capabilities. The design baseline is sufficiently mature to plan a bounded A3 tranche, but the complete A2 exit record still requires:
1. completion of the threat-to-control-to-test mapping for the proposed first A3 tranche;
2. confirmation or scoped deferral of A1 formal freeze by the Owner;
3. reconciliation of all controlling registers with this amendment; and
4. explicit Owner acceptance of A2 closure and separate A3 transition.

**No implementation has been started by this assessment.** No merge, release, deployment, handset modification, enrollment, production key generation, live service provisioning or production signing is authorized. A5 remains separate.

## 8. Progress snapshot at amendment

| Phase | Status | Progress |
|---|---|---:|
| A1 | Documentation complete; formal freeze not confirmed | 100% documented |
| A2 | Amendment and focused assessment recorded; closure pending remaining exit items and Owner decision | ~95% estimated; OPEN |
| A3 | Component eligibility assessed; implementation not started | 0% |
| A4 | Not started | 0% |
| Independent implementation assessment | Deferred to A4/pre-release | NOT PERFORMED |
| A5 | Separate gate; not authorized | 0% |

**Production readiness: NOT ESTABLISHED.**

**Immediate next step:** Complete the first-tranche threat/control/test traceability and present the A1 baseline/freeze and A2 closure decisions to the Owner. Do not begin A3 code until the Owner separately authorizes the stage transition.
