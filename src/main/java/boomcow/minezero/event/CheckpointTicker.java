package boomcow.minezero.event;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.jmx.Server;

@Mod.EventBusSubscriber
public class CheckpointTicker {

    private static final Logger LOGGER = LogManager.getLogger(CheckpointTicker.class);

    public static long lastCheckpointTick = 0;
    private static long nextCheckpointInterval = 0;

    private static boolean randomIntervalSelected = false;

    private static int intervalTicks = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START)
            return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        if (level == null)
            return;

        var autoRule = level.getGameRules().getRule(ModGameRules.AUTO_CHECKPOINT_ENABLED);
        if (autoRule == null) {
            LOGGER.warn("Auto checkpoint game rule not found in world. Skipping checkpoint ticker.");
            return;
        }
        boolean autoEnabled = autoRule.get();
        if (!autoEnabled) {
            return;
        }

        var useRandomRule = level.getGameRules().getRule(ModGameRules.USE_RANDOM_INTERVAL);
        boolean useRandom = useRandomRule != null && useRandomRule.get();

        if (useRandom && !randomIntervalSelected) {
            var lowerRule = level.getGameRules().getRule(ModGameRules.RANDOM_CHECKPOINT_LOWER_BOUND);
            var upperRule = level.getGameRules().getRule(ModGameRules.RANDOM_CHECKPOINT_UPPER_BOUND);
            int lowerSeconds = lowerRule != null ? lowerRule.get() : 600;
            int upperSeconds = upperRule != null ? upperRule.get() : 1200;
            int lowerTicks = lowerSeconds * 20;
            int upperTicks = upperSeconds * 20;
            if (upperTicks <= lowerTicks) {
                LOGGER.warn(
                        "Random checkpoint interval upper bound must be greater than lower bound. Using lower bound as fixed interval.");
                intervalTicks = lowerTicks;
            } else {
                intervalTicks = lowerTicks + ThreadLocalRandom.current().nextInt(upperTicks - lowerTicks);
                randomIntervalSelected = true;
            }
            LOGGER.debug("Using random interval: {} ticks (lower bound: {} ticks, upper bound: {} ticks)",
                    intervalTicks, lowerTicks, upperTicks);
        } else if (!useRandom) {
            var fixedRule = level.getGameRules().getRule(ModGameRules.CHECKPOINT_FIXED_INTERVAL);
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

        Random random = new Random();

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

            // Random anchor logic
            if (level.getGameRules().getRule(ModGameRules.RANDOM_ANCHOR_ENABLED).get()) {
                int playerAmount = server.getPlayerCount();
                int randomInt = random.nextInt(playerAmount);
                ServerPlayer randomPlayer = server.getPlayerList().getPlayers().get(randomInt);
                data.setAnchorPlayerUUID(randomPlayer.getUUID());
                LOGGER.debug("Player {} is set as new Anchor.", randomPlayer.getName());
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
            LOGGER.debug("Reset checkpoint timer: next interval {} ticks, new last tick {}", nextCheckpointInterval,
                    lastCheckpointTick);
        }
    }
}
