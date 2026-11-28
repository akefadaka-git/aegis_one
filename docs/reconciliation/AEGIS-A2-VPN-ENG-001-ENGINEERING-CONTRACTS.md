# AEGIS ONE — A2 VPN Engineering Contracts and Cross-Platform Reconciliation

**Contract ID:** AEGIS-A2-VPN-ENG-001  
**Version:** 1.0  
**Date:** 2026-11-28  
**Status:** CONTRACT BASELINE RECORDED — IMPLEMENTATION-READINESS GATES OPEN  
**Parent architecture:** AEGIS-A2-AMD-002 and AEGIS-A2-AMD-003  
**Product:** One AEGIS ONE product; Personal and Enterprise VPN service modes  
**Protocol:** WireGuard first  
**Infrastructure:** Hybrid (AEGIS-operated, approved partner-operated, customer-operated)  
**Repository:** akefadaka-git/aegis_one

## 1. Purpose and effect

This document converts the Owner-approved VPN direction into versioned engineering contracts for the VPN domain, endpoint, control plane, gateway registry, peer lifecycle, policy, privacy, platform adapters and conformance tests. It supplements, and does not replace, AEGIS-A2-AMD-002, AEGIS-A2-AMD-003, AEGIS-A1-AMD-001 or the frozen A1 v1.0 baseline.

The contracts are implementation-facing specifications. They do not claim that any API, server, gateway, tunnel, key store, platform adapter or test harness already exists. They do not authorize source-code changes, production provisioning, paid services, production credentials, live key issuance, merge, release or deployment.

## 2. Normative language and shared invariants

The terms MUST, MUST NOT, SHOULD and MAY express requirements.

1. One product contains separate Personal VPN and Enterprise VPN contexts. Family remains a governed Personal sub-context.
2. Personal, Family and Enterprise authority, identity bindings, device registrations, keys, routes, DNS policy, gateway eligibility, audit visibility and retention MUST remain isolated.
3. AEGIS local policy is authoritative. IROKA and other AI services are advisory; they cannot approve, authorize or execute a protected VPN action.
4. Deny by default. Missing, stale, revoked, ambiguous, untrusted or unavailable authority MUST deny provisioning or stop/restrict the affected operation according to the approved local failure policy.
5. No private WireGuard key, preshared key, recovery secret or tunnel secret may be sent to the control plane, telemetry, audit or logs.
6. Endpoint key generation is unique per installation and per authority context. A Personal key MUST NOT be reused for Enterprise or another tenant.
7. WireGuard cryptography and handshake implementation MUST come from a maintained, reviewed implementation. AEGIS MUST NOT implement protocol cryptography itself.
8. A capability is active only when the platform adapter provides current, attributable evidence. Unsupported features MUST remain visibly unsupported.
9. No browsing history, destination history, DNS query history or packet payload is collected by default.
10. A production gateway, paid provider, subscription, public capacity, production trust root or production key issuance requires a separate authorization and operational readiness gate.

## 3. Component and trust-boundary model

| Component | Responsibility | Trust boundary / prohibited authority |
|---|---|---|
| AEGIS VPN domain | Platform-neutral desired state, connection state, policy input, typed errors and health | No OS privilege; no direct network mutation |
| AEGIS local policy engine | Validate principal, mode, tenant, scope, consent, policy freshness, capability and action risk | Sole AEGIS decision point; AI cannot override |
| Native platform adapter | Obtain OS permission/entitlement and invoke supported tunnel APIs | May execute only a locally authorized operation; reports capability evidence |
| WireGuard engine | Established WireGuard tunnel implementation | No custom crypto; secrets stay in protected endpoint boundary |
| Control plane | Authenticate devices, return eligible gateway metadata, issue short-lived provisioning authorization, process revocation and policy versions | Never receives private keys or packet content; cannot bypass local policy |
| Gateway registry | Authenticated gateway identity, operator, region, protocol, status, trust evidence and policy eligibility | Discovery is not trust; stale/revoked entries are ineligible |
| Gateway/data plane | Terminate WireGuard peers and route only authorized traffic | Sees network metadata necessary to operate; no default content/DNS logging |
| IROKA | Risk analysis, correlation and recommendation | No authority to start/stop tunnel, change route/DNS, select provider or enable lockdown |

## 4. Canonical domain contracts

The implementation MUST define versioned typed equivalents of these records. Names may be mapped to repository conventions after A3.0 inventory; field semantics may not be weakened.

