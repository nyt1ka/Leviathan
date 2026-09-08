package com.agrofront.agrohelper.data

import android.content.Context

data class AppSettings(
    val defaultMixTankLiters: Double = 500.0,
    val defaultSolutionRateLHa: Double = 10.0,
    val rememberLastDrone: Boolean = true
)

class AppSettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("agrohelper_settings", Context.MODE_PRIVATE)

    fun get(): AppSettings = AppSettings(
        defaultMixTankLiters = prefs.getString("mix_tank", "500")?.toDoubleOrNull() ?: 500.0,
        defaultSolutionRateLHa = prefs.getString("solution_rate", "10")?.toDoubleOrNull() ?: 10.0,
        rememberLastDrone = prefs.getBoolean("remember_drone", true)
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putString("mix_tank", settings.defaultMixTankLiters.toString())
            .putString("solution_rate", settings.defaultSolutionRateLHa.toString())
            .putBoolean("remember_drone", settings.rememberLastDrone)
            .apply()
    }

    fun getLastDroneId(): String? = prefs.getString("last_drone_id", null)

    fun setLastDroneId(id: String?) {
        prefs.edit().putString("last_drone_id", id).apply()
    }
}
