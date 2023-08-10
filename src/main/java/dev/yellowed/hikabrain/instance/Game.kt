package dev.yellowed.hikabrain.instance

import dev.yellowed.hikabrain.GameState
import dev.yellowed.hikabrain.HikaBrain
import dev.yellowed.hikabrain.manager.SchematicManager
import dev.yellowed.hikabrain.extension.sendError
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import java.io.File
import java.util.*

class Game(protected val arena: Arena) : Listener {
    private val lastDamagers = HashMap<UUID, UUID>()

    init {
        Bukkit.getPluginManager().registerEvents(this, HikaBrain.instance)
        println("GAME CREATED")
    }

    fun start() {
        arena.state = GameState.LIVE

        onStart()
    }

    fun unregister() {
        HandlerList.unregisterAll(this)
        println("UNREGISTER")
    }

    fun onStart() {
        arena.players
            .mapNotNull { Bukkit.getPlayer(it) }
            .forEachIndexed { index, player ->
                player.inventory.addItem(ItemStack(Material.WOODEN_SWORD))
                if (index % 2 == 0) {
                    arena.teams[0].addPlayer(player)
                } else {
                    arena.teams[1].addPlayer(player)
                }
            }
        respawnPlayers()
        arena.sendMessage("${ChatColor.GOLD}Game ${ChatColor.GRAY}has started! Score ${ChatColor.GOLD}5 points ${ChatColor.GRAY}to ${ChatColor.GREEN}win!")
    }

    fun respawnPlayers() {
        SchematicManager.pasteSchematic(arena.spawn, File(HikaBrain.instance.dataFolder, "hikabrain-solo.schem"))
        arena.players.forEach {
            Bukkit.getPlayer(it)?.let { it1 -> resetPlayer(it1, false) }
        }
    }

    fun resetPlayer(player: Player, playSound: Boolean = true) {
        player.inventory.clear()

        if (lastDamagers.containsKey(player.uniqueId)) {
            lastDamagers.remove(player.uniqueId)
        }

        val sword = ItemStack(Material.DIAMOND_SWORD, 1).apply {
            val meta = itemMeta
            meta?.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", Double.MAX_VALUE, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            meta?.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 4.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            itemMeta = meta
        }
        val pickaxe = ItemStack(Material.IRON_PICKAXE, 1).apply {
            addUnsafeEnchantment(Enchantment.DIG_SPEED, 5)
            val meta = itemMeta
            meta?.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", Double.MAX_VALUE, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            meta?.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            itemMeta = meta
        }


        val gapple = ItemStack(Material.GOLDEN_APPLE, 64) // TODO("Utiliser des gapples custom")
        val sandstone = ItemStack(Material.SMOOTH_SANDSTONE, 64).apply {
            val meta = itemMeta
            meta?.setDisplayName("${ChatColor.YELLOW}Hika Block")
            itemMeta = meta
        }


        player.inventory.setItem(0, sword)
        player.inventory.setItem(1, pickaxe)
        player.inventory.setItem(2, gapple)
        player.inventory.setItemInOffHand(sandstone)

        player.health = 20.0
        player.foodLevel = 20
        player.saturation = 0f


        player.fallDistance = 0f
        arena.getPlayerTeam(player)?.spawn?.let { player.teleport(it) }

        if (playSound) {
            player.playSound(player.location, Sound.ENTITY_PLAYER_DEATH, 1.0f, 1.0f)
        }
    }

    fun giveKill(killer: Player, killed: Player) {
        killer.playSound(killer.location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 5.0f, 1.0f)
        arena.sendMessage("${arena.getPlayerTeam(killer)?.color}${killer.name}${ChatColor.GRAY} has killed ${arena.getPlayerTeam(killed)?.color}${killed.name}!")
    }

    @EventHandler
    fun onPlayerDeath(e: EntityDamageEvent) {
        val player = e.entity
        val damager = if (e is EntityDamageByEntityEvent && e.damager is Player) e.damager as Player else null

        println("EVENT CALLED BY : " + this)

        // Check if player is in arena
        if (player is Player && arena.players.contains(player.uniqueId)) {

            // add damager to lastDamagers
            if (damager != null) {
                lastDamagers[player.uniqueId] = damager.uniqueId
            }

            // Check for death
            if (player.health - e.finalDamage <= 0) {
                e.isCancelled = true
                resetPlayer(e.entity as Player)

                if (damager != null) {
                    giveKill(damager, player)
                }
            }
        }

    }

    @EventHandler
    fun onPlayerMove(e: PlayerMoveEvent) {
        val player = e.player
        val team = arena.getPlayerTeam(player) ?: return

        arena.teams.forEach {
            if (it != team) {
                if (player.location.distance(it.goal) <= 0.5) {
                    team.points += 1
                    arena.sendMessage("${arena.getPlayerTeam(player)?.color}${player.name} ${ChatColor.GRAY}has ${ChatColor.GOLD}scored!")
                    respawnPlayers()
                    arena.playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 5f, 1f)

                    if (team.points >= 5) {
                        arena.sendMessage("${team.color}${team.name.capitalize()} ${ChatColor.GRAY}has ${ChatColor.GOLD}won!")
                        arena.reset(true)
                        return
                    }
                }
            }
        }

        if (player.location.y <= arena.minimumHeight) {

            if (lastDamagers.containsKey(player.uniqueId)) {
                val damagerUUID = lastDamagers[player.uniqueId]
                val damager = damagerUUID?.let { Bukkit.getPlayer(it) }

                if (damager != null) {
                    giveKill(damager, player)
                }
            }

            resetPlayer(player)
        }
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {


        if (arena.players.contains(e.player.uniqueId)) {
            // Check for height limit
            if (e.block.location.y >= arena.maximumHeight) {
                e.isCancelled = true
                e.player.sendError("Height limit!")
            }
        }
    }

    @EventHandler
    fun onPlayerConsume(e: PlayerItemConsumeEvent) {
        val player = e.player
        val item = e.item

        if (!arena.players.contains(player.uniqueId)) {
            return
        }

        if (item.type == Material.GOLDEN_APPLE) {
            // Cancel the event to prevent the normal golden apple effects from applying
            e.isCancelled = true

            // Clear any existing effects
            for (effect in player.activePotionEffects) {
                player.removePotionEffect(effect.type)
            }

            // Restore health
            player.health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
        }
    }

    @EventHandler
    fun onFoodLevelChange(event: FoodLevelChangeEvent) {
        val player = event.entity as? Player ?: return

        if (!arena.players.contains(player.uniqueId)) {
            return
        }

        // If the new food level is lower than the current food level, cancel the event
        if (event.foodLevel < player.foodLevel) {
            event.isCancelled = true
        }
    }

}