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

    public static final GameRules.Key<BooleanValue> AUTO_CHECKPOINT_ENABLED =
            GameRules.register("autoCheckpointEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_INTERVAL_SECONDS =
            GameRules.register("autoCheckpointIntervalSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(600));

    public static final GameRules.Key<BooleanValue> AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED =
            GameRules.register("autoCheckpointRandomIntervalEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));

    public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MIN_SECONDS =
            GameRules.register("autoCheckpointRandomMinSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(600));

    public static final GameRules.Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MAX_SECONDS =
            GameRules.register("autoCheckpointRandomMaxSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(1200));

    public static final GameRules.Key<BooleanValue> ARTIFACT_FLUTE_COOLDOWN_ENABLED =
            GameRules.register("artifactFluteCooldownEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<IntegerValue> ARTIFACT_FLUTE_COOLDOWN_SECONDS =
            GameRules.register("artifactFluteCooldownSeconds", GameRules.Category.PLAYER, GameRules.IntegerValue.create(60));

    public static final GameRules.Key<BooleanValue> ARTIFACT_FLUTE_ENABLED =
            GameRules.register("artifactFluteEnabled", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    public static final GameRules.Key<BooleanValue> CHECKPOINT_ON_WORLD_CREATION =
            GameRules.register("checkpointOnWorldCreation", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
}
