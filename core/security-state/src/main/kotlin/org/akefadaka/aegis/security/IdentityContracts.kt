package org.akefadaka.aegis.security

/** Opaque installation-scoped identity; never an IMEI, MAC, serial or advertising identifier. */
@JvmInline
value class InstallationId(val value:String) {
 init { require(value.isNotBlank()) }
}

enum class DeviceIdentityState {
 UNREGISTERED, PENDING_VERIFICATION, ACTIVE, RESTRICTED, REVOKED, LOST, REPLACED, RECOVERY_PENDING, DECOMMISSIONED
}

data class DeviceBinding(
 val installationId:InstallationId,
 val deviceRecordId:String,
 /** Reference to a public-key record only; private key material is never part of this contract. */
 val deviceKeyId:String,
 val state:DeviceIdentityState
) {
 init { require(deviceRecordId.isNotBlank() && deviceKeyId.isNotBlank()) }
}

enum class MembershipStatus { ACTIVE, RESTRICTED, REVOKED, EXPIRED }

data class MembershipSnapshot(
 val principalId:String,
 val mode:OperatingMode,
 val tenantId:String?,
 val version:Long,
 val status:MembershipStatus,
 val issuedAtEpochMillis:Long,
 val expiresAtEpochMillis:Long
) {
 init {
  require(principalId.isNotBlank() && version>0)
  require(issuedAtEpochMillis>=0 && expiresAtEpochMillis>issuedAtEpochMillis)
  require(tenantId==null || tenantId.isNotBlank())
 }
}

enum class AuthorityRole {
 DEVICE_OWNER, FAMILY_GUARDIAN, FAMILY_MEMBER,
 ENTERPRISE_TENANT_OWNER, ENTERPRISE_ADMIN, EMPLOYEE, SUPPORT_OPERATOR,
 AEGIS_SERVICE, IROKA_KERNEL, AI_AGENT
}
enum class RoleGrantStatus { ACTIVE, REVOKED }

data class RoleGrant(
 val grantId:String,
 val principalId:String,
 val role:AuthorityRole,
 val mode:OperatingMode,
 val tenantId:String?,
 val membershipVersion:Long,
 /** Exact resource scope. No wildcard or implicit role inheritance is supported. */
 val scope:ResourceScope,
 val issuedAtEpochMillis:Long,
 val expiresAtEpochMillis:Long,
 val status:RoleGrantStatus,
 val issuerId:String
) {
 init {
  require(grantId.isNotBlank() && principalId.isNotBlank() && issuerId.isNotBlank())
  require(membershipVersion>0)
  require(issuedAtEpochMillis>=0 && expiresAtEpochMillis>issuedAtEpochMillis)
  require(tenantId==null || tenantId.isNotBlank())
 }
}

data class IdentityContext(
 val principal:Principal,
 val mode:OperatingMode,
 val installationId:InstallationId,
 val device:DeviceBinding,
 val membership:MembershipSnapshot,
 val roleGrants:Set<RoleGrant>
)

enum class IdentityDecision {
 ALLOW,
 DENY_AUTHORITY_UNAVAILABLE,
 DENY_STALE_MEMBERSHIP,
 DENY_REVOKED_MEMBERSHIP,
 DENY_RESTRICTED_MEMBERSHIP,
 DENY_EXPIRED_MEMBERSHIP,
 DENY_MEMBERSHIP_NOT_YET_VALID,
 DENY_IDENTITY_MISMATCH,
 DENY_PRINCIPAL_MODE,
 DENY_TENANT_MISMATCH,
 DENY_INSTALLATION_BINDING,
 DENY_DEVICE_NOT_ACTIVE,
 DENY_UNSUPPORTED_PRINCIPAL,
 DENY_NO_EXPLICIT_ROLE,
 DENY_ROLE_REVOKED,
 DENY_ROLE_EXPIRED,
 DENY_ROLE_NOT_YET_VALID,
 DENY_ROLE_SCOPE
}

/**
 * Pure local validation of an already-supplied identity snapshot.
 * The caller must obtain currentMembershipVersion from the current authority source;
 * null/unavailable authority is always denied. This class performs no authentication,
 * registration, key generation, network call or protected side effect.
 */
