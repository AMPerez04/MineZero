package boomcow.minezero.command;

import com.mojang.brigadier.CommandDispatcher;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class SetCheckpointCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("setcheckpoint")
                        .requires(cs -> cs.hasPermission(2))
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerPlayer player;
                            try {
                                player = source.getPlayerOrException();
                            } catch (Exception e) {
                                source.sendFailure(Component.translatable("command.minezero.player_only"));
                                return 0;
                            }

                            CheckpointManager.setCheckpoint(player);
                            source.sendSuccess(() -> Component.translatable("command.minezero.checkpoint_set_self"), true);
                            return 1;
                        })
                        .then(Commands.argument("target", EntityArgument.player())
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");

                                    CheckpointManager.setCheckpoint(targetPlayer);
                                    source.sendSuccess(
                                            () -> Component.translatable("command.minezero.checkpoint_set_other",
                                                    targetPlayer.getName()),
                                            true);
                                    return 1;
                                })));
    }
}
