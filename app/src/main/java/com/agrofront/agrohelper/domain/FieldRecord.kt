package com.agrofront.agrohelper.domain

data class FieldRecord(
    val id: Long,
    val name: String,
    val areaHa: Double,
    val notes: String = ""
)
