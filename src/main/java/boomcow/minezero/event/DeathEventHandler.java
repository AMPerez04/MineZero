package boomcow.minezero.event;

import boomcow.minezero.ConfigHandler;
import boomcow.minezero.MineZeroMain;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathEventHandler {

    private static final Logger LOGGER = LogManager.getLogger(DeathEventHandler.class);

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (!(entity instanceof ServerPlayer player)) return true;

            try {
                ServerLevel level = player.serverLevel();
                CheckpointData data = CheckpointData.get(level);
                MinecraftServer server = player.getServer();
                if (server != null) {
                    CheckpointTicker.lastCheckpointTick = server.getTickCount();
                }
                if (data.getAnchorPlayerUUID() == null || !player.getUUID().equals(data.getAnchorPlayerUUID())) {
                    return true;
                }

                // Keep the anchor alive; restoreCheckpoint sets the real saved health.
                player.setHealth(player.getMaxHealth());
                level.getServer().execute(() -> CheckpointManager.restoreCheckpoint(player));

                String restoreSound = ConfigHandler.getRestoreSound();
                if ("death_chime".equalsIgnoreCase(restoreSound) || "CLASSIC".equalsIgnoreCase(restoreSound)) {
                    playClassicChime(player);
                } else if ("alt_death_chime".equalsIgnoreCase(restoreSound) || "ALTERNATE".equalsIgnoreCase(restoreSound)) {
                    playAlternateChime(player);
                }
                return false;
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return true;
            }
        });
    }

    private static void playClassicChime(ServerPlayer player) {
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "death_chime"),
                SoundSource.PLAYERS
        );

        if (player.connection != null) {
            player.connection.send(stopSoundPacket);
        }
        player.playNotifySound(ModSoundEvents.DEATH_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
    }

    private static void playAlternateChime(ServerPlayer player) {
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "alt_death_chime"),
                SoundSource.PLAYERS
        );

        if (player.connection != null) {
            player.connection.send(stopSoundPacket);
        }

        player.playNotifySound(ModSoundEvents.ALT_DEATH_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
    }
}
