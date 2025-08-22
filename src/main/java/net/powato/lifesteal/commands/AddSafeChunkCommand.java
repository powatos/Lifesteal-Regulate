package net.powato.lifesteal.commands;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.Chunk;
import net.powato.lifesteal.SafeChunks.SafeChunks;
import net.powato.lifesteal.networking.ShowSafeChunksPayload;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class AddSafeChunkCommand {

    public static void register(com.mojang.brigadier.CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("lifesteal_R")

                .then(literal("AddChunk")
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal("Add Called" ), false);

                            ServerPlayerEntity Player = context.getSource().getEntity() instanceof ServerPlayerEntity p ? p : null; // null if source is not Player, Player if source is Player
                            if (Player == null){
                                context.getSource().sendError(Text.literal("This command must be run by a Player!"));
                                return 0;
                            }
                        ServerWorld World = Player.getWorld();
                        MinecraftServer server = World.getServer();
                        assert server != null;

                        String data = "|"; // "X,Z,dim"
                        Chunk CurrentChunk = World.getChunk(context.getSource().getPlayer().getBlockPos());

                        data += Integer.toString(CurrentChunk.getPos().x) + ",";
                        data += Integer.toString(CurrentChunk.getPos().z) + ",";
                        data += World.getRegistryKey().getValue().toString() + ",";
                        data += Player.getUuidAsString();

                        SafeChunks serverState = SafeChunks.getServerState(server);
                        int result = serverState.AddChunk(World, data);
                        if (result == -1){
                            context.getSource().sendError(Text.literal("This chunk is already added to the safe list!"));
                            return 0;
                        } else if (result == -2){
                            context.getSource().sendError(Text.literal("You have reached the max safe chunk limit!"));
                            return 0;
                        }

                        return 1;
                    })
                )

                .then(literal("RemoveChunk")
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal("Remove Called"), false);

                        ServerPlayerEntity Player = context.getSource().getEntity() instanceof ServerPlayerEntity p ? p : null; // null if source is not Player, Player if source is Player
                        if (Player == null){
                            context.getSource().sendError(Text.literal("This command must be run by a Player!"));
                            return 0;
                        }
                        ServerWorld World = Player.getWorld();
                        MinecraftServer server = World.getServer();
                        assert server != null;

                        String data = "|"; // "|X,Z,dim"
                        Chunk CurrentChunk = World.getChunk(context.getSource().getPlayer().getBlockPos());

                        data += Integer.toString(CurrentChunk.getPos().x) + ",";
                        data += Integer.toString(CurrentChunk.getPos().z) + ",";
                        data += World.getRegistryKey().getValue().toString() + ",";
                        data += Player.getUuidAsString();

                        SafeChunks serverState = SafeChunks.getServerState(server);
                        int result = serverState.RemoveChunk(World, data);
                        if (result == -1){
                            context.getSource().sendError(Text.literal("This chunk isn't yet added to the safe list!"));
                            return 0;
                        } else if (result == -2){
                            context.getSource().sendError(Text.literal("You are not authorized to remove this chunk from the safe list!"));
                            return 0;
                        }
                        return 1;
                    })
                )

                .then(literal("ResetChunks")
                    .executes(context -> {
                        context.getSource().sendFeedback(() -> Text.literal("Reset Called"), false);

                        // TODO: change to only callable on server
                        ServerPlayerEntity Player = context.getSource().getEntity() instanceof ServerPlayerEntity p ? p : null; // null if source is not Player, Player if source is Player
                        if (Player == null){
                            context.getSource().sendError(Text.literal("This command must be run by the server!"));
                            return 0;
                        }
                        ServerWorld World = Player.getWorld();
                        MinecraftServer server = World.getServer();
                        assert server != null;

                        SafeChunks serverState = SafeChunks.getServerState(server);
                        int result = serverState.ResetChunks(World);
                        if (result == -1){
                            context.getSource().sendError(Text.literal("Chunks already reset; use AddChunk to add"));
                            return 0;
                        } else if (result == -2){
                            context.getSource().sendError(Text.literal("Data Mismatch Exception -> ... SafeChunkClass::SafeChunkClass::ResetChunks"));
                            return 0;
                        }

                        return 1;
                    })
                )

                .then(literal("ShowSafeChunks")
                    .then(argument("show", BoolArgumentType.bool())
                        .executes(context -> {
                            context.getSource().sendFeedback(() -> Text.literal("Show Called"), false);

                            ServerPlayerEntity Player = context.getSource().getEntity() instanceof ServerPlayerEntity p ? p : null; // null if source is not Player, Player if source is Player
                            if (Player == null){
                                context.getSource().sendError(Text.literal("This command must be run by the server!"));
                                return 0;
                            }
                            ServerWorld World = Player.getWorld();
                            MinecraftServer server = World.getServer();
                            assert server != null;

                            // commands always processed on server
                            boolean bShow = BoolArgumentType.getBool(context, "show");
                            ServerPlayNetworking.send(Player, new ShowSafeChunksPayload(bShow));

                            return 1;
                        })
                    )
                )



        );
    }


}