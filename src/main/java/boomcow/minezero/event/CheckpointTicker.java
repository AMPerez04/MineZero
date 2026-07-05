package boomcow.minezero.event;

import boomcow.minezero.ModGameRules;
import boomcow.minezero.checkpoint.CheckpointData;
import boomcow.minezero.checkpoint.CheckpointManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ThreadLocalRandom;

@Mod.EventBusSubscriber
public class CheckpointTicker {

    private static final Logger LOGGER = LogManager.getLogger(CheckpointTicker.class);
    public static long lastCheckpointTick = 0;
    private static long nextCheckpointInterval = 0;

    private static boolean randomIntervalSelected = false;

    private static int intervalTicks = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        // In 1.12.2, TickEvent doesn't provide the server instance directly
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        // Get Overworld (Dimension 0)
        WorldServer level = server.getWorld(0);
        if (level == null) return;

        GameRules rules = level.getGameRules();

        // Check if rule exists before getting boolean to avoid defaults/warnings if not initialized
        if (!rules.hasRule(ModGameRules.AUTO_CHECKPOINT_ENABLED)) {
            // If the rule is missing, we skip (it should be registered in ModGameRules.java)
            return;
        }

        boolean autoEnabled = rules.getBoolean(ModGameRules.AUTO_CHECKPOINT_ENABLED);
        if (!autoEnabled) {
            return;
        }

        boolean useRandom = rules.getBoolean(ModGameRules.AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED);

        if (useRandom && !randomIntervalSelected) {
            int lowerSeconds = rules.getInt(ModGameRules.AUTO_CHECKPOINT_RANDOM_MIN_SECONDS);
            int upperSeconds = rules.getInt(ModGameRules.AUTO_CHECKPOINT_RANDOM_MAX_SECONDS);

            // Safety defaults if rules return 0 (uninitialized)
            if (lowerSeconds == 0) lowerSeconds = 600;
            if (upperSeconds == 0) upperSeconds = 1200;

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
            int fixedSeconds = rules.getInt(ModGameRules.AUTO_CHECKPOINT_INTERVAL_SECONDS);
            if (fixedSeconds == 0) fixedSeconds = 600; // Safety default
            intervalTicks = fixedSeconds * 20;
        }

        // Use total world time as the clock
        long currentTick = level.getTotalWorldTime();

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
                    // In 1.12, getPlayers returns List<EntityPlayerMP>
                    EntityPlayerMP firstPlayer = server.getPlayerList().getPlayers().get(0);
                    data.setAnchorPlayerUUID(firstPlayer.getUniqueID());
                } else {
                    LOGGER.warn("No players online to set as anchor.");
                    return;
                }
            }

            EntityPlayerMP anchorPlayer = server.getPlayerList().getPlayerByUUID(data.getAnchorPlayerUUID());

            if (anchorPlayer != null) {
                CheckpointManager.setCheckpoint(anchorPlayer);
                randomIntervalSelected = false; // Reset random selection for next cycle
            } else {
                LOGGER.warn("Anchor player not found for UUID: {}", data.getAnchorPlayerUUID());
            }

            lastCheckpointTick = currentTick;
            nextCheckpointInterval = intervalTicks;
            LOGGER.debug("Reset checkpoint timer: next interval {} ticks, new last tick {}", nextCheckpointInterval, lastCheckpointTick);
        }
    }
}