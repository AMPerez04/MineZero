package boomcow.minezero.command;

import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;

import java.util.UUID;

public class RestoreCheckpointCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildCommand("restorecheckpoint"));
        // Deprecated alias, kept for one release cycle. See CHANGELOG.
        dispatcher.register(buildCommand("triggerRBD"));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> buildCommand(String name) {
        return Commands.literal(name)
                .requires(cs -> cs.hasPermission(2))
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    CheckpointData data = CheckpointData.get(level);
                    UUID anchorPlayerUUID = data.getAnchorPlayerUUID();

                    if (anchorPlayerUUID == null) {
                        context.getSource().sendFailure(Component.literal(
                                "No anchor player is set. Cannot restore the checkpoint."));
                        LOGGER.warn("Attempted to restore checkpoint, but no anchor player is set.");
                        return 0;
                    }

                    ServerPlayer anchorPlayer = level.getServer().getPlayerList().getPlayer(anchorPlayerUUID);
                    if (anchorPlayer == null) {

                        context.getSource().sendFailure(Component.literal("The anchor player (UUID: "
                                + anchorPlayerUUID.toString()
                                + ") is not currently online. Cannot restore the checkpoint with current implementation."));
                        LOGGER.warn("Attempted to restore checkpoint for anchor {}, but player is not online.",
                                anchorPlayerUUID);
                        return 0;
                    }

                    context.getSource().sendSuccess(
                            () -> Component.literal("Manually restoring checkpoint for anchor player: "
                                    + anchorPlayer.getName().getString() + ". World will reset to checkpoint."),
                            true);
                    LOGGER.info(
                            "Manually restoring checkpoint for anchor player: {} (UUID: {}) by command sender: {}",
                            anchorPlayer.getName().getString(), anchorPlayerUUID,
                            context.getSource().getDisplayName().getString());

                    level.getServer().execute(() -> {
                        CheckpointManager.restoreCheckpoint(anchorPlayer);
                        level.getServer().getPlayerList().getPlayers().forEach(p -> {
                            p.displayClientMessage(Component.literal(
                                    "The checkpoint restore has been manually triggered! Resetting to the last checkpoint."),
                                    false);
                        });
                        LOGGER.info("Checkpoint restore manually triggered and checkpoint restored.");
                    });

                    return 1;
                });
    }
}
