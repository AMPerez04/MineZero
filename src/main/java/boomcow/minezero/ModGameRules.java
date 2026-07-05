package boomcow.minezero;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import net.minecraft.world.level.GameRules.IntegerValue;
import net.minecraft.world.level.GameRules.Key;

/**
 * MineZero gamerules.
 *
 * Naming convention (see docs/HowItWorks.md):
 * - Keys are camelCase, matching vanilla gamerule style (e.g. doMobSpawning),
 *   namespaced on Fabric (use them as /gamerule minezero:<key>).
 * - Feature toggles end in "Enabled"; event-driven checkpoint triggers read
 *   "checkpointOn<Event>"; other booleans read as plain-English predicates.
 * - Integer rules end with their unit (e.g. "Seconds").
 * - Java constants are the SCREAMING_SNAKE form of the registered key.
 */
public class ModGameRules {
    public static final Key<BooleanValue> AUTO_CHECKPOINT_ENABLED =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":autoCheckpointEnabled",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    public static final Key<IntegerValue> AUTO_CHECKPOINT_INTERVAL_SECONDS =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":autoCheckpointIntervalSeconds",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createIntRule(600, 0)
            );

    public static final Key<BooleanValue> AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":autoCheckpointRandomIntervalEnabled",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(false)
            );

    public static final Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MIN_SECONDS =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":autoCheckpointRandomMinSeconds",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createIntRule(600, 0)
            );

    public static final Key<IntegerValue> AUTO_CHECKPOINT_RANDOM_MAX_SECONDS =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":autoCheckpointRandomMaxSeconds",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createIntRule(1200, 1)
            );

    public static final Key<BooleanValue> ARTIFACT_FLUTE_COOLDOWN_ENABLED =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":artifactFluteCooldownEnabled",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    public static final Key<IntegerValue> ARTIFACT_FLUTE_COOLDOWN_SECONDS =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":artifactFluteCooldownSeconds",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createIntRule(60, 0)
            );

    public static final Key<BooleanValue> ARTIFACT_FLUTE_ENABLED =
            GameRuleRegistry.register(
                    MineZeroMain.MOD_ID + ":artifactFluteEnabled",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    /**
     * Called once from onInitialize to force the static initializers (and thus
     * gamerule registration) to run at a predictable time.
     */
    public static void initialize() {
        MineZeroMain.LOGGER.info("MineZero game rules registered.");
    }
}
