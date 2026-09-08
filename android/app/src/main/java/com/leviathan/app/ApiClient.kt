package com.leviathan.app

import com.leviathan.app.BuildConfig.API_BASE_URL
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class ApiClient(private val tokens: SecureTokenStore) {
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private fun authedBuilder(path: String): Request.Builder {
        val token = requireNotNull(tokens.accessToken()) { "Not authenticated" }
        return Request.Builder()
            .url(API_BASE_URL + path)
            .header("Authorization", "Bearer $token")
    }

    fun login(username: String, password: String, deviceId: String): LoginResult {
        val body = JSONObject()
            .put("username", username)
            .put("password", password)
            .put("deviceId", deviceId)
            .put("deviceName", "Leviathan Android")
            .toString()
            .toRequestBody(jsonType)

        val response = http.newCall(
            Request.Builder().url("$API_BASE_URL/auth/login").post(body).build()
        ).execute()

        response.use {
            val text = it.body.string()
            if (!it.isSuccessful) throw IllegalStateException("Login failed (${it.code}): $text")
            val json = JSONObject(text)
            val userJson = json.getJSONObject("user")
            return LoginResult(
                accessToken = json.getString("accessToken"),
                refreshToken = json.getString("refreshToken"),
                user = User(
                    id = userJson.getLong("id"),
                    username = userJson.getString("username"),
                    displayName = userJson.getString("displayName"),
                    role = userJson.getString("role"),
                    status = userJson.getString("status")
                )
            )
        }
    }

    fun chats(): List<Chat> {
        val response = http.newCall(authedBuilder("/chats").get().build()).execute()
        response.use {
            val text = it.body.string()
            if (!it.isSuccessful) throw IllegalStateException("Chats failed (${it.code}): $text")
            val array = JSONArray(text)
            return (0 until array.length()).map { i ->
                val j = array.getJSONObject(i)
                val members = j.getJSONArray("members")
                Chat(
                    id = j.getString("id"),
                    type = j.getString("type"),
                    title = if (j.isNull("title")) null else j.getString("title"),
                    createdBy = j.getLong("created_by"),
                    updatedAt = j.getString("updated_at"),
                    members = (0 until members.length()).map { m ->
                        val u = members.getJSONObject(m)
                        ChatMember(
                            id = u.getLong("id"),
                            username = u.getString("username"),
                            displayName = u.getString("displayName"),
                            memberRole = u.getString("memberRole")
                        )
                    }
                )
            }
        }
    }

    fun messages(chatId: String): List<EncryptedMessage> {
        val response = http.newCall(authedBuilder("/chats/$chatId/messages").get().build()).execute()
        response.use {
            val text = it.body.string()
            if (!it.isSuccessful) throw IllegalStateException("Messages failed (${it.code}): $text")
            val array = JSONArray(text)
            return (0 until array.length()).map { i -> encryptedMessage(array.getJSONObject(i)) }
        }
    }

    fun sendEncrypted(chatId: String, algorithm: String, nonce: String, ciphertext: String): EncryptedMessage {
        require(algorithm != "TEST-ONLY") { "TEST-ONLY payloads are disabled in Android client" }
        val body = JSONObject()
            .put("clientMessageId", UUID.randomUUID().toString())
            .put("algorithm", algorithm)
            .put("nonce", nonce)
            .put("ciphertext", ciphertext)
            .toString()
            .toRequestBody(jsonType)
        val response = http.newCall(authedBuilder("/chats/$chatId/messages").post(body).build()).execute()
        response.use {
            val text = it.body.string()
            if (!it.isSuccessful) throw IllegalStateException("Send failed (${it.code}): $text")
            return encryptedMessage(JSONObject(text))
        }
    }

    private fun encryptedMessage(j: JSONObject) = EncryptedMessage(
        id = j.getString("id"),
        chatId = j.getString("chat_id"),
        senderUserId = j.getLong("sender_user_id"),
        senderDeviceId = j.getString("sender_device_id"),
        clientMessageId = j.getString("client_message_id"),
        algorithm = j.getString("algorithm"),
        nonce = j.getString("nonce"),
        ciphertext = j.getString("ciphertext"),
        createdAt = j.getString("created_at")
    )
}
