package boomcow.minezero.event;

import boomcow.minezero.MineZero;
import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.network.PacketHandler;
import boomcow.minezero.network.SelfDamagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod.EventBusSubscriber(modid = MineZero.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {

                while (KeyBindings.EXAMPLE_ACTION_KEY.consumeClick()) {
                    LOGGER.info("Example Action Key Pressed!");
                    mc.player.sendSystemMessage(Component.translatable("message.minezero.example_keybind"));
                }

                while (KeyBindings.SELF_DAMAGE_KEY.consumeClick()) {
                    LOGGER.debug("Self Damage Key Pressed - Sending Packet!");

                    PacketHandler.INSTANCE.sendToServer(new SelfDamagePacket());
                }

            }
        }
    }
}