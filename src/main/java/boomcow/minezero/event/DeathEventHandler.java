package boomcow.minezero.event;

import boomcow.minezero.ConfigHandler;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        Logger logger = LogManager.getLogger();
        try {
            // Check if entity is a Server Player (EntityPlayerMP in 1.12)
            if (!(event.getEntity() instanceof EntityPlayerMP)) return;
            EntityPlayerMP player = (EntityPlayerMP) event.getEntity();

            WorldServer level = player.getServerWorld();
            CheckpointData data = CheckpointData.get(level);

            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null) {
                CheckpointTicker.lastCheckpointTick = server.getTickCounter();
            }

            // Check Anchor UUID
            if (data.getAnchorPlayerUUID() == null || !player.getUniqueID().equals(data.getAnchorPlayerUUID())) {
                return;
            }

            // Cancel death event (prevents death screen and item drops)
            event.setCanceled(true);

            // Schedule restoration on the main thread
            server.addScheduledTask(() -> {
                CheckpointManager.restoreCheckpoint(player);
            });

            // Play Chime
            String restoreSound = ConfigHandler.getRestoreSound();
            if ("death_chime".equalsIgnoreCase(restoreSound) || "CLASSIC".equalsIgnoreCase(restoreSound)) {
                playClassicChime(player);
            } else if ("alt_death_chime".equalsIgnoreCase(restoreSound) || "ALTERNATE".equalsIgnoreCase(restoreSound)) {
                playAlternateChime(player);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void playClassicChime(EntityPlayerMP player) {
        Logger logger = LogManager.getLogger();
        logger.debug("Playing classic chime");

        // 1.12.2 does not support "StopSound" packets from server side.
        // We proceed directly to playing the sound.

        // Send packet to play sound only to this player
        player.connection.sendPacket(new SPacketSoundEffect(
                ModSoundEvents.DEATH_CHIME,
                SoundCategory.PLAYERS,
                player.posX,
                player.posY,
                player.posZ,
                0.8F,
                1.0F
        ));
    }

    private void playAlternateChime(EntityPlayerMP player) {
        Logger logger = LogManager.getLogger();
        logger.debug("Playing alternate chime");

        player.connection.sendPacket(new SPacketSoundEffect(
                ModSoundEvents.ALT_DEATH_CHIME,
                SoundCategory.PLAYERS,
                player.posX,
                player.posY,
                player.posZ,
                0.8F,
                1.0F
        ));
    }
}