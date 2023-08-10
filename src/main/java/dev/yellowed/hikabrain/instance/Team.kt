package dev.yellowed.hikabrain.instance

import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*

class Team(val color: ChatColor, val spawn: Location, val goal: Location, val name: String) {
    val players = mutableSetOf<UUID>()
    var points = 0

    fun addPlayer(player: Player) {
        players.add(player.uniqueId)
    }
}