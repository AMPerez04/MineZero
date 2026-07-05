package boomcow.minezero;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {

    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final CommonConfig COMMON;

    static {
        Pair<CommonConfig, ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_CONFIG = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.ConfigValue<String> restoreSound;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Sound settings").push("sounds");

            restoreSound = builder
                    .comment("Sound played when the anchor player dies and the checkpoint is restored.",
                            "Valid values (matching the mod's sound IDs):",
                            "  \"death_chime\"     - the classic restore chime",
                            "  \"alt_death_chime\" - the alternate restore chime")
                    .define("restoreSound", "death_chime");

            builder.pop();
        }
    }

    public static String getRestoreSound() {
        return COMMON.restoreSound.get();
    }

    public static void loadConfig(ModConfig config) {
    }
}
