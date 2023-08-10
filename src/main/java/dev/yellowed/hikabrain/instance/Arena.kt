package dev.yellowed.hikabrain.instance

import dev.yellowed.hikabrain.GameState
import dev.yellowed.hikabrain.manager.ConfigManager
import dev.yellowed.hikabrain.extension.sendError
import dev.yellowed.hikabrain.extension.sendSuccess
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*

class Arena(val id: Int, val spawn: Location) {
    val players = mutableSetOf<UUID>()

    var state = GameState.RECRUTING
    var game: Game = Game(this)
        protected set
    protected var countdown: Countdown

    val firstTeamSpawn = Location(spawn.world, spawn.x + 1.5, spawn.y - 3, spawn.z + 7.5, -90f, 0f)
    val firstTeamGoal = Location(spawn.world, spawn.x + 1.5, spawn.y - 8, spawn.z + 7.5)
    val secondTeamSpawn = Location(spawn.world, spawn.x + 49.5, spawn.y - 3, spawn.z + 7.5, 90f, 0f)
    val secondTeamGoal = Location(spawn.world, spawn.x + 49.5, spawn.y - 8, spawn.z + 7.5)
    val minimumHeight = spawn.y - 15
    val maximumHeight = spawn.y - 3

    var teams = listOf(Team(ChatColor.BLUE, firstTeamSpawn, firstTeamGoal, "blue"), Team(ChatColor.RED, secondTeamSpawn, secondTeamGoal, "red"))

    init {
        countdown = Countdown(this)
    }

    fun start() {
        game.start()
    }

    fun reset(kickPlayers: Boolean) {
        sendTitle("", "")
        if (kickPlayers) {
            val loc: Location = ConfigManager.lobbySpawn
            for (uuid in players) {
                Bukkit.getPlayer(uuid)?.teleport(loc)
            }
            playSound(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 5f, 1f)
            players.clear()
        }

        state = GameState.RECRUTING
        countdown.cancel()
        countdown = Countdown(this)
        game.unregister()

        resetGame()
    }

    fun resetGame() {
        game = Game(this)
        teams = listOf(Team(ChatColor.BLUE, firstTeamSpawn, firstTeamGoal, "blue"), Team(ChatColor.RED, secondTeamSpawn, secondTeamGoal, "red"))
    }

    fun getPlayerTeam(player: Player): Team? {
        return teams.find { it.players.contains(player.uniqueId) }
    }

    fun sendMessage(message: String) {
        players
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEach { it.sendMessage(message) }
    }

    fun sendSuccess(message: String) {
        players
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEach { it.sendSuccess(message) }
    }

    fun sendError(message: String) {
        players
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEach { it.sendError(message) }
    }

    fun sendTitle(title: String, subtitle: String) {
        for (uuid in players) {
            Bukkit.getPlayer(uuid)?.sendMessage(title, subtitle)
        }
    }

    fun playSound(sound: Sound, volume: Float, pitch: Float) {
        players
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEach { it.playSound(it.location, sound, volume, pitch) }
    }

    fun addPlayer(player: Player) {
        players.add(player.uniqueId)
        player.teleport(spawn)

        if (state == GameState.RECRUTING && players.size >= ConfigManager.requiredPlayers) {
            countdown.start()
        }
    }

    fun removePlayer(player: Player) {
        players.remove(player.uniqueId)
        player.teleport(ConfigManager.lobbySpawn)
        player.sendTitle("", "")

        if (state == GameState.COUTDOWN && players.size < ConfigManager.requiredPlayers) {
            sendError("Not ennough players, countdown stopped.")
            reset(false)
        }

        if (state == GameState.LIVE && players.size < ConfigManager.requiredPlayers) {
            sendError("Game ended. Too many players have left.")
            reset(false)
        }
    }
}