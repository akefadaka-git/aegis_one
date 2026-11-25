package org.akefadaka.aegis.security
import org.junit.Assert.*
import org.junit.Test
class SecurityStateTest {
 @Test fun unknownAndUnsupportedAreNotEnforced() {
  val p=SecurityPosture(mapOf("vpn" to CapabilityEvidence("vpn",CapabilityStatus.UNSUPPORTED,EvidenceSource.NONE)))
  assertFalse(p.capability("missing").isEnforced); assertFalse(p.capability("vpn").isEnforced)
  assertEquals(CapabilityStatus.UNKNOWN,p.capability("missing").status)
 }
 @Test fun ownerReportCannotClaimVerified() {
  try { CapabilityEvidence("strongbox",CapabilityStatus.VERIFIED,EvidenceSource.OWNER_REPORT); fail("owner report must not verify a capability") }
  catch (_:IllegalArgumentException) { }
 }
 @Test fun testFixtureCannotClaimRuntimeEnforcement() {
  val fixture=CapabilityEvidence("device_owner",CapabilityStatus.VERIFIED,EvidenceSource.TEST_FIXTURE)
  assertFalse(fixture.isEnforced)
 }
}
