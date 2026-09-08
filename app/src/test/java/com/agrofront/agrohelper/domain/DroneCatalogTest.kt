package com.agrofront.agrohelper.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DroneCatalogTest {
    @Test
    fun catalogContainsAllRequestedManufacturers() {
        val manufacturers = DroneCatalog.models.map { it.manufacturer }.toSet()

        assertTrue("DJI" in manufacturers)
        assertTrue("XAG" in manufacturers)
        assertTrue("HD / Huida Tech" in manufacturers)
    }

    @Test
    fun everyModelHasPositiveSprayTank() {
        assertTrue(DroneCatalog.models.all { it.sprayTankLiters > 0.0 })
    }

    @Test
    fun expectedModelsArePresent() {
        val ids = DroneCatalog.models.map { it.id }.toSet()

        assertTrue("dji-t25" in ids)
        assertTrue("dji-t50" in ids)
        assertTrue("xag-p100-pro" in ids)
        assertTrue("xag-p150" in ids)
        assertTrue("hd-540-pro" in ids)
        assertEquals(5, DroneCatalog.models.size)
    }
}
