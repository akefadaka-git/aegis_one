package org.akefadaka.aegis.security

enum class CapabilityStatus {
 VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED, REQUIRES_PERMISSION, REQUIRES_ENROLLMENT
}
enum class EvidenceSource { PLATFORM_API, SIGNED_ATTESTATION, OWNER_REPORT, TEST_FIXTURE, NONE }

enum class SecurityErrorCode {
 CAPABILITY_UNKNOWN, CAPABILITY_UNSUPPORTED, CAPABILITY_DEGRADED,
 PERMISSION_REQUIRED, ENROLLMENT_REQUIRED, OWNER_REPORTED_ONLY,
 EVIDENCE_STALE, EVIDENCE_NOT_VERIFIED
}

data class SecurityStateError(
 val code:SecurityErrorCode,
 val capability:String,
 val detailCode:String?=null
) {
 init { require(capability.isNotBlank()) }
}

data class CapabilityEvidence(
 val capability:String,
 val status:CapabilityStatus,
 val source:EvidenceSource,
 val observedAtEpochMillis:Long?=null,
 val validUntilEpochMillis:Long?=null,
 val detailCode:String?=null,
 val prerequisites:Set<String> = emptySet(),
 val requiredPermissions:Set<String> = emptySet(),
 val requiredEntitlements:Set<String> = emptySet(),
 val requiresEnrollment:Boolean = false,
 val supportedActions:Set<String> = emptySet(),
 val exclusions:Set<String> = emptySet()
) {
 init {
  require(capability.isNotBlank())
  require(observedAtEpochMillis==null || observedAtEpochMillis>=0)
  require(validUntilEpochMillis==null || (observedAtEpochMillis!=null && validUntilEpochMillis>observedAtEpochMillis))
  require(status!=CapabilityStatus.VERIFIED || source in setOf(EvidenceSource.PLATFORM_API,EvidenceSource.SIGNED_ATTESTATION,EvidenceSource.TEST_FIXTURE))
  require(status!=CapabilityStatus.OWNER_REPORTED || source==EvidenceSource.OWNER_REPORT)
  require(status!=CapabilityStatus.REQUIRES_PERMISSION || requiredPermissions.isNotEmpty() || requiredEntitlements.isNotEmpty())
  require(status!=CapabilityStatus.REQUIRES_ENROLLMENT || requiresEnrollment)
  require(status !in setOf(CapabilityStatus.UNKNOWN,CapabilityStatus.UNSUPPORTED) || supportedActions.isEmpty())
  require(prerequisites.none { it.isBlank() } && requiredPermissions.none { it.isBlank() })
  require(requiredEntitlements.none { it.isBlank() } && supportedActions.none { it.isBlank() } && exclusions.none { it.isBlank() })
 }

 fun isCurrentAt(nowEpochMillis:Long,maxAgeMillis:Long):Boolean {
  require(nowEpochMillis>=0 && maxAgeMillis>=0)
  val observed=observedAtEpochMillis ?: return false
  return nowEpochMillis>=observed &&
   nowEpochMillis-observed<=maxAgeMillis &&
   (validUntilEpochMillis==null || nowEpochMillis<validUntilEpochMillis)
 }

 /** Only fresh platform/attestation evidence can assert runtime enforcement; fixtures never can. */
 fun isEnforcedAt(nowEpochMillis:Long,maxAgeMillis:Long):Boolean =
  status==CapabilityStatus.VERIFIED &&
   source in setOf(EvidenceSource.PLATFORM_API,EvidenceSource.SIGNED_ATTESTATION) &&
   isCurrentAt(nowEpochMillis,maxAgeMillis)
}

data class SecurityPosture(val capabilities:Map<String,CapabilityEvidence>) {
 init { require(capabilities.all { (name,evidence) -> name.isNotBlank() && name==evidence.capability }) }

 fun capability(name:String)=capabilities[name] ?: CapabilityEvidence(name,CapabilityStatus.UNKNOWN,EvidenceSource.NONE)

 fun isEnforced(name:String,nowEpochMillis:Long,maxAgeMillis:Long):Boolean =
  capability(name).isEnforcedAt(nowEpochMillis,maxAgeMillis)

 fun errorFor(name:String,nowEpochMillis:Long,maxAgeMillis:Long):SecurityStateError? {
  val evidence=capability(name)
  val code=when(evidence.status) {
   CapabilityStatus.UNKNOWN -> SecurityErrorCode.CAPABILITY_UNKNOWN
   CapabilityStatus.UNSUPPORTED -> SecurityErrorCode.CAPABILITY_UNSUPPORTED
   CapabilityStatus.DEGRADED -> SecurityErrorCode.CAPABILITY_DEGRADED
   CapabilityStatus.REQUIRES_PERMISSION -> SecurityErrorCode.PERMISSION_REQUIRED
   CapabilityStatus.REQUIRES_ENROLLMENT -> SecurityErrorCode.ENROLLMENT_REQUIRED
   CapabilityStatus.OWNER_REPORTED -> SecurityErrorCode.OWNER_REPORTED_ONLY
   CapabilityStatus.VERIFIED -> when {
    !evidence.isCurrentAt(nowEpochMillis,maxAgeMillis) -> SecurityErrorCode.EVIDENCE_STALE
    evidence.source !in setOf(EvidenceSource.PLATFORM_API,EvidenceSource.SIGNED_ATTESTATION) -> SecurityErrorCode.EVIDENCE_NOT_VERIFIED
    else -> return null
   }
  }
  return SecurityStateError(code,name,evidence.detailCode)
 }
}

enum class OperatingMode { CONSUMER, FAMILY, ENTERPRISE }
enum class PrincipalKind {
 ACCOUNT_OWNER, FAMILY_GUARDIAN, FAMILY_MEMBER,
 ENTERPRISE_TENANT_OWNER, ENTERPRISE_ADMIN, EMPLOYEE, SUPPORT_OPERATOR,
 SYSTEM_SERVICE, IROKA_KERNEL, AI_AGENT
}
enum class SecurityTier { P0, P1, P2, P3, P4 }
data class Principal(val id:String,val kind:PrincipalKind,val tenantId:String?=null) { init { require(id.isNotBlank()) } }
data class ResourceScope(val resourceType:String,val resourceId:String,val tenantId:String?=null) {
 init { require(resourceType.isNotBlank() && resourceId.isNotBlank()) }
}
data class ConsentGrant(val subjectId:String,val purpose:String,val granted:Boolean)
data class HumanApproval(
 val approverId:String,
 val actionId:String,
 val scope:ResourceScope,
 val issuedAtEpochMillis:Long,
 val expiresAtEpochMillis:Long,
 val approvalId:String,
 val nonce:String,
 val signature:ByteArray,
 val approverKind:PrincipalKind=PrincipalKind.ACCOUNT_OWNER
) {
 init {
  require(approverId.isNotBlank() && actionId.isNotBlank() && approvalId.isNotBlank())
  require(nonce.isNotBlank() && signature.isNotEmpty())
  require(issuedAtEpochMillis>=0 && expiresAtEpochMillis>issuedAtEpochMillis)
 }
}