### 4.1 AuthorityContext

Required fields:
- `context_id`: opaque stable identifier for this authorization context.
- `mode`: `PERSONAL` or `ENTERPRISE`; Family is represented by a Personal policy relationship, never an Enterprise tenant.
- `principal_id`: authenticated user/service principal.
- `device_installation_id`: per-installation identifier; not IMEI, serial or MAC.
- `tenant_id`: required only for Enterprise; absent for Personal.
- `membership_version`: current membership/assignment version; required for Enterprise.
- `policy_version`: active signed policy version.
- `consent_ref`: current user consent evidence for user-mediated operations.
- `issued_at`, `expires_at`, `nonce`: bounded freshness and replay protection.
- `authority_status`: current verified status, never inferred from cached UI state.

Validation: mode/tenant consistency, principal, device binding, membership version, policy version, consent, expiry, nonce and revocation MUST be revalidated immediately before peer provisioning or a protected tunnel-policy change. Any unavailable validation dependency returns a typed denial, not an optimistic allow.

### 4.2 VpnProfile

Required fields:
- `profile_id`, `profile_version`, `mode`, `context_id`.
- `gateway_id`, `gateway_registry_version`, `operator_class`, `region_id`.
- `protocol` = `WIREGUARD` for v1.
- `routing_mode` = `PERSONAL_FULL_TUNNEL`, `PERSONAL_SPLIT_TUNNEL`, or `ENTERPRISE_PRIVATE_ROUTES`.
- `allowed_routes`, `excluded_routes`, `dns_policy_ref`, `ipv6_policy`, `mtu_policy`.
- `peer_id`, `public_key_fingerprint`, `key_generation`; never private key material.
- `created_at`, `not_before`, `expires_at`, `revocation_version`.
- `capability_requirements`, `consent_requirements`, `approval_ref` where applicable.

The profile MUST be signed/authenticated by an approved issuer, bound to one context and one device installation, version-monotonic, expiring, replay-resistant and rejected if any referenced registry/policy/revocation version is stale.

### 4.3 GatewayDescriptor

Required fields:
- `gateway_id`, `registry_version`, `operator_class` (`AEGIS`, `PARTNER`, `CUSTOMER`).
- `operator_id`, `region_id`, `jurisdiction`, `endpoint`, `protocols`.
- `trust_status`, `trust_evidence_refs`, `last_verified_at`, `valid_until`.
- `service_modes`, `tenant_binding` for customer gateways, `health_status`.
- `privacy_notice_version`, `retention_profile_id`, `incident_contact_ref`.

Eligibility MUST check operator trust, region/jurisdiction, service mode, tenant binding, protocol, health, expiry, policy and user/admin choice. A registry record is not proof of independent security or service availability. Unknown, expired, revoked or mismatched entries are ineligible. Partner substitution/failover MUST be explicit and policy-authorized; no silent operator change.

### 4.4 TunnelState and CapabilityEvidence

Tunnel states:
`UNAVAILABLE`, `STOPPED`, `AWAITING_CONSENT`, `AUTHORIZING`, `PROVISIONING`, `CONNECTING`, `CONNECTED`, `DEGRADED`, `RECONNECTING`, `STOPPING`, `FAILED`, `REVOKED`.

Every transition records a reason code, timestamp, profile version and evidence reference. Invalid transitions are rejected. `CONNECTED` requires OS adapter confirmation and a successful engine/interface health check; UI intent alone is insufficient.

Each capability result includes `capability_id`, platform/API version, state, evidence source, permission/entitlement/enrollment prerequisites, observed_at, expires_at and limitations. Supported states remain VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED, REQUIRES_PERMISSION and REQUIRES_ENROLLMENT. Only fresh VERIFIED evidence can satisfy a hard enforcement prerequisite.

### 4.5 Typed errors

At minimum:
`AUTHORITY_MISSING`, `AUTHORITY_STALE`, `MEMBERSHIP_REVOKED`, `POLICY_STALE`, `CONSENT_REQUIRED`, `APPROVAL_REQUIRED`, `CAPABILITY_UNSUPPORTED`, `PERMISSION_REQUIRED`, `ENROLLMENT_REQUIRED`, `GATEWAY_UNTRUSTED`, `GATEWAY_UNAVAILABLE`, `PROFILE_EXPIRED`, `PEER_REVOKED`, `KEY_UNAVAILABLE`, `DNS_POLICY_FAILED`, `ROUTE_POLICY_FAILED`, `TUNNEL_FAILED`, `RECOVERY_REQUIRED`, `CONTROL_PLANE_UNAVAILABLE`.

