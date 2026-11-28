package org.akefadaka.aegis.audit

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CommonCrypto.CC_SHA256

@OptIn(ExperimentalForeignApi::class)
actual fun platformSha256(input: ByteArray): ByteArray {
    require(input.isNotEmpty())
    val digest = ByteArray(32)
    input.usePinned { source ->
        digest.usePinned { destination ->
            CC_SHA256(
                source.addressOf(0),
                input.size.convert(),
                destination.addressOf(0)
            )
        }
    }
    return digest
}
