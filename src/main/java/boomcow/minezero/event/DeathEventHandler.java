package boomcow.minezero.event;

import boomcow.minezero.ConfigHandler;
import boomcow.minezero.MineZero;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        Logger logger = LogManager.getLogger();
        try {
            if (!(event.getEntity() instanceof ServerPlayer player)) return;


            ServerLevel level = player.serverLevel();
            CheckpointData data = CheckpointData.get(level);
            MinecraftServer server = player.getServer();
            if (server != null) {
                CheckpointTicker.lastCheckpointTick = server.getTickCount();
            }
            if (data.getAnchorPlayerUUID() == null || !player.getUUID().equals(data.getAnchorPlayerUUID())) {

                return;
            }

            event.setCanceled(true);
            level.getServer().execute(() -> {
                CheckpointManager.restoreCheckpoint(player);
            });
            level.getServer().getPlayerList().getPlayers().forEach(p -> {
                if (!p.getUUID().equals(data.getAnchorPlayerUUID())) {
                }
            });
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

    private void playClassicChime(ServerPlayer player) {
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath(MineZero.MODID, "death_chime"),
                SoundSource.PLAYERS
        );

        if (player.connection != null) {
            player.connection.send(stopSoundPacket);
        }
        player.playNotifySound(ModSoundEvents.DEATH_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private void playAlternateChime(ServerPlayer player) {
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath(MineZero.MODID, "alt_death_chime"),
                SoundSource.PLAYERS
        );

        if (player.connection != null) {
            player.connection.send(stopSoundPacket);
        }

        player.playNotifySound(ModSoundEvents.ALT_DEATH_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
    }

}

