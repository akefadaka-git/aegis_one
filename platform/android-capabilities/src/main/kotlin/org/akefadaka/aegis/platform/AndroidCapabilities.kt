package org.akefadaka.aegis.platform
import android.os.Build
import org.akefadaka.aegis.security.*

/** Reports runtime API only; does not infer Knox, StrongBox, attestation or management enrollment. */
class AndroidCapabilities {
 fun runtimeApi():Int=Build.VERSION.SDK_INT
 fun baseline():List<CapabilityEvidence> = listOf(
  CapabilityEvidence("android_runtime",CapabilityStatus.VERIFIED,EvidenceSource.PLATFORM_API,detailCode=Build.VERSION.SDK_INT.toString()),
  CapabilityEvidence("strongbox",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
  CapabilityEvidence("device_owner",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
  CapabilityEvidence("verified_boot",CapabilityStatus.UNKNOWN,EvidenceSource.NONE),
  CapabilityEvidence("knox",CapabilityStatus.UNKNOWN,EvidenceSource.NONE)
 )
}
