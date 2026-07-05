package boomcow.minezero.command;

import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class SetCheckpointCommand extends CommandBase {

    @Override
    public String getName() {
        return "setcheckpoint";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/setcheckpoint [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2; // OP Level 2
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP targetPlayer;

        if (args.length == 0) {
            // No arguments: Set checkpoint for the command sender (must be a player)
            targetPlayer = getCommandSenderAsPlayer(sender);
            CheckpointManager.setCheckpoint(targetPlayer);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Checkpoint set for yourself!"));
        } else {
            // Argument provided: Resolve target player (handles names and selectors like @p)
            targetPlayer = getPlayer(server, sender, args[0]);
            CheckpointManager.setCheckpoint(targetPlayer);
            notifyCommandListener(sender, this, "Checkpoint set for " + targetPlayer.getName());
        }
    }
}
