package net.powato.lifesteal;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.powato.lifesteal.SafeChunks.SafeChunks;
import net.powato.lifesteal.networking.ShowSafeChunksPayload;
import net.powato.lifesteal.networking.UpdateSafeChunksPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powato.lifesteal.commands.AddSafeChunkCommand;

public class LifestealRegulate implements ModInitializer {
	public static final String MOD_ID = "lifesteal-regulate";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            AddSafeChunkCommand.register(dispatcher);
        });


        PayloadTypeRegistry.playS2C().register(ShowSafeChunksPayload.ID, ShowSafeChunksPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateSafeChunksPayload.ID, UpdateSafeChunksPayload.CODEC);

        registerPlayerJoinEvent();
    }

    private void registerPlayerJoinEvent(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity Player = handler.getPlayer();

            SafeChunks state = SafeChunks.getServerState(server);
            ServerPlayNetworking.send(Player, new UpdateSafeChunksPayload(state.GetChunkData()));

        });
    }


}