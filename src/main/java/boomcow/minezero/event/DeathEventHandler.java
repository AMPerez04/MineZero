package boomcow.minezero.event;

import boomcow.minezero.ConfigHandler;
import boomcow.minezero.ModGameRules;
import boomcow.minezero.ModSoundEvents;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DeathEventHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(LivingDeathEvent event) {
        Logger logger = LogManager.getLogger();
        try {
            if (!(event.getEntity() instanceof ServerPlayer player))
                return;

            ServerLevel level = player.serverLevel();
            CheckpointData data = CheckpointData.get(level);
            MinecraftServer server = player.getServer();
            if (server != null) {
                CheckpointTicker.lastCheckpointTick = server.getTickCount();
            }

            if (data.getAnchorPlayerUUID() == null
                    ||
                (!player.getUUID().equals(data.getAnchorPlayerUUID()) &&
                        !level.getGameRules().getRule(ModGameRules.ALL_PLAYERS_RBD).get())
                ) // Check wether gamerule is enabled, if not, it does not matter who dies --> activate your logic anyway
            {

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

            String chime = ConfigHandler.getDeathChime();
            if ("CLASSIC".equalsIgnoreCase(chime)) {
                playClassicChime(player);
            } else if ("ALTERNATE".equalsIgnoreCase(chime)) {
                playAlternateChime(player);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void playClassicChime(ServerPlayer player) {

        Logger logger = LogManager.getLogger();
        logger.debug("Playing classic chime");
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                new ResourceLocation("minezero", "death_chime"),
                SoundSource.PLAYERS);
        player.connection.send(stopSoundPacket);

        player.playNotifySound(ModSoundEvents.DEATH_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.0F);

    }

    private void playAlternateChime(ServerPlayer player) {

        Logger logger = LogManager.getLogger();
        logger.debug("Playing alternate chime");
        ClientboundStopSoundPacket stopSoundPacket = new ClientboundStopSoundPacket(
                new ResourceLocation("minezero", "alt_death_chime"),
                SoundSource.PLAYERS);
        player.connection.send(stopSoundPacket);

        player.playNotifySound(ModSoundEvents.ALT_DEATH_CHIME.get(), SoundSource.PLAYERS, 0.8F, 1.0F);

    }

}
