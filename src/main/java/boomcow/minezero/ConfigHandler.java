package boomcow.minezero;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {
    public static final ModConfigSpec COMMON_CONFIG_SPEC;
    public static final CommonConfig COMMON;

    static {
        Pair<CommonConfig, ModConfigSpec> commonPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_CONFIG_SPEC = commonPair.getRight();
        COMMON = commonPair.getLeft();
    }

    public static class CommonConfig {
        public final ModConfigSpec.ConfigValue<String> restoreSound;

        public CommonConfig(ModConfigSpec.Builder builder) {
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
        if (COMMON != null && COMMON.restoreSound != null) {
            return COMMON.restoreSound.get();
        }
        return "death_chime";
    }
    public static void onLoad(final ModConfigEvent.Loading event) {
        System.out.println("MineZero Common Config Loaded: " + event.getConfig().getFileName());
    }

    public static void onReload(final ModConfigEvent.Reloading event) {
        System.out.println("MineZero Common Config Reloaded: " + event.getConfig().getFileName());
    }

}
