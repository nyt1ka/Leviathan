package com.leviathan.app

data class User(
    val id: Long,
    val username: String,
    val displayName: String,
    val role: String,
    val status: String
)

data class ChatMember(
    val id: Long,
    val username: String,
    val displayName: String,
    val memberRole: String
)

data class Chat(
    val id: String,
    val type: String,
    val title: String?,
    val createdBy: Long,
    val updatedAt: String,
    val members: List<ChatMember>
)

data class EncryptedMessage(
    val id: String,
    val chatId: String,
    val senderUserId: Long,
    val senderDeviceId: String,
    val clientMessageId: String,
    val algorithm: String,
    val nonce: String,
    val ciphertext: String,
    val createdAt: String
)

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
