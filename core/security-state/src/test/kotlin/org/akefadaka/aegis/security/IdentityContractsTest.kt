package org.akefadaka.aegis.security

import org.junit.Assert.*
import org.junit.Test

class IdentityContractsTest {
 private val now=1_000L
 private val scope=ResourceScope("setting","vpn")
 private val validator=IdentityContextValidator()

 private fun consumerContext(
  membershipVersion:Long=3,
  membershipStatus:MembershipStatus=MembershipStatus.ACTIVE,
  deviceState:DeviceIdentityState=DeviceIdentityState.ACTIVE,
  grants:Set<RoleGrant>?=null
 ):IdentityContext {
  val installation=InstallationId("install-random-opaque-001")
  val principal=Principal("owner-1",PrincipalKind.ACCOUNT_OWNER)
  val membership=MembershipSnapshot(principal.id,OperatingMode.CONSUMER,null,membershipVersion,
   membershipStatus,900,2_000)
  val defaultGrant=RoleGrant("grant-1",principal.id,AuthorityRole.DEVICE_OWNER,OperatingMode.CONSUMER,
   null,membershipVersion,scope,900,2_000,RoleGrantStatus.ACTIVE,"authority")
  return IdentityContext(principal,OperatingMode.CONSUMER,installation,
   DeviceBinding(installation,"device-record-1","public-key-ref-1",deviceState),
   membership,grants ?: setOf(defaultGrant))
 }

 private fun enterpriseContext(
  tenantId:String="tenant-1",
  membershipVersion:Long=4,
  grantRole:AuthorityRole=AuthorityRole.EMPLOYEE,
  grantScope:ResourceScope=ResourceScope("device","device-1","tenant-1"),
  grantVersion:Long=4
 ):IdentityContext {
  val installation=InstallationId("install-enterprise-opaque-01")
  val principal=Principal("employee-1",PrincipalKind.EMPLOYEE,tenantId)
  val membership=MembershipSnapshot(principal.id,OperatingMode.ENTERPRISE,tenantId,membershipVersion,
   MembershipStatus.ACTIVE,900,2_000)
  val grant=RoleGrant("grant-enterprise-1",principal.id,grantRole,OperatingMode.ENTERPRISE,
   "tenant-1",grantVersion,grantScope,900,2_000,RoleGrantStatus.ACTIVE,"tenant-authority")
  return IdentityContext(principal,OperatingMode.ENTERPRISE,installation,
   DeviceBinding(installation,"device-record-enterprise","device-public-key-ref",DeviceIdentityState.ACTIVE),
   membership,setOf(grant))
 }

 @Test fun validCurrentConsumerIdentityRequiresExplicitDeviceOwnerGrant() {
  val context=consumerContext()
  assertEquals(IdentityDecision.ALLOW,validator.validate(context,scope,now,3))
 }

 @Test fun unavailableOrStaleMembershipAuthorityDenies() {
  val context=consumerContext()
  assertEquals(IdentityDecision.DENY_AUTHORITY_UNAVAILABLE,validator.validate(context,scope,now,null))
  assertEquals(IdentityDecision.DENY_STALE_MEMBERSHIP,validator.validate(context,scope,now,4))
 }

 @Test fun revokedRestrictedAndExpiredMembershipsDeny() {
  assertEquals(IdentityDecision.DENY_REVOKED_MEMBERSHIP,
   validator.validate(consumerContext(membershipStatus=MembershipStatus.REVOKED),scope,now,3))
  assertEquals(IdentityDecision.DENY_RESTRICTED_MEMBERSHIP,
   validator.validate(consumerContext(membershipStatus=MembershipStatus.RESTRICTED),scope,now,3))
  val base=consumerContext()
  val expired=base.copy(membership=base.membership.copy(expiresAtEpochMillis=999))
  assertEquals(IdentityDecision.DENY_EXPIRED_MEMBERSHIP,validator.validate(expired,scope,now,3))
 }

 @Test fun familyPrincipalCannotEnterConsumerMode() {
  val base=consumerContext()
  val guardian=base.copy(
   principal=Principal("guardian-1",PrincipalKind.FAMILY_GUARDIAN),
   membership=base.membership.copy(principalId="guardian-1")
  )
  assertEquals(IdentityDecision.DENY_PRINCIPAL_MODE,validator.validate(guardian,scope,now,3))
 }

