package com.leviathan.app

import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WebSocketManager(
    private val api: ApiClient,
    private val tokens: SecureTokenStore,
    private val onEvent: (String) -> Unit
) {
    private var socket: WebSocket? = null

    fun connect() {
        val token = tokens.accessToken() ?: return
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL.replace("https://", "wss://") + "/ws")
            .header("Authorization", "Bearer $token")
            .build()
        socket = api.http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send("{\"type\":\"ping\"}")
            }
            override fun onMessage(webSocket: WebSocket, text: String) = onEvent(text)
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onEvent("{\"type\":\"socket.error\",\"message\":${org.json.JSONObject.quote(t.message ?: "unknown")} }")
            }
        })
    }

    fun close() {
        socket?.close(1000, "client closed")
        socket = null
    }
}
