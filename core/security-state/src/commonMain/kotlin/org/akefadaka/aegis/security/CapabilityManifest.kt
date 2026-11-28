package org.akefadaka.aegis.security

enum class PlatformFamily { ANDROID, IOS, IPADOS, WINDOWS, MACOS, LINUX }

data class PlatformDescriptor(
 val family:PlatformFamily,
 val osVersion:String,
 val apiLevel:Int?=null,
 val architecture:String?=null
) {
 init {
  require(osVersion.isNotBlank())
  require(apiLevel==null || apiLevel>0)
  require(architecture==null || architecture.isNotBlank())
 }
}

/** Portable capability snapshot; it describes evidence and limits, not blanket platform parity. */
data class CapabilityManifest(
 val platform:PlatformDescriptor,
 val policyVersion:String,
 val generatedAtEpochMillis:Long,
 val maxEvidenceAgeMillis:Long,
 val capabilities:Map<String,CapabilityEvidence>
) {
 init {
  require(policyVersion.isNotBlank())
  require(generatedAtEpochMillis>=0 && maxEvidenceAgeMillis>0)
  require(capabilities.all { (name,evidence) ->
   name.isNotBlank() && name==evidence.capability &&
    (evidence.observedAtEpochMillis==null || evidence.observedAtEpochMillis<=generatedAtEpochMillis)
  })
 }

 fun capability(name:String):CapabilityEvidence =
  capabilities[name] ?: CapabilityEvidence(name,CapabilityStatus.UNKNOWN,EvidenceSource.NONE)

 fun isEnforced(name:String,nowEpochMillis:Long):Boolean =
  capability(name).isEnforcedAt(nowEpochMillis,maxEvidenceAgeMillis)

 fun errorFor(name:String,nowEpochMillis:Long):SecurityStateError? {
  val evidence=capability(name)
  val code=when(evidence.status) {
   CapabilityStatus.UNKNOWN -> SecurityErrorCode.CAPABILITY_UNKNOWN
   CapabilityStatus.UNSUPPORTED -> SecurityErrorCode.CAPABILITY_UNSUPPORTED
   CapabilityStatus.DEGRADED -> SecurityErrorCode.CAPABILITY_DEGRADED
   CapabilityStatus.REQUIRES_PERMISSION -> SecurityErrorCode.PERMISSION_REQUIRED
   CapabilityStatus.REQUIRES_ENROLLMENT -> SecurityErrorCode.ENROLLMENT_REQUIRED
   CapabilityStatus.OWNER_REPORTED -> SecurityErrorCode.OWNER_REPORTED_ONLY
   CapabilityStatus.VERIFIED -> when {
    !evidence.isCurrentAt(nowEpochMillis,maxEvidenceAgeMillis) -> SecurityErrorCode.EVIDENCE_STALE
    evidence.source !in setOf(EvidenceSource.PLATFORM_API,EvidenceSource.SIGNED_ATTESTATION) -> SecurityErrorCode.EVIDENCE_NOT_VERIFIED
    else -> return null
   }
  }
  return SecurityStateError(code,name,evidence.detailCode)
 }
}
