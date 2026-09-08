package com.agrofront.agrohelper.data

import android.content.Context
import com.agrofront.agrohelper.domain.WorkRecord
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WorkJournalRepository(context: Context) {

    private val prefs = context.getSharedPreferences("agrohelper_journal", Context.MODE_PRIVATE)

    fun getAll(): List<WorkRecord> {
        val raw = prefs.getString(KEY_RECORDS, "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw
            .lineSequence()
            .mapNotNull(::decode)
            .sortedByDescending { it.id }
            .toList()
    }

    fun add(record: WorkRecord) {
        val records = getAll().toMutableList()
        records.add(record)
        saveAll(records)
    }

    fun delete(id: Long) {
        saveAll(getAll().filterNot { it.id == id })
    }

    fun clear() {
        prefs.edit().remove(KEY_RECORDS).apply()
    }

    private fun saveAll(records: List<WorkRecord>) {
        val encoded = records.joinToString("\n", transform = ::encode)
        prefs.edit().putString(KEY_RECORDS, encoded).apply()
    }

    private fun encode(record: WorkRecord): String =
        listOf(
            record.id.toString(),
            record.createdAt,
            record.fieldName,
            record.droneId,
            record.droneName,
            record.areaHa.toString(),
            record.solutionRateLHa.toString(),
            record.productName,
            record.productRatePerHa.toString(),
            record.productUnit,
            record.mixTankLiters.toString(),
            record.droneTankLiters.toString(),
            record.totalSolutionLiters.toString(),
            record.totalProductAmount.toString(),
            record.totalMixes.toString(),
            record.totalFlights.toString()
        ).joinToString("|") { escape(it) }

    private fun decode(line: String): WorkRecord? {
        val p = line.split("|")
        if (p.size != 16) return null

        return runCatching {
            WorkRecord(
                id = unescape(p[0]).toLong(),
                createdAt = unescape(p[1]),
                fieldName = unescape(p[2]),
                droneId = unescape(p[3]),
                droneName = unescape(p[4]),
                areaHa = unescape(p[5]).toDouble(),
                solutionRateLHa = unescape(p[6]).toDouble(),
                productName = unescape(p[7]),
                productRatePerHa = unescape(p[8]).toDouble(),
                productUnit = unescape(p[9]),
                mixTankLiters = unescape(p[10]).toDouble(),
                droneTankLiters = unescape(p[11]).toDouble(),
                totalSolutionLiters = unescape(p[12]).toDouble(),
                totalProductAmount = unescape(p[13]).toDouble(),
                totalMixes = unescape(p[14]).toInt(),
                totalFlights = unescape(p[15]).toInt()
            )
        }.getOrNull()
    }

    private fun escape(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

    private fun unescape(value: String): String =
        URLDecoder.decode(value, StandardCharsets.UTF_8.toString())

    private companion object {
        const val KEY_RECORDS = "records"
    }
}
