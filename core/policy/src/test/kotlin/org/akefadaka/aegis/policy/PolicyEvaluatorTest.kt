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
 @Test fun modesRejectForeignPrincipals() {
  val guardian=Principal("guardian",PrincipalKind.FAMILY_GUARDIAN)
  val employee=Principal("employee",PrincipalKind.EMPLOYEE,"tenant")
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("x",guardian,OperatingMode.ENTERPRISE,ResourceScope("r","1","tenant"),SecurityTier.P1),ctx()))
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("x",employee,OperatingMode.FAMILY,scope,SecurityTier.P1),ctx()))
 }
 @Test fun p3RequiresOnlineModeAuthorizedHumanApproval() {
  val r=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  assertEquals(PolicyDecision.REQUIRE_APPROVAL,e.evaluate(r,ctx()))
  val a=HumanApproval("owner-2","sensitive",scope,2000,"ap-1")
  assertEquals(PolicyDecision.ALLOW,e.evaluate(r,ctx().copy(approval=a)))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx(false).copy(approval=a)))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=a.copy(approverId="iroka",approverKind=PrincipalKind.AI_AGENT))))
 }
 @Test fun enterpriseP3RequiresAdminNotEmployeeApproval() {
  val admin=Principal("admin",PrincipalKind.ENTERPRISE_ADMIN,"tenant")
  val employee=Principal("employee",PrincipalKind.EMPLOYEE,"tenant")
  val s=ResourceScope("record","1","tenant")
  val r=ActionRequest("sensitive",employee,OperatingMode.ENTERPRISE,s,SecurityTier.P3)
  val approval=HumanApproval("employee-2","sensitive",s,2000,"ap-2",PrincipalKind.EMPLOYEE)
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval)))
  val adminApproval=approval.copy(approverId=admin.id,approverKind=PrincipalKind.ENTERPRISE_ADMIN)
  assertEquals(PolicyDecision.ALLOW,e.evaluate(r,ctx().copy(approval=adminApproval)))
 }
 @Test fun unknownCapabilityStalePolicyAndTenantMismatchDeny() {
  val r=ActionRequest("x",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,"missing")
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx()))
  assertEquals(PolicyDecision.DENY,e.evaluate(r.copy(requiredCapability=null),ctx(fresh=false)))
  val admin=Principal("a",PrincipalKind.ENTERPRISE_ADMIN,"t1")
  assertEquals(PolicyDecision.DENY,e.evaluate(ActionRequest("x",admin,OperatingMode.ENTERPRISE,ResourceScope("r","1","t2"),SecurityTier.P1),ctx()))
 }
}
