package com.agrofront.agrohelper.domain

import kotlin.math.ceil
import kotlin.math.floor

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

object CalculatorEngine {

    fun calculate(
        areaHa: Double,
        solutionRateLHa: Double,
        productRatePerHa: Double,
        mixTankLiters: Double,
        droneTankLiters: Double
    ): WorkCalculation {
        val totalSolution = areaHa * solutionRateLHa
        val totalProduct = areaHa * productRatePerHa

        val fullMixes = if (mixTankLiters > 0) floor(totalSolution / mixTankLiters).toInt() else 0
        val lastMix = if (mixTankLiters > 0) totalSolution % mixTankLiters else 0.0
        val totalMixes = if (mixTankLiters > 0 && totalSolution > 0) ceil(totalSolution / mixTankLiters).toInt() else 0

        val fullFlights = if (droneTankLiters > 0) floor(totalSolution / droneTankLiters).toInt() else 0
        val lastFlight = if (droneTankLiters > 0) totalSolution % droneTankLiters else 0.0
        val totalFlights = if (droneTankLiters > 0 && totalSolution > 0) ceil(totalSolution / droneTankLiters).toInt() else 0

        return WorkCalculation(
            totalSolutionLiters = totalSolution,
            totalProductAmount = totalProduct,
            fullMixes = fullMixes,
            lastMixLiters = lastMix,
            totalMixes = totalMixes,
            fullFlights = fullFlights,
            lastFlightLiters = lastFlight,
            totalFlights = totalFlights
        )
    }

    fun productivityHaPerHour(areaHa: Double, minutes: Double): Double {
        if (areaHa <= 0 || minutes <= 0) return 0.0
        return areaHa / (minutes / 60.0)
    }

    fun averageBatteryMinutes(values: List<Double>): Double {
        val valid = values.filter { it > 0 }
        return if (valid.isEmpty()) 0.0 else valid.average()
    }
}
