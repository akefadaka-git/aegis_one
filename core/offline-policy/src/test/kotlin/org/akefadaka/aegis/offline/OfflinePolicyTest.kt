package org.akefadaka.aegis.offline
import org.akefadaka.aegis.security.SecurityTier
import org.junit.Assert.*
import org.junit.Test
class OfflinePolicyTest {
 private val p=CachedPolicy("p",2,1000,999999999,SecurityTier.P1,"device","issuer",byteArrayOf(1))
 private val v=OfflinePolicyValidator(PolicySignatureVerifier { true })
 @Test fun capsP1AtSevenDays() {
  val r=v.validate(p,2000,TimeConfidence.TRUSTED,"device",1)
  assertEquals(PolicyState.ACCEPTED,r.state); assertEquals(1000+7L*24*60*60*1000,r.effectiveUntilEpochMillis)
 }
 @Test fun rejectsClockRollbackScopeAndSignature() {
  assertEquals(PolicyState.REJECTED_CLOCK,v.validate(p,2000,TimeConfidence.UNCERTAIN,"device",1).state)
  assertEquals(PolicyState.REJECTED_ROLLBACK,v.validate(p,2000,TimeConfidence.TRUSTED,"device",3).state)
  assertEquals(PolicyState.REJECTED_SCOPE,v.validate(p,2000,TimeConfidence.TRUSTED,"other",1).state)
  assertEquals(PolicyState.REJECTED_SIGNATURE,OfflinePolicyValidator(PolicySignatureVerifier { false }).validate(p,2000,TimeConfidence.TRUSTED,"device",1).state)
 }
 @Test fun p2ExpiresWithin24HoursAndP3IsNotCachedOffline() {
  val p2=p.copy(tier=SecurityTier.P2)
  assertEquals(1000+24L*60*60*1000,v.validate(p2,2000,TimeConfidence.TRUSTED,"device",1).effectiveUntilEpochMillis)
  assertEquals(PolicyState.EXPIRED,v.validate(p.copy(tier=SecurityTier.P3),2000,TimeConfidence.TRUSTED,"device",1).state)
 }
}
