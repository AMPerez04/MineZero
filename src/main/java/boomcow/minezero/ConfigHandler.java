package boomcow.minezero;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MineZero.MODID)
@Mod.EventBusSubscriber(modid = MineZero.MODID)
public class ConfigHandler {

    @Config.Comment("Sound settings")
    public static Sounds sounds = new Sounds();

    public static class Sounds {
        @Config.Comment({
                "Sound played when the anchor player dies and the checkpoint is restored.",
                "Valid values (matching the mod's sound IDs):",
                "  \"death_chime\"     - the classic restore chime",
                "  \"alt_death_chime\" - the alternate restore chime"
        })
        @Config.Name("restoreSound")
        public String restoreSound = "death_chime";
    }

    /**
     * Helper method to maintain API compatibility with the rest of the port.
     */
    public static String getRestoreSound() {
        return sounds.restoreSound;
    }

    /**
     * Syncs the config when changed from the in-game GUI.
     */
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(MineZero.MODID)) {
            ConfigManager.sync(MineZero.MODID, Config.Type.INSTANCE);
        }
    }
}
