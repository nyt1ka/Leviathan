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
    val productPerFullMix: Double,
    val productForLastMix: Double,
    val fullFlights: Int,
    val lastFlightLiters: Double,
    val totalFlights: Int,
    val productPerFullFlight: Double,
    val productForLastFlight: Double
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

        val fullMixes = if (mixTank > 0.0) floor(totalSolution / mixTank).toInt() else 0
        val lastMix = if (mixTank > 0.0) normalizedRemainder(totalSolution, mixTank) else 0.0
        val totalMixes = if (mixTank > 0.0 && totalSolution > 0.0) {
            ceil(totalSolution / mixTank).toInt()
        } else {
            0
        }

        val fullFlights = if (droneTank > 0.0) floor(totalSolution / droneTank).toInt() else 0
        val lastFlight = if (droneTank > 0.0) normalizedRemainder(totalSolution, droneTank) else 0.0
        val totalFlights = if (droneTank > 0.0 && totalSolution > 0.0) {
            ceil(totalSolution / droneTank).toInt()
        } else {
            0
        }

        return WorkCalculation(
            totalSolutionLiters = totalSolution,
            totalProductAmount = totalProduct,
            fullMixes = fullMixes,
            lastMixLiters = lastMix,
            totalMixes = totalMixes,
            productPerFullMix = productForSolutionVolume(
                solutionVolumeLiters = if (fullMixes > 0) mixTank else 0.0,
                solutionRateLHa = solutionRate,
                productRatePerHa = productRate
            ),
            productForLastMix = productForSolutionVolume(
                solutionVolumeLiters = lastMix,
                solutionRateLHa = solutionRate,
                productRatePerHa = productRate
            ),
            fullFlights = fullFlights,
            lastFlightLiters = lastFlight,
            totalFlights = totalFlights,
            productPerFullFlight = productForSolutionVolume(
                solutionVolumeLiters = if (fullFlights > 0) droneTank else 0.0,
                solutionRateLHa = solutionRate,
                productRatePerHa = productRate
            ),
            productForLastFlight = productForSolutionVolume(
                solutionVolumeLiters = lastFlight,
                solutionRateLHa = solutionRate,
                productRatePerHa = productRate
            )
        )
    }

    fun productForSolutionVolume(
        solutionVolumeLiters: Double,
        solutionRateLHa: Double,
        productRatePerHa: Double
    ): Double {
        if (solutionVolumeLiters <= 0.0 || solutionRateLHa <= 0.0 || productRatePerHa <= 0.0) {
            return 0.0
        }

        val coveredAreaHa = solutionVolumeLiters / solutionRateLHa
        return coveredAreaHa * productRatePerHa
    }

    fun productivityHaPerHour(areaHa: Double, minutes: Double): Double {
        if (areaHa <= 0.0 || minutes <= 0.0) return 0.0
        return areaHa / (minutes / 60.0)
    }

    /**
     * Форматы:
     * 90       -> 90 минут
     * 1:30     -> 1 час 30 минут
     * 01:30    -> 1 час 30 минут
     */
    fun parseWorkDurationMinutes(input: String): Double {
        val value = input.trim()
        if (value.isBlank()) return 0.0

        if (':' in value) {
            val parts = value.split(':')
            if (parts.size != 2) return 0.0

            val hours = parts[0].toIntOrNull() ?: return 0.0
            val minutes = parts[1].toIntOrNull() ?: return 0.0

            if (hours < 0 || minutes !in 0..59) return 0.0
            return hours * 60.0 + minutes
        }

        return value.replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    }

    /**
     * Формат замеров АКБ: мм:сс.
     * Разделители: пробел, запятая, точка с запятой, новая строка.
     */
    fun parseBatterySamples(input: String): List<Int> =
        input.split(Regex("""[\s,;]+"""))
            .mapNotNull { parseBatteryDurationToSeconds(it.trim()) }
            .filter { it > 0 }

    fun batteryStats(valuesSeconds: List<Int>): BatteryStats {
        val valid = valuesSeconds.filter { it > 0 }
        if (valid.isEmpty()) {
            return BatteryStats(
                averageSeconds = 0,
                minSeconds = 0,
                maxSeconds = 0,
                sampleCount = 0
            )
        }

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

    private fun parseBatteryDurationToSeconds(value: String): Int? {
        if (value.isBlank()) return null

        val parts = value.split(':')
        if (parts.size != 2) return null

        val minutes = parts[0].toIntOrNull() ?: return null
        val seconds = parts[1].toIntOrNull() ?: return null

        if (minutes < 0 || seconds !in 0..59) return null
        return minutes * 60 + seconds
    }

    private fun normalizedRemainder(total: Double, capacity: Double): Double {
        if (total <= 0.0 || capacity <= 0.0) return 0.0

        val remainder = total % capacity
        return if (remainder < 0.000001 || capacity - remainder < 0.000001) {
            0.0
        } else {
            remainder
        }
    }
}
