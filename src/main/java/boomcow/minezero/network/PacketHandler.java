package boomcow.minezero.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(PacketHandler.class);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(SelfDamagePacket.TYPE, SelfDamagePacket.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SelfDamagePacket.TYPE, (payload, context) -> {
            context.server().execute(() -> handleSelfDamage(context.player()));
        });

        LOGGER.info("MineZero network payloads registered.");
    }

    private static void handleSelfDamage(ServerPlayer player) {
        ItemStack mainHandItem = player.getMainHandItem();
        float damageAmount = 1.0f;

        if (!mainHandItem.isEmpty()) {
            ItemAttributeModifiers attributeModifiersComponent = mainHandItem.get(DataComponents.ATTRIBUTE_MODIFIERS);

            if (attributeModifiersComponent != null && attributeModifiersComponent != ItemAttributeModifiers.EMPTY) {
                double weaponDamageContribution = 0.0;
                boolean foundAttackDamage = false;
                for (ItemAttributeModifiers.Entry entry : attributeModifiersComponent.modifiers()) {
                    if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND)) {
                        foundAttackDamage = true;
                        AttributeModifier modifier = entry.modifier();
                        if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                            weaponDamageContribution += modifier.amount();
                        }
                    }
                }

                if (foundAttackDamage) {
                    damageAmount = Math.max(1.0f, (float) weaponDamageContribution + 1.0f);
                }
            }
        }

        if (damageAmount > 0) {
            DamageSource source = player.damageSources().playerAttack(player);
            player.hurt(source, damageAmount);
            LOGGER.info("Player {} self-inflicted {} damage.", player.getName().getString(), damageAmount);
        }
    }
}
