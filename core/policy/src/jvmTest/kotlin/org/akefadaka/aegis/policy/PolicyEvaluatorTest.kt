package org.akefadaka.aegis.policy

import org.akefadaka.aegis.security.*
import org.junit.Assert.*
import org.junit.Test

class PolicyEvaluatorTest {
 private val now=10_000L
 private val scope=ResourceScope("setting","vpn")
 private val owner=Principal("owner",PrincipalKind.ACCOUNT_OWNER)
 private val verifier=ApprovalVerifier { it.signature.contentEquals(byteArrayOf(1)) }
 private val evaluator=PolicyEvaluator(verifier)

 private fun identity(
  principal:Principal=owner,
  mode:OperatingMode=OperatingMode.CONSUMER,
  role:AuthorityRole=AuthorityRole.DEVICE_OWNER,
  tenant:String?=principal.tenantId,
  grantScope:ResourceScope=scope,
  version:Long=7,
  grantVersion:Long=7
 ):IdentityContext {
  val install=InstallationId("opaque-installation-123")
  val membership=MembershipSnapshot(principal.id,mode,tenant,version,MembershipStatus.ACTIVE,9_000,20_000)
  val grant=RoleGrant("grant-"+principal.id,principal.id,role,mode,tenant,grantVersion,
   grantScope,9_000,20_000,RoleGrantStatus.ACTIVE,"authority")
  return IdentityContext(principal,mode,install,
   DeviceBinding(install,"device-record","public-key-reference",DeviceIdentityState.ACTIVE),
   membership,setOf(grant))
 }

 private fun manifest(
  status:CapabilityStatus=CapabilityStatus.VERIFIED,
  source:EvidenceSource=EvidenceSource.PLATFORM_API,
  observedAt:Long=9_500,
  generatedAt:Long=9_800,
  maxAge:Long=5_000
 )=CapabilityManifest(
  PlatformDescriptor(PlatformFamily.ANDROID,"17",apiLevel=36),
  "capability-policy-v1",generatedAt,maxAge,
  mapOf("vpn" to CapabilityEvidence("vpn",status,source,observedAtEpochMillis=observedAt))
 )

 private fun context(
  actorIdentity:IdentityContext=identity(),
  version:Long?=7,
  capabilityManifest:CapabilityManifest?=manifest(),
  online:Boolean=true
 )=EvaluationContext(now,online,actorIdentity,version,capabilityManifest)

 private fun approval(
  action:String="sensitive",
  approverId:String="owner-2",
  kind:PrincipalKind=PrincipalKind.ACCOUNT_OWNER,
  s:ResourceScope=scope
 )=HumanApproval(approverId,action,s,9_900,12_000,"approval-unique-001",
  "nonce-unique-0001",byteArrayOf(1),kind)

 private fun approverIdentity()=identity(principal=Principal("owner-2",PrincipalKind.ACCOUNT_OWNER))

