package boomcow.minezero.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import boomcow.minezero.checkpoint.CheckpointData;

public class SetAnchorCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(buildCommand("setanchor"));
        // Deprecated alias, kept for one release cycle. See CHANGELOG.
        dispatcher.register(buildCommand("setSubaruPlayer"));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCommand(String name) {
        return Commands.literal(name)
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer target = EntityArgument.getPlayer(context, "target");
                            ServerLevel level = target.serverLevel();
                            CheckpointData data = CheckpointData.get(level);
                            data.setAnchorPlayerUUID(target.getUUID());
                            context.getSource().sendSuccess(() -> Component.literal("Anchor player set to " + target.getName().getString()), true);
                            return 1;
                        }));
    }
}
