package org.akefadaka.aegis.security
import org.junit.Assert.*
import org.junit.Test
class SecurityStateTest {
 @Test fun unknownAndUnsupportedAreNotEnforced() {
  val p=SecurityPosture(mapOf("vpn" to CapabilityEvidence("vpn",CapabilityStatus.UNSUPPORTED,EvidenceSource.NONE)))
  assertFalse(p.capability("missing").isEnforced); assertFalse(p.capability("vpn").isEnforced)
  assertEquals(CapabilityStatus.UNKNOWN,p.capability("missing").status)
 }
 @Test(expected=IllegalArgumentException::class) fun ownerReportCannotClaimVerified() {
  CapabilityEvidence("strongbox",CapabilityStatus.VERIFIED,EvidenceSource.OWNER_REPORT)
 }
}
