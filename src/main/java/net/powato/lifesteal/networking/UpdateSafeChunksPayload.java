package net.powato.lifesteal.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

import java.util.ArrayList;

public record UpdateSafeChunksPayload(String ChunksList) implements CustomPayload{
    public static final CustomPayload.Id<UpdateSafeChunksPayload> ID = new CustomPayload.Id<>(NetworkConstants.UPDATE_CHUNKS_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateSafeChunksPayload> CODEC = PacketCodec.tuple(PacketCodecs.STRING, UpdateSafeChunksPayload::ChunksList, UpdateSafeChunksPayload::new);



    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
