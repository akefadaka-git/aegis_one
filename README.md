# AEGIS ONE — Cross-Platform Device Security

**Personal protection first. Enterprise security as a governed second mode.**

AEGIS ONE is one cross-platform device-security product for personal phones, tablets and computers, with a separately governed Enterprise mode for authorized organizational device protection and management. IROKA is its governed intelligence/security backbone; AEGIS retains local enforcement and platform-specific authority boundaries.

## Product direction

- **One product:** Personal and Enterprise modes share a product identity, while keeping authority, data, keys and policy boundaries separate.
- **Primary focus:** Personal device security.
- **Secondary focus:** Enterprise endpoint and managed-device security.
- **Cross-platform by design:** Android, iOS/iPadOS, Windows, macOS and Linux are included in the target architecture from inception.
- **Family protection:** a distinct, transparent, least-privilege Personal policy context; not an Enterprise mode.
- **Android reference:** Samsung Galaxy A16 SM-A165F remains the Android reference device, not the sole target platform.

Cross-platform architecture does not imply feature parity. Each OS exposes different security APIs, permissions, entitlements, enrollment models and enforcement limits. AEGIS must report only verified capabilities and must not claim unrestricted antivirus scanning, device-wide control or management authority where the OS does not provide it.

## Architecture

- Preserve PMSS Architecture v1.2 as the existing architecture designation.
- Retain the native Kotlin Android client and existing platform-neutral core boundaries.
- Add native platform adapters/clients and common versioned domain/security contracts.
- Keep local protection useful when IROKA or network services are unavailable.
- IROKA and AI models may analyze and propose; they cannot self-authorize or override local policy or OS controls.
- Preserve explicit Personal, Family and Enterprise authority boundaries.
- The shared-core technology and non-Android client technologies remain OPEN pending a comparative architecture decision record and feasibility evidence.

## Project governance and status

- **A1:** `AEGIS-A1-BASE-001 v1.0` remains preserved and historically VERIFIED-CLOSED / FROZEN.
- **A1 amendment:** `AEGIS-A1-AMD-001` records the Owner's 2026-11-26 product-direction decisions.
- **A2:** Original closure record `AEGIS-A2-CLOSE-001` is preserved as the historical 2026-11-25 closure. A2 is now **REOPENED FOR THE SCOPED CROSS-PLATFORM REVISION** under `AEGIS-A2-AMD-002`.
- **A3:** The existing Android-focused plan must be reconciled against the revised cross-platform requirements before it governs expanded implementation. No new cross-platform A3 work is authorized by the A2 revision.
- **A4:** Not started. Independent implementation security assessment remains an A4/pre-release gate.
- **A5:** Not authorized; merge, release and deployment remain separate Owner approval gates.

## Preserved security decisions

OD-01 through OD-06 remain binding: user-controlled vault recovery; no enterprise vault escrow in v1; transparent least-privilege family protection; human-authorized P3 and disabled-by-default P4 actions; bounded offline authority; and platform-valid enterprise management enrollment. Per-installation identity, monotonic revocation, signed/scoped/replay-protected IROKA commands, local authorization, tamper-evident audit and personal/enterprise data separation are retained.

## Current constraints

No factory reset, enrollment, installation on the Owner's reference handset, production key generation, live service provisioning, merge, release or deployment is authorized by this documentation revision. Android Device Owner capabilities require valid Android Enterprise provisioning. Other platforms require their own supported enrollment/entitlement model.

The repository is public. No open-source license has been selected; public visibility does not itself grant permission to reuse or redistribute project materials. Do not commit credentials, signing keys, private user data or production secrets.

## Revision documents

- [A1 controlled product-direction amendment](docs/architecture/AEGIS-A1-AMD-001-CROSS-PLATFORM-PRODUCT-DIRECTION.md)
- [A2 cross-platform product requirements and architecture](docs/reconciliation/AEGIS-A2-AMD-002-CROSS-PLATFORM-REQUIREMENTS-AND-ARCHITECTURE.md)

---
