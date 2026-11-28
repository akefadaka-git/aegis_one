package org.akefadaka.aegis.security

import org.junit.Assert.*
import org.junit.Test

class SecurityStateTest {
 @Test fun unknownAndUnsupportedAreNotEnforced() {
  val p=SecurityPosture(mapOf("vpn" to CapabilityEvidence("vpn",CapabilityStatus.UNSUPPORTED,EvidenceSource.NONE)))
  assertFalse(p.isEnforced("missing",10_000,5_000))
  assertFalse(p.isEnforced("vpn",10_000,5_000))
  assertEquals(CapabilityStatus.UNKNOWN,p.capability("missing").status)
  assertEquals(SecurityErrorCode.CAPABILITY_UNKNOWN,p.errorFor("missing",10_000,5_000)?.code)
 }

 @Test fun ownerReportCannotClaimVerified() {
  try {
   CapabilityEvidence("strongbox",CapabilityStatus.VERIFIED,EvidenceSource.OWNER_REPORT)
   fail("owner report must not verify a capability")
  } catch (_:IllegalArgumentException) { }
 }

 @Test fun testFixtureCannotClaimRuntimeEnforcement() {
  val fixture=CapabilityEvidence("device_owner",CapabilityStatus.VERIFIED,EvidenceSource.TEST_FIXTURE,observedAtEpochMillis=9_500)
  assertFalse(fixture.isEnforcedAt(10_000,5_000))
  assertEquals(SecurityErrorCode.EVIDENCE_NOT_VERIFIED,
   SecurityPosture(mapOf("device_owner" to fixture)).errorFor("device_owner",10_000,5_000)?.code)
 }

 @Test fun freshPlatformEvidenceCanAssertOnlyItsNamedCapability() {
  val evidence=CapabilityEvidence("android_runtime",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,
   observedAtEpochMillis=9_500,validUntilEpochMillis=20_000,supportedActions=setOf("read_runtime_api"),
   exclusions=setOf("device_owner","strongbox"))
  val posture=SecurityPosture(mapOf("android_runtime" to evidence))
  assertTrue(posture.isEnforced("android_runtime",10_000,5_000))
  assertFalse(posture.isEnforced("android_runtime",15_000,5_000))
 }

 @Test fun staleAndFutureEvidenceCannotAssertEnforcement() {
  val stale=CapabilityEvidence("vpn",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,observedAtEpochMillis=1_000)
  val future=CapabilityEvidence("boot",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,observedAtEpochMillis=11_000)
  assertFalse(stale.isEnforcedAt(10_000,5_000))
  assertFalse(future.isEnforcedAt(10_000,5_000))
  assertEquals(SecurityErrorCode.EVIDENCE_STALE,
   SecurityPosture(mapOf("vpn" to stale)).errorFor("vpn",10_000,5_000)?.code)
 }

 @Test fun permissionAndEnrollmentRequirementsAreExplicit() {
  val permission=CapabilityEvidence("vpn",CapabilityStatus.REQUIRES_PERMISSION,EvidenceSource.PLATFORM_API,
   requiredPermissions=setOf("android.permission.BIND_VPN_SERVICE"))
  val enrollment=CapabilityEvidence("managed_profile",CapabilityStatus.REQUIRES_ENROLLMENT,EvidenceSource.PLATFORM_API,
   requiresEnrollment=true)
  assertEquals(SecurityErrorCode.PERMISSION_REQUIRED,
   SecurityPosture(mapOf("vpn" to permission)).errorFor("vpn",10_000,5_000)?.code)
  assertEquals(SecurityErrorCode.ENROLLMENT_REQUIRED,
   SecurityPosture(mapOf("managed_profile" to enrollment)).errorFor("managed_profile",10_000,5_000)?.code)
 }

 @Test fun permissionAndEnrollmentStatesRequireTheirPrerequisites() {
  try {
   CapabilityEvidence("vpn",CapabilityStatus.REQUIRES_PERMISSION,EvidenceSource.PLATFORM_API)
   fail("permission state requires a named permission or entitlement")
  } catch (_:IllegalArgumentException) { }
  try {
   CapabilityEvidence("managed",CapabilityStatus.REQUIRES_ENROLLMENT,EvidenceSource.PLATFORM_API)
   fail("enrollment state requires enrollment flag")
  } catch (_:IllegalArgumentException) { }
 }

 @Test fun manifestCarriesPlatformPolicyAndFreshnessBoundary() {
  val evidence=CapabilityEvidence("android_runtime",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,
   observedAtEpochMillis=9_500)
  val manifest=CapabilityManifest(
   PlatformDescriptor(PlatformFamily.ANDROID,"17",apiLevel=36,architecture="arm64-v8a"),
   "capability-policy-v1",10_000,5_000,mapOf("android_runtime" to evidence)
  )
  assertTrue(manifest.isEnforced("android_runtime",10_000))
  assertEquals(PlatformFamily.ANDROID,manifest.platform.family)
  assertEquals(SecurityErrorCode.CAPABILITY_UNKNOWN,manifest.errorFor("knox",10_000)?.code)
 }

 @Test fun manifestRejectsFutureEvidenceAndMismatchedKeys() {
  try {
   CapabilityManifest(PlatformDescriptor(PlatformFamily.ANDROID,"17"),"v1",10_000,5_000,
    mapOf("vpn" to CapabilityEvidence("vpn",CapabilityStatus.UNKNOWN,EvidenceSource.NONE,observedAtEpochMillis=10_001)))
   fail("manifest cannot contain evidence newer than its generation time")
  } catch (_:IllegalArgumentException) { }
  try {
   CapabilityManifest(PlatformDescriptor(PlatformFamily.ANDROID,"17"),"v1",10_000,5_000,
    mapOf("wrong-key" to CapabilityEvidence("vpn",CapabilityStatus.UNKNOWN,EvidenceSource.NONE)))
   fail("manifest key must match capability identifier")
  } catch (_:IllegalArgumentException) { }
 }
}
