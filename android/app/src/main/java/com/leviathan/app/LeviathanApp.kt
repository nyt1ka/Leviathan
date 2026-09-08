package com.leviathan.app

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun LeviathanApp(context: Context) {
    MaterialTheme {
        val tokens = remember { SecureTokenStore(context) }
        val api = remember { ApiClient(tokens) }
        var loggedIn by remember { mutableStateOf(tokens.accessToken() != null) }
        var selectedChat by remember { mutableStateOf<Chat?>(null) }

        when {
            !loggedIn -> LoginScreen(api, tokens) { loggedIn = true }
            selectedChat == null -> ChatsScreen(api, tokens, onChat = { selectedChat = it }, onLogout = {
                tokens.clear(); loggedIn = false
            })
            else -> ChatScreen(api, selectedChat!!, onBack = { selectedChat = null })
        }
    }
}

@Composable
private fun LoginScreen(api: ApiClient, tokens: SecureTokenStore, onLoggedIn: () -> Unit) {
    val scope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val deviceId = remember { UUID.randomUUID().toString() }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Leviathan", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(username, { username = it }, label = { Text("Логин") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            password, { password = it }, label = { Text("Пароль") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth()
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 12.dp)) }
        Button(
            enabled = !busy && username.isNotBlank() && password.isNotBlank(),
            onClick = {
                busy = true; error = null
                scope.launch {
                    runCatching { withContext(Dispatchers.IO) { api.login(username, password, deviceId) } }
                        .onSuccess { result -> tokens.save(result.accessToken, result.refreshToken); onLoggedIn() }
                        .onFailure { error = it.message ?: "Ошибка входа" }
                    password = ""
                    busy = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) { Text(if (busy) "Вход…" else "Войти") }
    }
}

@Composable
private fun ChatsScreen(api: ApiClient, tokens: SecureTokenStore, onChat: (Chat) -> Unit, onLogout: () -> Unit) {
    val scope = rememberCoroutineScope()
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var status by remember { mutableStateOf("Загрузка…") }
    var socketStatus by remember { mutableStateOf("WebSocket: подключение…") }
    val ws = remember {
        WebSocketManager(api, tokens) { event -> socketStatus = "WebSocket: $event" }
    }

    fun reload() {
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { api.chats() } }
                .onSuccess { chats = it; status = if (it.isEmpty()) "Чатов пока нет" else "" }
                .onFailure { status = it.message ?: "Ошибка загрузки" }
        }
    }

    LaunchedEffect(Unit) { reload(); ws.connect() }
    DisposableEffect(Unit) { onDispose { ws.close() } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Чаты", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) { Text("Выйти") }
        }
        Text(socketStatus, style = MaterialTheme.typography.bodySmall)
        if (status.isNotEmpty()) Text(status, modifier = Modifier.padding(vertical = 16.dp))
        LazyColumn {
            items(chats, key = { it.id }) { chat ->
                val peer = chat.members.firstOrNull { it.memberRole != "OWNER" } ?: chat.members.firstOrNull()
                ListItem(
                    headlineContent = { Text(chat.title ?: peer?.displayName ?: "Диалог") },
                    supportingContent = { Text(chat.type) },
                    modifier = Modifier.fillMaxWidth().clickable { onChat(chat) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ChatScreen(api: ApiClient, chat: Chat, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<EncryptedMessage>>(emptyList()) }
    var status by remember { mutableStateOf("Загрузка…") }

    LaunchedEffect(chat.id) {
        runCatching { withContext(Dispatchers.IO) { api.messages(chat.id) } }
            .onSuccess { messages = it; status = "" }
            .onFailure { status = it.message ?: "Ошибка" }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Назад") }
        Text(chat.title ?: "Защищённый диалог", style = MaterialTheme.typography.headlineSmall)
        Text("Отправка текста отключена до установки Signal-сессии.", style = MaterialTheme.typography.bodySmall)
        if (status.isNotEmpty()) Text(status, modifier = Modifier.padding(16.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(messages, key = { it.id }) { msg ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Зашифрованное сообщение", style = MaterialTheme.typography.titleSmall)
                        Text("${msg.algorithm} • ${msg.createdAt}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        Button(onClick = { status = "E2EE ещё не инициализировано — plaintext не отправлен." }, modifier = Modifier.fillMaxWidth()) {
            Text("Отправить (будет доступно после E2EE)")
        }
    }
}