class IdentityContextValidator {
 fun validate(
  context:IdentityContext,
  requestedScope:ResourceScope,
  nowEpochMillis:Long,
  currentMembershipVersion:Long?
 ):IdentityDecision {
  if(nowEpochMillis<0) return IdentityDecision.DENY_AUTHORITY_UNAVAILABLE
  if(currentMembershipVersion==null || currentMembershipVersion<=0) return IdentityDecision.DENY_AUTHORITY_UNAVAILABLE

  val membership=context.membership
  if(membership.status==MembershipStatus.REVOKED) return IdentityDecision.DENY_REVOKED_MEMBERSHIP
  if(membership.status==MembershipStatus.RESTRICTED) return IdentityDecision.DENY_RESTRICTED_MEMBERSHIP
  if(membership.status==MembershipStatus.EXPIRED || nowEpochMillis>=membership.expiresAtEpochMillis)
   return IdentityDecision.DENY_EXPIRED_MEMBERSHIP
  if(membership.version!=currentMembershipVersion) return IdentityDecision.DENY_STALE_MEMBERSHIP
  if(membership.issuedAtEpochMillis>nowEpochMillis) return IdentityDecision.DENY_MEMBERSHIP_NOT_YET_VALID

  val principal=context.principal
  if(membership.principalId!=principal.id || membership.mode!=context.mode)
   return IdentityDecision.DENY_IDENTITY_MISMATCH
  if(membership.tenantId!=principal.tenantId) return IdentityDecision.DENY_TENANT_MISMATCH
  if(principal.kind==PrincipalKind.AI_AGENT || principal.kind==PrincipalKind.IROKA_KERNEL)
   return IdentityDecision.DENY_UNSUPPORTED_PRINCIPAL
  if(!context.modeAllows(principal.kind)) return IdentityDecision.DENY_PRINCIPAL_MODE

  if(context.installationId!=context.device.installationId) return IdentityDecision.DENY_INSTALLATION_BINDING
  if(context.device.state!=DeviceIdentityState.ACTIVE) return IdentityDecision.DENY_DEVICE_NOT_ACTIVE

  if(context.mode==OperatingMode.ENTERPRISE) {
   val tenant=principal.tenantId
   if(tenant.isNullOrBlank() || requestedScope.tenantId!=tenant) return IdentityDecision.DENY_TENANT_MISMATCH
  } else if(principal.tenantId!=null || requestedScope.tenantId!=null) {
   return IdentityDecision.DENY_TENANT_MISMATCH
  }

  val expectedRole=expectedRole(context.mode,principal.kind)
   ?: return IdentityDecision.DENY_UNSUPPORTED_PRINCIPAL
  val roleCandidates=context.roleGrants.filter {
   it.principalId==principal.id && it.role==expectedRole && it.mode==context.mode
  }
  if(roleCandidates.isEmpty()) return IdentityDecision.DENY_NO_EXPLICIT_ROLE

  val currentVersionCandidates=roleCandidates.filter { it.membershipVersion==membership.version }
  if(currentVersionCandidates.isEmpty()) return IdentityDecision.DENY_STALE_MEMBERSHIP
  val tenantCandidates=currentVersionCandidates.filter { it.tenantId==principal.tenantId }
  if(tenantCandidates.isEmpty()) return IdentityDecision.DENY_TENANT_MISMATCH
  val scopedCandidates=tenantCandidates.filter { it.scope==requestedScope }
  if(scopedCandidates.isEmpty()) return IdentityDecision.DENY_ROLE_SCOPE

  if(scopedCandidates.any { it.status==RoleGrantStatus.REVOKED }) return IdentityDecision.DENY_ROLE_REVOKED
  val active=scopedCandidates.filter { it.status==RoleGrantStatus.ACTIVE }
  if(active.isEmpty()) return IdentityDecision.DENY_ROLE_REVOKED
  if(active.none { it.issuedAtEpochMillis<=nowEpochMillis }) return IdentityDecision.DENY_ROLE_NOT_YET_VALID
  if(active.none { nowEpochMillis<it.expiresAtEpochMillis }) return IdentityDecision.DENY_ROLE_EXPIRED
  if(active.none { it.issuedAtEpochMillis<=nowEpochMillis && nowEpochMillis<it.expiresAtEpochMillis })
   return IdentityDecision.DENY_ROLE_EXPIRED

  return IdentityDecision.ALLOW
 }

 private fun IdentityContext.modeAllows(kind:PrincipalKind):Boolean=when(mode) {
  OperatingMode.CONSUMER -> kind==PrincipalKind.ACCOUNT_OWNER || kind==PrincipalKind.SYSTEM_SERVICE
  OperatingMode.FAMILY -> kind==PrincipalKind.FAMILY_GUARDIAN || kind==PrincipalKind.FAMILY_MEMBER || kind==PrincipalKind.SYSTEM_SERVICE
  OperatingMode.ENTERPRISE -> kind==PrincipalKind.ENTERPRISE_TENANT_OWNER || kind==PrincipalKind.ENTERPRISE_ADMIN ||
   kind==PrincipalKind.EMPLOYEE || kind==PrincipalKind.SUPPORT_OPERATOR || kind==PrincipalKind.SYSTEM_SERVICE
 }

 private fun expectedRole(mode:OperatingMode,kind:PrincipalKind):AuthorityRole?=when(mode to kind) {
  OperatingMode.CONSUMER to PrincipalKind.ACCOUNT_OWNER -> AuthorityRole.DEVICE_OWNER
  OperatingMode.CONSUMER to PrincipalKind.SYSTEM_SERVICE -> AuthorityRole.AEGIS_SERVICE
  OperatingMode.FAMILY to PrincipalKind.FAMILY_GUARDIAN -> AuthorityRole.FAMILY_GUARDIAN
  OperatingMode.FAMILY to PrincipalKind.FAMILY_MEMBER -> AuthorityRole.FAMILY_MEMBER
  OperatingMode.FAMILY to PrincipalKind.SYSTEM_SERVICE -> AuthorityRole.AEGIS_SERVICE
  OperatingMode.ENTERPRISE to PrincipalKind.ENTERPRISE_TENANT_OWNER -> AuthorityRole.ENTERPRISE_TENANT_OWNER
  OperatingMode.ENTERPRISE to PrincipalKind.ENTERPRISE_ADMIN -> AuthorityRole.ENTERPRISE_ADMIN
  OperatingMode.ENTERPRISE to PrincipalKind.EMPLOYEE -> AuthorityRole.EMPLOYEE
  OperatingMode.ENTERPRISE to PrincipalKind.SUPPORT_OPERATOR -> AuthorityRole.SUPPORT_OPERATOR
  OperatingMode.ENTERPRISE to PrincipalKind.SYSTEM_SERVICE -> AuthorityRole.AEGIS_SERVICE
  else -> null
 }
}
