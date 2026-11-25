# AEGIS ONE — Roles, Consent and Policy Matrix

**ID:** AEGIS-CONTRACT-G02-001 | **Version:** 1.0 | **Status:** A2 proposed contract; approval required

## 1. Authority principles
Authority is scoped by principal, relationship, tenant, device, capability, purpose, and time. No role inherits another role's powers implicitly. A role assignment is not an Android platform permission or Device Owner grant.

## 2. Principal and role classes

| Role | Permitted scope | Explicit exclusions |
|---|---|---|
| Device user/owner | Own account, vault, local consent, settings, device status | Cannot administer another tenant or override Android OS policy |
| Family guardian | Only disclosed family controls explicitly consented to or legally authorized | No default access to vault contents, messages, photos, or unrelated personal files |
| Family member/child | Own account and transparent age-appropriate controls | No authority over guardian or other family members |
| Enterprise tenant owner | Tenant configuration and delegated administration | No cross-tenant access or unrestricted personal-content access |
| Enterprise administrator | Assigned devices and policy capabilities within tenant | No access outside assigned scope |
| Support operator | Time-limited diagnostic scope, preferably read-only | No standing vault or remote-control authority |
| AEGIS service | Registration, policy delivery, audit, recovery under service identity | Cannot impersonate user/guardian/admin or bypass local policy |
| IROKA Kernel | Evaluate policy and issue scoped authorization decisions | Cannot expand Android capabilities, silently enroll, or override local controls |
| AI model/agent | Analyze and propose typed actions | No authority, signing key, or direct protected side effects |

## 3. Approval classes
- **P0 — Informational:** status read and non-sensitive diagnostics.
- **P1 — Reversible local change:** user-visible settings change with undo.
- **P2 — Sensitive data access:** vault unlock/export; requires local user presence and purpose.
- **P3 — Device-impacting action:** lock, policy change, VPN lockdown, account/device restriction; requires scoped administrator authority and required local/pre-authorized approval.
- **P4 — Destructive/irreversible:** wipe, cryptographic erasure, key destruction, irreversible account closure; requires explicit confirmation, strong independent authorization, recovery analysis, and immutable audit.

AI output may not approve P2–P4. Exact dual-control requirements for P3/P4 remain an Owner decision.

## 4. Consent
- Permission prompts explain purpose before requesting Android runtime permissions.
- Family controls disclose collection, visibility, guardian powers, and challenge/help routes.
- Enterprise policy discloses managed status and categories of data administrators can access.
- VPN routing and lockdown consequences are disclosed before activation.
- Withdrawal stops future optional collection; required security records follow a separately disclosed retention schedule.
- Emergency exceptions are narrow, time-bounded, logged, and reviewable.

## 5. Policy precedence
1. Android OS and hardware security enforcement.
2. Applicable law and platform policy.
3. Local AEGIS hard safety constraints.
4. Verified current tenant/family policy within its authority.
5. User preferences and consent.
6. IROKA recommendations.

Lower layers cannot override higher layers. Conflicts or indeterminate authority deny or escalate protected actions. Cached policy cannot outlive signed expiry.

## 6. Revocation
Role grants and device credentials carry scope, issuer, issue time, expiry, and revocation state. Offline revocation latency is bounded by maximum cached-grant lifetime; high-risk operations require online confirmation or stricter short-lived grants.

## 7. Open decisions
Jurisdictional age/guardian verification; child assent; enterprise dual-control for P3/P4; support access approval/session evidence; jurisdiction-specific retention and data rights.

No implementation or remote action is authorized by this contract.
