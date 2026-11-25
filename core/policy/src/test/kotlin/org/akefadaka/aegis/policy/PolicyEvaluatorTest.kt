package org.akefadaka.aegis.policy
import org.akefadaka.aegis.security.*
import org.junit.Assert.*
import org.junit.Test
class PolicyEvaluatorTest {
 private val e=PolicyEvaluator(); private val owner=Principal("owner",PrincipalKind.ACCOUNT_OWNER)
 private val scope=ResourceScope("setting","vpn")
 private fun ctx(online:Boolean=true,fresh:Boolean=true)=EvaluationContext(1000,online,mapOf("vpn" to CapabilityStatus.VERIFIED),policyFresh=fresh)
 @Test fun aiAndP4AreDenied() {
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("x",Principal("ai",PrincipalKind.AI_AGENT),OperatingMode.CONSUMER,scope,SecurityTier.P1),ctx()))
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("wipe",owner,OperatingMode.CONSUMER,scope,SecurityTier.P4),ctx()))
 }
 @Test fun p3RequiresOnlineHumanApproval() {
  val r=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  assertEquals(PolicyDecision.REQUIRE_APPROVAL,e.evaluate(r,ctx()))
  val a=HumanApproval("admin","sensitive",scope,2000,"ap-1")
  assertEquals(PolicyDecision.ALLOW,e.evaluate(r,ctx().copy(approval=a)))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx(false).copy(approval=a)))
 }
 @Test fun unknownCapabilityStalePolicyAndTenantMismatchDeny() {
  val r=ActionRequest("x",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,"missing")
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx()))
  assertEquals(PolicyDecision.DENY,e.evaluate(r.copy(requiredCapability=null),ctx(fresh=false)))
  val admin=Principal("a",PrincipalKind.ENTERPRISE_ADMIN,"t1")
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("x",admin,OperatingMode.ENTERPRISE,ResourceScope("r","1","t2"),SecurityTier.P1),ctx()))
 }
}
