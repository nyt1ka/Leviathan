package com.agrofront.agrohelper.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun totalSolutionAndProductAreCalculated() {
        val result = CalculatorEngine.calculate(
            areaHa = 125.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 0.05,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(1250.0, result.totalSolutionLiters, 0.0001)
        assertEquals(6.25, result.totalProductAmount, 0.0001)
    }

    @Test
    fun exactMultipleHasNoPartialMixOrFlight() {
        val result = CalculatorEngine.calculate(
            areaHa = 100.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 0.5,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(2, result.totalMixes)
        assertEquals(0.0, result.lastMixLiters, 0.0001)
        assertEquals(25, result.totalFlights)
        assertEquals(0.0, result.lastFlightLiters, 0.0001)
    }

    @Test
    fun partialMixAndFlightAreCalculated() {
        val result = CalculatorEngine.calculate(
            areaHa = 125.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 0.05,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(3, result.totalMixes)
        assertEquals(250.0, result.lastMixLiters, 0.0001)

        assertEquals(32, result.totalFlights)
        assertEquals(10.0, result.lastFlightLiters, 0.0001)
    }

    @Test
    fun productPerMixIsCalculatedFromSolutionRate() {
        val result = CalculatorEngine.calculate(
            areaHa = 125.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 0.05,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(2.5, result.productPerFullMix, 0.0001)
        assertEquals(1.25, result.productForLastMix, 0.0001)
    }

    @Test
    fun productPerDroneTankIsCalculated() {
        val result = CalculatorEngine.calculate(
            areaHa = 125.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 0.05,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(0.2, result.productPerFullFlight, 0.0001)
        assertEquals(0.05, result.productForLastFlight, 0.0001)
    }

    @Test
    fun negativeInputsAreClampedToZero() {
        val result = CalculatorEngine.calculate(
            areaHa = -1.0,
            solutionRateLHa = 10.0,
            productRatePerHa = 1.0,
            mixTankLiters = 500.0,
            droneTankLiters = 40.0
        )

        assertEquals(0.0, result.totalSolutionLiters, 0.0001)
        assertEquals(0.0, result.totalProductAmount, 0.0001)
        assertEquals(0, result.totalMixes)
        assertEquals(0, result.totalFlights)
    }

    @Test
    fun productivitySupportsMinutes() {
        val minutes = CalculatorEngine.parseWorkDurationMinutes("90")
        val productivity = CalculatorEngine.productivityHaPerHour(30.0, minutes)

        assertEquals(1.5 * 60.0, minutes, 0.0001)
        assertEquals(20.0, productivity, 0.0001)
    }

    @Test
    fun productivitySupportsHoursAndMinutes() {
        val minutes = CalculatorEngine.parseWorkDurationMinutes("1:30")
        val productivity = CalculatorEngine.productivityHaPerHour(30.0, minutes)

        assertEquals(90.0, minutes, 0.0001)
        assertEquals(20.0, productivity, 0.0001)
    }

    @Test
    fun batteryMmSsWorks() {
        val values = CalculatorEngine.parseBatterySamples(
            "12:40, 13:05\n11:55 12:30"
        )
        val stats = CalculatorEngine.batteryStats(values)

        assertEquals(4, stats.sampleCount)
        assertEquals("12:33", CalculatorEngine.formatDuration(stats.averageSeconds))
        assertEquals("11:55", CalculatorEngine.formatDuration(stats.minSeconds))
        assertEquals("13:05", CalculatorEngine.formatDuration(stats.maxSeconds))
    }

    @Test
    fun invalidBatterySamplesAreIgnored() {
        val values = CalculatorEngine.parseBatterySamples(
            "12:40 abc 10:99 11:20"
        )

        assertEquals(listOf(760, 680), values)
    }
}
