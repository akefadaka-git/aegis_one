package org.akefadaka.aegis.audit
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/** Minimized event contract; identifier fields reject free-form text and secret-like payloads. */
data class AuditEvent(val eventId:String,val eventType:String,val actorRef:String,val outcomeCode:String,val occurredAtEpochMillis:Long) {
 init {
  val fields=listOf(eventId,eventType,actorRef,outcomeCode)
  require(fields.all { it.matches(Regex("[A-Za-z0-9][A-Za-z0-9._:-]{0,127}")) })
 }
}
data class ChainedAuditRecord(val sequence:Long,val event:AuditEvent,val previousDigest:String,val digest:String)
interface AuditStore { fun append(record:ChainedAuditRecord); fun readAll():List<ChainedAuditRecord> }
enum class AuditIntegrity { INTACT, GAP, TAMPERED }
class AuditLedger(private val store:AuditStore) {
 fun append(event:AuditEvent):ChainedAuditRecord {
  val rows=store.readAll(); val seq=(rows.lastOrNull()?.sequence ?: 0L)+1
  val prev=rows.lastOrNull()?.digest ?: GENESIS
  return ChainedAuditRecord(seq,event,prev,hash(seq,event,prev)).also(store::append)
 }
 fun verify():AuditIntegrity {
  var prev=GENESIS; var seq=1L
  for(r in store.readAll()) {
   if(r.sequence!=seq) return AuditIntegrity.GAP
   if(r.previousDigest!=prev || r.digest!=hash(r.sequence,r.event,prev)) return AuditIntegrity.TAMPERED
   prev=r.digest; seq++
  }
  return AuditIntegrity.INTACT
 }
 private fun hash(seq:Long,e:AuditEvent,prev:String):String {
  val text=listOf(seq,e.eventId,e.eventType,e.actorRef,e.outcomeCode,e.occurredAtEpochMillis,prev).joinToString("|")
  return MessageDigest.getInstance("SHA-256").digest(text.toByteArray(StandardCharsets.UTF_8)).joinToString("") { "%02x".format(it) }
 }
 companion object { private const val GENESIS="AEGIS-AUDIT-GENESIS-v1" }
}
