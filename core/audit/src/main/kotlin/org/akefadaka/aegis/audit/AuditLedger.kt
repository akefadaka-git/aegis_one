package org.akefadaka.aegis.audit
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

enum class AuditEventType { POLICY_DECISION, CAPABILITY_CHANGE, OFFLINE_POLICY_REJECTED }
enum class AuditOutcome { ALLOW, DENY, REQUIRE_APPROVAL, ESCALATE }
/** Events carry only enumerated codes and a one-way pseudonymous actor reference; no free-form payloads. */
data class AuditEvent(
 val eventId:String,val eventType:AuditEventType,val actorRef:String,val outcome:AuditOutcome,val occurredAtEpochMillis:Long
) {
 init {
  require(eventId.matches(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")))
  require(actorRef.matches(Regex("[a-f0-9]{64}")))
  require(occurredAtEpochMillis>=0)
 }
}
data class ChainedAuditRecord(val sequence:Long,val event:AuditEvent,val previousDigest:String,val digest:String)
interface AuditStore { fun append(record:ChainedAuditRecord); fun readAll():List<ChainedAuditRecord> }
enum class AuditIntegrity { INTACT, GAP, TAMPERED }
class AuditLedger(private val store:AuditStore) {
 /** Single-instance serialization only; production stores must provide transactional single-writer semantics. */
 @Synchronized fun append(event:AuditEvent):ChainedAuditRecord {
  check(verify()==AuditIntegrity.INTACT) { "Refusing to append to an invalid audit chain" }
  val rows=store.readAll(); val seq=(rows.lastOrNull()?.sequence ?: 0L)+1
  val prev=rows.lastOrNull()?.digest ?: GENESIS
  return ChainedAuditRecord(seq,event,prev,hash(seq,event,prev)).also(store::append)
 }
 @Synchronized fun verify():AuditIntegrity {
  var prev=GENESIS; var seq=1L
  for(r in store.readAll()) {
   if(r.sequence!=seq) return AuditIntegrity.GAP
   if(r.previousDigest!=prev || r.digest!=hash(r.sequence,r.event,prev)) return AuditIntegrity.TAMPERED
   prev=r.digest; seq++
  }
  return AuditIntegrity.INTACT
 }
 private fun hash(seq:Long,e:AuditEvent,prev:String):String {
  val text=listOf(seq,e.eventId,e.eventType.name,e.actorRef,e.outcome.name,e.occurredAtEpochMillis,prev).joinToString("|")
  return MessageDigest.getInstance("SHA-256").digest(text.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
 }
 companion object { private const val GENESIS="AEGIS-AUDIT-GENESIS-v1" }
}
