package org.akefadaka.aegis.policy

import org.akefadaka.aegis.security.*

enum class PolicyDecision { ALLOW, DENY, REQUIRE_APPROVAL, ESCALATE }

data class ActionRequest(
 val actionId:String,val actor:Principal,val mode:OperatingMode,val scope:ResourceScope,val tier:SecurityTier,
 val requiredCapability:String?=null,val requiresConsent:Boolean=false,val purpose:String?=null
)

data class EvaluationContext(
 val nowEpochMillis:Long,
 val online:Boolean,
 val identity:IdentityContext?=null,
 val currentMembershipVersion:Long?=null,
 val capabilityManifest:CapabilityManifest?=null,
 val consents:List<ConsentGrant> = emptyList(),
 val approval:HumanApproval?=null,
 val approvalIdentity:IdentityContext?=null,
 val approvalMembershipVersion:Long?=null,
 val policyFresh:Boolean=true,
 val usedApprovalIds:Set<String> = emptySet(),
 val usedApprovalNonces:Set<String> = emptySet()
)

/** Verifies the signature over canonical approval identity, role, action, scope, time bounds and nonce. */
fun interface ApprovalVerifier { fun verify(approval:HumanApproval):Boolean }

class PolicyEvaluator(
 private val approvalVerifier:ApprovalVerifier=ApprovalVerifier { false },
 private val identityValidator:IdentityContextValidator=IdentityContextValidator()
) {
 private fun approverKinds(mode:OperatingMode)=when(mode) {
  OperatingMode.CONSUMER -> setOf(PrincipalKind.ACCOUNT_OWNER)
  OperatingMode.FAMILY -> setOf(PrincipalKind.FAMILY_GUARDIAN)
  OperatingMode.ENTERPRISE -> setOf(PrincipalKind.ENTERPRISE_TENANT_OWNER,PrincipalKind.ENTERPRISE_ADMIN)
 }

 private fun validIdentity(snapshot:IdentityContext?,version:Long?,mode:OperatingMode,scope:ResourceScope,now:Long):Boolean {
  if(snapshot==null || snapshot.mode!=mode) return false
  return identityValidator.validate(snapshot,scope,now,version)==IdentityDecision.ALLOW
 }

 private fun capabilityIsFreshAndEnforced(manifest:CapabilityManifest?,name:String,now:Long):Boolean {
  if(manifest==null || now<manifest.generatedAtEpochMillis) return false
  if(now-manifest.generatedAtEpochMillis>manifest.maxEvidenceAgeMillis) return false
  return manifest.isEnforced(name,now) && manifest.errorFor(name,now)==null
 }

 fun evaluate(r:ActionRequest,c:EvaluationContext):PolicyDecision {
  if(r.actionId.isBlank() || c.nowEpochMillis<0 || !c.policyFresh) return PolicyDecision.DENY
  // P4 is disabled independently of identity, approval, and capability state.
  if(r.tier==SecurityTier.P4) return PolicyDecision.DENY

  val actorIdentity=c.identity ?: return PolicyDecision.DENY
  if(actorIdentity.principal!=r.actor || actorIdentity.mode!=r.mode) return PolicyDecision.DENY
  if(!validIdentity(actorIdentity,c.currentMembershipVersion,r.mode,r.scope,c.nowEpochMillis))
   return PolicyDecision.DENY

  if(r.requiresConsent && (r.purpose.isNullOrBlank() ||
   c.consents.none { it.subjectId==r.actor.id && it.purpose==r.purpose && it.granted })) return PolicyDecision.DENY

  if(r.requiredCapability!=null &&
   !capabilityIsFreshAndEnforced(c.capabilityManifest,r.requiredCapability,c.nowEpochMillis)) return PolicyDecision.DENY

  if(r.tier==SecurityTier.P3) {
   if(!c.online) return PolicyDecision.DENY
   val approval=c.approval ?: return PolicyDecision.REQUIRE_APPROVAL
   val approver=c.approvalIdentity ?: return PolicyDecision.DENY
   if(approval.approverId==r.actor.id ||
    approval.approverId!=approver.principal.id ||
    approval.approverKind!=approver.principal.kind ||
    approval.approverKind !in approverKinds(r.mode) ||
    approver.mode!=r.mode ||
    !validIdentity(approver,c.approvalMembershipVersion,r.mode,r.scope,c.nowEpochMillis) ||
    approver.principal.tenantId!=r.actor.tenantId ||
    approval.actionId!=r.actionId || approval.scope!=r.scope ||
    approval.issuedAtEpochMillis>c.nowEpochMillis ||
    approval.expiresAtEpochMillis<=c.nowEpochMillis ||
    approval.approvalId in c.usedApprovalIds ||
    approval.nonce in c.usedApprovalNonces ||
    !approvalVerifier.verify(approval)) return PolicyDecision.DENY
  }
  return PolicyDecision.ALLOW
 }
}