Errors MUST be safe for display and MUST NOT include secrets, raw private addresses beyond what is needed for diagnostics, DNS queries or traffic content.

## 5. Control-plane API contract

The following logical operations are required. HTTP paths are canonical proposed v1 paths; transport/versioning may be adapted in the implementation ADR without changing semantics.

| Operation | Request binding | Required response / side effect |
|---|---|---|
| `POST /v1/vpn/devices/register` | Authenticated principal, installation ID, mode, tenant/membership version where Enterprise, endpoint public key fingerprint, capability summary | Device registration ID and current authority/policy version; idempotent; no private key |
| `GET /v1/vpn/gateways` | Mode, region preference, protocol, tenant scope, registry version | Eligible GatewayDescriptors only; no implicit trust based on discovery |
| `POST /v1/vpn/peers/provision` | Device registration, context, gateway ID/version, public key, profile intent, nonce, consent/approval references | Short-lived signed peer/profile authorization; server-side peer create is idempotent and scoped |
| `POST /v1/vpn/peers/{peer_id}/revoke` | Current authority, reason code, expected peer version, idempotency key | Revocation receipt, monotonic revocation version, gateway cleanup status |
| `POST /v1/vpn/keys/rotate` | Public key only, existing peer, new key generation, proof of current device authority | New peer/profile version and old-peer revocation/overlap deadline |
| `GET /v1/vpn/devices/{id}/status` | Device/context authority | Minimized current status, policy/registry/revocation versions |
| `POST /v1/vpn/telemetry/health` | Authenticated device, bounded event schema, consent/policy scope | Acknowledgment; operational health only, no traffic content or browsing history |

API-wide requirements:
- TLS with server identity validation; authenticated, scoped clients; administrator MFA/strong auth for administrative actions.
- Canonical request encoding, bounded body size, strict schema validation, nonce/idempotency handling and replay rejection.
- Current identity, membership, policy, registry and revocation state are checked on every state-changing request.
- Tenant and mode scope are derived from authenticated authority, not trusted from caller-supplied fields alone.
- Peer creation/revocation/rotation return attributable receipts with request ID, device/context, gateway, peer, policy and registry versions, result, timestamp and issuer identity.
- Control-plane outage never causes a cached allow for new provisioning or authority-changing operations. Existing tunnel continuation follows explicit signed offline policy and OS behavior; it does not renew or broaden authority.
- No endpoint accepts private keys, preshared keys, configuration secrets or packet payloads.

## 6. Key and peer lifecycle contract

1. Generate a cryptographically random unique private key on the endpoint for each installation + authority context + peer generation.
2. Store it using the strongest documented platform-protected facility available. Record capability evidence; do not claim hardware-backed/non-exportable storage without verification.
3. Send only public key and non-secret proof/metadata to provisioning. Preshared keys, if a separately reviewed design ever requires them, must also remain outside logs and control-plane observability; v1 should avoid them unless justified.
4. Bind peer to installation, context, mode, tenant (Enterprise), gateway, profile version and expiry.
5. Rotation creates a new generation, verifies the new peer, then revokes the old peer within a bounded overlap. No indefinite dual-active peers.
6. Lost device, user logout, tenant offboarding, membership removal, device replacement, key compromise or administrator revocation triggers monotonic revocation and server-side peer cleanup.
7. Revoked identity/key generations are never reactivated. Reinstall/replacement creates a new installation identity and new keypair.
8. If control-plane revocation cannot be confirmed, show pending revocation/degraded state; do not report complete. Local disable is immediate where possible, while server cleanup remains separately tracked.
9. No provider/operator recovery of Personal VPN private keys. This does not alter OD-01 user-controlled vault recovery or OD-02 prohibition on Enterprise vault-key escrow.

Required receipts: `KEY_GENERATED`, `PEER_PROVISION_REQUESTED`, `PEER_PROVISIONED`, `KEY_ROTATION_STARTED`, `OLD_PEER_REVOKED`, `DEVICE_REVOKED`, `PEER_CLEANUP_CONFIRMED`, `REVOCATION_PENDING`. Receipts contain public-key fingerprints only.

## 7. Routing, DNS and failure semantics

### 7.1 Routing profiles

