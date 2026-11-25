package org.akefadaka.aegis.policy
import org.akefadaka.aegis.security.*

enum class PolicyDecision { ALLOW, DENY, REQUIRE_APPROVAL, ESCALATE }
data class ActionRequest(
 val actionId:String,val actor:Principal,val mode:OperatingMode,val scope:ResourceScope,val tier:SecurityTier,
 val requiredCapability:String?=null,val requiresConsent:Boolean=false,val purpose:String?=null
)
data class EvaluationContext(
 val nowEpochMillis:Long,val online:Boolean,val capabilities:Map<String,CapabilityStatus>,
 val consents:List<ConsentGrant> = emptyList(),val approval:HumanApproval?=null,val policyFresh:Boolean=true,
 val usedApprovalIds:Set<String> = emptySet(),val usedApprovalNonces:Set<String> = emptySet()
)
/** Verifies the signature over the canonical approval identity, role, action, scope, time bounds and nonce. */
fun interface ApprovalVerifier { fun verify(approval:HumanApproval):Boolean }
class PolicyEvaluator(private val approvalVerifier:ApprovalVerifier=ApprovalVerifier { false }) {
 private fun allowedKinds(mode:OperatingMode)=when(mode) {
  OperatingMode.CONSUMER -> setOf(PrincipalKind.ACCOUNT_OWNER,PrincipalKind.SYSTEM_SERVICE)
  OperatingMode.FAMILY -> setOf(PrincipalKind.FAMILY_GUARDIAN,PrincipalKind.FAMILY_MEMBER,PrincipalKind.SYSTEM_SERVICE)
  OperatingMode.ENTERPRISE -> setOf(PrincipalKind.ENTERPRISE_ADMIN,PrincipalKind.EMPLOYEE,PrincipalKind.SYSTEM_SERVICE)
 }
 private fun approverKinds(mode:OperatingMode)=when(mode) {
  OperatingMode.CONSUMER -> setOf(PrincipalKind.ACCOUNT_OWNER)
  OperatingMode.FAMILY -> setOf(PrincipalKind.FAMILY_GUARDIAN)
  OperatingMode.ENTERPRISE -> setOf(PrincipalKind.ENTERPRISE_ADMIN)
 }
 fun evaluate(r:ActionRequest,c:EvaluationContext):PolicyDecision {
  if(r.actionId.isBlank() || r.actor.kind !in allowedKinds(r.mode) || r.tier==SecurityTier.P4 || !c.policyFresh) return PolicyDecision.DENY
  if(r.mode==OperatingMode.ENTERPRISE && (r.scope.tenantId==null || r.actor.tenantId!=r.scope.tenantId)) return PolicyDecision.DENY
  if(r.mode!=OperatingMode.ENTERPRISE && r.scope.tenantId!=null) return PolicyDecision.DENY
  if(r.requiresConsent && (r.purpose.isNullOrBlank() || c.consents.none { it.subjectId==r.actor.id && it.purpose==r.purpose && it.granted })) return PolicyDecision.DENY
  if(r.requiredCapability!=null && c.capabilities[r.requiredCapability]!=CapabilityStatus.VERIFIED) return PolicyDecision.DENY
  if(r.tier==SecurityTier.P3) {
   if(!c.online) return PolicyDecision.DENY
   val a=c.approval ?: return PolicyDecision.REQUIRE_APPROVAL
   if(a.approverId==r.actor.id || a.approverKind !in approverKinds(r.mode) ||
      a.actionId!=r.actionId || a.scope!=r.scope ||
      a.issuedAtEpochMillis>c.nowEpochMillis || a.expiresAtEpochMillis<=c.nowEpochMillis ||
      a.approvalId in c.usedApprovalIds || a.nonce in c.usedApprovalNonces ||
      !approvalVerifier.verify(a)) return PolicyDecision.DENY
  }
  return PolicyDecision.ALLOW
 }
}