 @Test fun principalTenantAndMembershipTenantMustMatch() {
  val context=enterpriseContext()
  val mismatch=context.copy(membership=context.membership.copy(tenantId="tenant-2"))
  assertEquals(IdentityDecision.DENY_TENANT_MISMATCH,
   validator.validate(mismatch,ResourceScope("device","device-1","tenant-1"),now,4))
 }

 @Test fun enterpriseResourceScopeCannotCrossTenant() {
  val context=enterpriseContext()
  assertEquals(IdentityDecision.DENY_TENANT_MISMATCH,
   validator.validate(context,ResourceScope("device","device-1","tenant-2"),now,4))
 }

 @Test fun deviceInstallationBindingAndLifecycleAreEnforced() {
  val context=consumerContext()
  val wrongBinding=context.copy(device=context.device.copy(installationId=InstallationId("different-install-opaque")))
  assertEquals(IdentityDecision.DENY_INSTALLATION_BINDING,validator.validate(wrongBinding,scope,now,3))
  val lost=consumerContext(deviceState=DeviceIdentityState.LOST)
  assertEquals(IdentityDecision.DENY_DEVICE_NOT_ACTIVE,validator.validate(lost,scope,now,3))
 }

 @Test fun roleGrantMustBeExplicitAndExactScope() {
  val noGrant=consumerContext(grants=emptySet())
  assertEquals(IdentityDecision.DENY_NO_EXPLICIT_ROLE,validator.validate(noGrant,scope,now,3))
  val otherScope=consumerContext(grants=setOf(
   RoleGrant("grant-other", "owner-1", AuthorityRole.DEVICE_OWNER,OperatingMode.CONSUMER,
    null,3,ResourceScope("setting","other"),900,2_000,RoleGrantStatus.ACTIVE,"authority")
  ))
  assertEquals(IdentityDecision.DENY_ROLE_SCOPE,validator.validate(otherScope,scope,now,3))
 }

 @Test fun staleRevokedAndExpiredRoleGrantsDeny() {
  val staleGrant=consumerContext(grants=setOf(
   RoleGrant("grant-stale","owner-1",AuthorityRole.DEVICE_OWNER,OperatingMode.CONSUMER,
    null,2,scope,900,2_000,RoleGrantStatus.ACTIVE,"authority")
  ))
  assertEquals(IdentityDecision.DENY_STALE_MEMBERSHIP,validator.validate(staleGrant,scope,now,3))
  val revoked=consumerContext(grants=setOf(
   RoleGrant("grant-revoked","owner-1",AuthorityRole.DEVICE_OWNER,OperatingMode.CONSUMER,
    null,3,scope,900,2_000,RoleGrantStatus.REVOKED,"authority")
  ))
  assertEquals(IdentityDecision.DENY_ROLE_REVOKED,validator.validate(revoked,scope,now,3))
  val expired=consumerContext(grants=setOf(
   RoleGrant("grant-expired","owner-1",AuthorityRole.DEVICE_OWNER,OperatingMode.CONSUMER,
    null,3,scope,800,999,RoleGrantStatus.ACTIVE,"authority")
  ))
  assertEquals(IdentityDecision.DENY_ROLE_EXPIRED,validator.validate(expired,scope,now,3))
 }

 @Test fun enterpriseRoleMustMatchPrincipalAndTenantExactly() {
  val context=enterpriseContext(grantRole=AuthorityRole.ENTERPRISE_TENANT_OWNER)
  assertEquals(IdentityDecision.DENY_NO_EXPLICIT_ROLE,
   validator.validate(context,ResourceScope("device","device-1","tenant-1"),now,4))
 }

 @Test fun aiAgentCannotBecomeIdentityAuthority() {
  val base=consumerContext()
  val ai=base.copy(
   principal=Principal("agent",PrincipalKind.AI_AGENT),
   membership=base.membership.copy(principalId="agent")
  )
  assertEquals(IdentityDecision.DENY_UNSUPPORTED_PRINCIPAL,validator.validate(ai,scope,now,3))
 }
}
