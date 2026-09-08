package com.agrofront.agrohelper.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {
    @Test
    fun fullCalculationWorks() {
        val result = CalculatorEngine.calculate(125.0, 10.0, 0.05, 500.0, 40.0)

        assertEquals(1250.0, result.totalSolutionLiters, 0.0001)
        assertEquals(6.25, result.totalProductAmount, 0.0001)
        assertEquals(3, result.totalMixes)
        assertEquals(250.0, result.lastMixLiters, 0.0001)
        assertEquals(32, result.totalFlights)
        assertEquals(10.0, result.lastFlightLiters, 0.0001)
        assertEquals(2.5, result.productPerFullMix, 0.0001)
        assertEquals(1.25, result.productForLastMix, 0.0001)
        assertEquals(0.2, result.productPerFullFlight, 0.0001)
        assertEquals(0.05, result.productForLastFlight, 0.0001)
    }

    @Test
    fun batteryMmSsWorks() {
        val stats = CalculatorEngine.batteryStats(
            CalculatorEngine.parseBatterySamples("12:40 13:05 11:55 12:30")
        )

        assertEquals(4, stats.sampleCount)
        assertEquals("12:33", CalculatorEngine.formatDuration(stats.averageSeconds))
        assertEquals("11:55", CalculatorEngine.formatDuration(stats.minSeconds))
        assertEquals("13:05", CalculatorEngine.formatDuration(stats.maxSeconds))
    }

    @Test
    fun durationSupportsHoursAndMinutes() {
        assertEquals(90.0, CalculatorEngine.parseWorkDurationMinutes("1:30"), 0.0001)
        assertEquals(90.0, CalculatorEngine.parseWorkDurationMinutes("90"), 0.0001)
    }
}
