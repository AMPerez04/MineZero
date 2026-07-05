package boomcow.minezero.event;

import boomcow.minezero.MineZero;
import boomcow.minezero.input.KeyBindings;
import boomcow.minezero.network.PacketHandler;
import boomcow.minezero.network.SelfDamagePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = MineZero.MODID, value = Side.CLIENT)
public class ClientForgeEvents {

    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getMinecraft();

            // Ensure player exists to prevent crashes in menus
            if (mc.player != null) {

                // isPressed() acts like consumeClick() in 1.12.2 (reads and resets state)
                while (KeyBindings.EXAMPLE_ACTION_KEY.isPressed()) {
                    LOGGER.info("Example Action Key Pressed!");
                    mc.player.sendMessage(new TextComponentTranslation("message.minezero.example_keybind"));
                }

                while (KeyBindings.SELF_DAMAGE_KEY.isPressed()) {
                    LOGGER.debug("Self Damage Key Pressed - Sending Packet!");
                    PacketHandler.INSTANCE.sendToServer(new SelfDamagePacket());
                }
            }
        }
    }
}