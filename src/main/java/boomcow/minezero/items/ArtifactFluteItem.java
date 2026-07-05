package boomcow.minezero.items;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class ArtifactFluteItem extends Item {

    public ArtifactFluteItem() {
        // In 1.12.2, properties are set directly in the constructor
        this.setMaxStackSize(1);
        // You typically set registry/unlocalized names here or during registration
        // this.setCreativeTab(CreativeTabs.MISC);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // Check GameRule: Enabled
        if (!world.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_ENABLED)) {
            if (!world.isRemote && player instanceof EntityPlayerMP) {
                // False = Chat Message
                player.sendMessage(new TextComponentString("The Artifact Flute is currently disabled by a game rule."));
            }
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }

        if (!world.isRemote) {
            if (player instanceof EntityPlayerMP) {
                EntityPlayerMP serverPlayer = (EntityPlayerMP) player;

                // Handle Cooldowns
                boolean cooldownEnabled = world.getGameRules().getBoolean(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_ENABLED);
                if (cooldownEnabled) {
                    // In 1.12, getInt requires the String rule name
                    int cooldownSeconds = world.getGameRules().getInt(ModGameRules.ARTIFACT_FLUTE_COOLDOWN_SECONDS);
                    int cooldownTicks = cooldownSeconds * 20;

                    if (serverPlayer.getCooldownTracker().hasCooldown(this)) {
                        // True = Action Bar Message
                        serverPlayer.sendStatusMessage(new TextComponentString(TextFormatting.RED + "Artifact Flute is on cooldown!"), true);
                        return new ActionResult<>(EnumActionResult.FAIL, stack);
                    } else {
                        serverPlayer.getCooldownTracker().setCooldown(this, cooldownTicks);
                    }
                }

                // Set Checkpoint
                CheckpointManager.setCheckpoint(serverPlayer);

                // Play Sound
                // Assuming ModSoundEvents.FLUTE_CHIME is a static SoundEvent field
                world.playSound(null, player.posX, player.posY, player.posZ,
                        ModSoundEvents.FLUTE_CHIME,
                        SoundCategory.PLAYERS,
                        1.0f,
                        1.0f
                );

                // Success Message (Action Bar)
                serverPlayer.sendStatusMessage(new TextComponentString(TextFormatting.GREEN + "Checkpoint set using the Artifact Flute!"), true);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}