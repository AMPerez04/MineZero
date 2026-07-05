package boomcow.minezero.items;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointManager;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ArtifactFluteItem extends Item {
    public ArtifactFluteItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStackInHand = player.getStackInHand(hand);

        if (!world.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_ENABLED)) {
            if (!world.isClient() && player instanceof ServerPlayerEntity serverPlayer) {
                serverPlayer.sendMessage(Text.translatable("message.minezero.flute_disabled"), false);
            }
            return ActionResult.FAIL;
        }

        if (!world.isClient()) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                boolean cooldownEnabled = world.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_ENABLED);
                if (cooldownEnabled) {
                    int cooldownSeconds = world.getGameRules().getInt(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_SECONDS);
                    int cooldownTicks = cooldownSeconds * 20;

                    if (serverPlayer.getItemCooldownManager().isCoolingDown(this)) {
                        serverPlayer.sendMessage(Text.translatable("message.minezero.flute_cooldown"), true);
                        return ActionResult.FAIL;
                    } else {
                        serverPlayer.getItemCooldownManager().set(this, cooldownTicks);
                    }
                }

                CheckpointManager.setCheckpoint(serverPlayer);

                world.playSound(
                        null,
                        player.getX(), player.getY(), player.getZ(),
                        ModSoundEvents.FLUTE_CHIME,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );

                serverPlayer.sendMessage(Text.translatable("message.minezero.flute_checkpoint_set"), true);
            }
        }
        return ActionResult.SUCCESS;
    }
}