package com.agrofront.agrohelper.data

import android.content.Context
import com.agrofront.agrohelper.domain.FieldRecord
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class FieldRepository(context: Context) {
    private val prefs = context.getSharedPreferences("agrohelper_fields", Context.MODE_PRIVATE)

    fun getAll(): List<FieldRecord> {
        val raw = prefs.getString(KEY_FIELDS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.lineSequence()
            .mapNotNull(::decode)
            .sortedBy { it.name.lowercase() }
            .toList()
    }

    fun add(field: FieldRecord) {
        saveAll(getAll() + field)
    }

    fun delete(id: Long) {
        saveAll(getAll().filterNot { it.id == id })
    }

    private fun saveAll(fields: List<FieldRecord>) {
        prefs.edit()
            .putString(KEY_FIELDS, fields.joinToString("\n", transform = ::encode))
            .apply()
    }

    private fun encode(field: FieldRecord): String =
        listOf(
            field.id.toString(),
            field.name,
            field.areaHa.toString(),
            field.notes
        ).joinToString("|") { escape(it) }

    private fun decode(line: String): FieldRecord? {
        val p = line.split("|")
        if (p.size != 4) return null
        return runCatching {
            FieldRecord(
                id = unescape(p[0]).toLong(),
                name = unescape(p[1]),
                areaHa = unescape(p[2]).toDouble(),
                notes = unescape(p[3])
            )
        }.getOrNull()
    }

    private fun escape(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    private fun unescape(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.toString())

    private companion object {
        const val KEY_FIELDS = "fields"
    }
}
