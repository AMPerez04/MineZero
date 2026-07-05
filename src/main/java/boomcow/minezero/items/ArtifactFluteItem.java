package boomcow.minezero.items;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArtifactFluteItem extends Item {
    public ArtifactFluteItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStackInHand = player.getItemInHand(hand);

        if (!level.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_ENABLED)) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("message.minezero.flute_disabled"), false);
            }
            return InteractionResultHolder.fail(itemStackInHand);
        }

        if (!level.isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                boolean cooldownEnabled = level.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_ENABLED);
                if (cooldownEnabled) {
                    int cooldownSeconds = level.getGameRules().getInt(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_SECONDS);
                    int cooldownTicks = cooldownSeconds * 20;

                    if (serverPlayer.getCooldowns().isOnCooldown(this)) {
                        serverPlayer.displayClientMessage(Component.translatable("message.minezero.flute_cooldown"), true);
                        return InteractionResultHolder.fail(itemStackInHand);
                    } else {
                        serverPlayer.getCooldowns().addCooldown(this, cooldownTicks);
                    }
                }

                CheckpointManager.setCheckpoint(serverPlayer);

                level.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        ModSoundEvents.FLUTE_CHIME,
                        SoundSource.PLAYERS,
                        1.0f,
                        1.0f
                );

                serverPlayer.displayClientMessage(Component.translatable("message.minezero.flute_checkpoint_set"), true);
            }
        }
        return InteractionResultHolder.success(itemStackInHand);
    }
}
