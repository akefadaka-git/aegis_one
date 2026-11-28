package org.akefadaka.aegis.audit

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import org.akefadaka.aegis.audit.commoncrypto.aegis_sha256

@OptIn(ExperimentalForeignApi::class)
actual fun platformSha256(input: ByteArray): ByteArray {
    require(input.isNotEmpty())
    val digest = ByteArray(32)
    val status = input.usePinned { source ->
        digest.usePinned { destination ->
            aegis_sha256(
                source.addressOf(0),
                input.size.convert(),
                destination.addressOf(0)
            )
        }
    }
    check(status == 1) { "Apple CommonCrypto SHA-256 operation failed" }
    return digest
}
