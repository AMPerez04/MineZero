package boomcow.minezero;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSoundEvents {
    public static final ResourceLocation DEATH_CHIME_ID = ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "death_chime");
    public static final ResourceLocation ALT_DEATH_CHIME_ID = ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "alt_death_chime");
    public static final ResourceLocation EMPTY_SOUND_ID = ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "empty_sound");
    public static final ResourceLocation FLUTE_CHIME_ID = ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "flute_chime");

    public static final SoundEvent DEATH_CHIME = SoundEvent.createVariableRangeEvent(DEATH_CHIME_ID);
    public static final SoundEvent ALT_DEATH_CHIME = SoundEvent.createVariableRangeEvent(ALT_DEATH_CHIME_ID);
    public static final SoundEvent EMPTY_SOUND = SoundEvent.createVariableRangeEvent(EMPTY_SOUND_ID);
    public static final SoundEvent FLUTE_CHIME = SoundEvent.createVariableRangeEvent(FLUTE_CHIME_ID);

    public static void registerSoundEvents() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, DEATH_CHIME_ID, DEATH_CHIME);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ALT_DEATH_CHIME_ID, ALT_DEATH_CHIME);
        Registry.register(BuiltInRegistries.SOUND_EVENT, EMPTY_SOUND_ID, EMPTY_SOUND);
        Registry.register(BuiltInRegistries.SOUND_EVENT, FLUTE_CHIME_ID, FLUTE_CHIME);

        MineZeroMain.LOGGER.info("MineZero sound events registered.");
    }
}
