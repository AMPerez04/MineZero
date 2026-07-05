package boomcow.minezero.event;

import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.network.SelfDamagePacket;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;

public class ClientForgeEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onClientTickPostEvent(ClientTickEvent.Post event) {

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (KeyBindings.EXAMPLE_ACTION_KEY != null) {
            while (KeyBindings.EXAMPLE_ACTION_KEY.get().consumeClick()) {
                LOGGER.info("Example Action Key Pressed!");
                mc.player.sendSystemMessage(Component.translatable("message.minezero.example_keybind"));
            }
        }

        if (KeyBindings.SELF_DAMAGE_KEY != null) {
            while (KeyBindings.SELF_DAMAGE_KEY.get().consumeClick()) {
                LOGGER.debug("Self Damage Key Pressed - Sending Packet!");
                PacketDistributor.sendToServer(new SelfDamagePacket());
            }
        }
    }
}