 @Test fun actorMustHaveCurrentMembershipAndExplicitExactScopeRole() {
  val request=ActionRequest("read",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,"vpn")
  assertEquals(PolicyDecision.ALLOW,evaluator.evaluate(request,context()))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(version=null)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(version=8)))
  val noGrant=identity().copy(roleGrants=emptySet())
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=noGrant)))
  val otherScope=identity(grantScope=ResourceScope("setting","other"))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=otherScope)))
 }

 @Test fun identitySnapshotMustMatchRequestActorAndMode() {
  val request=ActionRequest("read",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1)
  val different=identity(principal=Principal("other",PrincipalKind.ACCOUNT_OWNER))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=different)))
  val wrongMode=identity(principal=Principal("employee",PrincipalKind.EMPLOYEE,"tenant"),
   mode=OperatingMode.ENTERPRISE,role=AuthorityRole.EMPLOYEE,
   grantScope=ResourceScope("setting","vpn","tenant"))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=wrongMode)))
 }

 @Test fun revokedRestrictedExpiredAndUnavailableMembershipDeny() {
  val request=ActionRequest("read",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1)
  val base=identity()
  for(status in listOf(MembershipStatus.REVOKED,MembershipStatus.RESTRICTED,MembershipStatus.EXPIRED)) {
   val changed=base.copy(membership=base.membership.copy(status=status))
   assertEquals(status.toString(),PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=changed)))
  }
  val expired=base.copy(membership=base.membership.copy(expiresAtEpochMillis=now))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=expired)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(version=null)))
 }

 @Test fun capabilityRequiresFreshRuntimeEvidenceNotStatusAlone() {
  val request=ActionRequest("connect",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,"vpn")
  assertEquals(PolicyDecision.ALLOW,evaluator.evaluate(request,context()))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=null)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=manifest(status=CapabilityStatus.UNKNOWN))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=manifest(source=EvidenceSource.TEST_FIXTURE))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=manifest(observedAt=1_000))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=manifest(observedAt=10_001,generatedAt=10_002))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(capabilityManifest=manifest(generatedAt=10_001))))
 }

 @Test fun aiAndP4AreAlwaysDenied() {
  val ai=Principal("agent",PrincipalKind.AI_AGENT)
  val request=ActionRequest("read",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1)
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context(actorIdentity=identity(ai))))
  val p4=ActionRequest("wipe",owner,OperatingMode.CONSUMER,scope,SecurityTier.P4)
  val approved=context().copy(approval=approval(),approvalIdentity=approverIdentity())
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(p4,approved))
 }

 @Test fun p3RequiresOnlineSeparatelyValidatedAuthorizedHumanApproval() {
  val request=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  assertEquals(PolicyDecision.REQUIRE_APPROVAL,evaluator.evaluate(request,context()))
  val approved=context().copy(approval=approval(),approvalIdentity=approverIdentity(),approvalMembershipVersion=7)
  assertEquals(PolicyDecision.ALLOW,evaluator.evaluate(request,approved))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,approved.copy(online=false)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,approved.copy(approvalIdentity=null)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,approved.copy(approvalMembershipVersion=null)))
 }

 @Test fun p3RejectsSelfApprovalAiApprovalWrongScopeAndReplay() {
  val request=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  val base=context().copy(approval=approval(),approvalIdentity=approverIdentity(),approvalMembershipVersion=7)
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(approval=approval(approverId=owner.id))))
  val ai=identity(principal=Principal("agent",PrincipalKind.AI_AGENT))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(
   approval=approval(approverId="agent",kind=PrincipalKind.AI_AGENT),approvalIdentity=ai)))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(
   approval=approval(s=ResourceScope("setting","other")))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(usedApprovalIds=setOf("approval-unique-001"))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(usedApprovalNonces=setOf("nonce-unique-0001"))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(
   approval=approval().copy(signature=byteArrayOf(2)))))
 }

 @Test fun p3ApprovalMustBeCurrentAndAuthorizedForSameTenantAndScope() {
  val request=ActionRequest("sensitive",owner,OperatingMode.CONSUMER,scope,SecurityTier.P3)
  val base=context().copy(approval=approval(),approvalIdentity=approverIdentity(),approvalMembershipVersion=7)
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(
   approval=approval().copy(expiresAtEpochMillis=now))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(
   approval=approval().copy(issuedAtEpochMillis=now+1))))
  val wrongRole=identity(principal=Principal("owner-2",PrincipalKind.ACCOUNT_OWNER),
   role=AuthorityRole.FAMILY_GUARDIAN)
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,base.copy(approvalIdentity=wrongRole)))
 }

 @Test fun enterpriseP3RequiresExplicitAdminRoleAndTenantBoundApproval() {
  val employee=Principal("employee",PrincipalKind.EMPLOYEE,"tenant-1")
  val admin=Principal("admin",PrincipalKind.ENTERPRISE_ADMIN,"tenant-1")
  val tenantScope=ResourceScope("device","device-1","tenant-1")
  val request=ActionRequest("isolate",employee,OperatingMode.ENTERPRISE,tenantScope,SecurityTier.P3)
  val actor=identity(employee,OperatingMode.ENTERPRISE,AuthorityRole.EMPLOYEE,"tenant-1",tenantScope)
  val adminIdentity=identity(admin,OperatingMode.ENTERPRISE,AuthorityRole.ENTERPRISE_ADMIN,"tenant-1",tenantScope)
  val signedApproval=approval("isolate","admin",PrincipalKind.ENTERPRISE_ADMIN,tenantScope)
  val c=context(actor,7).copy(approval=signedApproval,approvalIdentity=adminIdentity,approvalMembershipVersion=7)
  assertEquals(PolicyDecision.ALLOW,evaluator.evaluate(request,c))
  val crossTenant=identity(Principal("admin-2",PrincipalKind.ENTERPRISE_ADMIN,"tenant-2"),
   OperatingMode.ENTERPRISE,AuthorityRole.ENTERPRISE_ADMIN,"tenant-2",
   ResourceScope("device","device-1","tenant-2"))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,c.copy(
   approval=signedApproval.copy(approverId="admin-2"),approvalIdentity=crossTenant)))
 }

 @Test fun consentAndStalePolicyRemainDenyByDefault() {
  val request=ActionRequest("share",owner,OperatingMode.CONSUMER,scope,SecurityTier.P1,
   requiresConsent=true,purpose="share_diagnostics")
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context()))
  val grant=ConsentGrant(owner.id,"share_diagnostics",true)
  assertEquals(PolicyDecision.ALLOW,evaluator.evaluate(request,context().copy(consents=listOf(grant))))
  assertEquals(PolicyDecision.DENY,evaluator.evaluate(request,context().copy(policyFresh=false)))
 }
}
