# AEGIS ONE — Powered by IROKA

**Intelligent Android Security. Powered by IROKA.**

AEGIS ONE is a dedicated Android security and protection platform. IROKA provides governed intelligence and security orchestration; AEGIS ONE remains the Android product and local enforcement boundary.

## Project status

- **A1:** Product and architecture baseline **VERIFIED-CLOSED / FROZEN** in [A1 Product and Architecture Baseline](docs/architecture/A1-PRODUCT-AND-ARCHITECTURE-BASELINE.md).
- **A2:** **VERIFIED-CLOSED** under [A2 Owner Closure Record](docs/reconciliation/AEGIS-A2-CLOSE-001-OWNER-CLOSURE-RECORD.md); residual device, backend, legal and assurance items remain feature-specific or later-stage gates.
- **A3:** Owner authorized bounded implementation **A3.0–A3.5** under [AEGIS-A3-PLAN-001](docs/implementation/AEGIS-A3-BOUNDED-IMPLEMENTATION-PLAN.md). Initial foundation is committed on the existing implementation branch; build/test verification is pending.
- **A3.6–A3.9:** Not included in the current authorization.
- **A4:** Not started. Independent implementation security assessment remains an A4/pre-release gate.
- **A5:** Not authorized; merge, release and deployment remain separate Owner approval gates.

## Architecture and scope

- Native Android application, Kotlin.
- PMSS Architecture v1.2.
- Initial reference device: Samsung Galaxy A16 SM-A165F.
- Separate consumer, family and fully managed enterprise modes with explicit identity boundaries.
- Local protection must remain useful when IROKA is unavailable.
- IROKA models and agents may analyze and propose; they cannot self-authorize or override Android controls.
- OEM integration, custom OS, rooting, and bootloader modification are out of scope.

## Current constraints

No device enrollment, application installation on the reference handset, production key generation, merge, release or deployment is authorized by the current A3 implementation approval. Device Owner capabilities are conditional on valid Android Enterprise provisioning and are not assumed in ordinary consumer mode.

## Repository governance

This repository is public. No open-source license has been selected; public visibility does not itself grant permission to reuse or redistribute project materials. Do not commit credentials, signing keys, private user data, or production secrets.

A1 remains frozen and A2 remains closed. Current A3 authorization is limited to A3.0–A3.5 on `docs/a1-baseline-a2-reconciliation`; do not create a branch, modify `main`, merge or deploy. A5 is a separate Owner authorization gate.
