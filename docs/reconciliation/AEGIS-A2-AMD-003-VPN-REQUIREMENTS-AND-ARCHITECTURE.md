# AEGIS ONE — A2 Controlled VPN Architecture Amendment

**Amendment ID:** AEGIS-A2-AMD-003  
**Version:** 1.0  
**Date:** 2026-11-28  
**Status:** OWNER DECISIONS RECORDED — VPN FEATURE A2 DESIGN / TRACEABILITY OPEN  
**Amends:** AEGIS-A2-AMD-002, supplemented by AEGIS-A1-AMD-001  
**Repository:** akefadaka-git/aegis_one  
**Architecture designation:** PMSS v1.2, with controlled cross-platform and VPN amendments

## 1. Owner decisions and scope

The Owner approved these decisions on 2026-11-28:

1. **Service model: BOTH.** AEGIS ONE provides Personal VPN and Enterprise VPN within one product.
2. **Infrastructure: HYBRID.** The service supports AEGIS-operated gateways and explicitly approved compatible third-party or customer-operated gateways. Provider selection is policy/configuration driven, not hard-coded into clients.
3. **Initial protocol: WireGuard.** Integrate maintained established implementations. Do not design a proprietary VPN protocol or cryptographic primitive.

VPN is an independently bounded subsystem inside AEGIS ONE, not a separate product or repository by default. It has its own tunnel lifecycle, configuration, credentials, gateway selection, platform adapters, health state, failure behavior and security tests. It uses common AEGIS identity, capability, policy, consent and audit contracts through narrow interfaces.

These decisions authorize formal architecture and roadmap incorporation only. They do not authorize production gateway provisioning, paid services, live account/key issuance, source implementation, release or deployment.

## 2. Product and service model

### 2.1 Personal VPN

Personal VPN provides user-controlled network privacy and protection. Planned capabilities, each subject to platform support and acceptance:

- On-demand connection/disconnection with clear tunnel state.
- Gateway/location selection from eligible gateways.
- Automatic reconnect and tunnel-health reporting.
- Optional always-on and kill-switch/lockdown only where supported and explicitly enabled after disclosure of consequences.
- DNS configuration and protected DNS routing through the tunnel.
- Optional split tunneling where the OS permits reliable per-application routing.
- Public Wi-Fi risk prompts and user-configurable auto-connect rules.
- Data usage, connection duration, latency and tunnel diagnostics.
- Privacy-preserving connection records; no browsing-history collection or content logging by default.

Personal VPN credentials and policy are separate from Enterprise credentials and policy. Family members remain governed by the transparent, age-appropriate, least-privilege family policy. Guardian status does not grant access to browsing content or Personal VPN secrets.

### 2.2 Enterprise VPN

Enterprise VPN provides organization-authorized connectivity to private organizational resources. Planned capabilities:

- Tenant-scoped gateway and route profiles.
- Device/user assignment and revocation.
- Organization-managed configuration, DNS and allowed-route policy.
- Device-posture conditions and compliance reporting where reliable platform signals exist.
- Explicit separation of corporate routes/resources from personal traffic.
- BYOD and corporate-owned modes with platform-valid enrollment and disclosure.
- Administrative audit of configuration, assignment, policy changes and connection health without default access to browsing content.

Administrators manage only their tenant, assigned devices and authorized network capabilities. Enterprise VPN must not silently route all personal traffic through an organization gateway. Full-tunnel routing, traffic inspection and content-level monitoring require explicit policy, lawful basis, transparent notice and platform/user authorization. Enterprise VPN grants no access to a Personal vault or its keys. OD-02 (no Enterprise escrow of Personal vault keys in v1) remains binding.

### 2.3 Shared product, separate authority

Personal and Enterprise VPN may share client UX and protocol integration, but must separate identities, device registrations, credentials, gateway eligibility, configuration, route/DNS policies, administrative scopes, consent, audit visibility and retention. A device must not silently switch authority contexts. Ambiguous authority means deny/stop with a user-visible explanation.

## 3. Hybrid gateway architecture

The VPN control plane and data plane are separate.

### 3.1 Control plane

