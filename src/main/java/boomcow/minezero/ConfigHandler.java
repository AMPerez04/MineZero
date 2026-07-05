package boomcow.minezero;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Config(name = MineZeroMain.MOD_ID)
public class ConfigHandler implements ConfigData {
    private static final Logger LOGGER = LoggerFactory.getLogger(MineZeroMain.MOD_ID + "-config");

    // Sound played when the anchor player dies and the checkpoint is restored.
    // Valid values match the mod's sound IDs: "death_chime", "alt_death_chime".
    @ConfigEntry.Gui.Tooltip
    public String restoreSound = "death_chime";
    private static ConfigHandler INSTANCE = null;

    public static void register() {
        AutoConfig.register(ConfigHandler.class, GsonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(ConfigHandler.class).getConfig();
        LOGGER.info("MineZero config loaded/initialized with Cloth Config.");
        AutoConfig.getConfigHolder(ConfigHandler.class).registerSaveListener((manager, data) -> {
            LOGGER.info("MineZero config saved!");
            INSTANCE = data;
            return InteractionResult.SUCCESS;
        });
    }

    public static ConfigHandler get() {
        if (INSTANCE == null) {
            LOGGER.warn("ConfigHandler.INSTANCE was null, attempting to retrieve from AutoConfig. Ensure register() is called.");
            INSTANCE = AutoConfig.getConfigHolder(ConfigHandler.class).getConfig();
        }
        return INSTANCE;
    }

    public static String getRestoreSound() {
        return get().restoreSound;
    }

    public static Screen getClothConfigScreen(Screen parentScreen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable("config." + MineZeroMain.MOD_ID + ".title"));
        builder.setSavingRunnable(() -> {
            AutoConfig.getConfigHolder(ConfigHandler.class).save();
        });

        ConfigCategory sounds = builder.getOrCreateCategory(Component.translatable("config." + MineZeroMain.MOD_ID + ".category.sounds"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        List<String> restoreSoundOptions = Arrays.asList("death_chime", "alt_death_chime");
        sounds.addEntry(entryBuilder.startSelector(
                                Component.translatable("config." + MineZeroMain.MOD_ID + ".option.restoreSound"),
                                restoreSoundOptions.toArray(new String[0]),
                                get().restoreSound
                        )
                        .setDefaultValue("death_chime")
                        .setTooltip(Component.translatable("config." + MineZeroMain.MOD_ID + ".option.restoreSound.tooltip"))
                        .setSaveConsumer(newValue -> get().restoreSound = newValue)
                        .build()
        );

        return builder.build();
    }

    @Override
    public void validatePostLoad() throws ConfigData.ValidationException {
        ConfigData.super.validatePostLoad();
        // "CLASSIC"/"ALTERNATE" accepted as legacy values from pre-rename configs.
        List<String> validSounds = Arrays.asList("death_chime", "alt_death_chime", "CLASSIC", "ALTERNATE");
        if (!validSounds.contains(restoreSound)) {
            LOGGER.warn("Invalid restoreSound value '{}' found in config, resetting to default 'death_chime'.", restoreSound);
            restoreSound = "death_chime";
        }
    }
}
