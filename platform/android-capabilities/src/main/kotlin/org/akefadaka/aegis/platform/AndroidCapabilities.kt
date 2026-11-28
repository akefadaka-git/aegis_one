package org.akefadaka.aegis.platform
import android.os.Build
import org.akefadaka.aegis.security.*

/** Reports runtime API only; does not infer Knox, StrongBox, attestation or management enrollment. */
class AndroidCapabilities {
 fun runtimeApi():Int=Build.VERSION.SDK_INT
 fun baseline():List<CapabilityEvidence> {
  val observedAt=System.currentTimeMillis()
  return listOf(
   CapabilityEvidence("android_runtime",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,
    observedAtEpochMillis=observedAt,detailCode=Build.VERSION.SDK_INT.toString(),
    supportedActions=setOf("read_runtime_api"),
    exclusions=setOf("strongbox","device_owner","verified_boot","knox")),
   CapabilityEvidence("strongbox",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
   CapabilityEvidence("device_owner",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
   CapabilityEvidence("verified_boot",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
   CapabilityEvidence("knox",CapabilityStatus.UNKNOWN,EvidenceSource.NONE)
  )
 }
}
