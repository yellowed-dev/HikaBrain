package dev.yellowed.hikabrain.instance

import dev.yellowed.hikabrain.GameState
import dev.yellowed.hikabrain.HikaBrain
import dev.yellowed.hikabrain.manager.ConfigManager
import org.bukkit.scheduler.BukkitRunnable

class Countdown(private val arena: Arena) : BukkitRunnable() {
    private var countdownSeconds = ConfigManager.countdownSeconds

    fun start() {
        arena.state = GameState.COUTDOWN
        runTaskTimer(HikaBrain.instance, 0, 20)
    }

    override fun run() {
        if (countdownSeconds == 0) {
            cancel()
            arena.start()
            return
        }

        arena.sendSuccess("Game will start in $countdownSeconds second${if (countdownSeconds == 1) "" else "s"}.")
        countdownSeconds--

    }
}