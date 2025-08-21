package net.powato.lifesteal.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;


public record ShowSafeChunksPayload(boolean bShow) implements CustomPayload {
    public static final CustomPayload.Id<ShowSafeChunksPayload> ID = new CustomPayload.Id<>(NetworkConstants.SHOW_CHUNKS_ID);
    public static final PacketCodec<RegistryByteBuf, ShowSafeChunksPayload> CODEC = PacketCodec.tuple(PacketCodecs.BOOLEAN, ShowSafeChunksPayload::bShow, ShowSafeChunksPayload::new);



    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