- Personal full tunnel: default route through the selected VPN gateway only after explicit user consent and verified OS routing capability.
- Personal split tunnel: explicit include/exclude app or route list only where the OS supports the requested semantics. Display exact limitations.
- Enterprise private routes: only organization-approved CIDRs/domains/resources and required DNS; no implicit full-device personal traffic capture.
- Full-tunnel Enterprise mode, traffic inspection and content monitoring require separate transparent policy, lawful basis, platform authorization and user notice; not default.
- Personal traffic MUST NOT be sent through customer Enterprise gateways. Enterprise traffic MUST NOT be sent through Personal gateways. Mixed contexts require OS-supported route separation and explicit policy; otherwise deny concurrent ambiguous configuration.

### 7.2 DNS and IP

- DNS policy is versioned and bound to the VpnProfile. DNS resolver identity, routing and fallback behavior are explicit.
- No silent fallback to an unapproved resolver when a protected DNS policy fails.
- Test IPv4 and IPv6 separately; IPv6 MUST be tunneled, explicitly blocked under a user-approved supported policy, or reported as an uncovered leak risk. It must not be silently ignored.
- Test DNS leak, split-DNS, private-domain resolution, captive portal behavior, MTU/fragmentation, roaming, sleep/resume and network changes.
- Never record DNS query content by default. Diagnostic capture is synthetic, bounded and consented.

### 7.3 Failure and recovery

- Each profile declares `failure_mode`: `FAIL_OPEN` or `FAIL_CLOSED`, permitted only where supported and explicitly consented.
- Kill switch/lockdown is a separately consented capability, not implied by VPN activation. It requires platform verification and recovery procedure.
- Do not unexpectedly strand a user offline when lockdown was not explicitly enabled.
- Never silently bypass an enabled lockdown to restore connectivity.
- On tunnel loss: transition to DEGRADED/RECONNECTING/FAILED, apply the declared supported policy, notify user/admin as scoped, and preserve evidence.
- On OS restart, clock uncertainty, stale profile, gateway outage or control-plane outage, apply expiry and offline policy; do not mint new authority from cached state.
- Recovery must allow a user to disable/repair the tunnel through the platform-supported recovery path without weakening unrelated AEGIS protections.

## 8. Personal, Family and Enterprise policy contracts

| Control | Personal | Family | Enterprise |
|---|---|---|---|
| Primary authority | Device owner/user | User + transparent guardian policy | Tenant administrator within enrollment and user/legal boundary |
| Gateway choice | User-selected eligible gateway | Member/guardian policy as disclosed; age-appropriate | Tenant-approved customer/AEGIS/partner gateway |
| Routing | Full tunnel or optional supported split tunnel | Explicit policy and member disclosure | Private routes by default; full tunnel separately gated |
| Key custody | Endpoint-generated; user controls account access | Member device key; guardian never receives private key | Endpoint-generated; tenant receives public peer metadata only |
| Admin visibility | User's own connection health/usage | Minimal status; no content/history | Assignment, policy, connection health and security events only |
| Content/DNS logging | Off by default | Off by default | Off by default; exception requires separate lawful, transparent policy |
| Revocation | User/device/session revocation | Guardian relationship change cannot silently expose content; revoke only scoped authority | Tenant-scoped membership/device/peer revocation |
| Offboarding | User disconnect/delete account workflow | Remove relationship and policy scope | Revoke tenant membership, device assignment and gateway peer |

Personal and Family events MUST not be visible to Enterprise administrators unless a distinct, explicit, legally valid and separately approved data-sharing contract exists. Enterprise administrators cannot read Personal vaults or obtain Personal VPN keys.

## 9. Gateway trust, hybrid operations and privacy

### 9.1 Gateway admission

A gateway is eligible only when the registry contains verified operator identity, ownership/control evidence, region/jurisdiction, supported protocol, endpoint identity, service mode, tenant binding if customer-operated, privacy/retention profile, incident contact, current security review and valid status. Partner gateways require due diligence and contractual privacy/security terms before production. Customer gateways require tenant ownership/authorization and isolation validation.

### 9.2 Provider lifecycle

States: `PROPOSED`, `UNDER_REVIEW`, `APPROVED_TEST`, `APPROVED_PRODUCTION`, `SUSPENDED`, `REVOKED`, `RETIRED`.

Transitions require attributable human approval at the appropriate risk level. Production approval additionally requires security review, operations owner, incident response, abuse handling, patching, monitoring, cost approval, data-processing terms and retention. AEGIS cannot silently fail over from a customer gateway to a partner gateway or vice versa.

