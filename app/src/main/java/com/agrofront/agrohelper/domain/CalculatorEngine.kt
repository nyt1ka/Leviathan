package com.agrofront.agrohelper.domain

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

data class WorkCalculation(
    val totalSolutionLiters: Double,
    val totalProductAmount: Double,
    val fullMixes: Int,
    val lastMixLiters: Double,
    val totalMixes: Int,
    val fullFlights: Int,
    val lastFlightLiters: Double,
    val totalFlights: Int
)

data class BatteryStats(
    val averageSeconds: Int,
    val minSeconds: Int,
    val maxSeconds: Int,
    val sampleCount: Int
)

object CalculatorEngine {
    fun calculate(
        areaHa: Double,
        solutionRateLHa: Double,
        productRatePerHa: Double,
        mixTankLiters: Double,
        droneTankLiters: Double
    ): WorkCalculation {
        val area = areaHa.coerceAtLeast(0.0)
        val solutionRate = solutionRateLHa.coerceAtLeast(0.0)
        val productRate = productRatePerHa.coerceAtLeast(0.0)
        val mixTank = mixTankLiters.coerceAtLeast(0.0)
        val droneTank = droneTankLiters.coerceAtLeast(0.0)

        val totalSolution = area * solutionRate
        val totalProduct = area * productRate

        val fullMixes = if (mixTank > 0) floor(totalSolution / mixTank).toInt() else 0
        val lastMix = if (mixTank > 0) totalSolution % mixTank else 0.0
        val totalMixes = if (mixTank > 0 && totalSolution > 0) ceil(totalSolution / mixTank).toInt() else 0

        val fullFlights = if (droneTank > 0) floor(totalSolution / droneTank).toInt() else 0
        val lastFlight = if (droneTank > 0) totalSolution % droneTank else 0.0
        val totalFlights = if (droneTank > 0 && totalSolution > 0) ceil(totalSolution / droneTank).toInt() else 0

        return WorkCalculation(
            totalSolution, totalProduct,
            fullMixes, lastMix, totalMixes,
            fullFlights, lastFlight, totalFlights
        )
    }

    fun productivityHaPerHour(areaHa: Double, minutes: Double): Double {
        if (areaHa <= 0 || minutes <= 0) return 0.0
        return areaHa / (minutes / 60.0)
    }

    fun parseBatterySamples(input: String): List<Int> =
        input.split(',', ';', '\n', ' ', '\t')
            .mapNotNull { parseDurationToSeconds(it.trim()) }
            .filter { it > 0 }

    fun batteryStats(valuesSeconds: List<Int>): BatteryStats {
        val valid = valuesSeconds.filter { it > 0 }
        if (valid.isEmpty()) return BatteryStats(0, 0, 0, 0)
        return BatteryStats(
            averageSeconds = valid.average().roundToInt(),
            minSeconds = valid.minOrNull() ?: 0,
            maxSeconds = valid.maxOrNull() ?: 0,
            sampleCount = valid.size
        )
    }

    fun formatDuration(seconds: Int): String {
        if (seconds <= 0) return "00:00"
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }

    private fun parseDurationToSeconds(value: String): Int? {
        if (value.isBlank()) return null

        if (':' in value) {
            val parts = value.split(':')
            if (parts.size != 2) return null
            val minutes = parts[0].toIntOrNull() ?: return null
            val seconds = parts[1].toIntOrNull() ?: return null
            if (minutes < 0 || seconds !in 0..59) return null
            return minutes * 60 + seconds
        }

        val decimalMinutes = value.replace(',', '.').toDoubleOrNull() ?: return null
        if (decimalMinutes <= 0) return null
        return (decimalMinutes * 60).roundToInt()
    }
}
