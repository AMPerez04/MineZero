package boomcow.minezero;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.Category;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Key;

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
    public static final Key<BooleanRule> AUTO_CHECKPOINT_ENABLED =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "autoCheckpointEnabled").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    public static final Key<IntRule> AUTO_CHECKPOINT_INTERVAL_SECONDS =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "autoCheckpointIntervalSeconds").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createIntRule(600, 0)
            );

    public static final Key<BooleanRule> AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "autoCheckpointRandomIntervalEnabled").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createBooleanRule(false)
            );

    public static final Key<IntRule> AUTO_CHECKPOINT_RANDOM_MIN_SECONDS =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "autoCheckpointRandomMinSeconds").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createIntRule(600, 0)
            );

    public static final Key<IntRule> AUTO_CHECKPOINT_RANDOM_MAX_SECONDS =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "autoCheckpointRandomMaxSeconds").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createIntRule(1200, 1)
            );
    public static final Key<BooleanRule> ARTIFACT_FLUTE_COOLDOWN_ENABLED =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "artifactFluteCooldownEnabled").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    public static final Key<IntRule> ARTIFACT_FLUTE_COOLDOWN_SECONDS =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "artifactFluteCooldownSeconds").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createIntRule(60, 0)
            );

    public static final Key<BooleanRule> ARTIFACT_FLUTE_ENABLED =
            GameRuleRegistry.register(
                    Identifier.of(MineZeroMain.MOD_ID, "artifactFluteEnabled").toString(),
                    Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );

    /**
     * This method should be called once during your mod's initialization (e.g., in onInitialize)
     * to ensure the game rules are registered.
     * <p>
     * Calling this method explicitly isn't strictly necessary if the static final fields
     * are accessed, as that will trigger the static initializers. However, for clarity
     * and to ensure registration happens, it's good practice to have an explicit init method.
     * <p>
     * Update: With Fabric API's GameRuleRegistry, the static final initializers are the
     * registration itself. So, simply loading this class (e.g., by referencing one of its
     * fields from your main initializer) is enough. No explicit `init()` call is needed.
     */
    public static void initialize() {
        MineZeroMain.LOGGER.info("MineZero game rules registered.");
    }
}
