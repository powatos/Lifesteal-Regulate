package net.powato.lifesteal;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.powato.lifesteal.SafeChunks.ClientChunkData;
import net.powato.lifesteal.networking.ShowSafeChunksPayload;
import net.powato.lifesteal.networking.UpdateSafeChunksPayload;

import java.rmi.registry.Registry;
import java.util.ArrayList;

public class LifestealRegulateClient implements ClientModInitializer {

    private static boolean bShowSafeChunks = false;

    @Override
    public void onInitializeClient() {

        registerUpdateSafeChunksPacket();
        registerShowChunksPacket();

        registerChunkHighlights();

    }

    private static void registerShowChunksPacket() {
        ClientPlayNetworking.registerGlobalReceiver(ShowSafeChunksPayload.ID, ((payload, context) -> {
            context.client().execute(() -> {
                // ONLY CLIENT
                bShowSafeChunks = payload.bShow();


            });
        })
        );
    }

    private static void registerUpdateSafeChunksPacket(){
        ClientPlayNetworking.registerGlobalReceiver(UpdateSafeChunksPayload.ID, ((payload, context) -> {
                    context.client().execute(() -> {
                        // ONLY CLIENT
                        ClientChunkData.setChunks(payload.ChunksList());
                    });
                })
        );
    }

    private static void registerChunkHighlights(){
        WorldRenderEvents.LAST.register(worldRenderContext -> {
            if (bShowSafeChunks) {

                RegistryKey<World> dimension = MinecraftClient.getInstance().world.getRegistryKey();

                for (ChunkPos pos : ClientChunkData.GetChunks(dimension)) {
                    BlockPos start = new BlockPos(pos.getStartX(), -64, pos.getStartZ());
                    BlockPos end = start.add(15, 255, 15); // full height of chunk
                    DebugRenderer.drawBox(worldRenderContext.matrixStack(), worldRenderContext.consumers(), start, end, 0f, 1f, 0f, 0.3f); // red, semi-transparent
                }
            }
        });
    }

}
