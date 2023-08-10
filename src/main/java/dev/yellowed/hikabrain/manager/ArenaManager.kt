package dev.yellowed.hikabrain.manager

import dev.yellowed.hikabrain.HikaBrain
import dev.yellowed.hikabrain.instance.Arena
import org.bukkit.entity.Player

object ArenaManager {
    val arenas = mutableListOf<Arena>()

    init {
        val config = HikaBrain.instance.config
        config.getConfigurationSection("arenas.")?.getKeys(false)?.forEach { str ->

            when(config.getString("arenas.$str.type")) {
                "HIKABRAIN-SOLO" -> {
                    arenas.add(
                        Arena(str.toInt(), ConfigManager.getLocation("arenas.$str.paste-position"))
                    )
                }
            }


        }
    }

    fun getArenaByPlayer(player: Player): Arena? {
        return arenas.find { arena: Arena -> arena.players.contains(player.uniqueId) }
    }
}