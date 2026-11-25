package org.akefadaka.aegis.policy
import org.akefadaka.aegis.security.*
import org.junit.Assert.*
import org.junit.Test
class PolicyEvaluatorTest {
 private val e=PolicyEvaluator(ApprovalVerifier { it.signature.contentEquals(byteArrayOf(1)) })
 private val owner=Principal("owner",PrincipalKind.ACCOUNT_OWNER)
 private val scope=ResourceScope("setting","vpn")
 private fun ctx(online:Boolean=true,fresh:Boolean=true)=EvaluationContext(1000,online,mapOf("vpn" to CapabilityStatus.VERIFIED),policyFresh=fresh)
 private fun approval(action:String="sensitive",s:ResourceScope=scope,kind:PrincipalKind=PrincipalKind.ACCOUNT_OWNER)=HumanApproval(
  approverId="owner-2",actionId=action,scope=s,issuedAtEpochMillis=900,expiresAtEpochMillis=2000,
  approvalId="approval-unique-001",nonce="nonce-unique-0001",signature=byteArrayOf(1),approverKind=kind
 )
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
 @Test fun p3RequiresSignedOnlineModeAuthorizedHumanApproval() {
  val r=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  assertEquals(PolicyDecision.REQUIRE_APPROVAL,e.evaluate(r,ctx()))
  assertEquals(PolicyDecision.ALLOW,e.evaluate(r,ctx().copy(approval=approval())))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx(false).copy(approval=approval())))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval(kind=PrincipalKind.AI_AGENT))))
  assertEquals(PolicyDecision.DENY,PolicyEvaluator().evaluate(r,ctx().copy(approval=approval())))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval().copy(signature=byteArrayOf(2)))))
 }
 @Test fun p3RejectsExpiredFutureWrongScopeAndReplayedApprovals() {
  val r=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval().copy(expiresAtEpochMillis=1000))))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval().copy(issuedAtEpochMillis=1001))))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval(s=ResourceScope("setting","other")))))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval(),usedApprovalIds=setOf("approval-unique-001"))))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(approval=approval(),usedApprovalNonces=setOf("nonce-unique-0001"))))
 }
 @Test fun enterpriseP3RequiresAdminNotEmployeeApproval() {
  val admin=Principal("admin",PrincipalKind.ENTERPRISE_ADMIN,"tenant")
  val employee=Principal("employee",PrincipalKind.EMPLOYEE,"tenant")
  val s=ResourceScope("record","1","tenant")
  val r=ActionRequest("sensitive",employee,OperatingMode.ENTERPRISE,s,SecurityTier.P3)
  val approval=approval("sensitive",s,PrincipalKind.EMPLOYEE)
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
 @Test fun requiredConsentMustBeExplicitAndPurposeBound() {
  val r=ActionRequest("share",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,requiresConsent=true,purpose="share_diagnostics")
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx()))
  val grant=ConsentGrant(owner.id,"share_diagnostics",true)
  assertEquals(PolicyDecision.ALLOW,e.evaluate(r,ctx().copy(consents=listOf(grant))))
  assertEquals(PolicyDecision.DENY,e.evaluate(r,ctx().copy(consents=listOf(grant.copy(purpose="other")))))
 }
}
