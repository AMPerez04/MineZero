package boomcow.minezero.network;

import boomcow.minezero.MineZeroMain;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelfDamagePacket() implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MineZeroMain.MOD_ID, "self_damage_trigger_v1");
    public static final Type<SelfDamagePacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SelfDamagePacket> STREAM_CODEC = StreamCodec.unit(new SelfDamagePacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
