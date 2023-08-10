package dev.yellowed.hikabrain

import dev.yellowed.hikabrain.command.ArenaCommand
import dev.yellowed.hikabrain.manager.SchematicManager
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.plugin.java.JavaPlugin

public final class HikaBrain : JavaPlugin() {

    companion object {
        lateinit var instance: HikaBrain
            private set
    }

    override fun onEnable() {
        instance = this

        logger.info("Hikabrain started")

        Bukkit.createWorld(WorldCreator("void"))

        saveDefaultConfig()
        SchematicManager.extractSchematic("hikabrain-solo")

        getCommand("arena")?.setExecutor(ArenaCommand())
    }
}