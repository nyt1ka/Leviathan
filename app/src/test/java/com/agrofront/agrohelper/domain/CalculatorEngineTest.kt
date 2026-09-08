package com.agrofront.agrohelper.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {
    @Test
    fun exactMultipleHasNoRemainder() {
        val r = CalculatorEngine.calculate(100.0, 10.0, 0.5, 500.0, 40.0)
        assertEquals(2, r.totalMixes)
        assertEquals(0.0, r.lastMixLiters, 0.0001)
        assertEquals(25, r.totalFlights)
        assertEquals(0.0, r.lastFlightLiters, 0.0001)
    }

    @Test
    fun partialMixAndFlightAreCalculated() {
        val r = CalculatorEngine.calculate(125.0, 10.0, 0.05, 500.0, 40.0)
        assertEquals(1250.0, r.totalSolutionLiters, 0.0001)
        assertEquals(3, r.totalMixes)
        assertEquals(250.0, r.lastMixLiters, 0.0001)
        assertEquals(32, r.totalFlights)
        assertEquals(10.0, r.lastFlightLiters, 0.0001)
    }

    @Test
    fun batteryMmSsWorks() {
        val values = CalculatorEngine.parseBatterySamples("12:40 13:05 11:55 12:30")
        val stats = CalculatorEngine.batteryStats(values)
        assertEquals(4, stats.sampleCount)
        assertEquals("12:33", CalculatorEngine.formatDuration(stats.averageSeconds))
        assertEquals("11:55", CalculatorEngine.formatDuration(stats.minSeconds))
        assertEquals("13:05", CalculatorEngine.formatDuration(stats.maxSeconds))
    }
}