### 9.3 Logging and retention

Allowed default operational metadata is minimized to peer identifier, gateway identifier, connection start/end, byte counters where necessary for service operation, coarse health/error code and security event. No destination IP history, URL, DNS query, packet content or browsing history by default. Exact fields and retention period must be declared in a versioned retention profile and privacy notice before live service. Administrative access to logs is role-scoped, audited and purpose-bound.

### 9.4 No-cost development boundary

A3 development may use synthetic gateway descriptors, mocks, local test networks and self-hosted test gateways only where existing resources permit. No paid provider, cloud instance, bandwidth commitment, domain purchase, subscription, top-up or production capacity is implied. Local test environment teardown and data reset must be documented.

## 10. Platform adapter contract

Each adapter implements the same semantic interface while exposing platform differences rather than hiding them:

- `discoverCapabilities()`
- `requestUserConsent(profileIntent)`
- `prepareTunnel(profile, authorization)`
- `connect()`
- `disconnect(reason)`
- `getTunnelState()`
- `getHealth()`
- `applyRouteAndDnsPolicy(policy)`
- `recover(failureContext)`
- `dispose()`

All methods return typed results with evidence references. Adapter operations MUST be idempotent where possible and safe on repeated invocation. The policy engine authorizes each consequential transition before the adapter call.

| Platform | Adapter boundary | A2 acceptance condition before platform-specific implementation |
|---|---|---|
| Android | Kotlin + VpnService; maintained WireGuard integration; foreground lifecycle and Android consent | Exact API levels, VpnService lifecycle, always-on/lockdown support, OEM behavior and supported engine/license pinned in ADR |
| iOS/iPadOS | Network Extension packet-tunnel provider and supported WireGuard integration | Entitlement/provisioning feasibility, App Store/distribution constraints, supported OS versions and key storage documented |
| Windows | Native service/tunnel integration with maintained WireGuard implementation | Service privilege, driver/runtime provenance, signing, update/uninstall and supported OS matrix documented |
| macOS | Network Extension or supported native integration | Entitlements, signing/notarization, OS support and lifecycle constraints documented |
| Linux | Kernel WireGuard interface or maintained userspace implementation | Supported distribution/kernel matrix, privilege model, network manager and safe teardown documented |

A platform may be declared unsupported for a specific capability without blocking other platforms. Shared domain contracts and conformance vectors are mandatory; identical implementation or feature parity is not.

## 11. Security and test contract

Every test must include expected result and side-effect assertion. Minimum suites:

- Authority: unauthenticated, stale membership, revoked membership, expired policy, stale registry, wrong tenant, wrong mode, wrong device, replayed nonce and missing consent are denied.
- Key lifecycle: private key never crosses endpoint boundary; key rotation, replacement, lost device, revocation, delayed cleanup and retry are idempotent and auditable.
- Registry: forged, expired, revoked, mismatched operator, region, tenant, protocol and unsupported gateway are rejected.
- Routing: Personal/Enterprise crossover denied; route leaks, DNS leaks, IPv4/IPv6, split tunnel, captive portal, MTU, roam, sleep/resume, reconnect and reboot covered.
- Failure: gateway outage, control-plane outage, time rollback, stale profile, tunnel drop, OS process kill and failed cleanup follow declared policy.
- Platform: permissions/entitlements/enrollment absent, revoked or downgraded; unsupported capability never appears active.
- Privacy: logs and telemetry exclude private keys, preshared keys, URLs, DNS queries, destinations and payloads; retention enforcement verified.
- AI: malicious or stale IROKA recommendation cannot change gateway, route, DNS, tunnel, lockdown, key or peer without fresh local authorization.
- Supply chain: pinned dependency, license allowlist, provenance, vulnerability scan, SBOM, reproducible build and update/removal review.

VPN-TH-01 through VPN-TH-12 in AEGIS-A2-AMD-003 map to these suites. Each traceability row must carry requirement ID, implementation path, test ID, evidence artifact, exact commit SHA and status. A2 defines expected tests; A3 creates/runs them; A4 verifies them against frozen candidate SHA.

## 12. Cross-platform architecture reconciliation

This contract resolves the VPN-specific additions to AEGIS-A2-AMD-002:

