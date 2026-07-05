package boomcow.minezero.input;

import boomcow.minezero.MineZeroClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {

    public static final String KEY_CATEGORY_MINEZERO = "key.categories.minezero";

    public static final KeyMapping EXAMPLE_ACTION_KEY;
    public static final KeyMapping SELF_DAMAGE_KEY;

    static {
        EXAMPLE_ACTION_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.minezero.example_action",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KEY_CATEGORY_MINEZERO
        ));

        SELF_DAMAGE_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.minezero.self_damage",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                KEY_CATEGORY_MINEZERO
        ));
    }

    public static void registerKeyBindings() {
        MineZeroClient.LOGGER.info("MineZero keybindings registered.");
    }
}
