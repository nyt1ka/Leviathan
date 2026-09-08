package com.agrofront.agrohelper.domain

data class WorkRecord(
    val id: Long,
    val createdAt: String,
    val fieldName: String,
    val droneId: String,
    val droneName: String,
    val areaHa: Double,
    val solutionRateLHa: Double,
    val productName: String,
    val productRatePerHa: Double,
    val productUnit: String,
    val mixTankLiters: Double,
    val droneTankLiters: Double,
    val totalSolutionLiters: Double,
    val totalProductAmount: Double,
    val totalMixes: Int,
    val totalFlights: Int
)
