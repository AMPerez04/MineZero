package boomcow.minezero.event;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber
public class SleepCheckpointHandler {

    private static final Logger LOGGER = LogManager.getLogger(SleepCheckpointHandler.class);

    // Set by the sleep event; consumed by CheckpointTicker on the next Phase.START
    // (which fires after the time-skip has already been applied).
    static volatile ServerPlayer pendingSleepAnchor = null;

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.getGameRules().getRule(ModGameRules.CHECKPOINT_ON_SLEEP).get()) return;

        MinecraftServer server = level.getServer();
        CheckpointData data = CheckpointData.get(level);

        ServerPlayer anchor = resolveAnchor(server, level, data);
        if (anchor == null) {
            LOGGER.warn("No anchor player found for sleep checkpoint.");
            return;
        }

        pendingSleepAnchor = anchor;
    }

    static ServerPlayer resolveAnchor(MinecraftServer server, ServerLevel level, CheckpointData data) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) return null;

        if (level.getGameRules().getRule(ModGameRules.RANDOM_ANCHOR_ENABLED).get()) {
            ServerPlayer random = players.get(new Random().nextInt(players.size()));
            data.setAnchorPlayerUUID(random.getUUID());
            return random;
        }

        if (data.getAnchorPlayerUUID() != null) {
            ServerPlayer anchor = server.getPlayerList().getPlayer(data.getAnchorPlayerUUID());
            if (anchor != null) return anchor;
        }

        data.setAnchorPlayerUUID(players.get(0).getUUID());
        return players.get(0);
    }
}
