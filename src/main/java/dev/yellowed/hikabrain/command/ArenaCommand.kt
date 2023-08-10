package dev.yellowed.hikabrain.command

import dev.yellowed.hikabrain.GameState
import dev.yellowed.hikabrain.extension.sendError
import dev.yellowed.hikabrain.extension.sendSuccess
import dev.yellowed.hikabrain.manager.ArenaManager
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ArenaCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, s: String, args: Array<out String>): Boolean {

        val player = sender as? Player ?: return false

        when (args.size) {
            1 -> {
                when(args[0].lowercase()) {
                    "list" -> {
                        for (arena in ArenaManager.arenas) {
                            player.sendSuccess("- ${arena.id}(${arena.state.name})")
                        }
                    }
                    "leave" -> {
                        val arena = ArenaManager.getArenaByPlayer(player)

                        if (arena != null) {
                            player.sendError("You left the arena.")
                            arena.removePlayer(player)
                        } else {
                            player.sendError("You are not in an arena.")
                        }
                    }
                }
            }
            2 -> {
                when(args[0].lowercase()) {
                    "join" -> {
                        if (ArenaManager.getArenaByPlayer(player) != null) {
                            player.sendError("You are already in an arena!")
                            return false
                        }

                        val id = args[1].toIntOrNull()
                        if (id == null) {
                            player.sendError("You specified an invalid arena ID.")
                            return false
                        }

                        val arena = ArenaManager.arenas.find {
                            it.id == id
                        } ?: return false // TODO(send message) et peut-être changé

                        if (arena.state in listOf(GameState.RECRUTING, GameState.COUTDOWN)) {
                            player.sendSuccess("You are now playing in Arena ${id}.")
                            arena.addPlayer(player)
                        } else {
                            player.sendError("You cannot join this arena right now!")
                        }
                    }
                }
            }
            else -> {
                player.sendError("Invalid usages!")
                player.sendError("- /arena list \n" +
                        "-/arena leave \n" +
                        "-arena join <id>")
            }
        }
        return false
    }

}