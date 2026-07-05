package boomcow.minezero.command;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
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
            TextComponentTranslation noAnchorMsg = new TextComponentTranslation("command.minezero.restore_no_anchor");
            noAnchorMsg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(noAnchorMsg);
            LOGGER.warn("Attempted to restore checkpoint, but no anchor player is set.");
            return;
        }

        EntityPlayerMP anchorPlayer = server.getPlayerList().getPlayerByUUID(anchorPlayerUUID);
        if (anchorPlayer == null) {
            TextComponentTranslation offlineMsg = new TextComponentTranslation("command.minezero.restore_anchor_offline", anchorPlayerUUID.toString());
            offlineMsg.getStyle().setColor(TextFormatting.RED);
            sender.sendMessage(offlineMsg);
            LOGGER.warn("Attempted to restore checkpoint for anchor {}, but player is not online.", anchorPlayerUUID);
            return;
        }

        notifyCommandListener(sender, this, "command.minezero.restore_starting", anchorPlayer.getName());
        LOGGER.info("Manually restoring checkpoint for anchor player: {} (UUID: {}) by command sender: {}", anchorPlayer.getName(), anchorPlayerUUID, sender.getName());

        // Execute on main server thread
        server.addScheduledTask(() -> {
            CheckpointManager.restoreCheckpoint(anchorPlayer);

            // Broadcast message to all online players
            TextComponentTranslation broadcastMsg = new TextComponentTranslation("message.minezero.restore_broadcast");
            broadcastMsg.getStyle().setColor(TextFormatting.GOLD);
            server.getPlayerList().sendMessage(broadcastMsg);

            LOGGER.info("Checkpoint restore manually triggered and checkpoint restored.");
        });
    }
}