The control plane handles authenticated device registration, gateway discovery/eligibility, configuration distribution, short-lived provisioning authorization, key lifecycle coordination, revocation, policy versioning, optional subscription/quota metadata if later approved, and operational health metadata.

The control plane must not receive or store endpoint private WireGuard keys. Private keys are generated and retained on the endpoint using platform-protected storage where available. Only the public key and minimum provisioning metadata are sent to the authorized gateway/control service. Any exception requires a separate cryptographic design and explicit Owner decision.

APIs authenticate clients and administrators, enforce mode/tenant boundaries, use replay-resistant expiring authorization and emit minimized audit events. AEGIS/AI services cannot self-authorize tunnel or configuration changes.

### 3.2 Gateway classes

| Gateway class | Operator | Intended use | Required controls |
|---|---|---|---|
| AEGIS-operated | AEGIS or authorized operator | Personal service and designated Enterprise service | Region disclosure, patching, key handling, access control, monitoring, incident response and privacy policy |
| Partner-operated | Contracted/approved provider | Optional geographic capacity | Due diligence, contract, data-processing terms, security evidence, region/retention disclosure and revocation |
| Customer-operated | Enterprise tenant | Private corporate network access | Tenant ownership, admin authorization, peer lifecycle, route/DNS scope and tenant isolation |

The client uses a provider-neutral gateway interface and verified gateway registry. Third-party gateway use is explicit; never silently substituted. Discovery presence alone does not establish trust. Gateway operator, region, protocol, policy scope, status and last verification time must be available to policy evaluation and presented intelligibly to users/admins.

Personal traffic must not be routed to a customer-operated Enterprise gateway. Enterprise traffic must not be routed to a Personal gateway unless a separately reviewed enterprise architecture explicitly authorizes it.

### 3.3 Cost and operations

Open-source protocol software does not eliminate gateway compute, public IP, bandwidth, transit, monitoring, abuse handling or support costs. Support local/test gateways and synthetic configurations without paid dependencies. No paid provider, subscription, top-up, server purchase or production capacity commitment without separate Owner authorization.

## 4. WireGuard and cryptographic boundaries

- WireGuard is the initial tunnel protocol, integrated from a maintained upstream implementation or supported platform distribution.
- Do not implement WireGuard cryptographic primitives or handshake logic from scratch.
- Evaluate each platform library/embedding option for maintenance, supported OS versions, licensing, provenance, vulnerability history, API stability and reproducible builds.
- Pin exact dependency versions and record provenance; generate/update the SBOM when dependencies are introduced and before release.
- Generate unique private keys per device/installation and retain them on endpoints using platform-protected storage where available. Document platforms that cannot provide non-exportable key storage.
- Define key rotation, peer revocation, device replacement, lost-device handling and server-side peer cleanup before live provisioning.
- Never log private keys, preshared keys, tunnel configuration secrets, DNS query history, destination history or packet payloads.
- WireGuard transport encryption does not guarantee anonymity, protect a compromised endpoint, hide all metadata from the gateway, or protect traffic after it exits the gateway. Product claims must disclose these limits.
- IKEv2 or another protocol is outside the initial v1 commitment and requires a separate Owner-approved amendment and threat/test mapping.

## 5. Cross-platform integration boundaries

| Platform | VPN adapter direction | Constraints / required evidence |
|---|---|---|
| Android | Kotlin adapter using Android VpnService and maintained WireGuard integration | User-mediated consent; always-on/lockdown only where supported and explicitly configured; lifecycle, foreground service, battery and OEM validation |
| iOS/iPadOS | Native Network Extension packet-tunnel integration with maintained WireGuard implementation | Required entitlement, provisioning profile, OS behavior and distribution-policy validation |
| Windows | Native service/tunnel adapter using maintained WireGuard integration | Service privilege, driver/runtime provenance, install/update/uninstall safety and version compatibility |
| macOS | Network Extension / supported native tunnel integration | Entitlement, signing/notarization and OS restrictions |
| Linux | Native WireGuard interface or supported userspace integration | Distribution/kernel capability, privilege model, network-manager integration and safe rollback |

