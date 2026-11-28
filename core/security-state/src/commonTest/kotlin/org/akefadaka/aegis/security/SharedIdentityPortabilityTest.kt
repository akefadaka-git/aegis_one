package org.akefadaka.aegis.security

import kotlin.test.Test
import kotlin.test.assertEquals

class SharedIdentityPortabilityTest {
    @Test
    fun identityValidationHasSameExactScopeAndTenantSemantics() {
        val scope = ResourceScope("device", "device-1", "tenant-1")
        val install = InstallationId("opaque-installation")
        val principal = Principal("employee-1", PrincipalKind.EMPLOYEE, "tenant-1")
        val membership = MembershipSnapshot("employee-1", OperatingMode.ENTERPRISE, "tenant-1", 4,
            MembershipStatus.ACTIVE, 100, 1_000)
        val grant = RoleGrant("grant-1", "employee-1", AuthorityRole.EMPLOYEE, OperatingMode.ENTERPRISE,
            "tenant-1", 4, scope, 100, 1_000, RoleGrantStatus.ACTIVE, "authority")
        val context = IdentityContext(principal, OperatingMode.ENTERPRISE, install,
            DeviceBinding(install, "device-record", "key-reference", DeviceIdentityState.ACTIVE),
            membership, setOf(grant))
        val validator = IdentityContextValidator()
        assertEquals(IdentityDecision.ALLOW, validator.validate(context, scope, 500, 4))
        assertEquals(IdentityDecision.DENY_ROLE_SCOPE,
            validator.validate(context, ResourceScope("device", "device-2", "tenant-1"), 500, 4))
        assertEquals(IdentityDecision.DENY_TENANT_MISMATCH,
            validator.validate(context, ResourceScope("device", "device-1", "tenant-2"), 500, 4))
    }

    @Test
    fun unavailableMembershipAuthorityAndAiIdentityFailClosed() {
        val install = InstallationId("opaque-installation")
        val principal = Principal("owner-1", PrincipalKind.ACCOUNT_OWNER)
        val scope = ResourceScope("setting", "vpn")
        val membership = MembershipSnapshot("owner-1", OperatingMode.CONSUMER, null, 1,
            MembershipStatus.ACTIVE, 100, 1_000)
        val grant = RoleGrant("grant-1", "owner-1", AuthorityRole.DEVICE_OWNER, OperatingMode.CONSUMER,
            null, 1, scope, 100, 1_000, RoleGrantStatus.ACTIVE, "authority")
        val context = IdentityContext(principal, OperatingMode.CONSUMER, install,
            DeviceBinding(install, "device-record", "key-reference", DeviceIdentityState.ACTIVE),
            membership, setOf(grant))
        val validator = IdentityContextValidator()
        assertEquals(IdentityDecision.DENY_AUTHORITY_UNAVAILABLE,
            validator.validate(context, scope, 500, null))
        val ai = context.copy(
            principal = Principal("agent", PrincipalKind.AI_AGENT),
            membership = membership.copy(principalId = "agent")
        )
        assertEquals(IdentityDecision.DENY_UNSUPPORTED_PRINCIPAL,
            validator.validate(ai, scope, 500, 1))
    }
}
