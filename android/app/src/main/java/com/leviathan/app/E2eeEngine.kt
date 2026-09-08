package com.leviathan.app

/**
 * Boundary for the Signal Protocol integration.
 *
 * v0.5 intentionally does not invent cryptography. The app already depends on
 * Signal's libsignal runtime, but key generation, pre-key publication, session
 * establishment, Double Ratchet persistence and multi-device handling are added
 * only together with the matching backend key-bundle API.
 */
interface E2eeEngine {
    suspend fun encrypt(recipientUserId: Long, plaintext: ByteArray): CipherEnvelope
    suspend fun decrypt(senderUserId: Long, envelope: CipherEnvelope): ByteArray
}

data class CipherEnvelope(
    val algorithm: String,
    val nonce: String,
    val ciphertext: String
)

class E2eeNotReadyException : IllegalStateException(
    "E2EE session is not established yet; plaintext must never be sent to the API"
)

class PendingSignalE2eeEngine : E2eeEngine {
    override suspend fun encrypt(recipientUserId: Long, plaintext: ByteArray): CipherEnvelope =
        throw E2eeNotReadyException()

    override suspend fun decrypt(senderUserId: Long, envelope: CipherEnvelope): ByteArray =
        throw E2eeNotReadyException()
}
