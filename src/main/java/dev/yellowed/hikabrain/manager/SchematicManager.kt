package dev.yellowed.hikabrain.manager

import dev.yellowed.hikabrain.HikaBrain
import org.bukkit.Location
import java.io.File
import java.io.FileInputStream
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.function.operation.Operation
import com.sk89q.worldedit.function.operation.Operations

object SchematicManager {

    fun extractSchematic(schematicName: String) {
        val schematicFile = File(HikaBrain.instance.dataFolder, "$schematicName.schem")

        if (!schematicFile.exists()) {
            HikaBrain.instance.saveResource("$schematicName.schem", false)

            // Check if file was successfully saved
            if (!schematicFile.exists()) {
                HikaBrain.instance.logger.severe("Could not save resource $schematicName.schem!")
            }
        }
    }

    fun pasteSchematic(location: Location, schematicFile: File) {
        println("FILE NAME : ${schematicFile.path}")
        val format = ClipboardFormats.findByFile(schematicFile)

        println(" FORMAT : $format")

        var clipboard: Clipboard? = null
        if (format != null) {
            format.getReader(FileInputStream(schematicFile)).use { reader -> clipboard = reader.read() }
        } else {
            println("FORMAT IS NULL")
        }


        WorldEdit.getInstance().editSessionFactory.getEditSession(BukkitAdapter.adapt(location.world), -1).use { editSession ->
            val operation: Operation = ClipboardHolder(clipboard)
                .createPaste(editSession)
                .to(BlockVector3.at(location.x, location.y, location.z))
                .ignoreAirBlocks(false)
                .build()
            Operations.complete(operation)
        }
    }
}