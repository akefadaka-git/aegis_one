package org.akefadaka.aegis.policy

import org.akefadaka.aegis.security.*
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedPolicyPortabilityTest {
    @Test
    fun p4AlwaysDeniesAndP3RequiresApproval() {
        val scope = ResourceScope("setting", "vpn")
        val install = InstallationId("opaque-installation")
        val principal = Principal("owner", PrincipalKind.ACCOUNT_OWNER)
        val membership = MembershipSnapshot("owner", OperatingMode.CONSUMER, null, 2,
            MembershipStatus.ACTIVE, 100, 2_000)
        val grant = RoleGrant("grant", "owner", AuthorityRole.DEVICE_OWNER, OperatingMode.CONSUMER,
            null, 2, scope, 100, 2_000, RoleGrantStatus.ACTIVE, "authority")
        val identity = IdentityContext(principal, OperatingMode.CONSUMER, install,
            DeviceBinding(install, "device", "key-ref", DeviceIdentityState.ACTIVE), membership, setOf(grant))
        val evaluator = PolicyEvaluator()
        val p4 = ActionRequest("wipe", principal, OperatingMode.CONSUMER, scope, SecurityTier.P4)
        val p3 = ActionRequest("sensitive", principal, OperatingMode.CONSUMER, scope, SecurityTier.P3)
        val context = EvaluationContext(500, true, identity, 2)
        assertEquals(PolicyDecision.DENY, evaluator.evaluate(p4, context))
        assertEquals(PolicyDecision.REQUIRE_APPROVAL, evaluator.evaluate(p3, context))
    }
}