The common VPN domain layer defines desired state, tunnel state, gateway metadata, route/DNS policy, errors, health and audit events. Native adapters perform only operations supported by their OS. The common abstraction must not imply feature parity where permissions, entitlements or routing semantics differ.

## 6. Shared security control and authority

1. OS/hardware and applicable law remain highest priority.
2. AEGIS local hard safety constraints and verified platform capabilities are mandatory.
3. Personal/Family/Enterprise policy applies only within its authorized context.
4. User consent and administrator authorization are explicit and scoped.
5. IROKA may analyze risk and recommend a gateway, route or response; it cannot create a tunnel, enable lockdown, change routes, select a provider, approve P3 or override local policy by itself.

Connection/disconnection and ordinary user-selected gateway changes are user-authorized reversible actions. Enterprise policy changes and lockdown/network restriction are P3 where they affect device/network availability: scoped human authority, signed/time-limited approval as applicable, local policy verification and audit are required. P4 remains disabled by default in v1.

Use existing capability states: VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED, REQUIRES_PERMISSION and REQUIRES_ENROLLMENT. If a platform cannot enforce kill switch, split tunnel, DNS route or always-on state, report the limitation; never represent it as active.

## 7. Failure, privacy and abuse requirements

- Define fail-open/fail-closed behavior per user-selected policy. Kill switch is fail-closed only when explicitly enabled and supported; otherwise do not unexpectedly strand users offline.
- Provide tested recovery for tunnel failure, gateway unavailability, revoked peer, expired configuration, DNS failure, OS restart, app upgrade and clock uncertainty.
- Never silently bypass configured lockdown to restore connectivity.
- Never silently enable full-tunnel routing, traffic inspection, split-tunnel exceptions or third-party gateways.
- Test DNS leakage where supported; report coverage and limitations.
- Test IPv4/IPv6 routing, DNS, captive portals, network changes, roaming, sleep/resume and MTU/fragmentation.
- Gateway operations require abuse prevention, rate limits, secure administration, patching, monitoring, incident response and documented retention.
- Minimize gateway logs to operational/security necessity. No browsing history, packet payload or DNS query logging by default. Exceptional security logging requires purpose, minimized fields, retention limit, access control and disclosure.
- Publish accurate privacy statements distinguishing endpoint-visible data, control-plane metadata and gateway-visible network metadata.
- Family and Enterprise monitoring must not silently expose content-level browsing activity.

## 8. VPN threat-to-control-to-test traceability

| ID / threat | Required control | Minimum verification evidence | Status |
|---|---|---|---|
| VPN-TH-01 unauthorized peer enrollment | Authenticated registration, scoped provisioning, endpoint-generated keys | Invalid identity, cross-tenant and replayed provisioning denied; no peer created | DESIGN REQUIRED / NOT RUN |
| VPN-TH-02 private-key disclosure | Endpoint-only key generation/storage; secret-safe logs | Static secret scan, storage inspection and log-redaction tests | DESIGN REQUIRED / NOT RUN |
| VPN-TH-03 gateway substitution | Authenticated registry and explicit operator/region identity | Forged, stale, revoked and mismatched gateway rejected | DESIGN REQUIRED / NOT RUN |
| VPN-TH-04 Personal/Enterprise crossover | Separate identities, profiles, keys, routes and policy | Cross-mode and cross-tenant route/credential denial | DESIGN REQUIRED / NOT RUN |
| VPN-TH-05 tunnel drop / traffic leak | Explicit failure policy, supported kill switch, DNS/route handling | Packet/DNS leak tests during drop, reconnect, network switch and reboot | DESIGN REQUIRED / NOT RUN |
| VPN-TH-06 unsafe lockdown | Explicit consent, capability evidence and recovery | Unsupported platform cannot claim lockdown; recovery tested | DESIGN REQUIRED / NOT RUN |
| VPN-TH-07 stale/revoked peer | Expiring provisioning authorization, peer revocation and rotation | Revoked/expired peer denied; server cleanup and client state verified | DESIGN REQUIRED / NOT RUN |
| VPN-TH-08 provider compromise | Provider due diligence, tenant isolation, minimized logs and revocation | Provider disable/failover, access audit and incident drill | DESIGN REQUIRED / NOT RUN |
| VPN-TH-09 DNS/IP metadata overclaim | Accurate privacy disclosure and limited logging | UI/privacy claims compared with packet capture and gateway logs | DESIGN REQUIRED / NOT RUN |
| VPN-TH-10 malicious AI recommendation | Local policy/consent gate independent of IROKA | AI cannot change gateway, route, lockdown or credentials without authorization | DESIGN REQUIRED / NOT RUN |
| VPN-TH-11 supply-chain compromise | Pinned maintained implementation, provenance, SBOM and vulnerability review | Dependency/license/SBOM scan and reproducible build evidence | DESIGN REQUIRED / NOT RUN |
| VPN-TH-12 platform capability mismatch | Native adapter capability manifest and deny-by-default | Per-platform unsupported/permission/entitlement tests | DESIGN REQUIRED / NOT RUN |

