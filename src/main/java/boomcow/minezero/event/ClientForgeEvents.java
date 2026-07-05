package boomcow.minezero.event;

import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.network.SelfDamagePacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientForgeEvents {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientForgeEvents.class);

    public static void registerClientEvents() {
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (mc.player == null || mc.level == null) {
                return;
            }

            while (KeyBindings.EXAMPLE_ACTION_KEY.consumeClick()) {
                LOGGER.info("Example Action Key Pressed!");
                mc.player.sendSystemMessage(Component.translatable("message.minezero.example_keybind"));
            }

            while (KeyBindings.SELF_DAMAGE_KEY.consumeClick()) {
                LOGGER.debug("Self Damage Key Pressed - Sending Packet!");
                ClientPlayNetworking.send(new SelfDamagePacket());
            }
        });
    }
}
