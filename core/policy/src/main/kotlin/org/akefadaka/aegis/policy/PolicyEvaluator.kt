package org.akefadaka.aegis.policy
import org.akefadaka.aegis.security.*

enum class PolicyDecision { ALLOW, DENY, REQUIRE_APPROVAL, ESCALATE }
data class ActionRequest(
 val actionId:String,val actor:Principal,val mode:OperatingMode,val scope:ResourceScope,val tier:SecurityTier,
 val requiredCapability:String?=null,val requiresConsent:Boolean=false,val purpose:String?=null
)
data class EvaluationContext(
 val nowEpochMillis:Long,val online:Boolean,val capabilities:Map<String,CapabilityStatus>,
 val consents:List<ConsentGrant> = emptyList(),val approval:HumanApproval?=null,val policyFresh:Boolean=true
)
class PolicyEvaluator {
 fun evaluate(r:ActionRequest,c:EvaluationContext):PolicyDecision {
  if(r.actionId.isBlank() || r.actor.kind==PrincipalKind.AI_AGENT || r.tier==SecurityTier.P4 || !c.policyFresh) return PolicyDecision.DENY
  if(r.mode==OperatingMode.ENTERPRISE && (r.scope.tenantId==null || r.actor.tenantId!=r.scope.tenantId)) return PolicyDecision.DENY
  if(r.mode!=OperatingMode.ENTERPRISE && r.scope.tenantId!=null) return PolicyDecision.DENY
  if(r.requiresConsent && c.consents.none { it.subjectId==r.actor.id && it.purpose==r.purpose && it.granted }) return PolicyDecision.DENY
  if(r.requiredCapability!=null && c.capabilities[r.requiredCapability]!=CapabilityStatus.VERIFIED) return PolicyDecision.DENY
  if(r.tier==SecurityTier.P3) {
   if(!c.online) return PolicyDecision.DENY
   val a=c.approval ?: return PolicyDecision.REQUIRE_APPROVAL
   if(a.approverId==r.actor.id || a.actionId!=r.actionId || a.scope!=r.scope || a.expiresAtEpochMillis<=c.nowEpochMillis) return PolicyDecision.DENY
  }
  return PolicyDecision.ALLOW
 }
}
