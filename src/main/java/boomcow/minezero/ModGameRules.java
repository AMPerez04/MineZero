package boomcow.minezero;

import net.minecraft.world.GameRules;

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

    // Rule Names (Keys)
    public static final String AUTO_CHECKPOINT_ENABLED = "autoCheckpointEnabled";
    public static final String AUTO_CHECKPOINT_INTERVAL_SECONDS = "autoCheckpointIntervalSeconds";
    public static final String AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED = "autoCheckpointRandomIntervalEnabled";
    public static final String AUTO_CHECKPOINT_RANDOM_MIN_SECONDS = "autoCheckpointRandomMinSeconds";
    public static final String AUTO_CHECKPOINT_RANDOM_MAX_SECONDS = "autoCheckpointRandomMaxSeconds";
    public static final String ARTIFACT_FLUTE_COOLDOWN_ENABLED = "artifactFluteCooldownEnabled";
    public static final String ARTIFACT_FLUTE_COOLDOWN_SECONDS = "artifactFluteCooldownSeconds";
    public static final String ARTIFACT_FLUTE_ENABLED = "artifactFluteEnabled";
    public static final String CHECKPOINT_ON_WORLD_CREATION = "checkpointOnWorldCreation";

    /**
     * Registers the custom GameRules with the server.
     * This MUST be called during the FMLServerStartingEvent in your main class.
     *
     * @param rules The GameRules instance from the Overworld (server.getWorld(0).getGameRules())
     */
    public static void register(GameRules rules) {
        addRule(rules, AUTO_CHECKPOINT_ENABLED, "true", GameRules.ValueType.BOOLEAN_VALUE);
        addRule(rules, AUTO_CHECKPOINT_INTERVAL_SECONDS, "600", GameRules.ValueType.NUMERICAL_VALUE);
        addRule(rules, AUTO_CHECKPOINT_RANDOM_INTERVAL_ENABLED, "false", GameRules.ValueType.BOOLEAN_VALUE);
        addRule(rules, AUTO_CHECKPOINT_RANDOM_MIN_SECONDS, "600", GameRules.ValueType.NUMERICAL_VALUE);
        addRule(rules, AUTO_CHECKPOINT_RANDOM_MAX_SECONDS, "1200", GameRules.ValueType.NUMERICAL_VALUE);
        addRule(rules, ARTIFACT_FLUTE_COOLDOWN_ENABLED, "true", GameRules.ValueType.BOOLEAN_VALUE);
        addRule(rules, ARTIFACT_FLUTE_COOLDOWN_SECONDS, "60", GameRules.ValueType.NUMERICAL_VALUE);
        addRule(rules, ARTIFACT_FLUTE_ENABLED, "true", GameRules.ValueType.BOOLEAN_VALUE);
        addRule(rules, CHECKPOINT_ON_WORLD_CREATION, "true", GameRules.ValueType.BOOLEAN_VALUE);
    }

    /**
     * Helper to add a rule only if it doesn't exist (prevents overwriting saved values).
     */
    private static void addRule(GameRules rules, String key, String defaultValue, GameRules.ValueType type) {
        if (!rules.hasRule(key)) {
            rules.addGameRule(key, defaultValue, type);
        }
    }
}
