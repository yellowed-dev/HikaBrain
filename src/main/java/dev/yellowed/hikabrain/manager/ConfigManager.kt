package dev.yellowed.hikabrain.manager

import dev.yellowed.hikabrain.HikaBrain
import org.bukkit.Bukkit
import org.bukkit.Location

object ConfigManager {
    private val config = HikaBrain.instance.config

    val requiredPlayers: Int
        get() = config.getInt("required-players")
    val countdownSeconds: Int
        get() = config.getInt("countdown-seconds")
    val lobbySpawn: Location
        get() = Location(
            Bukkit.getWorld(HikaBrain.instance.config.getString("lobby-spawn.world")!!),
            config.getDouble("lobby-spawn.x"),
            config.getDouble("lobby-spawn.y"),
            config.getDouble("lobby-spawn.z"),
            config.getDouble("lobby-spawn.yaw").toFloat(),
            config.getDouble("lobby-spawn.pitch").toFloat())

    fun getLocation(path: String): Location {
        return Location(
            Bukkit.getWorld(config.getString("$path.world")!!),
            config.getDouble("$path.x"),
            config.getDouble("$path.y"),
            config.getDouble("$path.z"),
            config.getDouble("$path.yaw").toFloat(),
            config.getDouble("$path.pitch").toFloat())
    }
}