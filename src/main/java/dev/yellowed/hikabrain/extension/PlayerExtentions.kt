package dev.yellowed.hikabrain.extension

import org.bukkit.ChatColor
import org.bukkit.entity.Player

fun Player.sendSuccess(message: String) {
    this.sendMessage("${ChatColor.GREEN}$message")
}

fun Player.sendError(message: String) {
    this.sendMessage("${ChatColor.RED}$message")
}

fun Player.kill() {
    player?.health = 0.0
}