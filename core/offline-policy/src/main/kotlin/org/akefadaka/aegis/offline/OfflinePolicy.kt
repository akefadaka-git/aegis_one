package org.akefadaka.aegis.offline
import org.akefadaka.aegis.security.SecurityTier

enum class TimeConfidence { TRUSTED, UNCERTAIN, ROLLBACK_DETECTED }
enum class PolicyState { ACCEPTED, EXPIRED, REJECTED_SIGNATURE, REJECTED_ROLLBACK, REJECTED_CLOCK, REJECTED_SCOPE }
data class CachedPolicy(
 val policyId:String,val version:Long,val issuedAtEpochMillis:Long,val expiresAtEpochMillis:Long,
 val tier:SecurityTier,val scope:String,val issuerId:String,val signature:ByteArray
) { init { require(policyId.isNotBlank() && issuerId.isNotBlank() && scope.isNotBlank() && version>0) } }
fun interface PolicySignatureVerifier { fun verify(policy:CachedPolicy):Boolean }
data class PolicyCheck(val state:PolicyState,val effectiveUntilEpochMillis:Long?=null)
class OfflinePolicyValidator(private val verifier:PolicySignatureVerifier) {
 fun validate(p:CachedPolicy,now:Long,time:TimeConfidence,scope:String,highestVersion:Long):PolicyCheck {
  if(time!=TimeConfidence.TRUSTED) return PolicyCheck(PolicyState.REJECTED_CLOCK)
  if(p.scope!=scope) return PolicyCheck(PolicyState.REJECTED_SCOPE)
  if(p.version<highestVersion) return PolicyCheck(PolicyState.REJECTED_ROLLBACK)
  if(!verifier.verify(p)) return PolicyCheck(PolicyState.REJECTED_SIGNATURE)
  val maxAge=when(p.tier) {
   SecurityTier.P0,SecurityTier.P1 -> 7L*24*60*60*1000
   SecurityTier.P2 -> 24L*60*60*1000
   SecurityTier.P3,SecurityTier.P4 -> return PolicyCheck(PolicyState.EXPIRED)
  }
  val expiry=minOf(p.expiresAtEpochMillis,p.issuedAtEpochMillis+maxAge)
  if(now<p.issuedAtEpochMillis || now>=expiry) return PolicyCheck(PolicyState.EXPIRED)
  return PolicyCheck(PolicyState.ACCEPTED,expiry)
 }
}
