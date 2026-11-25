package org.akefadaka.aegis.audit
import org.junit.Assert.*
import org.junit.Test
class AuditLedgerTest {
 private class Store:AuditStore {
  val rows=mutableListOf<ChainedAuditRecord>()
  override fun append(record:ChainedAuditRecord){ rows.add(record) }
  override fun readAll()=rows.toList()
 }
 @Test fun detectsTamperingAndValidChain() {
  val s=Store(); val l=AuditLedger(s)
  l.append(AuditEvent("1","POLICY_CHECK","owner-ref","DENY",10))
  assertEquals(AuditIntegrity.INTACT,l.verify())
  s.rows[0]=s.rows[0].copy(event=s.rows[0].event.copy(outcomeCode="ALLOW"))
  assertEquals(AuditIntegrity.TAMPERED,l.verify())
 }
 @Test(expected=IllegalArgumentException::class) fun rejectsFreeFormSecretLikeValues() {
  AuditEvent("1","EVENT","password=secret","DENY",1)
 }
}
