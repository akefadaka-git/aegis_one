package org.akefadaka.aegis.audit
import org.junit.Assert.*
import org.junit.Test
class AuditLedgerTest {
 private class Store:AuditStore {
  val rows=mutableListOf<ChainedAuditRecord>()
  override fun append(record:ChainedAuditRecord){ rows.add(record) }
  override fun readAll()=rows.toList()
 }
 private fun event(outcome:AuditOutcome=AuditOutcome.DENY)=AuditEvent(
  "00000000-0000-4000-8000-000000000001",AuditEventType.POLICY_DECISION,"a".repeat(64),outcome,10
 )
 @Test fun detectsTamperingAndValidChain() {
  val s=Store(); val l=AuditLedger(s)
  l.append(event())
  assertEquals(AuditIntegrity.INTACT,l.verify())
  s.rows[0]=s.rows[0].copy(event=event(AuditOutcome.ALLOW))
  assertEquals(AuditIntegrity.TAMPERED,l.verify())
 }
 @Test fun detectsSequenceGaps() {
  val s=Store(); val l=AuditLedger(s)
  l.append(event())
  s.rows[0]=s.rows[0].copy(sequence=2)
  assertEquals(AuditIntegrity.GAP,l.verify())
 }
 @Test(expected=IllegalStateException::class) fun refusesAppendAfterTampering() {
  val s=Store(); val l=AuditLedger(s)
  l.append(event()); s.rows[0]=s.rows[0].copy(digest="0".repeat(64))
  l.append(event(AuditOutcome.ALLOW))
 }
 @Test fun rejectsRawPayloadsAndInvalidEventIds() {
  try { AuditEvent("not-a-uuid",AuditEventType.POLICY_DECISION,"a".repeat(64),AuditOutcome.DENY,1); fail("invalid event ID accepted") }
  catch (_:IllegalArgumentException) { }
  try { AuditEvent("00000000-0000-4000-8000-000000000002",AuditEventType.POLICY_DECISION,"password=secret",AuditOutcome.DENY,1); fail("raw payload accepted") }
  catch (_:IllegalArgumentException) { }
 }
}