These are design requirements. No VPN implementation, tunnel, server, packet-capture test, leak test, provider assessment or production operation is claimed.

## 9. A2 design outputs still required

The Owner has selected the service model, hybrid infrastructure and WireGuard protocol. Remaining engineering outputs are:

- Compare/select maintained WireGuard integration per platform, with license and provenance review.
- Define gateway registry schema, trust criteria, region metadata and provider onboarding/offboarding.
- Define control-plane API, device registration, peer provisioning, revocation and key-rotation contracts.
- Define Personal subscription/quota and Enterprise tenant licensing only if a commercial model is separately approved.
- Define gateway regions and initial capacity only after costed operational assessment and separate infrastructure authorization.
- Define data retention, abuse handling, incident response, legal jurisdiction and provider contracts before live service.
- Complete platform-specific VPN threat/control/test mappings and conformance harness.
- Decide whether an AEGIS-operated gateway is required for first operational release or whether customer-operated/test gateways suffice for the first client milestone. No gateway is provisioned here.

## 10. Stage and authorization effect

- A1 v1.0 remains preserved as the frozen historical baseline.
- A1-AMD-001 remains the cross-platform product-direction amendment.
- A2-AMD-002 remains the cross-platform architecture amendment; this A2-AMD-003 supplements it and does not erase prior decisions.
- VPN A2 design/traceability remain OPEN until technical selections, API/key lifecycle contracts, provider trust criteria, platform capability matrix and test plan are reconciled.
- Existing Android source/module boundaries must be reconciled before implementation; do not discard or replace the current foundation.
- A3 VPN work follows the relevant A2 design gates and separate A3 authorization.
- A4 includes independent security assessment of the exact frozen candidate, tunnel/traffic/DNS leak testing, gateway operational review and platform-specific verification.
- A5 remains a separate Owner authorization.

No main-branch change, source implementation, paid dependency/service, server provisioning, production key issuance, handset installation/enrollment, merge, release or deployment is authorized.

**Production readiness:** NOT ESTABLISHED.

---
**End of A2 VPN architecture amendment.**


## 11. Controlled VPN engineering contract reference — AEGIS-A2-VPN-ENG-001

The detailed VPN engineering contracts are now recorded in [AEGIS-A2-VPN-ENG-001](AEGIS-A2-VPN-ENG-001-ENGINEERING-CONTRACTS.md). This amendment remains the controlling VPN product/security architecture; the engineering contract makes its interfaces testable without changing the Owner's decisions.

**Reconciled status:** Personal + Enterprise service model, hybrid AEGIS/partner/customer gateway classes and WireGuard-first are OWNER-DECIDED. Domain and authority context, VpnProfile, GatewayDescriptor, tunnel state machine, capability evidence, typed errors, control-plane logical API, endpoint key/peer lifecycle, routing/DNS/failure semantics, privacy/retention boundaries and native adapter contract are CONTRACT-DEFINED. Exact engine/library per OS, API transport/auth provider, OS entitlement/capability evidence, backend ownership and production provider/operational decisions remain OPEN and gated.

The contract does not authorize source implementation, live peer provisioning, production key issuance, paid infrastructure or deployment. A3.0 must first reconcile the actual repository foundation and exact SHA; A3.12 onward cannot bypass the library, platform and security gates.

---
**End of VPN architecture amendment with engineering-contract reference.**
