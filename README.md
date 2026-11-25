# AEGIS ONE — Powered by IROKA

**Intelligent Android Security. Powered by IROKA.**

AEGIS ONE is a dedicated Android security and protection platform. IROKA provides governed intelligence and security orchestration; AEGIS ONE remains the Android product and local enforcement boundary.

## Project status

- **A1:** Product and architecture baseline established in [A1 Product and Architecture Baseline](docs/architecture/A1-PRODUCT-AND-ARCHITECTURE-BASELINE.md).
- **A2:** Android/device and IROKA integration reconciliation is **PARTIAL / OPEN**. See [A2 Android and Device Reconciliation](docs/reconciliation/A2-ANDROID-AND-DEVICE-RECONCILIATION.md).
- **Threat model:** Initial threat model recorded in [A1 Threat Model](docs/security/THREAT-MODEL-A1.md).
- **A3:** Not started; requires A2 closure and separate explicit Owner authorization.
- **A4:** Not run.
- **A5:** Not authorized; deployment/release remains a separate approval gate.

## Architecture and scope

- Native Android application, Kotlin.
- PMSS Architecture v1.2.
- Initial reference device: Samsung Galaxy A16 SM-A165F.
- Separate unmanaged/consumer and fully managed enterprise modes.
- Local protection must remain useful when IROKA is unavailable.
- IROKA models and agents may analyze and propose; they cannot self-authorize or override Android controls.
- OEM integration, custom OS, rooting, and bootloader modification are out of scope.

## Current constraints

No factory reset, device enrollment, application installation, deployment, or publication is authorized as part of the current A1/A2 work. Device Owner capabilities are conditional on valid Android Enterprise provisioning and are not assumed in ordinary consumer mode.

## Repository governance

This repository is public. No open-source license has been selected; public visibility does not itself grant permission to reuse or redistribute project materials. Do not commit credentials, signing keys, private user data, or production secrets.

All implementation, merge, release, and deployment actions remain subject to their respective stage gates and explicit approvals.
