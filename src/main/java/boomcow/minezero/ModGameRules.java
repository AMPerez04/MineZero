package boomcow.minezero;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;

/**
 * MineZero gamerules.
 *
 * Naming convention (see docs/HowItWorks.md):
 * - Keys are camelCase, matching vanilla gamerule style (e.g. doMobSpawning).
 * - Feature toggles end in "Enabled"; event-driven checkpoint triggers read
 *   "checkpointOn<Event>"; other booleans read as plain-English predicates.
 * - Integer rules end with their unit (e.g. "Seconds").
 * - Java constants are the SCREAMING_SNAKE form of the registered key.
 */
public class ModGameRules {

        // Enable or disable automatic checkpoints set periodically by the server
        // ticker.
        // If false, no periodic checkpoints are created. Default: true
        public static final GameRules.Key<BooleanValue> AUTO_CHECKPOINT_ENABLED = GameRules.register(
                        "autoCheckpointEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Fixed interval (in seconds) between auto-checkpoints when random interval is
        // disabled.
        // Default: 600 seconds (10 minutes)
        public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_INTERVAL_SECONDS = GameRules.register(
                        "autoCheckpointIntervalSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(600));

        // Whether to use a randomized auto-checkpoint interval between the min/max
        // bounds below.
        // If false, the fixed interval above is used. Default: false
        public static final GameRules.Key<BooleanValue> AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED = GameRules.register(
                        "autoCheckpointRandomIntervalEnabled", GameRules.Category.PLAYER,
                        GameRules.BooleanValue.create(false));

        // Minimum (in seconds) for the randomized auto-checkpoint interval.
        // Default: 600 seconds (10 minutes)
        public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MIN_SECONDS = GameRules.register(
                        "autoCheckpointRandomMinSeconds", GameRules.Category.PLAYER,
                        GameRules.IntegerValue.create(600));

        // Maximum (in seconds) for the randomized auto-checkpoint interval.
        // Default: 1200 seconds (20 minutes)
        public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MAX_SECONDS = GameRules.register(
                        "autoCheckpointRandomMaxSeconds", GameRules.Category.PLAYER,
                        GameRules.IntegerValue.create(1200));

        // Enable a cooldown on the Artifact Flute item.
        // Default: true
        public static final GameRules.Key<BooleanValue> ARTIFACT_FLUTE_COOLDOWN_ENABLED = GameRules.register(
                        "artifactFluteCooldownEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Cooldown duration for the Artifact Flute (in seconds).
        // Default: 60 seconds
        public static final GameRules.Key<IntegerValue> ARTIFACT_FLUTE_COOLDOWN_SECONDS = GameRules.register(
                        "artifactFluteCooldownSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(60));

        // Enable or disable the Artifact Flute item entirely.
        // Default: true
        public static final GameRules.Key<BooleanValue> ARTIFACT_FLUTE_ENABLED = GameRules.register(
                        "artifactFluteEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Automatically set a checkpoint when the world is created.
        // Default: true
        public static final GameRules.Key<BooleanValue> CHECKPOINT_ON_WORLD_CREATION = GameRules.register(
                        "checkpointOnWorldCreation", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Randomly pick a new anchor player each time an automatic checkpoint is set.
        // Default: false
        public static final GameRules.Key<BooleanValue> RANDOM_ANCHOR_ENABLED = GameRules.register(
                        "randomAnchorEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

        // If true, any player's death restores the checkpoint, not just the anchor's.
        // Default: false
        public static final GameRules.Key<BooleanValue> ANY_PLAYER_DEATH_TRIGGERS_RESTORE = GameRules.register(
                        "anyPlayerDeathTriggersRestore", GameRules.Category.PLAYER,
                        GameRules.BooleanValue.create(false));

        // Set a checkpoint when all players sleep and the night is skipped.
        // Default: false
        public static final GameRules.Key<BooleanValue> CHECKPOINT_ON_SLEEP = GameRules.register(
                        "checkpointOnSleep", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

}
