package boomcow.minezero.event;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

public class CheckpointTicker {

    private static final Logger LOGGER = LogManager.getLogger(CheckpointTicker.class);
    public static long lastCheckpointTick = 0;
    private static long nextCheckpointInterval = 0;

    private static boolean randomIntervalSelected = false;

    private static int intervalTicks = 0;

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        if (level == null) return;
        var autoRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_ENABLED);
        if (autoRule == null) {
            LOGGER.warn("Auto checkpoint game rule not found in world. Skipping checkpoint ticker.");
            return;
        }
        boolean autoEnabled = autoRule.get();
        if (!autoEnabled) {
            return;
        }
        var useRandomRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED);
        boolean useRandom = useRandomRule != null && useRandomRule.get();

        if (useRandom && !randomIntervalSelected) {
            var lowerRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_RANDOM_MIN_SECONDS);
            var upperRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_RANDOM_MAX_SECONDS);
            int lowerSeconds = lowerRule != null ? lowerRule.get() : 600;
            int upperSeconds = upperRule != null ? upperRule.get() : 1200;
            int lowerTicks = lowerSeconds * 20;
            int upperTicks = upperSeconds * 20;
            if (upperTicks <= lowerTicks) {
                LOGGER.warn("Random checkpoint interval upper bound must be greater than lower bound. Using lower bound as fixed interval.");
                intervalTicks = lowerTicks;
            } else {
                intervalTicks = lowerTicks + ThreadLocalRandom.current().nextInt(upperTicks - lowerTicks);
                randomIntervalSelected = true;
            }
            LOGGER.debug("Using random interval: {} ticks (lower bound: {} ticks, upper bound: {} ticks)", intervalTicks, lowerTicks, upperTicks);
        } else if (!useRandom) {
            var fixedRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_INTERVAL_SECONDS);
            int fixedSeconds = fixedRule != null ? fixedRule.get() : 600;
            intervalTicks = fixedSeconds * 20;
        }

        long currentTick = server.getTickCount();
        if (intervalTicks != nextCheckpointInterval) {

            nextCheckpointInterval = intervalTicks;
            lastCheckpointTick = currentTick;
            return;
        }
        if (nextCheckpointInterval == 0) {
            nextCheckpointInterval = intervalTicks;
            lastCheckpointTick = currentTick;
            return;
        }
        if (currentTick - lastCheckpointTick >= nextCheckpointInterval) {
            CheckpointData data = CheckpointData.get(level);
            if (data.getAnchorPlayerUUID() == null) {
                if (!server.getPlayerList().getPlayers().isEmpty()) {
                    ServerPlayer firstPlayer = server.getPlayerList().getPlayers().get(0);
                    data.setAnchorPlayerUUID(firstPlayer.getUUID());

                } else {
                    LOGGER.warn("No players online to set as anchor.");
                    return;
                }
            }
            ServerPlayer anchorPlayer = server.getPlayerList().getPlayer(data.getAnchorPlayerUUID());
            if (anchorPlayer != null) {

                CheckpointManager.setCheckpoint(anchorPlayer);
                randomIntervalSelected = false;
            } else {
                LOGGER.warn("Anchor player not found for UUID: {}", data.getAnchorPlayerUUID());
            }
            lastCheckpointTick = currentTick;
            nextCheckpointInterval = intervalTicks;
            LOGGER.debug("Reset checkpoint timer: next interval {} ticks, new last tick {}", nextCheckpointInterval, lastCheckpointTick);
        }
    }
}
