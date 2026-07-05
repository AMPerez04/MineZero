package boomcow.minezero.command;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class RestoreCheckpointCommand extends CommandBase {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName() {
        return "restorecheckpoint";
    }

    @Override
    public List<String> getAliases() {
        // Deprecated alias, kept for one release cycle. See CHANGELOG.
        return Collections.singletonList("triggerRBD");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/restorecheckpoint";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP Level 2
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        // Use the sender's world to access global storage (CheckpointData handles accessing the correct map storage)
        World world = sender.getEntityWorld();

        CheckpointData data = CheckpointData.get(world);
        UUID anchorPlayerUUID = data.getAnchorPlayerUUID();

        if (anchorPlayerUUID == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No anchor player is set. Cannot restore the checkpoint."));
            LOGGER.warn("Attempted to restore checkpoint, but no anchor player is set.");
            return;
        }

        EntityPlayerMP anchorPlayer = server.getPlayerList().getPlayerByUUID(anchorPlayerUUID);
        if (anchorPlayer == null) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "The anchor player (UUID: " + anchorPlayerUUID.toString() + ") is not currently online. Cannot restore the checkpoint with current implementation."));
            LOGGER.warn("Attempted to restore checkpoint for anchor {}, but player is not online.", anchorPlayerUUID);
            return;
        }

        notifyCommandListener(sender, this, "Manually restoring checkpoint for anchor player: " + anchorPlayer.getName() + ". World will reset to checkpoint.");
        LOGGER.info("Manually restoring checkpoint for anchor player: {} (UUID: {}) by command sender: {}", anchorPlayer.getName(), anchorPlayerUUID, sender.getName());

        // Execute on main server thread
        server.addScheduledTask(() -> {
            CheckpointManager.restoreCheckpoint(anchorPlayer);

            // Broadcast message to all online players
            server.getPlayerList().sendMessage(new TextComponentString(TextFormatting.GOLD + "The checkpoint restore has been manually triggered! Resetting to the last checkpoint."));

            LOGGER.info("Checkpoint restore manually triggered and checkpoint restored.");
        });
    }
}
