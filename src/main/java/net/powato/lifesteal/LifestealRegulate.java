package net.powato.lifesteal;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

        SafeChunks.readMaxSafeChunks(); // init max chunks for server instance based on config


        PayloadTypeRegistry.playS2C().register(ShowSafeChunksPayload.ID, ShowSafeChunksPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateSafeChunksPayload.ID, UpdateSafeChunksPayload.CODEC);

        registerPlayerJoinEvent();
    }

    private void registerPlayerJoinEvent(){
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity Player = handler.getPlayer();

            SafeChunks state = SafeChunks.getServerState(server);
            ServerPlayNetworking.send(Player, new UpdateSafeChunksPayload(state.GetChunkData()));


            updatePlayerName(server, Player);

        });
    }

    public static void updatePlayerName(MinecraftServer server, ServerPlayerEntity Player){


        // show max hearts in tab menu
        int maxHearts = (int) (Player.getMaxHealth() / 2);

        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam("Hearts" + Player.getUuidAsString());
        if (team == null) {
            team = scoreboard.addTeam("Hearts" + Player.getUuidAsString());
        }
        team.setPrefix(Text.literal("[❤" + maxHearts + "] ").formatted(Formatting.RED));
        scoreboard.addScoreHolderToTeam(Player.getNameForScoreboard(), team);

    }
}