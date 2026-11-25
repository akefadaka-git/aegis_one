package org.akefadaka.aegis.security

enum class CapabilityStatus { VERIFIED, OWNER_REPORTED, UNKNOWN, UNSUPPORTED, DEGRADED }
enum class EvidenceSource { PLATFORM_API, SIGNED_ATTESTATION, OWNER_REPORT, TEST_FIXTURE, NONE }
data class CapabilityEvidence(
 val capability:String,val status:CapabilityStatus,val source:EvidenceSource,
 val observedAtEpochMillis:Long?=null,val detailCode:String?=null
) {
 init {
  require(capability.isNotBlank())
  require(status!=CapabilityStatus.VERIFIED || source in setOf(EvidenceSource.PLATFORM_API,EvidenceSource.SIGNED_ATTESTATION,EvidenceSource.TEST_FIXTURE))
  require(status!=CapabilityStatus.OWNER_REPORTED || source==EvidenceSource.OWNER_REPORT)
 }
 /** Test fixtures may model a verified result but can never assert a runtime enforcement fact. */
 val isEnforced:Boolean get()=status==CapabilityStatus.VERIFIED && source!=EvidenceSource.TEST_FIXTURE
}
data class SecurityPosture(val capabilities:Map<String,CapabilityEvidence>) {
 fun capability(name:String)=capabilities[name] ?: CapabilityEvidence(name,CapabilityStatus.UNKNOWN,EvidenceSource.NONE)
}
enum class OperatingMode { CONSUMER, FAMILY, ENTERPRISE }
enum class PrincipalKind { ACCOUNT_OWNER, FAMILY_GUARDIAN, FAMILY_MEMBER, ENTERPRISE_ADMIN, EMPLOYEE, SYSTEM_SERVICE, AI_AGENT }
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