1. The shared domain layer owns authority/profile/state/error semantics; it does not own OS tunnel execution.
2. The deterministic local policy layer validates identity, tenant/membership, consent, policy freshness, revocation, capability and risk immediately before protected operations.
3. The VPN subsystem is a bounded domain/service area inside AEGIS ONE. It is not a separate product, repository or independent authority root.
4. Each native platform adapter maps contracts to its OS APIs and reports capability evidence. No adapter may elevate privileges or claim parity.
5. Control plane and IROKA are remote dependencies, not enforcement roots. Offline operation is bounded by OD-05.
6. The existing Kotlin Android foundation remains the Android path. Cross-platform shared-core technology remains an ADR decision; do not rewrite Android or choose Rust/KMP/UI framework without comparative evidence.
7. AEGIS personal vault and VPN credentials remain separate security domains. OD-01 and OD-02 remain unchanged.
8. The common conformance suite checks semantic equivalence across adapters, while platform-specific tests prove each OS's actual permissions, entitlements, routing and recovery behavior.

### 12.1 Reconciled architecture decision status

| Decision | Contract outcome | Status |
|---|---|---|
| Product/service model | Personal + Enterprise in one AEGIS ONE | OWNER-DECIDED |
| Gateway infrastructure | Hybrid AEGIS/partner/customer classes | OWNER-DECIDED |
| Initial VPN protocol | WireGuard | OWNER-DECIDED |
| Private-key custody | Endpoint-generated; no private-key upload | CONTRACT-DEFINED |
| Personal/Enterprise isolation | Separate authority, keys, routes, DNS, logs and retention | CONTRACT-DEFINED |
| Control-plane operations | Versioned logical API and receipts | CONTRACT-DEFINED |
| Gateway registry trust | Admission, status and revocation lifecycle | CONTRACT-DEFINED |
| Routing/failure semantics | Full/split/private routes, DNS/IP, fail policy and recovery | CONTRACT-DEFINED |
| Native adapter contract | Common semantic methods + per-OS evidence | CONTRACT-DEFINED |
| Exact WireGuard library per OS | Requires dependency/license/provenance comparison | OPEN ADR |
| Gateway regions/provider/operator contracts | Requires cost, legal, privacy and operations evidence | OPEN / NO PRODUCTION APPROVAL |
| Exact API transport/auth provider | Must align with existing repo/backend baseline | OPEN ADR |
| Commercial/subscription model | Not decided or authorized | OUT OF SCOPE |

## 13. A2 closure checklist and evidence requirements

A2 VPN engineering contract is considered specification-complete for A3 planning only when the following artifacts are recorded:
- [x] Product model, hybrid gateway classes and WireGuard-first direction.
- [x] Domain types, authority binding and tunnel state machine.
- [x] Control-plane logical API and idempotency/replay rules.
- [x] Endpoint key/peer lifecycle and revocation semantics.
- [x] Gateway registry admission, trust and provider lifecycle.
- [x] Personal/Family/Enterprise routing, DNS, privacy and visibility boundaries.
- [x] Failure policy, recovery and platform adapter semantic interface.
- [x] VPN threat-to-test mapping and A4 evidence requirements.
- [ ] Per-platform WireGuard implementation/library ADR, licenses and provenance.
- [ ] Reconciliation against actual repository module/dependency/build baseline at A3.0.
- [ ] Control-plane transport/auth details aligned to an identified backend owner.
- [ ] Platform-specific capability/entitlement/API evidence matrix.
- [ ] Jurisdiction, retention, incident response and provider contract decisions before live service.
- [ ] Costed gateway operating model and separate infrastructure approval before production.

The checked items mean the specification is documented here, not implemented or tested. The unchecked items are explicit blockers or later-stage decisions, not silently assumed.

## 14. Stage status and authorization

- A1 v1.0 and OD-01 through OD-06 remain preserved.
- A2-AMD-002 remains the cross-platform product/architecture authority.
- A2-AMD-003 remains the VPN product and security amendment.
- This document is the VPN engineering contract supplement.
- VPN A2 specification is sufficiently defined to consolidate the A3 plan, with platform/library, repository, backend, legal and production-operations ADRs remaining gated.
- A3 implementation remains NOT AUTHORIZED / NOT STARTED until the Owner makes the separate stage-transition decision.
- No production gateway, live peer, production key, paid service, merge, release or deployment is authorized.
- A4 and A5 remain separate future stages.

**Production readiness:** NOT ESTABLISHED.

---
**End of A2 VPN engineering contract.**
