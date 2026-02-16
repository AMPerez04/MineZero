package boomcow.minezero;

import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;

public class ModGameRules {

        // Enable or disable automatic checkpoints set periodically by the server
        // ticker.
        // If false, no periodic checkpoints are created. Default: true
        public static final GameRules.Key<BooleanValue> AUTO_CHECKPOINT_ENABLED = GameRules.register(
                        "autoCheckpointEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Fixed interval (in seconds) between auto-checkpoints when random interval is
        // disabled.
        // Default: 600 seconds (10 minutes)
        public static final GameRules.Key<IntegerValue> CHECKPOINT_FIXED_INTERVAL = GameRules.register(
                        "checkpointFixedInterval", GameRules.Category.PLAYER, GameRules.IntegerValue.create(600));

        // Whether to use a randomized checkpoint interval between the lower/upper
        // bounds below.
        // If false, the fixed interval above is used. Default: false
        public static final GameRules.Key<BooleanValue> USE_RANDOM_INTERVAL = GameRules.register(
                        "useRandomCheckpointInterval", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

        // Lower bound (in seconds) for randomized auto-checkpoint interval.
        // Default: 600 seconds (10 minutes)
        public static final GameRules.Key<IntegerValue> RANDOM_CHECKPOINT_LOWER_BOUND = GameRules.register(
                        "randomCheckpointLowerBound", GameRules.Category.PLAYER, GameRules.IntegerValue.create(600)); // Default:
                                                                                                                      // 10
                                                                                                                      // minutes

        // Upper bound (in seconds) for randomized auto-checkpoint interval.
        // Default: 1200 seconds (20 minutes)
        public static final GameRules.Key<IntegerValue> RANDOM_CHECKPOINT_UPPER_BOUND = GameRules.register(
                        "randomCheckpointUpperBound", GameRules.Category.PLAYER, GameRules.IntegerValue.create(1200)); // Default:
                                                                                                                       // 20
                                                                                                                       // minutes

        // Enable a cooldown on the Artifact Flute item.
        // Default: true
        public static final GameRules.Key<BooleanValue> FLUTE_COOLDOWN_ENABLED = GameRules.register(
                        "fluteCooldownEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Cooldown duration for the Artifact Flute (in seconds).
        // Default: 60 seconds
        public static final GameRules.Key<IntegerValue> FLUTE_COOLDOWN_DURATION = GameRules.register(
                        "fluteCooldownDuration", GameRules.Category.PLAYER, GameRules.IntegerValue.create(60));

        // Enable or disable the Artifact Flute item entirely.
        // Default: true
        public static final GameRules.Key<BooleanValue> ARTIFACT_FLUTE_ENABLED = GameRules.register(
                        "artifactFluteEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Automatically set a checkpoint when the world is created.
        // Default: true
        public static final GameRules.Key<BooleanValue> SET_CHECKPOINT_ON_WORLD_CREATION = GameRules.register(
                        "setCheckpointOnWorldCreation", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

        // Enable wether the mod should randomly choose an anchor on each
        public static final GameRules.Key<BooleanValue> RANDOM_ANCHOR_ENABLED = GameRules.register(
                "enableRandomAnchor", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

        // Enable wether all players on the Server should be an anchor
        public static final GameRules.Key<BooleanValue> ALL_PLAYERS_RBD = GameRules.register(
                "enableAllPlayersRBD", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

}
